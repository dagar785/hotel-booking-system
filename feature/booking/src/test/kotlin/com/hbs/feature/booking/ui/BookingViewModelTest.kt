package com.hbs.feature.booking.ui

import androidx.lifecycle.SavedStateHandle
import com.hbs.core.testing.FakeRepository
import com.hbs.core.testing.MainDispatcherRule
import com.hbs.core.testing.TestDispatcherProvider
import com.hbs.feature.booking.domain.GetBookingSummaryUseCase
import com.hbs.feature.booking.domain.GuestFormValidator
import com.hbs.feature.booking.domain.PayForBookingUseCase
import com.hbs.feature.booking.domain.PaymentState
import com.hbs.payment.ChargeRequest
import com.hbs.payment.FakePaymentGateway
import com.hbs.payment.GatewayResult
import com.hbs.payment.PaymentGateway
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BookingViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private class CountingGateway(private val delegate: PaymentGateway) : PaymentGateway by delegate {
        var charges = 0
        override suspend fun charge(request: ChargeRequest): GatewayResult { charges++; return delegate.charge(request) }
    }

    private val gateway = CountingGateway(FakePaymentGateway(latencyMs = 0))

    private fun vm() = BookingViewModel(
        SavedStateHandle(mapOf(BookingViewModel.EXTRA_HOTEL_ID to "h1", BookingViewModel.EXTRA_ROOM_ID to "r2", BookingViewModel.EXTRA_NIGHTS to 2)),
        GetBookingSummaryUseCase(FakeRepository()),
        PayForBookingUseCase(gateway, TestDispatcherProvider(mainRule.dispatcher)),
        GuestFormValidator(),
    )

    private fun BookingViewModel.fill(card: String = "4242424242424242") =
        onFormChanged { it.copy(name = "Ada", email = "ada@example.com", cardNumber = card) }

    @Test fun `summary is loaded from arguments`() = runTest { assertEquals(50_000L, vm().state.value.summary?.total?.minor) }

    @Test fun `invalid form blocks payment and shows errors`() = runTest {
        val vm = vm()
        vm.onPayClicked()
        assertTrue(vm.state.value.errors.hasErrors)
        assertEquals(0, gateway.charges)
    }

    @Test fun `successful payment marks booking paid`() = runTest {
        val vm = vm().apply { fill() }
        vm.onPayClicked()
        assertTrue(vm.state.value.isPaid)
    }

    @Test fun `paid booking cannot be submitted again`() = runTest {
        val vm = vm().apply { fill() }
        vm.onPayClicked()
        vm.onPayClicked()
        assertEquals(1, gateway.charges)
        assertFalse(vm.state.value.canSubmit)
    }

    @Test fun `declined card shows failure and allows retry`() = runTest {
        val vm = vm().apply { fill("4000000000000002") }
        vm.onPayClicked()
        assertTrue(vm.state.value.payment is PaymentState.Failed)
        assertTrue(vm.state.value.canSubmit)
    }
}
