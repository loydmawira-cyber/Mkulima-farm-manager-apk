package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.utils.PoultryAgeAndVaccinationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Material 3 Calendar Popup Dialog with selectable dates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateMillis: Long? = null,
    onDateSelected: (formattedDate: String, millis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultMillis = initialDateMillis ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = defaultMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: defaultMillis
                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selectedMillis
                    }
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val formatted = sdf.format(utcCalendar.time)
                    onDateSelected(formatted, selectedMillis)
                    onDismiss()
                }
            ) {
                Text("OK", color = ForestGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF64748B), fontSize = 14.sp)
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
 * Unified, standard date-picker input field across the entire app.
 * Has exactly ONE calendar icon and is 100% clickable across its entire surface.
 */
@Composable
fun AppDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select date",
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

    Box(
        modifier = modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showDatePicker = true }
            )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Pick date from calendar",
                    tint = ForestGreenPrimary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color(0xFF1E293B),
                disabledBorderColor = Color(0xFFCBD5E1),
                disabledLabelColor = Color(0xFF475569),
                disabledPlaceholderColor = Color(0xFF94A3B8),
                disabledTrailingIconColor = ForestGreenPrimary,
                disabledContainerColor = Color.White
            ),
            singleLine = true
        )
        // Full overlay box ensures taps anywhere on the field open the calendar
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
        )
    }
}
