package com.hbs.core.data

import com.hbs.core.common.Money

data class Room(val id: String, val name: String, val description: String, val pricePerNight: Money)

data class Hotel(
    val id: String,
    val name: String,
    val city: String,
    val rating: Double,
    val rooms: List<Room>,
) {
    val startingPrice: Money get() = rooms.minOf { it.pricePerNight }
}
