package com.hbs.feature.hoteldetail.presentation

internal object HotelDetailReducer {
    const val MAX_NIGHTS = 14

    fun reduce(state: HotelDetailState, msg: HotelDetailMsg): HotelDetailState = when (msg) {
        is HotelDetailMsg.Loaded -> state.copy(isLoading = false, hotel = msg.hotel, selectedRoomId = msg.hotel.rooms.first().id)
        is HotelDetailMsg.Failed -> state.copy(isLoading = false, error = msg.message)
        is HotelDetailMsg.RoomSelected -> state.copy(selectedRoomId = msg.roomId)
        is HotelDetailMsg.NightsChanged -> state.copy(nights = (state.nights + msg.delta).coerceIn(1, MAX_NIGHTS))
    }
}
