//Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    alias(libs.plugins.android.application) apply false
//    id("com.google.gms.google-services") version "4.4.2" apply false
//}


buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.5.0")
        classpath ("com.google.gms:google-services:4.4.2")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }

}

plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
