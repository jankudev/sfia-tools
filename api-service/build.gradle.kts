plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")

  // supportive framework plugins
  kotlin("plugin.spring") version "2.1.10"
  kotlin("plugin.jpa") version "2.1.10"
  id("org.springframework.boot") version "3.4.2"
  id("io.spring.dependency-management") version "1.1.7"

  // Apply the Application plugin to add support for building an executable JVM application.
  application
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  // SQLite
  implementation("org.xerial:sqlite-jdbc:3.48.0.0")
  implementation("org.hibernate.orm:hibernate-community-dialects")

  // SpringDoc - OpenAPI generation
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
  implementation("org.springdoc:springdoc-openapi-starter-common:2.8.4")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.4")

  // Test dependencies
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Configure processResources task
tasks.processResources {
  from("src/main/resources") {
    include("application.yml")
    include("application-local.yml")
  }
}

tasks.jar {
  manifest {
    attributes["Main-Class"] = "dev.janku.sfia.apiservice.ApiServiceApplicationKt"
  }
}

// Configure the run task to read the environment property at execution time
tasks.named<JavaExec>("run") {
  doFirst {
    // Re-read the environment property at execution time
    val currentEnv = project.findProperty("env") ?: "prod"
    jvmArgs = listOf("-Dspring.profiles.active=$currentEnv")
  }
}

application {
  // Define the Fully Qualified Name for the application main class
  // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
  mainClass = "dev.janku.sfia.apiservice.ApiServiceApplicationKt"
}

// Create custom task for running with local profile
tasks.register("runLocal") {
  group = "application"
  description = "Runs the application with local profile"

  // Set the env property at configuration time
  project.extra["env"] = "local"

  // Declare inputs and outputs for caching
  inputs.file(project(":data-scraper").layout.buildDirectory.file("db/sfia-sqlite.db"))
  outputs.file(layout.buildDirectory.file("sfia-sqlite.db"))

  doFirst {
    // Define source and target files
    val targetFile = layout.buildDirectory.file("sfia-sqlite.db").get().asFile
    val sourceFile = project(":data-scraper").layout.buildDirectory.file("db/sfia-sqlite.db").get().asFile

    // Add dependency only if needed
    if (!targetFile.exists() && !sourceFile.exists()) {
      throw IllegalStateException("SQLite database not found. To create it run the data-scraper module first.")
    }

    // Check if target file doesn't exist
    if (!targetFile.exists()) {
      // Ensure parent directories exist
      targetFile.parentFile.mkdirs()

      // Copy the file
      sourceFile.copyTo(targetFile, overwrite = false)
      logger.lifecycle("Copied SQLite database from data-scraper module")
    } else {
      logger.lifecycle("SQLite database already exists in build directory")
    }
  }

  // Use finalizedBy instead of modifying the run task in doFirst
  finalizedBy("run")
}