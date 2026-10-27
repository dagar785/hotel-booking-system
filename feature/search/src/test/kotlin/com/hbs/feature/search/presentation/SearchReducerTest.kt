package com.hbs.feature.search.presentation

import com.hbs.core.testing.TestHotels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchReducerTest {
    @Test fun `query change starts loading and clears error`() {
        val s = SearchReducer.reduce(SearchState(error = "x"), SearchMsg.QueryChanged("par"))
        assertTrue(s.isLoading)
        assertEquals("par", s.query)
        assertEquals(null, s.error)
    }

    @Test fun `loaded stops loading`() {
        val s = SearchReducer.reduce(SearchState(isLoading = true), SearchMsg.Loaded(listOf(TestHotels.paris)))
        assertFalse(s.isLoading)
        assertEquals(listOf(TestHotels.paris), s.hotels)
    }

    @Test fun `failure surfaces message`() {
        assertEquals("boom", SearchReducer.reduce(SearchState(isLoading = true), SearchMsg.Failed("boom")).error)
    }
}
