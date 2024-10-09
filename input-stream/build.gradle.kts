import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.spotbugs.snom.SpotBugsTask
import com.github.jk1.license.render.ReportRenderer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.TextReportRenderer
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("buildlogic.java-library-conventions")
    id("io.freefair.lombok") version "8.10.2"
    id("me.champeau.jmh") version "0.7.2"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.morethan.jmhreport") version "0.9.6"
    `java-test-fixtures`
    id("com.github.jk1.dependency-license-report") version "2.9"
    `maven-publish`
}

licenseReport {
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer("report.html", "Backend"), TextReportRenderer())
}


// Allow to separate dependencies for reference testing
sourceSets {
    create("referenceTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val referenceTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val referenceTestRuntimeOnly by configurations.getting
configurations["referenceTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())


val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly by configurations.getting
configurations["referenceTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    api(project(":object-client"))

    implementation(project(":common"))
    implementation(libs.parquet.format)
    implementation(libs.slf4j.api)

    jmhImplementation(libs.s3)
    jmhImplementation(libs.s3.transfer.manager)
    jmhImplementation(testFixtures(project(":input-stream")))

    testFixturesImplementation(libs.s3)
    testFixturesImplementation(project(":input-stream"))
    testFixturesImplementation(project(":object-client"))
    testFixturesImplementation(project(":common"))

    testImplementation(libs.s3)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.sdk.url.connection.client)
    testImplementation(libs.netty.nio.client)
    testRuntimeOnly(libs.junit.jupiter.launcher)

    referenceTestImplementation(libs.s3mock.testcontainers)
    referenceTestImplementation(libs.testcontainers.junit.jupiter)
    referenceTestImplementation(libs.jqwik)
    referenceTestImplementation(libs.jqwik.testcontainers)
    referenceTestImplementation(libs.testcontainers)
    referenceTestRuntimeOnly(libs.junit.jupiter.launcher)
}

tasks.withType<JavaCompile>().configureEach {
}

tasks.compileJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.named("compileReferenceTestJava", JavaCompile::class) {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }

    options.compilerArgs.add("-parameters")
}

val shadowJar = tasks.withType<ShadowJar> {
    relocate("org.apache.parquet.format", "com.amazon.shaded.apache.parquet.format")
    relocate("shaded.parquet.org.apache.thrift", "com.amazon.shaded.parquet.org.apache.thrift")
}

val refTest = task<Test>("referenceTest") {
    description = "Runs reference tests."
    group = "verification"

    testClassesDirs = sourceSets["referenceTest"].output.classesDirs
    classpath = sourceSets["referenceTest"].runtimeClasspath

    useJUnitPlatform()

    testLogging {
        events("passed")
        events("failed")
    }

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }

    environment("AWS_REGION", "eu-west-1")
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()

    testLogging {
        events("passed")
        events("failed")
    }

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.spotbugsJmh {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.named<SpotBugsTask>("spotbugsReferenceTest") {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.named<SpotBugsTask>("spotbugsIntegrationTest") {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.named<SpotBugsTask>("spotbugsTestFixtures") {
    reports.create("html") {
        required = true
        setStylesheet("fancy-hist.xsl")
    }
}

tasks.build {dependsOn(shadowJar)}

val jmhOutputPath = "reports/jmh"
val jmhJsonOutputResultsPath = "reports/jmh/results.json"

// JMH micro-benchmarks
jmh {
    jmhVersion = "1.37"
    failOnError = true
    forceGC = true
    includeTests = false
    resultFormat = "JSON"
    resultsFile = project.layout.buildDirectory.file(jmhJsonOutputResultsPath)
    zip64 = true
}

jmhReport {
    jmhResultPath = project.layout.buildDirectory.file(jmhJsonOutputResultsPath).get().toString()
    jmhReportOutput = project.layout.buildDirectory.file(jmhOutputPath).get().toString()
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}


publishing {
    publications {
        create<MavenPublication>("inputStream") {
            groupId = "com.amazon.connector.s3"
            version = "0.0.1"

            from(components["java"])
        }
    }
}
