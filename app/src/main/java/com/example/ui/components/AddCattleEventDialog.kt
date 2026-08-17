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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddCattleEventDialog(
    animalName: String,
    unitId: Long,
    onDismiss: () -> Unit,
    onSaveEvent: (
        eventType: String,
        title: String,
        date: String,
        details: String,
        notes: String,
        metricValue: String,
        reminderText: String
    ) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("PD") } // PD, INSEMINATION, CALVING, DRY_OFF, HEAT, WEIGHT, HEALTH, OTHER
    var pdResult by remember { mutableStateOf("CONFIRMED_POSITIVE") } // CONFIRMED_POSITIVE, NEGATIVE

    val todayDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    var dateText by remember { mutableStateOf(todayDate) }
    var detailsText by remember(selectedCategory) {
        mutableStateOf(
            when (selectedCategory) {
                "INSEMINATION" -> "Inseminated with high-grade dairy semen straw."
                "CALVING" -> "Delivered healthy calf. Mother and calf in good condition."
                "DRY_OFF" -> "Milking stopped. Administered intramammary dry cow therapy."
                "HEAT" -> "Observed standing heat, clear mucus discharge."
                "WEIGHT" -> "Weighed on herd scale."
                "HEALTH" -> "Routine treatment / vaccination administered."
                "OTHER" -> "General management or observation event."
                else -> "Pregnancy confirmed positive via rectal palpation / ultrasound. Gestation normal."
            }
        )
    }
    var notesText by remember { mutableStateOf("Technician: Dr. Otieno (Vet)") }
    var metricText by remember(selectedCategory) {
        mutableStateOf(
            when (selectedCategory) {
                "INSEMINATION" -> "Straw #88"
                "CALVING" -> "Birth Wt: 34 kg"
                "DRY_OFF" -> "Dry period started"
                "HEAT" -> "Standing Heat"
                "WEIGHT" -> "480 kg"
                "HEALTH" -> "2 ml"
                "OTHER" -> ""
                else -> "Positive (In-Calf)"
            }
        )
    }
    var reminderText by remember(selectedCategory) {
        mutableStateOf(
            when (selectedCategory) {
                "INSEMINATION" -> "PD Check in 60-90 days"
                "CALVING" -> "First Heat Check in 45-60 days"
                "DRY_OFF" -> "Expected Calving in ~60 days"
                "HEAT" -> "AI Insemination within 12-18 hours"
                "WEIGHT" -> "Weight check in 30 days"
                "HEALTH" -> "Booster shot in 6 months"
                "OTHER" -> ""
                else -> "Dry Off Alert in ~5 months"
            }
        )
    }

    fun updateDefaultsForCategory(category: String) {
        selectedCategory = category
        when (category) {
            "PD" -> {
                if (pdResult == "CONFIRMED_POSITIVE") {
                    detailsText = "Pregnancy confirmed positive via veterinary check. Cow is now In-Calf."
                    metricText = "Positive (In-Calf)"
                    reminderText = "Dry Off check 60 days before expected calving"
                } else {
                    detailsText = "Pregnancy test negative. Cow is open and ready for next heat detection / re-insemination."
                    metricText = "Negative (Open)"
                    reminderText = "Heat check in 21 days"
                }
                notesText = "Technician: Dr. Otieno (Vet)"
            }
            "INSEMINATION" -> {
                detailsText = "Inseminated with Friesian Bull Straw #FRIESIAN-88 (Sire: Thunder #045)."
                notesText = "Technician: Dr. Otieno (Vet)"
                metricText = "Straw #88"
                reminderText = "In 21 days (Repeat Heat / PD Check)"
            }
            "CALVING" -> {
                detailsText = "Successfully gave birth to healthy calf. Mother transition to fresh lactation milking."
                notesText = "Calf Tag: #132 (Heifer Calf)"
                metricText = "Birth Wt: 34 kg"
                reminderText = "Colostrum Feeding & Post-calving check in 24h"
            }
            "DRY_OFF" -> {
                detailsText = "Lactation halted. Cow dried off 60 days prior to expected calving with dry cow antibiotic sealant."
                notesText = "Administered Dry Cow Cloxacillin intramammary"
                metricText = "Teats Sealed"
                reminderText = "Close-up transitional feed in 4 weeks"
            }
            "HEAT" -> {
                detailsText = "Clear mucus discharge and standing heat recorded during morning herd inspection."
                notesText = "Observed by Worker John"
                metricText = "Standing Heat"
                reminderText = "AI Service Due within 12-18 hours (AM/PM Rule)"
            }
            "WEIGHT" -> {
                detailsText = "Body Condition Score: 3.5/5. Healthy weight progression."
                notesText = "Weighed on digital livestock scale"
                metricText = "510 kg"
                reminderText = "In 30 days (Monthly Weighing)"
            }
            "HEALTH" -> {
                detailsText = "Foot & Mouth booster 2ml administered subcutaneously."
                notesText = "Dosage: 2ml subcutaneously (Batch #FMD-2026-X)"
                metricText = "2 ml"
                reminderText = "In 6 months (Booster Due)"
            }
            "OTHER" -> {
                detailsText = "General observation or other farm management activity."
                notesText = "Recorded by: Staff"
                metricText = ""
                reminderText = ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("add_cattle_event_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Event,
                            contentDescription = null,
                            tint = ForestGreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Log Cattle Event",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Animal: $animalName",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Automatic stage info card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ Logging PD, AI, Calving, or Dry-off automatically updates the animal's stage & production status.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Event Type Selection Bar
                Text(
                    text = "SELECT EVENT TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val eventTypesRow1 = listOf(
                    "PD" to "🧪 PD",
                    "INSEMINATION" to "🧬 AI / Service",
                    "CALVING" to "🍼 Calving",
                    "DRY_OFF" to "🍂 Dry Off"
                )

                val eventTypesRow2 = listOf(
                    "HEAT" to "🔥 Heat",
                    "WEIGHT" to "⚖️ Weight",
                    "HEALTH" to "🩺 Health",
                    "OTHER" to "📌 Other"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventTypesRow1.forEach { (typeKey, label) ->
                        val isSelected = selectedCategory == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreenPrimary else Color(0xFFF1F5F9))
                                .clickable { updateDefaultsForCategory(typeKey) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventTypesRow2.forEach { (typeKey, label) ->
                        val isSelected = selectedCategory == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreenPrimary else Color(0xFFF1F5F9))
                                .clickable { updateDefaultsForCategory(typeKey) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }

                // If Pregnancy Diagnosis (PD) is selected, show Positive vs Negative choice
                if (selectedCategory == "PD") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "PREGNANCY DIAGNOSIS RESULT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pdResult == "CONFIRMED_POSITIVE") Color(0xFFDCFCE7) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.5.dp,
                                if (pdResult == "CONFIRMED_POSITIVE") ForestGreenPrimary else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    pdResult = "CONFIRMED_POSITIVE"
                                    detailsText = "Pregnancy confirmed positive. Cow is now in-calf."
                                    metricText = "Positive (In-Calf)"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🤰 POSITIVE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ForestGreenPrimary)
                                Text("Sets stage to IN-CALF", fontSize = 10.sp, color = Color(0xFF166534))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (pdResult == "NEGATIVE") Color(0xFFFEE2E2) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                1.5.dp,
                                if (pdResult == "NEGATIVE") Color(0xFFDC2626) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    pdResult = "NEGATIVE"
                                    detailsText = "Pregnancy test negative. Cow is open."
                                    metricText = "Negative (Open)"
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("❌ NEGATIVE", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                                Text("Open (Not In-Calf)", fontSize = 10.sp, color = Color(0xFF991B1B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Metric Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppDatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Event Date",
                        modifier = Modifier.weight(1f),
                        testTag = "cattle_event_date_picker"
                    )

                    OutlinedTextField(
                        value = metricText,
                        onValueChange = { metricText = it },
                        label = {
                            Text(
                                when (selectedCategory) {
                                    "PD" -> "PD Result"
                                    "INSEMINATION" -> "Straw / Sire ID"
                                    "CALVING" -> "Calf Weight"
                                    "DRY_OFF" -> "Treatment"
                                    "WEIGHT" -> "Weight (kg)"
                                    "HEALTH" -> "Dosage"
                                    else -> "Details / Metric"
                                }
                            )
                        },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    "WEIGHT" -> "480 kg"
                                    "OTHER" -> "e.g. Hoof trim"
                                    else -> "Details"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Details Text Field
                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    label = { Text("Event Observations / Details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes / Technician Text Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Technician / Performed By / Notes") },
                    placeholder = { Text("e.g. Dr. Otieno (Vet) / John") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Upcoming Notification / Reminder
                OutlinedTextField(
                    value = reminderText,
                    onValueChange = { reminderText = it },
                    label = { Text("🔔 Next Follow-up Alert / Reminder") },
                    placeholder = { Text("e.g. Dry-off check in 60 days") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("Cancel", color = Color(0xFF475569))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val computedTitle = when (selectedCategory) {
                                "PD" -> if (pdResult == "CONFIRMED_POSITIVE") "Pregnancy Diagnosis (PD) - Positive" else "Pregnancy Diagnosis (PD) - Negative"
                                "INSEMINATION" -> "Artificial Insemination (AI)"
                                "CALVING" -> "Calving & Calf Delivery"
                                "DRY_OFF" -> "Dry Off"
                                "HEAT" -> "Estrus (Heat Period) Observed"
                                "WEIGHT" -> "Weight Measurement"
                                "HEALTH" -> "Health & Treatment"
                                "OTHER" -> "Other Cattle Event"
                                else -> selectedCategory.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                            }
                            onSaveEvent(
                                selectedCategory,
                                computedTitle,
                                dateText,
                                detailsText,
                                notesText,
                                metricText,
                                reminderText
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("SAVE EVENT LOG", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
