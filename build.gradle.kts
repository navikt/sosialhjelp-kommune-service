plugins {
  kotlin("jvm") version libs.versions.kotlin
  alias(libs.plugins.ktor)
  alias(libs.plugins.kotlin.plugin.serialization)
  alias(libs.plugins.ktfmt)
  alias(libs.plugins.versions)
}

group = "no.nav.sosialhjelp"

version = "1.0.0"

buildscript {
  dependencies {
    classpath("org.codehaus.plexus:plexus-utils:4.0.3") {
      because("Vulnerability GHSA-6fmv-xxpf-w3cw / CVE-2025-67030")
    }
    classpath(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.22.1"))
    classpath("org.apache.httpcomponents.client5:httpclient5:5.6.4")
  }
}

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
  // pga sårbarheter
  implementation(platform("tools.jackson:jackson-bom:3.2.2"))

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
