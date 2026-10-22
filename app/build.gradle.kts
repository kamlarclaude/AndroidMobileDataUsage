import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cz.kamlar.androiddatausage"
    compileSdk = 35

    defaultConfig {
        applicationId = "cz.kamlar.androiddatausage"
        minSdk = 33
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"
    }

    // Release podpis: klíč v signing/release.jks, hesla v keystore.properties (obojí mimo verzování).
    // Bez tohoto souboru se release sestaví nepodepsaný. Klíč neztratit — bez něj nejde
    // vydat aktualizaci, která se nainstaluje přes stávající verzi.
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) {
        val ks = Properties().apply { keystoreFile.inputStream().use { load(it) } }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(ks.getProperty("storeFile"))
                storePassword = ks.getProperty("storePassword")
                keyAlias = ks.getProperty("keyAlias")
                keyPassword = ks.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
