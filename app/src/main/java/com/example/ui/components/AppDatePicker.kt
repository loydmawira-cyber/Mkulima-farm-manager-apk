package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ForestGreenPrimary
import com.example.utils.PoultryAgeAndVaccinationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Material 3 Calendar Popup Dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long? = null,
    onDateSelected: (formattedDate: String, millis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        // Material3 DatePicker returns UTC epoch millis; format correctly in UTC to avoid off-by-one day
                        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = selectedMillis
                        }
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val formatted = sdf.format(utcCalendar.time)
                        onDateSelected(formatted, selectedMillis)
                    }
                    onDismiss()
                }
            ) {
                Text("OK", color = ForestGreenPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B))
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = ForestGreenPrimary,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = ForestGreenPrimary,
                todayContentColor = ForestGreenPrimary
            )
        )
    }
}

/**
 * Unified, standard date-picker input field across the whole app.
 * Tapping anywhere on the field displays the Material 3 calendar popup.
 */
@Composable
fun AppDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select date",
    leadingIcon: ImageVector = Icons.Filled.CalendarMonth,
    testTag: String = "app_date_picker_field"
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        val parsed = PoultryAgeAndVaccinationUtils.parseDate(value)
        parsed?.time ?: System.currentTimeMillis()
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = initialMillis,
            onDateSelected = { formatted, _ ->
                onValueChange(formatted)
            },
            onDismiss = { showDatePicker = false }
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                showDatePicker = true
            }
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "Pick date from calendar",
                    tint = ForestGreenPrimary
                )
            },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Open Calendar Popup",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clickable { showDatePicker = true },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestGreenPrimary,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedLabelColor = ForestGreenPrimary
            ),
            singleLine = true,
            interactionSource = interactionSource
        )
    }
}
