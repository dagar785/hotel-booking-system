package com.hbs.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbs.feature.search.domain.SearchHotelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchHotels: SearchHotelsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _effects = Channel<SearchEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        onIntent(SearchIntent.QueryChanged(""))
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                reduce(SearchMsg.QueryChanged(intent.query))
                search(intent.query)
            }
            SearchIntent.Retry -> search(_state.value.query)
            is SearchIntent.HotelClicked -> viewModelScope.launch { _effects.send(SearchEffect.NavigateToDetail(intent.hotelId)) }
        }
    }

    // Every keystroke launches an independent request: no debounce, no cancellation of stale ones.
    private fun search(query: String) {
        viewModelScope.launch {
            searchHotels(query)
                .onSuccess { reduce(SearchMsg.Loaded(it)) }
                .onFailure { reduce(SearchMsg.Failed(it.message ?: "Something went wrong")) }
        }
    }

    private fun reduce(msg: SearchMsg) = _state.update { SearchReducer.reduce(it, msg) }
}
