package buildsrc.convention

plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

detekt {
    config.setFrom(rootProject.file("detekt.yml"))
    buildUponDefaultConfig = true
    ignoreFailures = true
}

ktlint {
}

tasks.configureEach {
    if (name.startsWith("detekt", ignoreCase = true) || name.startsWith("ktlint", ignoreCase = true)) {
        enabled = false
    }
}

