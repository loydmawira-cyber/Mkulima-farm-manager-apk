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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
    onDismiss: () -> Unit,
    onSaveEvent: (
        eventType: String, // "HEAT", "INSEMINATION", "WEIGHT", "HEALTH"
        title: String,
        date: String,
        details: String,
        notes: String,
        metricValue: String,
        nextReminderDate: String
    ) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("HEAT") } // HEAT, INSEMINATION, WEIGHT, HEALTH

    val todayDate = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    var dateText by remember { mutableStateOf(todayDate) }
    var titleText by remember { mutableStateOf("Estrus (Heat Period) Observed") }
    var detailsText by remember { mutableStateOf("Clear mucus discharge and standing heat observed during morning check.") }
    var notesText by remember { mutableStateOf("Observed by field worker") }
    var metricText by remember { mutableStateOf("") }
    var reminderText by remember { mutableStateOf("21 days later (Next Heat Check)") }

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
                    .padding(20.dp)
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
                                fontSize = 20.sp,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Event Type Selection Bar
                Text(
                    text = "SELECT EVENT TYPE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val eventTypes = listOf(
                    "HEAT" to "🔥 Heat",
                    "INSEMINATION" to "🧬 AI",
                    "WEIGHT" to "⚖️ Weight",
                    "HEALTH" to "🩺 Health"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    eventTypes.forEach { (typeKey, label) ->
                        val isSelected = selectedCategory == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ForestGreenPrimary else Color.Transparent)
                                .clickable {
                                    selectedCategory = typeKey
                                    // Update defaults according to selected type
                                    when (typeKey) {
                                        "HEAT" -> {
                                            titleText = "Estrus (Heat Period) Observed"
                                            detailsText = "Clear mucus discharge & standing heat recorded."
                                            metricText = ""
                                            reminderText = "21 days later (Next expected heat)"
                                        }
                                        "INSEMINATION" -> {
                                            titleText = "Artificial Insemination (AI)"
                                            detailsText = "Inseminated with Friesian Bull Straw #45."
                                            notesText = "Technician: Dr. Otieno"
                                            metricText = "Straw #45"
                                            reminderText = "In 21 days (Repeat Heat / PD Check)"
                                        }
                                        "WEIGHT" -> {
                                            titleText = "Routine Weight Check"
                                            detailsText = "Body Condition Score: 3.5/5. Good health."
                                            metricText = "485 kg"
                                            reminderText = "In 30 days (Monthly Weighing)"
                                        }
                                        "HEALTH" -> {
                                            titleText = "Vaccination / Medication"
                                            detailsText = "Foot & Mouth booster 2ml administered."
                                            notesText = "Dosage: 2ml subcutaneously"
                                            metricText = "2 ml"
                                            reminderText = "In 6 months (Booster Due)"
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Date & Metric Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Event Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (selectedCategory == "WEIGHT" || selectedCategory == "HEALTH" || selectedCategory == "INSEMINATION") {
                        OutlinedTextField(
                            value = metricText,
                            onValueChange = { metricText = it },
                            label = {
                                Text(
                                    when (selectedCategory) {
                                        "WEIGHT" -> "Weight (kg)"
                                        "HEALTH" -> "Dosage / Value"
                                        else -> "Straw / Bull ID"
                                    }
                                )
                            },
                            placeholder = { Text(if (selectedCategory == "WEIGHT") "480 kg" else "Details") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
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
                    label = { Text("🔔 Next Event Notification / Follow-up Alert") },
                    placeholder = { Text("e.g. Next Heat Check in 21 days") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                            if (titleText.isNotBlank()) {
                                onSaveEvent(
                                    selectedCategory,
                                    titleText,
                                    dateText,
                                    detailsText,
                                    notesText,
                                    metricText,
                                    reminderText
                                )
                            }
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
