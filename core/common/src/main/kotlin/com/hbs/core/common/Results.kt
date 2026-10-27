package com.hbs.core.common

import kotlin.coroutines.cancellation.CancellationException

/** runCatching that does not swallow coroutine cancellation. */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
