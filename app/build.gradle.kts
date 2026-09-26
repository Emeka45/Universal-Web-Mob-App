plugins {
    id("com.android.application")
}

android {
    namespace = "com.coeric.universalwebmob"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.coeric.universalwebmob"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "2.0.0"
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


    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    implementation("org.mozilla.geckoview:geckoview:147.0.20260212191108")
}
