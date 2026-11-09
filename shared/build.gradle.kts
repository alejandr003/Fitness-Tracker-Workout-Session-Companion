plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.jesuschanyneyzerlopez.fitnesstrackerworkoutsessioncompanion.shared"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit)
}
