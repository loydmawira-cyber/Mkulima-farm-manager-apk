package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FarmUnit
import com.example.data.MilkLog
import com.example.data.MilkLogEntryRules
import com.example.ui.screens.AnimalCowItem
import com.example.ui.screens.isMilkingCow
import com.example.ui.screens.mockAnimals
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddMilkLogDialog(
    availableUnits: List<FarmUnit>,
    milkLogs: List<MilkLog> = emptyList(),
    userRole: String = "OWNER",
    canEditPastDaysLogs: Boolean = true,
    onDismiss: () -> Unit,
    onSaveMilkLog: (
        cowName: String,
        unitName: String,
        litres: Double,
        session: String,
        fatPercentage: Double,
        date: String,
        notes: String?
    ) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val deletedPrefs = remember { context.getSharedPreferences("mkulima_deleted_animals", android.content.Context.MODE_PRIVATE) }
    val deletedSet = remember {
        try {
            deletedPrefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            val raw = try { deletedPrefs.getString("deleted_ids", "") ?: "" } catch (ex: Exception) { "" }
            if (raw.isNotBlank()) raw.split(",").toSet() else emptySet()
        }
    }

    val cowsList = remember(availableUnits, deletedSet) {
        val result = mutableListOf<AnimalCowItem>()

        // 1. From Room units (registered farm livestock)
        availableUnits.filter {
            (it.type.equals("Cattle", ignoreCase = true) || it.type.equals("CATTLE", ignoreCase = true)) &&
            !deletedSet.contains("unit_${it.id}") && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(
                name = it.name,
                breed = it.breed,
                status = it.healthStatus,
                tag = it.tagNumber,
                lastMilk = it.currentWeight
            )
        }.forEach { unit ->
            val tag = unit.tagNumber.ifBlank { "#${unit.id + 100}" }
            val displayName = if (unit.name.contains(tag)) unit.name else "${unit.name} ($tag)"
            result.add(
                AnimalCowItem(
                    tagId = tag,
                    name = displayName,
                    breed = unit.breed.ifBlank { "Dairy Cattle" },
                    lactationDay = 90
                )
            )
        }

        // 2. From mockAnimals (registered farm livestock list)
        mockAnimals.filter {
            it.category.equals("CATTLE", ignoreCase = true) &&
            !deletedSet.contains(it.id) && !deletedSet.contains(it.name.lowercase()) &&
            isMilkingCow(
                name = it.name,
                breed = it.breed,
                status = it.status,
                tag = it.tagNumber,
                lastMilk = it.lastMilk,
                breedingStatus = it.breedingStatus
            )
        }.forEach { animal ->
            val tag = animal.tagNumber.ifBlank { "#100" }
            val displayName = if (animal.name.contains(tag)) animal.name else "${animal.name} ($tag)"
            result.add(
                AnimalCowItem(
                    tagId = tag,
                    name = displayName,
                    breed = animal.breed.ifBlank { "Dairy Cattle" },
                    lactationDay = 90
                )
            )
        }

        // Deduplicate by normalized base name
        result.distinctBy {
            it.name.substringBefore(" (").substringBefore(" -").substringBefore("#").trim().lowercase()
        }
    }

    var cowSearchQuery by remember { mutableStateOf("") }
    var selectedCow by remember { mutableStateOf<AnimalCowItem?>(cowsList.firstOrNull()) }
    var showCowDropdown by remember { mutableStateOf(false) }
    var milkLitresText by remember { mutableStateOf("") }
    var fatPercentageText by remember { mutableStateOf("3.8") }
    var selectedSession by remember { mutableStateOf("Morning") } // Morning, Afternoon, Evening

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val todayDateStr = remember { dateFormat.format(Date()) }
    val yesterdayDateStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        dateFormat.format(cal.time)
    }
    val twoDaysAgoDateStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
        dateFormat.format(cal.time)
    }
    var selectedLogDate by remember { mutableStateOf(todayDateStr) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val isOwner = userRole.equals("OWNER", ignoreCase = true)
    val cannotEditPast = !isOwner && !canEditPastDaysLogs
    val selectedDateIsPastRestricted = cannotEditPast && com.example.util.DateValidationUtils.isPastDate(selectedLogDate)
    val dairyUnits = availableUnits.filter { it.type.contains("Cattle", ignoreCase = true) || it.name.contains("Dairy", ignoreCase = true) }
    var selectedUnitName by remember(dairyUnits) { mutableStateOf(dairyUnits.firstOrNull()?.name ?: "Dairy Section") }
    var notesText by remember { mutableStateOf("") }

    val matchingCows = remember(cowsList, cowSearchQuery) {
        cowsList.filter {
            cowSearchQuery.isEmpty() ||
            it.name.contains(cowSearchQuery, ignoreCase = true) ||
            it.tagId.contains(cowSearchQuery, ignoreCase = true) ||
            it.breed.contains(cowSearchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(cowsList) {
        if (selectedCow == null || cowsList.none { it.tagId == selectedCow?.tagId || it.name == selectedCow?.name }) {
            selectedCow = cowsList.firstOrNull()
        }
    }

    LaunchedEffect(selectedCow, selectedSession, selectedLogDate) {
        saveSuccessMessage = null
    }

    val selectedDateIsFuture = MilkLogEntryRules.isFutureDate(selectedLogDate)
    val selectedDateIsValid = MilkLogEntryRules.canonicalDateKey(selectedLogDate) != null
    val targetCowName = selectedCow?.name ?: cowSearchQuery.trim()
    val selectedMilkSlotRecorded = if (targetCowName.isNotBlank()) {
        milkLogs.any { log -> MilkLogEntryRules.isSameSlot(log, targetCowName, selectedLogDate, selectedSession) }
    } else false

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_milk_log_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, ForestGreenPrimary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WaterDrop,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Quick Milk Entry",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Record collection volume & session details",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 1: Search Animal by Name, ID, or Tag
                Text(
                    text = "1. SEARCH & SELECT ANIMAL (MILKING COWS ONLY)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedCow != null && cowSearchQuery.isBlank()) selectedCow?.name ?: "" else cowSearchQuery,
                        onValueChange = { query ->
                            cowSearchQuery = query
                            selectedCow = cowsList.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
                            showCowDropdown = true
                        },
                        placeholder = { Text("Search by Name (e.g. Bessie), Tag (#102)...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreenPrimary) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedCow != null || cowSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            selectedCow = null
                                            cowSearchQuery = ""
                                            showCowDropdown = true
                                        }
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp), tint = Color(0xFF94A3B8))
                                    }
                                }
                                IconButton(onClick = { showCowDropdown = !showCowDropdown }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF475569))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_cow_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    DropdownMenu(
                        expanded = showCowDropdown,
                        onDismissRequest = { showCowDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color.White)
                    ) {
                        if (matchingCows.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No matching milking cows found", color = Color.Gray, fontSize = 13.sp) },
                                onClick = { showCowDropdown = false }
                            )
                        } else {
                            matchingCows.forEach { cow ->
                                val isCurrent = selectedCow?.tagId == cow.tagId || selectedCow?.name == cow.name
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = cow.name,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isCurrent) ForestGreenPrimary else Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "${cow.breed} • ${cow.tagId}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                            if (isCurrent) {
                                                Text("✓", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedCow = cow
                                        cowSearchQuery = ""
                                        showCowDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Select Animal Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cowsList) { cow ->
                        val isSel = selectedCow?.tagId == cow.tagId || selectedCow?.name == cow.name
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) ForestGreenPrimary else Color(0xFFF1F5F9),
                            border = if (isSel) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable {
                                selectedCow = cow
                                cowSearchQuery = ""
                            }
                        ) {
                            Text(
                                text = cow.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Color(0xFF334155),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 2: Session Selection (Morning, Afternoon, Evening)
                Text(
                    text = "2. MILKING SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        Triple("Morning", "🌅 Morning", Icons.Filled.WbSunny),
                        Triple("Afternoon", "☀️ Afternoon", Icons.Filled.WbCloudy),
                        Triple("Evening", "🌙 Evening", Icons.Filled.NightsStay)
                    ).forEach { (sessionKey, sessionLabel, icon) ->
                        val isSelected = selectedSession.equals(sessionKey, ignoreCase = true)
                        val isSessionRecorded = if (targetCowName.isNotBlank()) {
                            milkLogs.any { log -> MilkLogEntryRules.isSameSlot(log, targetCowName, selectedLogDate, sessionKey) }
                        } else false

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> ForestGreenPrimary
                                        isSessionRecorded -> Color(0xFFE2E8F0)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { selectedSession = sessionKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSessionRecorded && !isSelected) Icons.Filled.Check else icon,
                                    contentDescription = null,
                                    tint = when {
                                        isSelected -> Color.White
                                        isSessionRecorded -> ForestGreenPrimary
                                        else -> Color(0xFF64748B)
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSessionRecorded && !isSelected) "${sessionKey} ✓" else sessionLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSelected -> Color.White
                                        isSessionRecorded -> Color(0xFF1E293B)
                                        else -> Color(0xFF475569)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 3: Record Date (Today, Yesterday, 2 Days Ago, Custom)
                Text(
                    text = "3. RECORD DATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedLogDate == todayDateStr,
                        onClick = { selectedLogDate = todayDateStr },
                        label = { Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (!cannotEditPast) {
                        FilterChip(
                            selected = selectedLogDate == yesterdayDateStr,
                            onClick = { selectedLogDate = yesterdayDateStr },
                            label = { Text("Yesterday", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedLogDate == twoDaysAgoDateStr,
                            onClick = { selectedLogDate = twoDaysAgoDateStr },
                            label = { Text("2 Days Ago", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                AppDatePickerField(
                    value = selectedLogDate,
                    onValueChange = { selectedLogDate = it },
                    label = "Log Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_milk_quick_log_date_picker"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 4: Enter Milk Details (Litres & Fat %)
                Text(
                    text = "4. MILK VOLUME & DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = milkLitresText,
                        onValueChange = { milkLitresText = it },
                        label = { Text("Volume (Litres)*") },
                        placeholder = { Text("e.g. 14.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("dialog_milk_litres_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fatPercentageText,
                        onValueChange = { fatPercentageText = it },
                        label = { Text("Fat % (Optional)") },
                        placeholder = { Text("e.g. 3.8") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Chilled to 4°C, Delivered to Co-op") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val cowName = selectedCow?.name ?: cowSearchQuery.ifBlank { "Unassigned Cow" }
                            val litres = milkLitresText.toDoubleOrNull() ?: 0.0
                            val fat = fatPercentageText.toDoubleOrNull() ?: 3.8
                            val fullNote = notesText.trim().ifBlank { null }

                            if (litres > 0 && !selectedMilkSlotRecorded && !selectedDateIsFuture && !selectedDateIsPastRestricted && selectedDateIsValid) {
                                onSaveMilkLog(
                                    cowName,
                                    selectedUnitName,
                                    litres,
                                    selectedSession,
                                    fat,
                                    selectedLogDate,
                                    fullNote
                                )
                                saveSuccessMessage = "Successfully recorded ${litres}L for $cowName ($selectedSession session)!"
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("dialog_quick_save_milk_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ForestGreenPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = if (selectedMilkSlotRecorded) Color(0xFF475569) else Color(0xFFCBD5E1),
                            disabledContentColor = if (selectedMilkSlotRecorded) Color.White else Color(0xFF64748B)
                        ),
                        enabled = milkLitresText.isNotBlank() && (selectedCow != null || cowSearchQuery.isNotBlank()) &&
                            !selectedMilkSlotRecorded && !selectedDateIsFuture && !selectedDateIsPastRestricted && selectedDateIsValid
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                selectedMilkSlotRecorded -> "RECORDED"
                                selectedDateIsFuture -> "FUTURE DATE NOT ALLOWED"
                                selectedDateIsPastRestricted -> "PREVIOUS DAYS LOCKED"
                                !selectedDateIsValid -> "SELECT A VALID DATE"
                                else -> "SAVE COW MILK LOG"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (saveSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = saveSuccessMessage ?: "",
                                color = ForestGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (selectedMilkSlotRecorded || selectedDateIsFuture || selectedDateIsPastRestricted || !selectedDateIsValid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            selectedMilkSlotRecorded -> "This cow's ${MilkLogEntryRules.normalizedSession(selectedSession).lowercase()} milk is already recorded for this date. Delete it in Log History before recording again."
                            selectedDateIsFuture -> "Milk cannot be recorded for a future date."
                            selectedDateIsPastRestricted -> "Recording logs for previous days is disabled for worker accounts. Please select today's date."
                            else -> "Choose a valid date on or before today."
                        },
                        color = if (selectedMilkSlotRecorded) Color(0xFF475569) else Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
