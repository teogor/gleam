import com.vanniktech.maven.publish.SonatypeHost
import dev.teogor.winds.api.MavenPublish
import dev.teogor.winds.api.getValue
import dev.teogor.winds.api.model.Developer
import dev.teogor.winds.api.model.LicenseType
import dev.teogor.winds.api.model.createVersion
import dev.teogor.winds.api.provider.Scm
import dev.teogor.winds.gradle.utils.afterWindsPluginConfiguration
import dev.teogor.winds.gradle.utils.attachTo
import org.jetbrains.dokka.gradle.DokkaPlugin

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false

  alias(libs.plugins.teogor.winds) apply true

  alias(libs.plugins.jetbrains.kotlin.android) apply false
  alias(libs.plugins.jetbrains.dokka) apply true
  alias(libs.plugins.jetbrains.api.validator) apply true

  alias(libs.plugins.vanniktech.maven) apply true
  alias(libs.plugins.spotless) apply true
}

winds {
  buildFeatures {
    mavenPublish = true

    docsGenerator = true
  }

  mavenPublish {
    displayName = "Gleam"
    name = "gleam"

    canBePublished = false

    description = "\uD83D\uDD16 Gleam effortlessly integrates modern, customizable bottom sheets into your Kotlin Compose app for a polished user experience."

    groupId = "dev.teogor.gleam"
    artifactIdElements = 2
    url = "https://source.teogor.dev/gleam"

    version = createVersion(1, 0, 0) {
      alphaRelease(1)
    }

    inceptionYear = 2024

    sourceControlManagement(
      Scm.Git(
        owner = "teogor",
        repo = "gleam",
      ),
    )

    addLicense(LicenseType.APACHE_2_0)

    addDeveloper(TeogorDeveloper())
  }

  docsGenerator {
    name = "Gleam"
    identifier = "gleam"
    alertOnDependentModules = true
  }
}

afterWindsPluginConfiguration { winds ->
  // TODO winds
  //  required by dokka
  group = winds.mavenPublish.groupId ?: "undefined"
  version = winds.mavenPublish.version ?: "undefined"

  if (!plugins.hasPlugin("com.gradle.plugin-publish")) {
    val mavenPublish: MavenPublish by winds
    if (mavenPublish.canBePublished) {
      mavenPublishing {
        publishToMavenCentral(SonatypeHost.S01)
        signAllPublications()

        @Suppress("UnstableApiUsage")
        pom {
          coordinates(
            groupId = mavenPublish.groupId!!,
            artifactId = mavenPublish.artifactId!!,
            version = mavenPublish.version!!.toString(),
          )
          mavenPublish attachTo this
        }
      }
    }
  }
}

data class TeogorDeveloper(
  override val id: String = "teogor",
  override val name: String = "Teodor Grigor",
  override val email: String = "open-source@teogor.dev",
  override val url: String = "https://teogor.dev",
  override val roles: List<String> = listOf("Code Owner", "Developer", "Designer", "Maintainer"),
  override val timezone: String = "UTC+2",
  override val organization: String = "Teogor",
  override val organizationUrl: String = "https://github.com/teogor",
) : Developer

val ktlintVersion = "0.50.0"

val excludedProjects = listOf(
  project.name,
  "app",
)

subprojects {
  if (!excludedProjects.contains(this.name)) {
    apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
      kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(ktlintVersion)
          .editorConfigOverride(
            mapOf(
              "ij_kotlin_allow_trailing_comma" to "true",
              "disabled_rules" to
                "filename," +
                "annotation,annotation-spacing," +
                "argument-list-wrapping," +
                "double-colon-spacing," +
                "enum-entry-name-case," +
                "multiline-if-else," +
                "no-empty-first-line-in-method-block," +
                "package-name," +
                "trailing-comma," +
                "spacing-around-angle-brackets," +
                "spacing-between-declarations-with-annotations," +
                "spacing-between-declarations-with-comments," +
                "unary-op-spacing," +
                "no-trailing-spaces," +
                "no-wildcard-imports," +
                "max-line-length",
            ),
          )
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
        trimTrailingWhitespace()
        endWithNewline()
      }
      format("kts") {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        // Look for the first line that doesn't have a block comment (assumed to be the license)
        licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
      }
      format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
        licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
      }
    }
  }
}

apiValidation {
  /**
   * Subprojects that are excluded from API validation
   */
  ignoredProjects.addAll(excludedProjects)
}

subprojects {
  if (!excludedProjects.contains(project.name)) {
    apply<DokkaPlugin>()
  }
}
