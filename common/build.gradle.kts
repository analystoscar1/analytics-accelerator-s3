/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-library-conventions")
    id("io.freefair.lombok") version "8.6"
    `maven-publish`
}

dependencies {
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)

    testRuntimeOnly(libs.junit.jupiter.launcher)
}

tasks.withType<JavaCompile>().configureEach {
}

publishing {
    publications {
        create<MavenPublication>("common") {
            // TODO: update this when we figure out versioning
            //  ticket: https://app.asana.com/0/1206885953994785/1207481230403504/f
            version = "1.0.0"

            from(components["java"])
        }
    }
}
