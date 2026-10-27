package com.hbs.app

internal object Routes {
    const val SEARCH = "search"
    const val DETAIL = "detail/{hotelId}"
    fun detail(hotelId: String) = "detail/$hotelId"
}
