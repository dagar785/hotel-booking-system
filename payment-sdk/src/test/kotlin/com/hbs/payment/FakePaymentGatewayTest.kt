package com.hbs.payment

import com.hbs.core.common.Money
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class FakePaymentGatewayTest {
    private val gateway = FakePaymentGateway(latencyMs = 0)
    private fun req(token: String, key: String = "key-12345678") = ChargeRequest(key, Money.usd(1000), token)

    @Test fun `success is idempotent per key`() = runTest {
        val first = gateway.charge(req(CardTokenizer.OK))
        assertEquals(first, gateway.charge(req(CardTokenizer.OK)))
    }

    @Test fun `declined card`() = runTest {
        assertEquals(GatewayResult.Declined("insufficient_funds"), gateway.charge(req(CardTokenizer.DECLINED)))
    }

    @Test fun `flaky card fails once then succeeds with same key`() = runTest {
        assertThrows(IOException::class.java) { kotlinx.coroutines.runBlocking { gateway.charge(req(CardTokenizer.FLAKY)) } }
        assertEquals(GatewayResult.Success("tx_key-1234"), gateway.charge(req(CardTokenizer.FLAKY)))
    }

    @Test fun `timeout still settles server side`() = runTest {
        assertThrows(IOException::class.java) { kotlinx.coroutines.runBlocking { gateway.charge(req(CardTokenizer.TIMEOUT)) } }
        assertEquals(GatewayResult.Success("tx_key-1234"), gateway.status("key-12345678"))
    }

    @Test fun `unknown key has no status`() = runTest { assertEquals(GatewayResult.Unknown, gateway.status("nope")) }

    @Test fun `3ds challenge then confirm`() = runTest {
        assertEquals(GatewayResult.RequiresAuth("chal_key-1234"), gateway.charge(req(CardTokenizer.THREE_DS)))
        assertEquals(GatewayResult.Success("tx_key-1234"), gateway.confirmAuth("key-12345678"))
    }
}
