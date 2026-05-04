plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.calfCore)

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
        implementation(libs.kotlinx.coroutines.android)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.kotlinx.coroutines.swing)
    }
}
