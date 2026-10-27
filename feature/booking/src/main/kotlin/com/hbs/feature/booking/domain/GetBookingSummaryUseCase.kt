package com.hbs.feature.booking.domain

import com.hbs.core.common.Money
import com.hbs.core.common.runCatchingCancellable
import com.hbs.core.data.HotelRepository
import javax.inject.Inject

data class BookingSummary(val hotelName: String, val roomName: String, val nights: Int, val total: Money)

class GetBookingSummaryUseCase @Inject constructor(private val repository: HotelRepository) {
    suspend operator fun invoke(hotelId: String, roomId: String, nights: Int): Result<BookingSummary> = runCatchingCancellable {
        val hotel = repository.get(hotelId) ?: error("Hotel not found")
        val room = hotel.rooms.firstOrNull { it.id == roomId } ?: error("Room not found")
        BookingSummary(hotel.name, room.name, nights, room.pricePerNight * nights)
    }
}
