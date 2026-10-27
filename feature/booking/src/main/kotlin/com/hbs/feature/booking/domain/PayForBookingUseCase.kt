package com.hbs.feature.booking.domain

import com.hbs.core.common.DispatcherProvider
import com.hbs.core.common.Money
import com.hbs.payment.CardTokenizer
import com.hbs.payment.ChargeRequest
import com.hbs.payment.GatewayResult
import com.hbs.payment.PaymentGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

class PayForBookingUseCase @Inject constructor(
    private val gateway: PaymentGateway,
    private val dispatchers: DispatcherProvider,
) {
    operator fun invoke(idempotencyKey: String, amount: Money, cardNumber: String): Flow<PaymentState> = flow {
        emit(PaymentState.Processing)
        val request = ChargeRequest(idempotencyKey, amount, CardTokenizer.tokenize(cardNumber))
        val result = try {
            gateway.charge(request)
        } catch (e: IOException) {
            emit(PaymentState.Failed("Network problem, please try again"))
            return@flow
        }
        emit(
            when (result) {
                is GatewayResult.Success -> PaymentState.Success(result.transactionId)
                is GatewayResult.Declined -> PaymentState.Failed("Card declined (${result.reason})")
                // Not handled yet: 3-D Secure (T3) and unknown outcomes (T4).
                else -> PaymentState.Failed("Unsupported payment result")
            },
        )
    }.flowOn(dispatchers.io)
}
