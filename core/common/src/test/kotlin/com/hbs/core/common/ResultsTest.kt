package com.hbs.core.common

import kotlin.coroutines.cancellation.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultsTest {
    @Test fun `wraps failures`() {
        assertTrue(runCatchingCancellable { error("boom") }.isFailure)
        assertEquals(1, runCatchingCancellable { 1 }.getOrNull())
    }

    @Test fun `rethrows cancellation`() {
        assertThrows(CancellationException::class.java) { runCatchingCancellable { throw CancellationException("x") } }
    }
}
