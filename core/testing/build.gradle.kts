plugins { id("hbs.android.library") }
android { namespace = "com.hbs.core.testing" }
dependencies {
    api(project(":core:data"))
    api(libs.junit)
    api(libs.coroutines.test)
}
