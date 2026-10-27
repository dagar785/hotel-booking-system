package com.hbs.feature.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hbs.core.data.Hotel

@Composable
fun SearchRoute(onHotelClick: (String) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SearchEffect.NavigateToDetail -> onHotelClick(effect.hotelId)
            }
        }
    }
    SearchContent(state, viewModel::onIntent)
}

@Composable
fun SearchContent(state: SearchState, onIntent: (SearchIntent) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Find a hotel", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.query,
            onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
            label = { Text("Hotel or city") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.isLoading) CircularProgressIndicator()
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { onIntent(SearchIntent.Retry) }) { Text("Retry") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.hotels, key = { it.id }) { hotel -> HotelCard(hotel) { onIntent(SearchIntent.HotelClicked(hotel.id)) } }
        }
    }
}

@Composable
private fun HotelCard(hotel: Hotel, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(hotel.name, style = MaterialTheme.typography.titleMedium)
                Text("${hotel.city} · ★ ${hotel.rating}", style = MaterialTheme.typography.bodySmall)
            }
            Text("from ${hotel.startingPrice.format()}", style = MaterialTheme.typography.labelLarge)
        }
    }
}
