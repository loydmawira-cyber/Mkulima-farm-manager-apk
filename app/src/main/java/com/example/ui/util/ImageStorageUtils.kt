package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ImageStorageUtils {
    /**
     * Saves and optimizes animal photo by downsampling and compressing to high-quality JPEG.
     * Prevents multi-megabyte uncompressed files from stalling the UI during list rendering.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "animal_photos").apply {
                if (!exists()) mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val destFile = File(photosDir, "animal_$timeStamp.jpg")

            // 1. Decode image bounds first to compute sample size
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val maxDimension = 800
            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            var sampleSize = 1
            if (srcWidth > maxDimension || srcHeight > maxDimension) {
                val largerDim = max(srcWidth, srcHeight)
                sampleSize = largerDim / maxDimension
                if (sampleSize < 1) sampleSize = 1
            }

            // 2. Decode sampled bitmap with compact RGB_565 config
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            var bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }

            // 3. Fix EXIF orientation if needed
            if (bitmap != null) {
                try {
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
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

                // 4. Compress to optimized JPEG (~60KB)
                FileOutputStream(destFile).use { output ->
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 82, output)
                }
                bitmap?.recycle()
                Uri.fromFile(destFile).toString()
            } else {
                // Fallback raw copy if decode fails
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Uri.fromFile(destFile).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }
}
