package com.hbs.feature.hoteldetail.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbs.core.common.Money
import com.hbs.core.data.Room

@Composable
fun HotelDetailRoute(
    onBook: (hotelId: String, roomId: String, nights: Int) -> Unit,
    viewModel: HotelDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HotelDetailEffect.NavigateToBooking -> onBook(effect.hotelId, effect.roomId, effect.nights)
            }
        }
    }
    HotelDetailContent(state, viewModel::onIntent)
}

@Composable
fun HotelDetailContent(state: HotelDetailState, onIntent: (HotelDetailIntent) -> Unit) {
    val hotel = state.hotel
    // The animated value is read here, at screen level, so the whole screen recomposes every frame (TASKS.md T5).
    val animatedTotal by animateIntAsState(state.total?.minor?.toInt() ?: 0, tween(600), label = "total")
    Scaffold(
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Total for ${state.nights} night(s)", style = MaterialTheme.typography.labelMedium)
                    Text(Money(animatedTotal.toLong(), state.total?.currency ?: "USD").format(), style = MaterialTheme.typography.titleLarge)
                }
                Button(onClick = { onIntent(HotelDetailIntent.BookClicked) }, enabled = state.total != null) { Text("Book") }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> CircularProgressIndicator(Modifier.padding(padding).padding(16.dp))
            hotel == null -> Text(state.error ?: "Not found", Modifier.padding(padding).padding(16.dp), color = MaterialTheme.colorScheme.error)
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(hotel.name, style = MaterialTheme.typography.headlineMedium)
                    Text("${hotel.city} · ★ ${hotel.rating}")
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { onIntent(HotelDetailIntent.ChangeNights(-1)) }) { Text("−") }
                        Text("${state.nights} night(s)")
                        OutlinedButton(onClick = { onIntent(HotelDetailIntent.ChangeNights(1)) }) { Text("+") }
                    }
                }
                items(hotel.rooms, key = { it.id }) { room ->
                    RoomCard(room, selected = room.id == state.selectedRoomId) { onIntent(HotelDetailIntent.SelectRoom(room.id)) }
                }
            }
        }
    }
}

@Composable
private fun RoomCard(room: Room, selected: Boolean, onClick: () -> Unit) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "roomColor",
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).animateContentSize(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(room.name, style = MaterialTheme.typography.titleMedium)
            Text("${room.pricePerNight.format()} / night")
            if (selected) Text(room.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
