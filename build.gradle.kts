plugins {
  kotlin("jvm") version libs.versions.kotlin
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.ktfmt)
  alias(libs.plugins.versions)
}

group = "no.nav.sosialhjelp"

version = "1.0.0"

application {
  mainClass.set("no.nav.sosialhjelp.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

tasks {
  val installPreCommitHook =
      register("installPreCommitHook", Copy::class) {
        from(File(rootProject.rootDir, "scripts/pre-commit"))
        into(File(rootProject.rootDir, ".git/hooks"))
        filePermissions {
          user {
            read = true
            write = true
            execute = true
          }
        }
        dirPermissions {
          user {
            read = true
            write = true
          }
        }
      }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { dependsOn(installPreCommitHook) }
}

dependencies {
  implementation(libs.coroutines.core)

  implementation(libs.bundles.ktor.server)

  implementation(libs.bundles.ktor.client)

  implementation(libs.bundles.kgraphql)

  implementation(libs.ktor.serialization.gson)

  implementation(libs.logback)

  runtimeOnly(libs.logstash)

  implementation(libs.nimbus)
  implementation(libs.micrometer)

  testImplementation(libs.ktor.test)
  testImplementation(libs.kotlin.test)
}
