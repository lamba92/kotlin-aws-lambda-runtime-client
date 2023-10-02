@file:Suppress("UnstableApiUsage")

import java.nio.file.Paths
import java.util.*

plugins {
    kotlin("multiplatform") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val GIT_TAG = System.getenv("GITHUB_REF")
    ?.takeIf { it.startsWith("refs/tags/") }
    ?.substringAfterLast("/")

group = "com.github.lamba92"
version = GIT_TAG ?: "1.0.0-SNAPSHOT"

fun getAndCheckEnv(key: String): String? {
    val envVar = System.getenv(key)
    if (envVar == null && System.getenv("CI") == "true")
        logger.warn("Environment variable $key missing, publication is disabled")
    return envVar
}

fun getPrivateKey(): String? {
    val envVariable = getAndCheckEnv("GPG_SECRET_KEY") ?: return null
    return runCatching { Paths.get(envVariable).toFile().readText() }
        .getOrNull()
        ?: Base64.getDecoder().decode(envVariable).toString(Charsets.UTF_8)
}

signing {
    useInMemoryPgpKeys(getPrivateKey(), getAndCheckEnv("GPG_SECRET_KEY_PASSWORD"))
    sign(publishing.publications)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier = "javadoc"
    from(tasks.dokkaHtml)
    destinationDirectory = layout.buildDirectory.dir("artifacts")
    archiveBaseName = project.name
}

kotlin {
    jvm()
    js {
        nodejs()
        useEsModules()
    }
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.ktor:ktor-client-content-negotiation:2.3.4")
                api("io.ktor:ktor-client-logging:2.3.4")
                api("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                api("io.ktor:ktor-client-cio:2.3.4")
            }
        }
        val jsMain by getting {
            dependencies {
                api("io.ktor:ktor-client-js:2.3.4")
                api("org.jetbrains.kotlinx:kotlinx-nodejs:0.0.7")
            }
        }
        val linuxX64Main by getting {
            dependencies {
                api("io.ktor:ktor-client-curl:2.3.4")
            }
        }
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set(project.name)
                url.set("https://github.com/lamba92/kotlin-aws-lambda-runtime-client")
                description.set(file("README.md").readLines()[2])
                licenses {
                    license {
                        name.set("APACHE LICENSE, VERSION 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                scm {
                    url.set("https://github.com/lamba92/kotlin-aws-lambda-runtime-client.git")
                    tag.set(project.version.toString())
                }
                developers {
                    developer {
                        name.set("Lamberto Basti")
                        email.set("basti.lamberto@gmail.com")
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(getAndCheckEnv("SONATYPE_USERNAME"))
            password.set(getAndCheckEnv("SONATYPE_PASSWORD"))
        }
    }
}
