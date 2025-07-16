plugins {
    id("kotlin-multiplatform")
    id("android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("module.publication")
    kotlin("plugin.serialization")
}

// Apply Android library setup
androidLibrarySetup()

kotlin {
    // Custom targets for webview - only Android and iOS
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    jvmToolchain(11)
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // Apply hierarchy template for proper source set organization
    applyDefaultHierarchyTemplate()
    
    sourceSets.commonMain.dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
        implementation(libs.kotlinx.coroutines.android)
    }
}
