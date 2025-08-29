plugins {
    id("kotlin.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.documentfile)
    }

    sourceSets.wasmJsMain.dependencies {
        implementation(libs.kotlinx.browser)
    }
}
