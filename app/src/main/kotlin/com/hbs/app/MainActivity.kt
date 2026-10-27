package com.hbs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hbs.feature.booking.ui.BookingActivity
import com.hbs.feature.hoteldetail.presentation.HotelDetailRoute
import com.hbs.feature.search.presentation.SearchRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val nav = rememberNavController()
                    NavHost(nav, startDestination = Routes.SEARCH) {
                        composable(Routes.SEARCH) { SearchRoute(onHotelClick = { nav.navigate(Routes.detail(it)) }) }
                        composable(Routes.DETAIL) {
                            HotelDetailRoute(onBook = { hotel, room, nights ->
                                startActivity(BookingActivity.intent(this@MainActivity, hotel, room, nights))
                            })
                        }
                    }
                }
            }
        }
    }
}
