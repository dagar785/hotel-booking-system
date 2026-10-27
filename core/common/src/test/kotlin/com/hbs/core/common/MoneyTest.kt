package com.hbs.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {
    @Test fun `formats minor units with two decimals`() = assertEquals("USD 12.05", Money.usd(1205).format())

    @Test fun `plus and times keep currency`() = assertEquals(Money.usd(900), Money.usd(300) * 2 + Money.usd(300))

    @Test fun `mixing currencies is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { Money.usd(1) + Money(1, "EUR") }
    }
}
