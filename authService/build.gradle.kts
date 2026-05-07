plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.kotlin-quality")

    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // Base module template depends on shared utilities.
    implementation(project(":utils"))

    implementation(platform(libs.spring.cloud.dependencies))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.cloud.starter.openfeign)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.mockito.kotlin)
}
