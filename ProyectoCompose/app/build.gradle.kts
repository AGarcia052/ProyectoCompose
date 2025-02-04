plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.proyectocompose"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectocompose"
        minSdk = 28
        targetSdk = 34
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
//    implementation (platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation (platform(libs.firebase.bom))

//    implementation("com.google.firebase:firebase-auth")
    implementation(libs.firebase.auth)

    //Google Sign-In
//    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation(libs.play.services.auth)


//    // Kotlin extensions and coroutines support for Jetpack Compose
//    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(libs.androidx.activity.compose.v193)

    //Jetpack Compose
//    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.lifecycle.runtime.compose)

//    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation(libs.androidx.navigation.compose.v285)

//    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.coil.compose)

//    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation(libs.play.services.maps)

//    implementation("com.google.maps.android:maps-compose:2.11.2")
    implementation(libs.maps.compose)

//    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.play.services.location)

//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation(libs.androidx.lifecycle.runtime.ktx)

}