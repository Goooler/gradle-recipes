/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("android.recipes.variant_output")
}

android {
    namespace = "com.example.android.recipes.variant_output"
    compileSdk = $COMPILE_SDK
    defaultConfig {
        minSdk = $MINIMUM_SDK
        targetSdk = $COMPILE_SDK
    }

    // Specify one flavor dimension and two product flavors
    flavorDimensions += "dimension1"
    productFlavors {
        create("flavor1") {
            dimension = "dimension1"
        }
        create("flavor2") {
            dimension = "dimension1"
        }
    }
}
