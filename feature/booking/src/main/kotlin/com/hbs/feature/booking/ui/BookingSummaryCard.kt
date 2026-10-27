package com.hbs.feature.booking.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hbs.feature.booking.domain.BookingSummary

@Composable
fun BookingSummaryCard(summary: BookingSummary?, error: String?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            when {
                summary != null -> {
                    Text(summary.hotelName, style = MaterialTheme.typography.titleMedium)
                    Text("${summary.roomName} · ${summary.nights} night(s)")
                    Text("Total ${summary.total.format()}", style = MaterialTheme.typography.titleLarge)
                }
                error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                else -> Text("Loading…")
            }
        }
    }
}
