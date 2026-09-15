plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Автоматическое создание манифеста силами сервера, минуя редактор GitHub
tasks.register("generateMainManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")
    doLast {
        manifestFile.parentFile.mkdirs()
        manifestFile.writeText("""
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://android.com">
                <application
                    android:allowBackup="true"
                    android:label="Fog of War Chess"
                    android:supportsRtl="true">
                    <activity
                        android:name="com.example.chess.MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent())
    }
}

// Привязываем генерацию к началу сборки
tasks.configureEach {
    if (name.startsWith("process") && name.contains("Manifest")) {
        dependsOn("generateMainManifest")
    }
}

android {
    namespace = "com.example.chess"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chess"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
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
    buildFeatures { compose = true }
    composeOptions { 
        kotlinCompilerExtensionVersion = "1.5.14" 
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}
