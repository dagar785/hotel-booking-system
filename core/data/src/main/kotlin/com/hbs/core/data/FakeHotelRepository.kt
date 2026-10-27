package com.hbs.core.data

import com.hbs.core.common.Money
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * In-memory "backend". Latency shrinks as the query grows, so a slow response for "pa" can arrive
 * AFTER the fast response for "paris" (see TASKS.md T2).
 */
class FakeHotelRepository @Inject constructor() : HotelRepository {
    override suspend fun search(query: String): List<Hotel> {
        delay((600L - 100L * query.length).coerceAtLeast(50L))
        val q = query.trim()
        return hotels.filter { q.isEmpty() || it.name.contains(q, true) || it.city.contains(q, true) }
    }

    override suspend fun get(id: String): Hotel? {
        delay(150)
        return hotels.firstOrNull { it.id == id }
    }

    private fun rooms(id: String, base: Long) = listOf(
        Room("$id-std", "Standard Room", "Queen bed, city view", Money.usd(base)),
        Room("$id-dlx", "Deluxe Room", "King bed, balcony, breakfast included", Money.usd(base + 6_000)),
        Room("$id-ste", "Executive Suite", "Lounge access, two bedrooms, spa credit", Money.usd(base + 15_000)),
    )

    private val hotels = listOf(
        Hotel("h1", "Grand Paris Palace", "Paris", 4.7, rooms("h1", 21_000)),
        Hotel("h2", "Paris Budget Inn", "Paris", 3.9, rooms("h2", 8_900)),
        Hotel("h3", "Tokyo Skyline Hotel", "Tokyo", 4.5, rooms("h3", 17_500)),
        Hotel("h4", "Shinjuku Capsule Stay", "Tokyo", 4.0, rooms("h4", 5_500)),
        Hotel("h5", "Manhattan Suites", "New York", 4.3, rooms("h5", 25_000)),
        Hotel("h6", "Brooklyn Loft House", "New York", 4.1, rooms("h6", 14_000)),
        Hotel("h7", "Goa Beach Resort", "Goa", 4.6, rooms("h7", 9_500)),
        Hotel("h8", "Jaipur Heritage Haveli", "Jaipur", 4.8, rooms("h8", 12_000)),
    )
}
