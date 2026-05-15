plugins {
    alias(libs.plugins.android.application)
    // Thêm dòng này ngay bên dưới các plugin có sẵn
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nightlife_finder"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.nightlife_finder"
        minSdk = 36
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Import Firebase Bill of Materials (BoM) để tự động quản lý phiên bản tương thích
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Các module Firebase theo đúng Scope đồ án
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Thư viện Glide hỗ trợ load ảnh từ URL về app mượt mà
    implementation("com.github.bumptech.glide:glide:4.16.0")
}