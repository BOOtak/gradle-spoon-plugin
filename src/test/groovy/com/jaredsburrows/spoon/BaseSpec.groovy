package com.jaredsburrows.spoon

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
abstract class BaseSpec extends Specification {
  @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
  static def COMPILE_SDK_VERSION = 27
  static def BUILD_TOOLS_VERSION = "27.0.1"
  static def APPLICATION_ID = "com.example"
  static def MANIFEST_FILE_PATH = "src/main/AndroidManifest.xml"
  static def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$APPLICATION_ID\"/>"
  static def APP_APK = "project-debug.apk"
  static def TEST_APK = "project-debug-androidTest.apk"

  // Project
  Project project
  File appApk
  File testApk

  def "setup"() {
    // Setup project
    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName("project")
      .build()

    // Make sure Android projects have a manifest
    testProjectDir.newFolder("src", "main")
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST
    testProjectDir.newFolder("build", "outputs", "apk", "debug")
    appApk = testProjectDir.newFile("build/outputs/apk/debug/" + APP_APK)
    testApk = testProjectDir.newFile("build/outputs/apk/debug/" + TEST_APK)
  }
}
