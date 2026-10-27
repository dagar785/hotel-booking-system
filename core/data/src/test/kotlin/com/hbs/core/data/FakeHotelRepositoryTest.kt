package com.hbs.core.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FakeHotelRepositoryTest {
    private val repo = FakeHotelRepository()

    @Test fun `blank query returns everything`() = runTest { assertEquals(8, repo.search("").size) }

    @Test fun `matches city case-insensitively`() = runTest { assertEquals(2, repo.search("paris").size) }

    @Test fun `unknown id is null`() = runTest { assertNull(repo.get("nope")) }

    @Test fun `starting price is the cheapest room`() = runTest { assertEquals(8_900L, repo.get("h2")!!.startingPrice.minor) }
}
