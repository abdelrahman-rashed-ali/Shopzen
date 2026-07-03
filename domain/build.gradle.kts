plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    //rashed
    api(libs.kotlinx.coroutines.core)
    //rashed
    //ziad
    implementation(libs.javax.inject)
    //ziad
}
