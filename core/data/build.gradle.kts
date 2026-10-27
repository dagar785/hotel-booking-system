plugins {
    id("hbs.android.library")
    id("hbs.android.hilt")
}
android { namespace = "com.hbs.core.data" }
dependencies { api(project(":core:common")) }
