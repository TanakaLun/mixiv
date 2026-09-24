plugins {
    id("pixiv.multiplatform.compose")
}

kotlin {
    android {
        namespace = "com.mrl.pixiv.common.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib_strings"))
            implementation(project(":common:data"))
            implementation(project(":common:repository"))
            implementation(project(":common:core"))

            // Paging
            implementation(libs.bundles.androidx.paging)
            // Coil3
            implementation(project.dependencies.platform(libs.coil3.bom))
            implementation(libs.bundles.coil3)
            // Toast
            implementation(libs.sonner)
            // miuix (api: features compile against miuix types)
            api(libs.bundles.miuix)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
