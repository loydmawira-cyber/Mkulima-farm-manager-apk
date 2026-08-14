package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FarmUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEggLogDialog(
    availableUnits: List<FarmUnit>,
    onDismiss: () -> Unit,
    onSaveEggLog: (unitName: String, totalEggs: Int, damagedEggs: Int, grade: String, notes: String?) -> Unit
) {
    val poultryUnits = availableUnits.filter { it.type.contains("Poultry", ignoreCase = true) || it.name.contains("Flock", ignoreCase = true) }
    var selectedUnitName by remember { mutableStateOf(poultryUnits.firstOrNull()?.name ?: "Flock B - Kienyeji Layers") }
    var totalText by remember { mutableStateOf("300") }
    var damagedText by remember { mutableStateOf("2") }
    var selectedGrade by remember { mutableStateOf("Grade A") }
    var notesText by remember { mutableStateOf("") }
    var collectionDate by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_egg_log_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "🥚 Log Egg Collection",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "Record daily egg count and crates collected from poultry flock",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppDatePickerField(
                    value = collectionDate,
                    onValueChange = { collectionDate = it },
                    label = "Collection Date",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "egg_collection_date_picker"
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = selectedUnitName,
                    onValueChange = { selectedUnitName = it },
                    label = { Text("Poultry Flock Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = it },
                        label = { Text("Total Eggs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = damagedText,
                        onValueChange = { damagedText = it },
                        label = { Text("Cracked / Damaged") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Egg Quality Grade",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Grade A", "Grade B", "Mixed").forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEADDFF),
                                selectedLabelColor = Color(0xFF21005D)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Crates Count (Optional)") },
                    placeholder = { Text("e.g. 10 crates packed for market") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(100.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val total = totalText.toIntOrNull() ?: 0
                            val damaged = damagedText.toIntOrNull() ?: 0
                            val fullNote = if (notesText.isNotBlank()) "[$collectionDate] $notesText" else "[$collectionDate]"
                            onSaveEggLog(selectedUnitName, total, damaged, selectedGrade, fullNote)
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Save Egg Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
