package com.hbs.feature.hoteldetail.domain

import com.hbs.core.common.runCatchingCancellable
import com.hbs.core.data.Hotel
import com.hbs.core.data.HotelRepository
import javax.inject.Inject

class GetHotelUseCase @Inject constructor(private val repository: HotelRepository) {
    suspend operator fun invoke(id: String): Result<Hotel> =
        runCatchingCancellable { repository.get(id) ?: error("Hotel not found") }
}
