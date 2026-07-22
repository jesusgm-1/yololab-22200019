
    defaultConfig {
        applicationId = "com.jbn.yololab_22200019"
        minSdk = 35
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildFeatures {
            compose = true
        }
        androidResources {
            noCompress += "tflite"
        }
    }

        dependencies {
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.litert)
            implementation(libs.litert.gpu)
            testImplementation(libs.junit)
            androidTestImplementation(platform(libs.androidx.compose.bom))
            androidTestImplementation(libs.androidx.compose.ui.test.junit4)
            androidTestImplementation(libs.androidx.espresso.core)
            androidTestImplementation(libs.androidx.junit)
            debugImplementation(libs.androidx.compose.ui.test.manifest)
            debugImplementation(libs.androidx.compose.ui.tooling)
        }
        }