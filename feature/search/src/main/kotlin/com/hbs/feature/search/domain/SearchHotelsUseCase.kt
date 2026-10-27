package com.hbs.feature.search.domain

import com.hbs.core.common.runCatchingCancellable
import com.hbs.core.data.Hotel
import com.hbs.core.data.HotelRepository
import javax.inject.Inject

class SearchHotelsUseCase @Inject constructor(private val repository: HotelRepository) {
    suspend operator fun invoke(query: String): Result<List<Hotel>> =
        runCatchingCancellable { repository.search(query.trim()) }
}
