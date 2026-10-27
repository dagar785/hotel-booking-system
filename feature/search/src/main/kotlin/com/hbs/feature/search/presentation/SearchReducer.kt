package com.hbs.feature.search.presentation

internal object SearchReducer {
    fun reduce(state: SearchState, msg: SearchMsg): SearchState = when (msg) {
        is SearchMsg.QueryChanged -> state.copy(query = msg.query, isLoading = true, error = null)
        is SearchMsg.Loaded -> state.copy(isLoading = false, hotels = msg.hotels)
        is SearchMsg.Failed -> state.copy(isLoading = false, error = msg.message)
    }
}
