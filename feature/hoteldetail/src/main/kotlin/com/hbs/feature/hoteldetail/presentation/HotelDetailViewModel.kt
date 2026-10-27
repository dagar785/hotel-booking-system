package com.hbs.feature.hoteldetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbs.feature.hoteldetail.domain.GetHotelUseCase
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
class HotelDetailViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val getHotel: GetHotelUseCase,
) : ViewModel() {
    private val hotelId: String = checkNotNull(handle[ARG_HOTEL_ID]) { "hotelId argument missing" }

    private val _state = MutableStateFlow(HotelDetailState())
    val state: StateFlow<HotelDetailState> = _state.asStateFlow()

    private val _effects = Channel<HotelDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            getHotel(hotelId)
                .onSuccess { reduce(HotelDetailMsg.Loaded(it)) }
                .onFailure { reduce(HotelDetailMsg.Failed(it.message ?: "Something went wrong")) }
        }
    }

    fun onIntent(intent: HotelDetailIntent) {
        when (intent) {
            is HotelDetailIntent.SelectRoom -> reduce(HotelDetailMsg.RoomSelected(intent.roomId))
            is HotelDetailIntent.ChangeNights -> reduce(HotelDetailMsg.NightsChanged(intent.delta))
            HotelDetailIntent.BookClicked -> {
                val s = _state.value
                val roomId = s.selectedRoomId ?: return
                viewModelScope.launch { _effects.send(HotelDetailEffect.NavigateToBooking(hotelId, roomId, s.nights)) }
            }
        }
    }

    private fun reduce(msg: HotelDetailMsg) = _state.update { HotelDetailReducer.reduce(it, msg) }

    companion object {
        const val ARG_HOTEL_ID = "hotelId"
    }
}
