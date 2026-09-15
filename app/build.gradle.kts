plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Запись файла в байтах, чтобы обойти баг текстового парсера GitHub
tasks.register("generateMainManifest") {
    val manifestFile = file("src/main/AndroidManifest.xml")
    doLast {
        manifestFile.parentFile.mkdirs()
        // Идеальный байтовый массив правильного AndroidManifest.xml
        val bytes = byteArrayOf(
            60,63,120,109,108,32,118,101,114,115,105,111,110,61,34,49,46,48,34,32,101,110,99,111,
            100,105,110,103,61,34,117,116,102,45,56,34,63,62,10,60,109,97,110,105,102,101,115,116,
            32,120,109,108,110,115,58,97,110,100,114,111,105,100,61,34,104,116,116,117,112,58,47,
            47,115,99,104,101,109,97,115,46,97,110,100,114,111,105,100,46,99,111,109,47,97,112,107,
            47,114,101,115,47,97,110,100,114,111,105,100,34,62,10,32,32,32,32,60,97,112,112,108,105,
            99,97,116,105,111,110,32,97,110,100,114,111,105,100,58,97,108,108,111,119,66,97,99,107,
            117,112,61,34,116,114,117,101,34,32,97,110,100,114,111,105,100,58,108,97,98,101,108,61,
            34,70,111,103,32,111,102,32,87,97,114,32,67,104,101,115,115,34,62,10,32,32,32,32,32,32,
            32,32,60,97,99,116,105,118,105,116,121,32,97,110,100,114,111,105,100,58,110,97,109,101,
            61,34,99,111,109,46,101,120,97,109,112,108,101,46,99,104,101,115,115,46,77,97,105,110,
            65,99,116,105,118,105,116,121,34,32,97,110,100,114,111,105,100,58,101,120,112,111,114,
            116,101,100,61,34,116,114,117,101,34,62,10,32,32,32,32,32,32,32,32,32,32,32,32,60,105,
            110,116,101,110,116,45,102,105,108,116,101,114,62,10,32,32,32,32,32,32,32,32,32,32,32,
            32,32,32,32,32,60,97,99,116,105,111,110,32,97,110,100,114,111,105,100,58,110,97,109,101,
            61,34,97,110,100,114,111,105,100,46,105,110,116,101,110,116,46,97,99,116,105,111,110,46,
            77,65,73,78,34,32,47,62,10,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,60,99,97,116,
            101,103,111,114,121,32,97,110,100,114,111,105,100,58,110,97,109,101,61,34,97,110,100,114,
            111,105,100,46,105,110,116,101,110,116,46,99,97,116,101,103,111,114,121,46,76,65,85,78,
            67,72,69,82,34,32,47,62,10,32,32,32,32,32,32,32,32,32,32,32,32,60,47,105,110,116,101,110,
            116,45,102,105,108,116,101,114,62,10,32,32,32,32,32,32,32,32,60,47,97,99,116,105,118,105,
            116,121,62,10,32,32,32,32,60,47,97,112,112,108,105,99,97,116,105,111,110,62,10,60,47,109,
            97,110,105,102,101,115,116,62
        )
        // Корректируем опечатку в протоколе, которую делает парсер
        var s = String(bytes, Charsets.UTF_8)
        s = s.replace("httiup://", "http://")
        manifestFile.writeText(s)
    }
}

// Принудительно запускаем инъекцию до того, как Android начнет проверять манифест
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
