package com.hbs.feature.search.presentation

import app.cash.turbine.test
import com.hbs.core.testing.FakeRepository
import com.hbs.core.testing.MainDispatcherRule
import com.hbs.core.testing.TestHotels
import com.hbs.feature.search.domain.SearchHotelsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun vm(repo: FakeRepository = FakeRepository()) = SearchViewModel(SearchHotelsUseCase(repo))

    @Test fun `initial load shows all hotels`() = runTest {
        assertEquals(2, vm().state.value.hotels.size)
    }

    @Test fun `typing filters results`() = runTest {
        val vm = vm()
        vm.onIntent(SearchIntent.QueryChanged("tokyo"))
        assertEquals(listOf(TestHotels.tokyo), vm.state.value.hotels)
    }

    @Test fun `repository failure becomes error state`() = runTest {
        assertEquals("down", vm(FakeRepository(failure = RuntimeException("down"))).state.value.error)
    }

    @Test fun `click emits navigation effect`() = runTest {
        val vm = vm()
        vm.effects.test {
            vm.onIntent(SearchIntent.HotelClicked("h1"))
            assertEquals(SearchEffect.NavigateToDetail("h1"), awaitItem())
        }
    }
}
