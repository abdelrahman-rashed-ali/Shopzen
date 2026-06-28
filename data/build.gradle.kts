plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    //reashed
    id("com.google.gms.google-services")
    //reashed
}

android {
    namespace = "iti.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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