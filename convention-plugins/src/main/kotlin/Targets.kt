import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinMultiplatformExtension.applyTargets() {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    jvmToolchain(11)
    jvm("desktop")

    js().browser()

    // Commenting out wasmJs target due to serialization dependency issues
    // and it's not needed for iOS/Android only projects
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs().browser()

    iosX64()
    iosArm64()
    iosSimulatorArm64()
}
