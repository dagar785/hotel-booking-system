plugins {
    id("hbs.android.library")
    id("hbs.android.hilt")
}
android { namespace = "com.hbs.core.common" }
dependencies { api(libs.coroutines.android) }
