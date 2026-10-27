package com.hbs.feature.search.presentation

import com.hbs.core.data.Hotel

data class SearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val hotels: List<Hotel> = emptyList(),
    val error: String? = null,
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Retry : SearchIntent
    data class HotelClicked(val hotelId: String) : SearchIntent
}

sealed interface SearchEffect {
    data class NavigateToDetail(val hotelId: String) : SearchEffect
}

/** Internal results that the reducer folds into state. */
internal sealed interface SearchMsg {
    data class QueryChanged(val query: String) : SearchMsg
    data class Loaded(val hotels: List<Hotel>) : SearchMsg
    data class Failed(val message: String) : SearchMsg
}
