import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

fun Properties.stringProperty(name: String, default: String = ""): String =
    getProperty(name)?.takeIf { it.isNotBlank() } ?: default

fun Properties.intProperty(name: String, default: Int = 0): Int =
    getProperty(name)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: default

android {
    namespace = "shopzen.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iti.shopzen"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }
        buildConfigField("String", "SHOPIFY_HOSTNAME", "\"${properties.stringProperty("SHOPIFY_HOSTNAME")}\"")
        buildConfigField("String", "SHOPIFY_API_VERSION", "\"${properties.stringProperty("SHOPIFY_API_VERSION")}\"")
        buildConfigField("String", "SHOPIFY_API_KEY", "\"${properties.stringProperty("SHOPIFY_API_KEY")}\"")
        buildConfigField("String", "SHOPIFY_PASSWORD", "\"${properties.stringProperty("SHOPIFY_PASSWORD")}\"")
        buildConfigField("String", "SHOPIFY_STOREFRONT_TOKEN", "\"${properties.stringProperty("SHOPIFY_STOREFRONT_TOKEN")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${properties.stringProperty("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "PAYMOB_BASE_URL", "\"${properties.stringProperty("PAYMOB_BASE_URL", "https://accept.paymob.com")}\"")
        buildConfigField("String", "PAYMOB_PUBLIC_KEY", "\"${properties.stringProperty("PAYMOB_PUBLIC_KEY")}\"")
        buildConfigField("String", "PAYMOB_SECRET_KEY", "\"${properties.stringProperty("PAYMOB_SECRET_KEY")}\"")
        buildConfigField("String", "PAYMOB_CURRENCY", "\"${properties.stringProperty("PAYMOB_CURRENCY", "EGP")}\"")
        buildConfigField(
            "int",
            "PAYMOB_ONLINE_CARD_INTEGRATION_ID",
            "${properties.intProperty("PAYMOB_ONLINE_CARD_INTEGRATION_ID")}"
        )
        buildConfigField("String", "LLM_API_KEY", "\"${properties.stringProperty("LLM_API_KEY")}\"")
        buildConfigField("String", "MAPBOX_ACCESS_TOKEN", "\"${properties.stringProperty("MAPBOX_ACCESS_TOKEN")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        dataBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.googleid)
    implementation(libs.androidx.datastore.core)
    implementation(libs.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    //yousef
    //yousef
    //rashed
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(project(":presentation"))
    implementation(project(":data"))
    implementation(project(":domain"))
    //rashed
    //nour
    implementation(libs.room.runtime)
    //nour
    //ziad
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.room)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.mapbox.maps.android)
    //ziad
}
