/*
 * Copyright 2022 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gradle_recipe.converter.converters

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class ConverterUtilsKtTest {

    // build.gradle
    private val buildGradlePluginsBlockSource = listOf(
        "id 'com.android.application' version \$AGP_VERSION apply false",
        "id 'com.android.library' version \$AGP_VERSION apply false",
        "id 'org.jetbrains.kotlin.android' version \$KOTLIN_VERSION apply false"
    )

    private val buildGradlePluginsBlockWorkingCopy = listOf(
        "//  >>> WORKING_COPY >>>",
        "//  id 'com.android.application' version \$AGP_VERSION apply false",
        "id 'com.android.application' version \"7.4.4\" apply false",
        "//  <<< WORKING_COPY <<<",
        "//  >>> WORKING_COPY >>>",
        "//  id 'com.android.library' version \$AGP_VERSION apply false",
        "id 'com.android.library' version \"7.4.4\" apply false",
        "//  <<< WORKING_COPY <<<",
        "//  >>> WORKING_COPY >>>",
        "//  id 'org.jetbrains.kotlin.android' version \$KOTLIN_VERSION apply false",
        "id 'org.jetbrains.kotlin.android' version \"1.5.20\" apply false",
        "//  <<< WORKING_COPY <<<"
    )

    private val buildGradlePluginsBlockRelease = listOf(
        "id 'com.android.application' version \"7.4.4\" apply false",
        "id 'com.android.library' version \"7.4.4\" apply false",
        "id 'org.jetbrains.kotlin.android' version \"1.5.20\" apply false"
    )

    @Test
    fun testBuildGradlePluginsBlockWorkingCopy() {
        val agpVersionWrapped =
            buildGradlePluginsBlockSource.wrapGradlePlaceholdersWithInlineValue(
                "\$AGP_VERSION",
                "\"7.4.4\""
            )
        val kotlinAndAgpVersionWrapped =
            agpVersionWrapped.wrapGradlePlaceholdersWithInlineValue(
                "\$KOTLIN_VERSION",
                "\"1.5.20\""
            )
        assertThat(kotlinAndAgpVersionWrapped).isEqualTo(
            buildGradlePluginsBlockWorkingCopy
        )
    }

    @Test
    fun testBuildGradlePluginsBlockSource() {
        val result = buildGradlePluginsBlockWorkingCopy.unwrapGradlePlaceholders()
        assertThat(result).isEqualTo(buildGradlePluginsBlockSource)
    }

    @Test
    fun testBuildGradlePlauginsBlockRelease() {
        val agpVersionReplaced =
            buildGradlePluginsBlockSource.replaceGradlePlaceholdersWithInlineValue(
                "\$AGP_VERSION",
                "\"7.4.4\""
            )
        val kotlinAndAgpVersionReplaced =
            agpVersionReplaced.replaceGradlePlaceholdersWithInlineValue(
                "\$KOTLIN_VERSION",
                "\"1.5.20\""
            )

        assertThat(kotlinAndAgpVersionReplaced).isEqualTo(
            buildGradlePluginsBlockRelease
        )
    }

    private val buildGradleAndroidBlockSource = """
android {
    namespace = "com.example.android.recipes.per_variant_manifest_placeholder"
    compileSdk = ${'$'}COMPILE_SDK
    defaultConfig {
       minSdk = ${'$'}MINIMUM_SDK
       targetSdk = ${'$'}COMPILE_SDK
    }
}
"""
    private val buildGradleAndroidBlockRelease = """
android {
    namespace = "com.example.android.recipes.per_variant_manifest_placeholder"
    compileSdk = 33
    defaultConfig {
       minSdk = 21
       targetSdk = 33
    }
}
"""

    private val buildGradleAndroidBlockWorkingCopy = """
android {
    namespace = "com.example.android.recipes.per_variant_manifest_placeholder"
//  >>> WORKING_COPY >>>
//      compileSdk = ${'$'}COMPILE_SDK
    compileSdk = 33
//  <<< WORKING_COPY <<<
    defaultConfig {
//  >>> WORKING_COPY >>>
//         minSdk = ${'$'}MINIMUM_SDK
       minSdk = 21
//  <<< WORKING_COPY <<<
//  >>> WORKING_COPY >>>
//         targetSdk = ${'$'}COMPILE_SDK
       targetSdk = 33
//  <<< WORKING_COPY <<<
    }
}
"""

    @Test
    fun testBuildGradleAndroidBlockRelease() {
        val compileSdkVersionReplaced =
            buildGradleAndroidBlockSource.lines()
                .replaceGradlePlaceholdersWithInlineValue("\$COMPILE_SDK", "33")

        val compileSdkMinimumSdkVersionReplaced =
            compileSdkVersionReplaced.replaceGradlePlaceholdersWithInlineValue(
                "\$MINIMUM_SDK",
                "21"
            )
        assertThat(compileSdkMinimumSdkVersionReplaced).isEqualTo(
            buildGradleAndroidBlockRelease.lines()
        )
    }

    @Test
    fun testBuildGradleAndroidBlockWorkingCopy() {
        val compileSdkVersionWrapped =
            buildGradleAndroidBlockSource.lines()
                .wrapGradlePlaceholdersWithInlineValue("\$COMPILE_SDK", "33")

        val compileSdkMinimumSdkVersionWrapped =
            compileSdkVersionWrapped.wrapGradlePlaceholdersWithInlineValue("\$MINIMUM_SDK", "21")
        assertThat(compileSdkMinimumSdkVersionWrapped).isEqualTo(
            buildGradleAndroidBlockWorkingCopy.lines()
        )
    }

    @Test
    fun testBuildGradleAndroidBlockSource() {
        assertThat(buildGradleAndroidBlockWorkingCopy.lines().unwrapGradlePlaceholders()).isEqualTo(
            buildGradleAndroidBlockSource.lines()
        )
    }

    // Tests for settings.gradle
    private val settingsGradleSource = """
pluginManagement {
    repositories {
${'$'}AGP_REPOSITORY
${'$'}PLUGIN_REPOSITORIES
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
${'$'}AGP_REPOSITORY
${'$'}DEPENDENCY_REPOSITORIES
    }
}"""

    private val settingsGradleWorkingCopy = """
pluginManagement {
    repositories {
//  >>> WORKING_COPY >>>
//  ${'$'}AGP_REPOSITORY
//  <<< WORKING_COPY <<<
//  >>> WORKING_COPY >>>
//  ${'$'}PLUGIN_REPOSITORIES
        gradlePluginPortal()
        google()
        mavenCentral()
//  <<< WORKING_COPY <<<
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//  >>> WORKING_COPY >>>
//  ${'$'}AGP_REPOSITORY
//  <<< WORKING_COPY <<<
//  >>> WORKING_COPY >>>
//  ${'$'}DEPENDENCY_REPOSITORIES
        google()
        mavenCentral()
//  <<< WORKING_COPY <<<
    }
}"""

    private val settingsGradleGithubRelease = """
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}"""

    private val settingsGradleCIRelease = """
pluginManagement {
    repositories {
..\..\private-repo\
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
..\..\private-repo\
    }
}"""

    @Test
    fun testSettingsGradleWorkingCopy() {
        val agpRepoConverted =
            settingsGradleSource.lines()
                .wrapGradlePlaceholdersWithInlineValue("\$AGP_REPOSITORY", "")

        val agpAndPluginRepoConverted =
            agpRepoConverted.wrapGradlePlaceholdersWithList(
                "\$PLUGIN_REPOSITORIES",
                listOf(
                    "        gradlePluginPortal()",
                    "        google()",
                    "        mavenCentral()"
                )
            )

        val agpAndPluginRepoAndDepsRepoConverted =
            agpAndPluginRepoConverted.wrapGradlePlaceholdersWithList(
                "\$DEPENDENCY_REPOSITORIES",
                listOf("        google()", "        mavenCentral()")
            )
        assertThat(agpAndPluginRepoAndDepsRepoConverted).isEqualTo(
            settingsGradleWorkingCopy.lines()
        )
    }

    @Test
    fun testSettingsGradleSource() {
        val result = settingsGradleWorkingCopy.lines().unwrapGradlePlaceholders()
        assertThat(result).isEqualTo(settingsGradleSource.lines())
    }

    @Test
    fun testSettingsGradleGithubRelease() {
        val agpRepoConverted =
            settingsGradleSource.lines().replacePlaceHolderWithLine("\$AGP_REPOSITORY", "")

        val agpAndPluginRepoConverted =
            agpRepoConverted.replacePlaceHolderWithList(
                "\$PLUGIN_REPOSITORIES",
                listOf(
                    "        gradlePluginPortal()",
                    "        google()",
                    "        mavenCentral()"
                )
            )

        val agpAndPluginAndDependencyRepoConverted =
            agpAndPluginRepoConverted.replacePlaceHolderWithList(
                "\$DEPENDENCY_REPOSITORIES",
                listOf("        google()", "        mavenCentral()")
            )

        assertThat(agpAndPluginAndDependencyRepoConverted).isEqualTo(
            settingsGradleGithubRelease.lines()
        )
    }

    @Test
    fun testSettingsGradleCIRelease() {
        val agpRepoConverted =
            settingsGradleSource.lines().replacePlaceHolderWithLine(
                "\$AGP_REPOSITORY",
                "..\\..\\private-repo\\"
            )

        val agpAndPluginRepoConverted =
            agpRepoConverted.replacePlaceHolderWithList("\$PLUGIN_REPOSITORIES", listOf())

        val agpAndPluginAndDependencyRepoConverted =
            agpAndPluginRepoConverted.replacePlaceHolderWithList(
                "\$DEPENDENCY_REPOSITORIES",
                listOf()
            )

        assertThat(agpAndPluginAndDependencyRepoConverted).isEqualTo(
            settingsGradleCIRelease.lines()
        )
    }

    // gradle wrapper tests
    private val gradleWrapperSource = listOf(
        "distributionUrl=\$GRADLE_LOCATION"
    )

    private val gradleWrapperWorkingCopy = listOf(
        "#  >>> WORKING_COPY >>>",
        "#  distributionUrl=\$GRADLE_LOCATION",
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-7.5-bin.zip",
        "#  <<< WORKING_COPY <<<",
    )

    private val gradleWrapperReleaseGithub = listOf(
        "distributionUrl=https\\://services.gradle.org/distributions/gradle-7.5-bin.zip"
    )

    @Test
    fun testGradleWrapperWorkingCopy() {
        val result =
            gradleWrapperSource.wrapGradleWrapperPlaceholders(
                "\$GRADLE_LOCATION",
                "https\\://services.gradle.org/distributions/gradle-7.5-bin.zip"
            )
        assertThat(result).isEqualTo(gradleWrapperWorkingCopy)
    }

    @Test
    fun testGradleWrapperReleaseGithub() {
        val result =
            gradleWrapperSource.replaceGradlePlaceholdersWithInlineValue(
                "\$GRADLE_LOCATION",
                "https\\://services.gradle.org/distributions/gradle-7.5-bin.zip"
            )
        assertThat(gradleWrapperReleaseGithub).isEqualTo(result)
    }

    // version catalog tests
    private val versionCatalogSource = listOf(
        "androidGradlePlugin = \$AGP_VERSION"
    )

    private val versionCatalogWorkingCopy = listOf(
        "#  >>> WORKING_COPY >>>",
        "#  androidGradlePlugin = \$AGP_VERSION",
        "androidGradlePlugin = \"8.0.0-alpha10\"",
        "#  <<< WORKING_COPY <<<",
    )

    private val versionCatalogRelease = listOf(
        "androidGradlePlugin = \"8.0.0-alpha10\""
    )

    @Test
    fun testVersionCatalogSource() {
        val result = versionCatalogWorkingCopy.unwrapVersionCatalogPlaceholders()
        assertThat(versionCatalogSource).isEqualTo(result)
    }

    @Test
    fun testVersionCatalogWorkingCopy() {
        val result =
            versionCatalogSource.wrapVersionCatalogPlaceholders(
                "\$AGP_VERSION",
                "\"8.0.0-alpha10\""
            )
        assertThat(versionCatalogWorkingCopy).isEqualTo(result)
    }

    @Test
    fun testVersionCatalogRelease() {
        val result =
            versionCatalogSource.replaceVersionCatalogPlaceholders(
                "\$AGP_VERSION",
                "\"8.0.0-alpha10\""
            )
        assertThat(versionCatalogRelease).isEqualTo(result)
    }
}