plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}




android {
    namespace = "com.vijay.cardkeeper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vijay.cardkeeper"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.0.0-alpha.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            if (System.getenv("CI") != "true") {
                signingConfig = signingConfigs.getByName("debug")
            }
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Room Database
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // CameraX & ML Kit
    val cameraxVersion = "1.5.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.1")
    implementation("com.google.zxing:core:3.5.4")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("app.cash.turbine:turbine:1.2.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.14.7")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("com.google.truth:truth:1.4.5")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Image Loading (Coil)
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    // Bouncy Castle for Crypto
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.70")

    // Security & Encryption
    implementation("net.zetetic:sqlcipher-android:4.12.0")
    implementation("androidx.biometric:biometric:1.1.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
}

// Task to download UIDAI certificates for Aadhar QR signature verification
tasks.register("downloadUidaiCertificates") {
    val certs = mapOf(
        "uidai_auth_prod.cer" to "https://uidai.gov.in/images/authDoc/uidai_auth_prod.cer",
        "uidai_auth_sign_prod_2026.cer" to "https://uidai.gov.in/images/authDoc/uidai_auth_sign_Prod_2026.cer",
        "uidai_offline_2026.cer" to "https://uidai.gov.in/images/authDoc/uidai_offline_publickey_17022026.cer"
    )
    
    val outputDir = file("src/main/assets/certs")
    
    doLast {
        if (System.getenv("CI") == "true") {
            println("Skipping UIDAI certificate download in CI environment.")
            return@doLast
        }
        
        outputDir.mkdirs()
        
        certs.forEach { (fileName, urlStr) ->
            val outputFile = file("$outputDir/$fileName")
            try {
                if (!outputFile.exists()) {
                    println("Downloading $fileName...")
                    val url = uri(urlStr).toURL()
                    url.openStream().use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Downloaded: ${outputFile.absolutePath}")
                } else {
                    println("Exists: ${outputFile.absolutePath}")
                }
            } catch (e: Exception) {
                println("Warning: Could not download $fileName: ${e.message}")
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("downloadUidaiCertificates")
}

