package com.hbs.feature.hoteldetail.presentation

import com.hbs.core.common.Money
import com.hbs.core.data.Hotel

data class HotelDetailState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val selectedRoomId: String? = null,
    val nights: Int = 1,
    val error: String? = null,
) {
    val total: Money? get() = hotel?.rooms?.firstOrNull { it.id == selectedRoomId }?.pricePerNight?.times(nights)
}

sealed interface HotelDetailIntent {
    data class SelectRoom(val roomId: String) : HotelDetailIntent
    data class ChangeNights(val delta: Int) : HotelDetailIntent
    data object BookClicked : HotelDetailIntent
}

sealed interface HotelDetailEffect {
    data class NavigateToBooking(val hotelId: String, val roomId: String, val nights: Int) : HotelDetailEffect
}

internal sealed interface HotelDetailMsg {
    data class Loaded(val hotel: Hotel) : HotelDetailMsg
    data class Failed(val message: String) : HotelDetailMsg
    data class RoomSelected(val roomId: String) : HotelDetailMsg
    data class NightsChanged(val delta: Int) : HotelDetailMsg
}
