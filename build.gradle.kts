// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
//    id("com.android.application") version "8.7.3" apply false

    alias(libs.plugins.kotlinx.serialization) apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false



}