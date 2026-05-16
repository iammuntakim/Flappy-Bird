plugins {
    id("com.android.application") version "8.12.0"
}

android {
    compileSdk = 34
    namespace = "com.maplays.flappybird"

    defaultConfig {
        applicationId = "com.maplays.flappybird"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}