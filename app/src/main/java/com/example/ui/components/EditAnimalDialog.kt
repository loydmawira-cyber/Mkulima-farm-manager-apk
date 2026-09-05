package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.example.R
import com.example.ui.screens.AnimalDetailData
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.util.ImageStorageUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditAnimalDialog(
    animal: AnimalDetailData,
    onDismiss: () -> Unit,
    onSaveAnimal: (
        name: String,
        tagNumber: String,
        breed: String,
        category: String,
        status: String,
        breedingStatus: String,
        age: String,
        dob: String,
        weightAtBirth: String,
        currentWeight: String,
        sire: String,
        dam: String,
        headCount: Int,
        photoUri: String?,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    val isInitiallyPoultry = animal.category.equals("POULTRY", ignoreCase = true) ||
            animal.breed.contains("Layer", ignoreCase = true) ||
            animal.breed.contains("Flock", ignoreCase = true)

    var category by remember(animal) { mutableStateOf(if (isInitiallyPoultry) "POULTRY" else "CATTLE") }

    // Animal Info State
    var name by remember(animal) { mutableStateOf(animal.name) }
    var tagNumber by remember(animal) { mutableStateOf(animal.tagNumber) }
    var breed by remember(animal) { mutableStateOf(animal.breed) }
    var dob by remember(animal) { mutableStateOf(animal.dateOfBirth) }
    var weightAtBirth by remember(animal) { mutableStateOf(animal.weightAtBirth) }
    var currentWeight by remember(animal) { mutableStateOf(animal.weight) }
    var sire by remember(animal) { mutableStateOf(animal.sire) }
    var dam by remember(animal) { mutableStateOf(animal.dam) }
    var status by remember(animal) { mutableStateOf(animal.status) }
    var breedingStatus by remember(animal) { mutableStateOf(animal.breedingStatus) }
    var ageText by remember(animal) { mutableStateOf(animal.age) }
    var photoUri by remember(animal) { mutableStateOf(animal.photoUri) }
    var notes by remember(animal) { mutableStateOf(animal.notes) }

    // Poultry HeadCount
    var headCount by remember(animal) { mutableIntStateOf(animal.headCountInt.coerceAtLeast(1)) }
    var headCountText by remember(animal) { mutableStateOf(animal.headCountInt.toString()) }

    var showCameraDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri, subDir = "animal_photos", prefix = "animal") ?: uri.toString()
            photoUri = saved
        }
    }

    val isCattle = category == "CATTLE"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. PINNED HEADER WITH TITLE, BADGE, AND CLOSE BUTTON
                Surface(
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Modify Animal Record",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Editing #${animal.tagNumber.ifBlank { animal.name }} • ${animal.breed.ifBlank { animal.category }}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color(0xFFE2E8F0), CircleShape)
                                .size(36.dp)
                                .testTag("edit_animal_header_close")
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // 2. SCROLLABLE FORM CONTENT
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // PHOTO / AVATAR UPLOAD SECTION
                    Text(
                        text = "Animal Photo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar display with Fallback
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isCattle) Color(0xFFE8F5E9) else Color(0xFFFEF3C7))
                                    .border(
                                        1.5.dp,
                                        if (isCattle) ForestGreenPrimary.copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(photoUri)
                                            .crossfade(true)
                                            .placeholder(R.drawable.ic_livestock_placeholder)
                                            .error(R.drawable.ic_livestock_placeholder)
                                            .memoryCachePolicy(CachePolicy.ENABLED)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .build(),
                                        contentDescription = "Animal Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Generic Icon Fallback
                                    Icon(
                                        imageVector = if (isCattle) Icons.Filled.Pets else Icons.Filled.Egg,
                                        contentDescription = if (isCattle) "Cow Icon" else "Poultry Icon",
                                        tint = if (isCattle) ForestGreenPrimary else Color(0xFFD97706),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // Photo Action Buttons
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showCameraDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("edit_animal_camera_button")
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("edit_animal_gallery_button")
                                    ) {
                                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Gallery", fontSize = 12.sp, color = Color(0xFF475569))
                                    }
                                }

                                if (!photoUri.isNullOrBlank()) {
                                    Text(
                                        text = "Remove Photo",
                                        color = Color(0xFFDC2626),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .clickable { photoUri = null }
                                            .padding(top = 2.dp)
                                    )
                                } else {
                                    Text(
                                        text = "No photo uploaded • Fallback icon active",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Toggle Pills
                    Text(
                        text = "Livestock Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { category = "CATTLE" },
                            color = if (isCattle) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isCattle) ForestGreenPrimary else Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Pets,
                                    contentDescription = null,
                                    tint = if (isCattle) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cattle / Dairy",
                                    color = if (isCattle) Color.White else Color(0xFF334155),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { category = "POULTRY" },
                            color = if (!isCattle) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (!isCattle) ForestGreenPrimary else Color(0xFFCBD5E1))
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Egg,
                                    contentDescription = null,
                                    tint = if (!isCattle) Color.White else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Poultry Flock",
                                    color = if (!isCattle) Color.White else Color(0xFF334155),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Identification Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (isCattle) "Animal Name *" else "Flock Name *") },
                            placeholder = { Text(if (isCattle) "e.g. Bessie" else "e.g. Layer Flock A") },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("edit_animal_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = tagNumber,
                            onValueChange = { tagNumber = it },
                            label = { Text(if (isCattle) "Ear Tag #" else "Flock / Batch #") },
                            placeholder = { Text(if (isCattle) "#102" else "FLK-01") },
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("edit_animal_tag_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isCattle) {
                        // CATTLE SPECIFIC FIELDS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = breed,
                                onValueChange = { breed = it },
                                label = { Text("Breed") },
                                placeholder = { Text("Friesian, Ayrshire, Jersey") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = currentWeight,
                                onValueChange = { currentWeight = it },
                                label = { Text("Current Weight") },
                                placeholder = { Text("520 kg") },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Breed Chips
                        Text("Popular Breeds:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Friesian", "Ayrshire", "Jersey", "Guernsey", "Boran", "Sahiwal", "Simmental").forEach { b ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { breed = b }
                                ) {
                                    Text(
                                        text = b,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stage / Health Status
                        OutlinedTextField(
                            value = status,
                            onValueChange = { status = it },
                            label = { Text("Current Production Stage / Status") },
                            placeholder = { Text("Milking, In-Calf, Inseminated, Heifer, Calf, Dry") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_animal_status_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Stage Chips
                        Text("Quick Select Stage:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Milking", "In-Calf / Milking", "In-Calf", "Inseminated", "Heifer", "Calf", "Dry").forEach { st ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (status.equals(st, ignoreCase = true)) ForestGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (status.equals(st, ignoreCase = true)) ForestGreenPrimary else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { status = st }
                                ) {
                                    Text(
                                        text = st,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (status.equals(st, ignoreCase = true)) ForestGreenPrimary else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = breedingStatus,
                            onValueChange = { breedingStatus = it },
                            label = { Text("Breeding / Reproductive Notes") },
                            placeholder = { Text("e.g. In-Calf (Day 110 of 283), AI Thunder #045") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = {
                                    dob = it
                                    val calcAge = com.example.util.CattleLifecycleEngine.calculateAgeFromDob(it)
                                    if (calcAge != it && calcAge.isNotBlank() && calcAge != "N/A") {
                                        ageText = calcAge
                                    }
                                },
                                label = { Text("Date of Birth") },
                                placeholder = { Text("12 Apr 2021") },
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                trailingIcon = {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                                }
                            )

                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Age") },
                                placeholder = { Text("3y 4m") },
                                modifier = Modifier.weight(0.9f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weightAtBirth,
                                onValueChange = { weightAtBirth = it },
                                label = { Text("Weight at Birth") },
                                placeholder = { Text("32 kg") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = sire,
                                onValueChange = { sire = it },
                                label = { Text("Sire (Bull)") },
                                placeholder = { Text("Thunder #045") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = dam,
                            onValueChange = { dam = it },
                            label = { Text("Dam (Cow / Mother)") },
                            placeholder = { Text("Bessie #102") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    } else {
                        // POULTRY SPECIFIC FIELDS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = headCountText,
                                onValueChange = {
                                    headCountText = it
                                    headCount = it.toIntOrNull()?.coerceAtLeast(0) ?: headCount
                                },
                                label = { Text("Flock Head Count *") },
                                placeholder = { Text("350") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = breed,
                                onValueChange = { breed = it },
                                label = { Text("Breed / Strain") },
                                placeholder = { Text("Isa Brown, Kuroiler") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Popular Poultry Breeds:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Isa Brown", "Kuroiler", "Kenbro", "Kienyeji", "Broiler Cobb 500", "Sasso", "Lohmann Brown").forEach { b ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFFE2E8F0)),
                                    modifier = Modifier.clickable { breed = b }
                                ) {
                                    Text(
                                        text = b,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (breed.equals(b, ignoreCase = true)) ForestGreenPrimary else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = status,
                            onValueChange = { status = it },
                            label = { Text("Health & Production Status") },
                            placeholder = { Text("ACTIVE LAYING, VACCINATION DUE") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Flock Age / Batch Info") },
                            placeholder = { Text("e.g. 28 Weeks (Point of Lay)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Origin (Optional)") },
                        placeholder = { Text("e.g. Bought from ..., Hatched on farm, or health background") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_animal_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. PINNED BOTTOM ACTION BAR (STICKY FOOTER WITH CLEAR SAVE / OK AND CANCEL BUTTONS)
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                Surface(
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF475569)),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("edit_animal_cancel_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cancel",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onSaveAnimal(
                                        name,
                                        tagNumber,
                                        breed,
                                        category,
                                        status,
                                        breedingStatus,
                                        ageText,
                                        dob,
                                        weightAtBirth,
                                        currentWeight,
                                        sire,
                                        dam,
                                        headCount,
                                        photoUri,
                                        notes.trim()
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(50.dp)
                                .testTag("edit_animal_save_button")
                                .testTag("edit_animal_ok_button")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Changes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCameraDialog) {
        CameraCaptureDialog(
            onDismiss = { showCameraDialog = false },
            onPhotoCaptured = { uri ->
                val saved = ImageStorageUtils.saveImageToInternalStorage(context, uri, subDir = "animal_photos", prefix = "animal") ?: uri.toString()
                photoUri = saved
                showCameraDialog = false
            }
        )
    }
}
