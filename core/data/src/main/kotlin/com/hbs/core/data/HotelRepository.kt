package com.hbs.core.data

interface HotelRepository {
    suspend fun search(query: String): List<Hotel>
    suspend fun get(id: String): Hotel?
}
