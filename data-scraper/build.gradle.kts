plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Apply the kotlinx bundle of dependencies from the version catalog (`gradle/libs.versions.toml`).
    implementation("org.jsoup:jsoup:1.18.3")
    implementation(libs.bundles.kotlinxEcosystem)
    implementation("org.jetbrains.exposed:exposed-jdbc:0.58.0")
    implementation("org.xerial:sqlite-jdbc:3.48.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    systemProperty("app.build.dir", layout.buildDirectory.get().asFile.absolutePath)
}

application {
    mainClass.set("dev.janku.sfia.SfiaWebScraperKt")
}