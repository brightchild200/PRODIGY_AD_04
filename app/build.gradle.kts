plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.tictactoe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tictactoe"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ----- AndroidX / Material -----
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")

    // ----- Firebase (BOM makes versions consistent) -----
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    // If you still need Realtime DB or Storage:
    // implementation("com.google.firebase:firebase-database-ktx")
    // implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics")

    // ----- Testing -----
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    implementation(libs.firebase.firestore)
//    implementation(libs.firebase.database.ktx)
//    implementation(libs.firebase.auth.ktx)
////    implementation(libs.firebase.storage)
////    implementation(libs.firebase.firestore.ktx)
//    testImplementation(libs.junit)
//
//    implementation ("com.google.firebase:firebase-auth:22.3.0")
//    implementation("com.google.firebase:firebase-analytics")
//    implementation ("com.google.firebase:firebase-firestore:24.9.0")
//    implementation("com.google.firebase:firebase-firestore")
//    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
//    implementation ("com.google.firebase:firebase-firestore-ktx:24.9.0")
//    implementation ("com.google.firebase:firebase-storage-ktx")
//    implementation ("androidx.core:core-ktx:1.12.0")
//    implementation ("androidx.cardview:cardview:1.0.0")
//    implementation ("androidx.appcompat:appcompat:1.6.1")
//
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//
//
//}