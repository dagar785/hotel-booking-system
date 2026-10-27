package com.hbs.feature.hoteldetail.presentation

import com.hbs.core.common.Money
import com.hbs.core.testing.TestHotels
import org.junit.Assert.assertEquals
import org.junit.Test

class HotelDetailReducerTest {
    private val loaded = HotelDetailReducer.reduce(HotelDetailState(), HotelDetailMsg.Loaded(TestHotels.paris))

    @Test fun `loading selects first room`() = assertEquals("r1", loaded.selectedRoomId)

    @Test fun `total is room price times nights`() {
        val s = HotelDetailReducer.reduce(HotelDetailReducer.reduce(loaded, HotelDetailMsg.RoomSelected("r2")), HotelDetailMsg.NightsChanged(2))
        assertEquals(Money.usd(75_000), s.total)
    }

    @Test fun `nights are clamped`() {
        assertEquals(1, HotelDetailReducer.reduce(loaded, HotelDetailMsg.NightsChanged(-5)).nights)
        assertEquals(HotelDetailReducer.MAX_NIGHTS, HotelDetailReducer.reduce(loaded, HotelDetailMsg.NightsChanged(99)).nights)
    }
}
