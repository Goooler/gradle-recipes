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

import com.google.android.gradle_recipe.converter.context.Context
import com.google.android.gradle_recipe.converter.printErrorAndTerminate
import com.google.android.gradle_recipe.converter.recipe.RECIPE_METADATA_FILE
import com.google.android.gradle_recipe.converter.recipe.RecipeData
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeLines

/**
 * This mode is for a recipe that has no placeholders.
 *  This is for releases ando/or tests
 */
class ReleaseConverter(
    private val context: Context,
    private val agpVersion: FullAgpVersion,
    gradleVersion: String?,
) : Converter(context) {

    private var pathToGradle: String = ""
    private var pathToAgpRepo: String = ""

    private var pluginRepo: List<String> = listOf()
    private var dependencyRepo: List<String> = listOf()

    private val releaseSkipFilenames = DEFAULT_SKIP_FILENAMES + RECIPE_METADATA_FILE

    init {
        if (gradleVersion != null) {
            // github release
            pathToGradle = "https\\://services.gradle.org/distributions/gradle-" +
                    "$gradleVersion-bin.zip"
            pluginRepo = listOf("        gradlePluginPortal()", "        google()", "        mavenCentral()")
            dependencyRepo = listOf("        google()", "        mavenCentral()")

        } else {
            // internal CI release
            pathToAgpRepo = context.repoLocation ?: printErrorAndTerminate("must specify path to repo")
            pathToGradle = context.gradlePath ?: printErrorAndTerminate("must specify path to Gradle")
        }
    }

    override val skippedFilenames: Set<String>
        get() = releaseSkipFilenames

    override fun isConversionCompliant(recipeData: RecipeData): Boolean {
        return recipeData.isCompliantWithAgp(agpVersion)
    }

    override fun convertBuildGradle(source: Path, target: Path) {
        val convertedText = Files.readAllLines(source)
            .replaceGradlePlaceholdersWithInlineValue(
                "\$AGP_VERSION",
                "\"$agpVersion\""
            ).replaceGradlePlaceholdersWithInlineValue(
                "\$KOTLIN_VERSION",
                "\"${context.getKotlinVersion(agpVersion)}\""
            ).replaceGradlePlaceholdersWithInlineValue(
                "\$COMPILE_SDK",
                compileSdkVersion
            ).replaceGradlePlaceholdersWithInlineValue(
                "\$MINIMUM_SDK",
                minimumSdkVersion
            )

        target.writeLines(convertedText, Charsets.UTF_8)
    }

    override fun convertSettingsGradle(source: Path, target: Path) {
        val convertedText = Files.readAllLines(source)
            .replacePlaceHolderWithLine(
                "\$AGP_REPOSITORY",
                "maven { url = uri(\"$pathToAgpRepo\") }"
            ).replacePlaceHolderWithList(
            "\$PLUGIN_REPOSITORIES",
            pluginRepo
        ).replacePlaceHolderWithList(
             "\$DEPENDENCY_REPOSITORIES",
            dependencyRepo
        )

        target.writeLines(convertedText, Charsets.UTF_8)
    }

    override fun convertVersionCatalog(source: Path, target: Path) {
        val convertedText = Files.readAllLines(source)
            .replaceVersionCatalogPlaceholders(
                "\$AGP_VERSION",
                "\"$agpVersion\""
            ).replaceGradlePlaceholdersWithInlineValue(
                "\$KOTLIN_VERSION",
                "\"${context.getKotlinVersion(agpVersion)}\""
            )

        target.writeLines(convertedText, Charsets.UTF_8)
    }

    override fun processGradleWrapperProperties(file: Path) {
        val resultLines =
            Files.readAllLines(file).replaceGradlePlaceholdersWithInlineValue("\$GRADLE_LOCATION", pathToGradle)

        file.writeLines(resultLines, Charsets.UTF_8)
    }
}