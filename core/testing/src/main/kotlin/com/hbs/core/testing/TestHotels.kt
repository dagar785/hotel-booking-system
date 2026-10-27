package com.hbs.core.testing

import com.hbs.core.common.Money
import com.hbs.core.data.Hotel
import com.hbs.core.data.HotelRepository
import com.hbs.core.data.Room

object TestHotels {
    val standard = Room("r1", "Standard", "Queen bed", Money.usd(10_000))
    val suite = Room("r2", "Suite", "Two rooms", Money.usd(25_000))
    val paris = Hotel("h1", "Paris Palace", "Paris", 4.5, listOf(standard, suite))
    val tokyo = Hotel("h2", "Tokyo Tower Inn", "Tokyo", 4.0, listOf(standard))
}

class FakeRepository(
    private val hotels: List<Hotel> = listOf(TestHotels.paris, TestHotels.tokyo),
    private val failure: Throwable? = null,
) : HotelRepository {
    override suspend fun search(query: String): List<Hotel> {
        failure?.let { throw it }
        return hotels.filter { it.name.contains(query, true) }
    }

    override suspend fun get(id: String): Hotel? {
        failure?.let { throw it }
        return hotels.firstOrNull { it.id == id }
    }
}
