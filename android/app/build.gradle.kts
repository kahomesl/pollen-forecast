import java.net.URI
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

private val releaseTaskRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

private fun configuredApiBaseUrl(): String? {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(localProperties::load)
    }
    return providers.gradleProperty("API_BASE_URL").orNull
        ?: localProperties.getProperty("API_BASE_URL")
}

private fun debugApiBaseUrl(): String = configuredApiBaseUrl() ?: "http://10.0.2.2:8080/"

private fun releaseApiBaseUrl(): String {
    val value = configuredApiBaseUrl()?.trim().orEmpty()
    if (value.isBlank()) throw GradleException("Release builds require API_BASE_URL with an explicit HTTPS endpoint.")
    val uri = try {
        URI(value)
    } catch (error: Exception) {
        throw GradleException("Release API_BASE_URL must be a valid HTTPS URL.", error)
    }
    val forbiddenHosts = setOf("localhost", "127.0.0.1", "::1", "10.0.2.2", "api.example.invalid")
    if (uri.scheme != "https" || uri.host.isNullOrBlank() || uri.host.lowercase() in forbiddenHosts) {
        throw GradleException("Release API_BASE_URL must use HTTPS and cannot point to localhost, 10.0.2.2, or example.invalid.")
    }
    return if (value.endsWith('/')) value else "$value/"
}

private fun requiredReleaseEnvironment(name: String): String = System.getenv(name)?.takeIf(String::isNotBlank)
    ?: throw GradleException("Release signing requires environment variable $name.")

private val releaseApiBaseUrlForTask = if (releaseTaskRequested) releaseApiBaseUrl() else null

android {
    namespace = "com.kahomesl.allergenradar"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.kahomesl.allergenradar"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "0.1.0-beta.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

    }

    signingConfigs {
        create("release") {
            if (releaseTaskRequested) {
                val keyStore = file(requiredReleaseEnvironment("ALLERGENRADAR_RELEASE_STORE_FILE"))
                if (!keyStore.isFile) throw GradleException("Release signing keystore does not exist: $keyStore")
                storeFile = keyStore
                storePassword = requiredReleaseEnvironment("ALLERGENRADAR_RELEASE_STORE_PASSWORD")
                keyAlias = requiredReleaseEnvironment("ALLERGENRADAR_RELEASE_KEY_ALIAS")
                keyPassword = requiredReleaseEnvironment("ALLERGENRADAR_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${debugApiBaseUrl()}\"")
        }
        release {
            val apiBaseUrl = releaseApiBaseUrlForTask ?: "https://release-configuration-required.invalid/"
            buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
            if (releaseTaskRequested) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime)

    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
