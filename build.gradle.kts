// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    id("com.diffplug.spotless") version "6.25.0"

    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

spotless {

    kotlin {
        target("**/*.kt")
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_function-naming" to "disabled",
            ),
        )
    }

    kotlinGradle {
        target("**/*.kts")
        ktlint()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false

    config.setFrom(files("$rootDir/detekt.yml"))

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
    }
}
