
plugins {
    kotlin("jvm") version "1.9.25"
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("com.nimbusds:nimbus-jose-jwt:9.37")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.4")
    testImplementation("org.postgresql:postgresql:42.6.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

