plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    //yousef
    //yousef
    //rashed
    //rashed
    //nour
    //nour
    //ziad
    //ziad
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
