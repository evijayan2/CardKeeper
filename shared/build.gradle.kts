plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")

    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs += listOf("-Xexpect-actual-classes")
            }
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.4")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
            implementation("io.coil-kt.coil3:coil-compose:3.0.0")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
        }
        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:2.0.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

sqldelight {
    databases {
        create("SqlDelightDatabase") {
            packageName.set("com.vijay.cardkeeper")
        }
    }
}

android {
    namespace = "com.vijay.cardkeeper.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}




