plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = rootProject.group
version = rootProject.version

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("com.nimbusds:nimbus-jose-jwt:9.37")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    testImplementation("org.postgresql:postgresql:42.6.0")
}

tasks.withType<Test> {
    testLogging.showStandardStreams = true
}
