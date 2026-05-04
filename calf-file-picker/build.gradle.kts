plugins {
    id("compose.multiplatform")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.calfIo)

        implementation(compose.runtime)
        implementation(compose.foundation)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.activity.compose)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.jna)
    }
}
