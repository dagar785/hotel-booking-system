plugins {
    id("hbs.android.library")
    id("hbs.android.hilt")
}
android { namespace = "com.hbs.payment" }
dependencies { api(project(":core:common")) }
