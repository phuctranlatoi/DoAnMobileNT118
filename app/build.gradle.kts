plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.doannt118"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.doannt118"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Test configuration
        testApplicationId = "com.example.doannt118.test"
        testFunctionalTest = true
        testHandleProfiling = true
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    
    lint {
        abortOnError = false
        warningsAsErrors = false
        checkReleaseBuilds = false
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.recyclerview)  // Via catalog, let BOM manage version
    
    // MultiDex support for large apps
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-android:5.7.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("com.google.truth:truth:1.1.4")
    
    // Instrumented Testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("com.google.truth:truth:1.1.4")
    
    // Firebase Testing
    androidTestImplementation("com.google.firebase:firebase-firestore")
    androidTestImplementation("com.google.firebase:firebase-auth")
    
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-firestore")  // No explicit version
    implementation("com.google.firebase:firebase-analytics")  // Optional
    implementation("com.google.firebase:firebase-auth") // Firebase Auth
    implementation("com.google.firebase:firebase-storage") // Firebase Storage
    implementation("com.google.firebase:firebase-messaging") // Firebase Cloud Messaging
    
    // Core libraries
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    
    // Email support
    implementation("com.sun.mail:javax.mail:1.6.2")
    
    // Image loading and UI
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // Stringee SDK for voice and video calling - UPDATED
    implementation("com.stringee.sdk.android:stringee-android-sdk:2.1.5")
    implementation("io.github.webrtc-sdk:android:137.7151.03")
    implementation("com.android.volley:volley:1.2.1")
}