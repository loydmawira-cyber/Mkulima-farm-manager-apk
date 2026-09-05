package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ImageStorageUtils {
    private const val TAG = "ImageStorageUtils"

    /**
     * Resiliently reads raw image bytes from any source:
     * - content:// URIs
     * - file:// URIs
     * - android.resource:// URIs
     * - absolute file paths (/data/user/0/...)
     * - inline Base64 data strings (data:image/... or raw Base64)
     *
     * Performs a single read to prevent permissions expiration or closed-stream exceptions.
     */
    fun readBytesFromUri(context: Context, uriSource: Any?): ByteArray? {
        if (uriSource == null) return null
        val uriStr = uriSource.toString().trim()
        if (uriStr.isBlank()) return null

        // 1. Inline Base64 Data URI or raw Base64 string
        if (uriStr.startsWith("data:image/") || uriStr.startsWith("data:application/")) {
            val commaIndex = uriStr.indexOf(',')
            val raw = if (commaIndex != -1) uriStr.substring(commaIndex + 1) else uriStr
            return runCatching { Base64.decode(raw.trim(), Base64.DEFAULT) }.getOrNull()
        }

        // 2. Parse URI
        val uri = if (uriSource is Uri) uriSource else runCatching { Uri.parse(uriStr) }.getOrNull()

        // 3. File scheme or raw filesystem path
        if (uri == null || uri.scheme == "file" || uri.scheme == null) {
            val path = uri?.path ?: uriStr.removePrefix("file://")
            val file = File(path)
            if (file.exists() && file.isFile && file.length() > 0) {
                return runCatching {
                    FileInputStream(file).use { it.readBytes() }
                }.getOrNull()
            }
        }

        // 4. Content scheme or android.resource scheme via ContentResolver
        if (uri != null) {
            return runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()
        }

        return null
    }

    /**
     * Saves and optimizes animal photo or task proof by downsampling and compressing to high-quality JPEG.
     * Guaranteed to persist in context.filesDir so it survives across app lifecycles.
     */
    fun saveImageToInternalStorage(
        context: Context,
        sourceUri: Any,
        subDir: String = "animal_photos",
        prefix: String = "animal"
    ): String? {
        return try {
            val bytes = readBytesFromUri(context, sourceUri)
            if (bytes == null || bytes.isEmpty()) {
                Log.w(TAG, "Cannot save image: source bytes are empty for $sourceUri")
                return null
            }

            val photosDir = File(context.filesDir, subDir).apply {
                if (!exists()) mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cleanPrefix = prefix.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destFile = File(photosDir, "${cleanPrefix}_${timeStamp}_${(System.currentTimeMillis() % 10000)}.jpg")

            // 1. Decode bounds
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

            val maxDimension = 900
            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            var sampleSize = 1
            if (srcWidth > maxDimension || srcHeight > maxDimension) {
                val largerDim = max(srcWidth, srcHeight)
                sampleSize = largerDim / maxDimension
                if (sampleSize < 1) sampleSize = 1
            }

            // 2. Decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)

            if (bitmap != null) {
                // 3. Handle EXIF orientation
                try {
                    ByteArrayInputStream(bytes).use { input ->
                        val exif = ExifInterface(input)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        val matrix = Matrix()
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                        if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                            val rotated = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, matrix, true)
                            if (rotated != bitmap) {
                                bitmap?.recycle()
                                bitmap = rotated
                            }
                        }
                    }
                } catch (_: Exception) {}

                // 4. Compress to optimized JPEG
                FileOutputStream(destFile).use { output ->
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    output.flush()
                }
                bitmap?.recycle()
                Uri.fromFile(destFile).toString()
            } else {
                // Fallback raw copy if bitmap decode failed
                FileOutputStream(destFile).use { output ->
                    output.write(bytes)
                    output.flush()
                }
                Uri.fromFile(destFile).toString()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image to internal storage: ${e.message}", e)
            null
        }
    }

    /**
     * Converts any image URI (file://, content://, raw path) to an optimized Base64 JPEG string
     * suitable for syncing and backing up in Firestore document fields.
     */
    fun uriToBase64(
        context: Context,
        uriString: String?,
        maxDimension: Int = 640,
        quality: Int = 80
    ): String? {
        if (uriString.isNullOrBlank()) return null
        if (uriString.startsWith("data:image/")) {
            val commaIndex = uriString.indexOf(',')
            return if (commaIndex != -1) uriString.substring(commaIndex + 1) else uriString
        }

        return try {
            val bytes = readBytesFromUri(context, uriString) ?: return null

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            var sampleSize = 1
            if (srcWidth > maxDimension || srcHeight > maxDimension) {
                val largerDim = max(srcWidth, srcHeight)
                sampleSize = largerDim / maxDimension
                if (sampleSize < 1) sampleSize = 1
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions) ?: return null

            // Handle EXIF orientation
            try {
                ByteArrayInputStream(bytes).use { input ->
                    val exif = ExifInterface(input)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val matrix = Matrix()
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    }
                    if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        if (rotated != bitmap) {
                            bitmap.recycle()
                            bitmap = rotated
                        }
                    }
                }
            } catch (_: Exception) {}

            val currentMax = max(bitmap.width, bitmap.height)
            val finalBitmap = if (currentMax > maxDimension) {
                val scale = maxDimension.toFloat() / currentMax.toFloat()
                val scaledWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val scaledHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    scaled
                } else bitmap
            } else bitmap

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            finalBitmap.recycle()
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to convert URI to Base64 for Firestore backup: ${e.message}")
            null
        }
    }

    /**
     * Decodes a Base64 string from Firestore into a local JPEG file and returns the file:// URI.
     */
    fun base64ToLocalUri(
        context: Context,
        base64Str: String?,
        subDir: String = "animal_photos",
        fileName: String
    ): String? {
        if (base64Str.isNullOrBlank()) return null
        return try {
            val cleanBase64 = if (base64Str.startsWith("data:image/")) {
                val commaIndex = base64Str.indexOf(',')
                if (commaIndex != -1) base64Str.substring(commaIndex + 1) else base64Str
            } else base64Str

            val bytes = Base64.decode(cleanBase64.trim(), Base64.DEFAULT)
            if (bytes == null || bytes.isEmpty()) return null

            val dir = File(context.filesDir, subDir).apply {
                if (!exists()) mkdirs()
            }
            val cleanFileName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destFile = File(dir, "$cleanFileName.jpg")
            FileOutputStream(destFile).use { output ->
                output.write(bytes)
                output.flush()
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore Base64 image from Firestore to local file: ${e.message}", e)
            null
        }
    }

    /**
     * Checks if the given local file URI is still present and non-empty on disk.
     */
    fun isLocalFileValid(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        if (uriString.startsWith("android.resource://")) return true
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" || uri.scheme == null) {
                val path = uri.path ?: uriString.removePrefix("file://")
                val f = File(path)
                f.exists() && f.isFile && f.length() > 0
            } else if (uri.scheme == "content") {
                context.contentResolver.openInputStream(uri)?.use { true } ?: false
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Ensures an image is locally available on the device.
     * If the local file is missing (e.g. after fresh app install / reinstall),
     * it recovers and restores the image from the Firestore Base64 backup.
     */
    fun restoreImageIfNeeded(
        context: Context,
        localUri: String?,
        base64Data: String?,
        subDir: String,
        fileName: String
    ): String? {
        if (!localUri.isNullOrBlank() && isLocalFileValid(context, localUri)) {
            return localUri
        }
        if (!base64Data.isNullOrBlank()) {
            val restoredUri = base64ToLocalUri(context, base64Data, subDir, fileName)
            if (!restoredUri.isNullOrBlank()) {
                Log.d(TAG, "Successfully restored image from Firestore backup: $restoredUri")
                return restoredUri
            }
        }
        return if (isLocalFileValid(context, localUri)) localUri else null
    }
}


