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
        versionCode = 3
        versionName = "2.1.0"
    }

    // GeckoView ships large native binaries. Split them by ABI so a phone
    // does not have to download native libraries for architectures it cannot use.
    splits {
        abi {
            isEnable = project.findProperty("splitApks") == "true"
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }

    buildTypes {
        debug {
            // Keep debug installable for direct testing while still using ABI splits.
            isMinifyEnabled = false
        }

        release {
            // R8 + resource shrinking removes unused app/library code and resources.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
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
