package com.hbs.feature.hoteldetail.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.hbs.core.testing.FakeRepository
import com.hbs.core.testing.MainDispatcherRule
import com.hbs.feature.hoteldetail.domain.GetHotelUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HotelDetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun vm(id: String = "h1") =
        HotelDetailViewModel(SavedStateHandle(mapOf(HotelDetailViewModel.ARG_HOTEL_ID to id)), GetHotelUseCase(FakeRepository()))

    @Test fun `loads hotel for id`() = runTest { assertEquals("Paris Palace", vm().state.value.hotel?.name) }

    @Test fun `unknown hotel shows error`() = runTest { assertEquals("Hotel not found", vm("zzz").state.value.error) }

    @Test fun `book emits selection`() = runTest {
        val vm = vm()
        vm.onIntent(HotelDetailIntent.SelectRoom("r2"))
        vm.onIntent(HotelDetailIntent.ChangeNights(1))
        vm.effects.test {
            vm.onIntent(HotelDetailIntent.BookClicked)
            assertEquals(HotelDetailEffect.NavigateToBooking("h1", "r2", 2), awaitItem())
        }
    }
}
