package com.hbs.feature.booking.domain

import app.cash.turbine.test
import com.hbs.core.common.Money
import com.hbs.core.testing.TestDispatcherProvider
import com.hbs.payment.FakePaymentGateway
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PayForBookingUseCaseTest {
    private val useCase = PayForBookingUseCase(FakePaymentGateway(latencyMs = 0), TestDispatcherProvider(UnconfinedTestDispatcher()))

    private suspend fun run(card: String, key: String = "key-12345678"): PaymentState {
        lateinit var last: PaymentState
        useCase(key, Money.usd(1000), card).test {
            assertEquals(PaymentState.Processing, awaitItem())
            last = awaitItem()
            awaitComplete()
        }
        return last
    }

    @Test fun `approved card succeeds`() = runTest { assertEquals(PaymentState.Success("tx_key-1234"), run("4242424242424242")) }

    @Test fun `declined card fails with reason`() = runTest {
        assertEquals(PaymentState.Failed("Card declined (insufficient_funds)"), run("4000000000000002"))
    }

    @Test fun `network error is reported`() = runTest {
        assertEquals(PaymentState.Failed("Network problem, please try again"), run("4000000000000341"))
    }
}
