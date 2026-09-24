plugins {
    id("pixiv.multiplatform.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.mrl.pixiv.common.datasource.local"
    }
    sourceSets {
        commonMain.dependencies {
            // Serialization
            implementation(libs.bundles.kotlinx.serialization)
            // Room
            implementation(libs.androidx.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            // Koin
            implementation(libs.bundles.koin)
            // FileKit
            implementation(libs.filekit.core)
        }
    }

}

dependencies {
    kspAndroid(libs.androidx.room3.compiler)
}
