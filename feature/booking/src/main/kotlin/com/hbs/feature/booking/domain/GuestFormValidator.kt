package com.hbs.feature.booking.domain

import javax.inject.Inject

class GuestFormValidator @Inject constructor() {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validate(form: GuestForm) = FormErrors(
        name = if (form.name.isBlank()) "Name is required" else null,
        email = if (!emailRegex.matches(form.email.trim())) "Enter a valid email" else null,
        card = if (!isValidCard(form.cardNumber)) "Enter a valid card number" else null,
    )

    internal fun isValidCard(raw: String): Boolean {
        val digits = raw.filter(Char::isDigit)
        if (digits.length !in 13..19) return false
        val sum = digits.reversed().mapIndexed { i, c ->
            val d = c - '0'
            if (i % 2 == 1) (d * 2).let { if (it > 9) it - 9 else it } else d
        }.sum()
        return sum % 10 == 0
    }
}
