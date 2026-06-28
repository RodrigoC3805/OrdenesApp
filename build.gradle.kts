plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Si estás usando una versión de Kotlin 2.0 o superior,
    // Jetpack Compose ahora usa este plugin oficial:
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}