plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.dtouch.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dtouch.app"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0-DTouch"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
