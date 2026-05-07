plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.kotlin-quality")

    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

val springCloudVersion = "2025.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
    tasks.register("prepareKotlinBuildScriptModel")
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testRuntimeOnly("com.h2database:h2")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}

