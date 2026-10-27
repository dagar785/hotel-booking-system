package com.hbs.feature.booking.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GuestFormValidatorTest {
    private val validator = GuestFormValidator()
    private val good = GuestForm("Ada", "ada@example.com", "4242 4242 4242 4242")

    @Test fun `valid form has no errors`() = assertFalse(validator.validate(good).hasErrors)

    @Test fun `reports each bad field`() {
        val e = validator.validate(GuestForm("", "nope", "1234"))
        assertEquals("Name is required", e.name)
        assertEquals("Enter a valid email", e.email)
        assertEquals("Enter a valid card number", e.card)
    }

    @Test fun `luhn check`() {
        assertTrue(validator.isValidCard("4242424242424242"))
        assertFalse(validator.isValidCard("4242424242424241"))
    }
}
