package com.jaredsburrows.spoon

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class SpoonPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "spoon"
        private const val APPLICATION_PLUGIN = "com.android.application"
        private const val LIBRARY_PLUGIN = "com.android.library"
    }

    override fun apply(project: Project) {
        configureAndroidProject(project)
    }

    /**
     * Configure project and all variants for Android.
     */
    private fun configureAndroidProject(project: Project) {
        // Use "variants" from "buildTypes" to get all types for "testVariants"
        // Configure tasks for all variants
        val variants = getTestVariants(project)

        // Create "spoon" extension
        val spoonExtension = project.extensions.create(EXTENSION_NAME, SpoonExtension::class.java)

        variants?.all { variant ->
            variant.outputs.all {
                // Create tasks based on variant
                val task = project.tasks.create("spoon${variant.name.capitalize()}", SpoonTask::class.java)
                task.description = "Run instrumentation tests for '${variant.name}' variant."
                task.group = "Verification"
                task.outputs.upToDateWhen { false }
                task.dependsOn(variant.testedVariant.assemble, variant.assemble)
                task.instrumentationApk = variant.outputs.first().outputFile
                task.doFirst {
                    val testedOutput = variant.testedVariant.outputs.first()
                    // This is a hack for library projects.
                    // We supply the same apk as an application and instrumentation to the soon runner.
                    task.applicationApk = if (testedOutput is ApkVariantOutput) testedOutput.outputFile else task.instrumentationApk

                    var outputBase = spoonExtension.baseOutputDir
                    if (SpoonExtension.DEFAULT_OUTPUT_DIRECTORY == outputBase) {
                        outputBase = File(project.buildDir, SpoonExtension.DEFAULT_OUTPUT_DIRECTORY).path
                    }
                    task.outputDir = File(outputBase, variant.testedVariant.name)
                }
                task.extension = spoonExtension
            }
        }
    }

    private fun getTestVariants(project: Project): DomainObjectSet<TestVariant>? {
        return when {
            project.plugins.hasPlugin(APPLICATION_PLUGIN) -> project.extensions.findByType(AppExtension::class.java)?.testVariants
            project.plugins.hasPlugin(LIBRARY_PLUGIN) -> project.extensions.findByType(LibraryExtension::class.java)?.testVariants
            else -> throw IllegalStateException("Spoon plugin can only be applied to android application or library projects.")
        }
    }
}
