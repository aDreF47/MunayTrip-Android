plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.dsm.munaytripandroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dsm.munaytripandroid"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    //Te permite declarar un NavHost con todas tus rutas y moverte entre pantallas usando un NavController.
    implementation(libs.androidx.navigation.compose)

    implementation("androidx.compose.material:material-icons-extended")

    // Importa el BoM para Firebase para asegurar compatibilidad de versiones
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // TODO: Add the dependencies for any other Firebase products you want to use
    // See https://firebase.google.com/docs/android/setup#available-libraries
    // For example, add the dependencies for Firebase Authentication and Cloud Firestore

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Cloud Functions (para invocar tus funciones desde la app)
    //implementation("com.google.firebase:firebase-functions")

    // Firebase Cloud Messaging (FCM)
    //implementation("com.google.firebase:firebase-messaging")

    // Cloud Storage para Firebase
    //implementation("com.google.firebase:firebase-storage")

    // Firebase Analytics (para tracking de comportamiento y recomendaciones)
    implementation("com.google.firebase:firebase-analytics")

    // Google Sign-In SDK (muy útil para "Autenticación unificada" si incluyes Google)
    // versión estable (23/07/25): https://developers.google.com/android/guides/releases?hl=es-419#july_23_2025
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // Firebase UI (opcional, pero puede acelerar el desarrollo de UI de Auth/Firestore)
    // implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    // implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}