package com.hbs.feature.booking.domain

data class GuestForm(val name: String = "", val email: String = "", val cardNumber: String = "")

data class FormErrors(val name: String? = null, val email: String? = null, val card: String? = null) {
    val hasErrors get() = name != null || email != null || card != null
}
