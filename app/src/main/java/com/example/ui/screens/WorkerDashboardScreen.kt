package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.*

@Composable
fun WorkerDashboardScreen(
    milkLogs: List<MilkLog>,
    eggLogs: List<EggLog>,
    onAddMilkLogClick: () -> Unit,
    onAddEggLogClick: () -> Unit,
    onAddRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Worker Dashboard", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Button(onClick = onAddMilkLogClick, modifier = Modifier.fillMaxWidth()) {
                Text("Log Milk")
            }
        }
        item {
            Button(onClick = onAddEggLogClick, modifier = Modifier.fillMaxWidth()) {
                Text("Log Eggs")
            }
        }
        item {
            Button(onClick = onAddRequestClick, modifier = Modifier.fillMaxWidth()) {
                Text("Submit Request")
            }
        }
        item {
            Text("Recent Logs", style = MaterialTheme.typography.titleMedium)
        }
        items(milkLogs.take(5)) { log ->
            Text("Milk: ${log.litres}L - ${log.date}")
        }
    }
}
