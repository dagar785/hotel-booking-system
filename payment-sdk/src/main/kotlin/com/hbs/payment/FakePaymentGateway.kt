package com.hbs.payment

import kotlinx.coroutines.delay
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory gateway with real-world behaviour: idempotent by key, latency, flaky network,
 * 3DS challenge and timeouts-after-success. State lives in THIS instance, so how it is
 * scoped by DI matters (see TASKS.md T1).
 */
class FakePaymentGateway(private val latencyMs: Long = 300) : PaymentGateway {
    private val settled = ConcurrentHashMap<String, GatewayResult>()
    private val attempts = ConcurrentHashMap<String, Int>()

    override suspend fun charge(request: ChargeRequest): GatewayResult {
        settled[request.idempotencyKey]?.let { return it }
        delay(latencyMs)
        val key = request.idempotencyKey
        val attempt = attempts.merge(key, 1, Int::plus)!!
        val id = "tx_" + key.take(8)
        return when (request.cardToken) {
            CardTokenizer.DECLINED -> GatewayResult.Declined("insufficient_funds")
            CardTokenizer.THREE_DS -> GatewayResult.RequiresAuth("chal_" + key.take(8))
            CardTokenizer.FLAKY -> if (attempt == 1) throw IOException("connection reset") else GatewayResult.Success(id)
            CardTokenizer.TIMEOUT -> {
                settled[key] = GatewayResult.Success(id)
                throw IOException("timeout")
            }
            else -> GatewayResult.Success(id)
        }.also { if (it !is GatewayResult.RequiresAuth) settled[key] = it }
    }

    override suspend fun status(idempotencyKey: String): GatewayResult = settled[idempotencyKey] ?: GatewayResult.Unknown

    override suspend fun confirmAuth(idempotencyKey: String): GatewayResult {
        delay(latencyMs)
        return GatewayResult.Success("tx_" + idempotencyKey.take(8)).also { settled[idempotencyKey] = it }
    }
}
