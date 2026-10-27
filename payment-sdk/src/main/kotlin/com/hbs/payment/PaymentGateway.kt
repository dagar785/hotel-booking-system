package com.hbs.payment

import com.hbs.core.common.Money

data class ChargeRequest(val idempotencyKey: String, val amount: Money, val cardToken: String)

sealed interface GatewayResult {
    data class Success(val transactionId: String) : GatewayResult
    data class Declined(val reason: String) : GatewayResult
    /** 3-D Secure: the user must confirm; call [PaymentGateway.confirmAuth] afterwards. */
    data class RequiresAuth(val challengeId: String) : GatewayResult
    /** The gateway has no record of this idempotency key. */
    data object Unknown : GatewayResult
}

/** Stand-in for a third-party payments SDK. All calls are suspending and may throw IOException. */
interface PaymentGateway {
    suspend fun charge(request: ChargeRequest): GatewayResult
    suspend fun status(idempotencyKey: String): GatewayResult
    suspend fun confirmAuth(idempotencyKey: String): GatewayResult
}
