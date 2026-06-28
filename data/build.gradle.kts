import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    //reashed
    id("com.google.gms.google-services")
}

android {
    namespace = "iti.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read Shopify credentials from local.properties → BuildConfig
        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) load(file.inputStream())
        }
        buildConfigField(
            "String",
            "SHOPIFY_ACCESS_TOKEN",
            "\"${localProperties.getProperty("SHOPIFY_ACCESS_TOKEN", "")}\""
        )
        buildConfigField(
            "String",
            "SHOPIFY_HOSTNAME",
            "\"${localProperties.getProperty("SHOPIFY_HOSTNAME", "")}\""
        )
        buildConfigField(
            "String",
            "SHOPIFY_API_VERSION",
            "\"${localProperties.getProperty("SHOPIFY_API_VERSION", "2026-01")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    //yousef
    implementation(project(":domain"))
    //yousef
    //rashed
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.javax.inject)
    //rashed
    //nour
    //nour
    //ziad
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.apollo)
    implementation(libs.javax.inject)
    //ziad
}