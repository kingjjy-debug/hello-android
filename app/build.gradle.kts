plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.kingjjy.hello"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kingjjy.hello"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // 🔧 Java 컴파일 타겟을 17로 통일
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 🔧 Kotlin 컴파일 타겟도 17로 통일
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release { isMinifyEnabled = false }
        debug { isMinifyEnabled = false }
    }
}

// 🔧 Kotlin JVM Toolchain을 17로 고정 (Gradle이 JDK 선택 일관되게)
kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
