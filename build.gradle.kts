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

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks {
  val installPreCommitHook =
      register("installPreCommitHook", Copy::class) {
        from(File(rootProject.rootDir, "scripts/pre-commit"))
        into(File(rootProject.rootDir, ".git/hooks"))
        fileMode = 0b111101101
        dirMode = 0b1010001010
      }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
    dependsOn(installPreCommitHook)
  }
}

dependencies {
  implementation(libs.coroutines.core)

  implementation(libs.bundles.ktor.server)

  implementation(libs.bundles.ktor.client)

  implementation(libs.bundles.kgraphql)

  implementation(libs.logback)

  runtimeOnly(libs.logstash)

  implementation(libs.nimbus)
  implementation(libs.micrometer)

  testImplementation(libs.ktor.test)
  testImplementation(libs.kotlin.test)
}
