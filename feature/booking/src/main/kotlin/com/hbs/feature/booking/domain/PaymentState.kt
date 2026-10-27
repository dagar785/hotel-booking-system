package com.hbs.feature.booking.domain

sealed interface PaymentState {
    data object Idle : PaymentState
    data object Processing : PaymentState
    data class Success(val transactionId: String) : PaymentState
    data class Failed(val message: String) : PaymentState
}
