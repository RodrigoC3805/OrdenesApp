plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Habilitamos el plugin de Compose compiler
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.ordenesapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ordenesapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        // Esto le dice a Android Studio que use Compose
        compose = true
    }
    // NOTA: Si usas Kotlin 2.0+, ya NO necesitas la línea "composeOptions { kotlinCompilerExtensionVersion = ... }"
}

dependencies {
    // Implementación Base de Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // las librerías Core de Jetpack Compose (Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Para el ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
}