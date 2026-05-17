plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Plugin nhận diện Firebase
}

android {
    namespace = "com.example.nightlife_finder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nightlife_finder"
        minSdk = 23
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // --- FIREBASE ---
    // Firebase BoM (Quản lý phiên bản tự động)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Các module Firebase cần thiết
    implementation("com.google.firebase:firebase-analytics") // Bổ sung Analytics
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")

    // --- LIBRARIES ---
    // Glide (Load ảnh)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // Bổ sung compiler cho Java

    // --- TEST ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}