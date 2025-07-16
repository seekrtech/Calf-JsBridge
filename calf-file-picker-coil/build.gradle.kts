plugins {
    id("kotlin.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfCore)
        api(projects.calfIo)
        implementation(libs.coil)
    }
}