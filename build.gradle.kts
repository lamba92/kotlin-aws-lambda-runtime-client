import java.util.Base64
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val GITHUB_REF = System.getenv("GITHUB_REF")?.substringAfterLast("/")

group = "com.github.lamba92"
version = GITHUB_REF ?: "1.0.0-SNAPSHOT"

fun getAndCheckEnv(key: String): String? {
    val envVar = System.getenv(key)
    if (envVar == null) logger.warn("Environment variable $key missing, publication is disabled")
    return envVar
}

fun getPrivateKey(): String? {
    val envVariable = getAndCheckEnv("GPG_SECRET_KEY") ?: return null
    return runCatching { Paths.get(envVariable).toFile().readText() }
        .getOrNull()
        ?:Base64.getDecoder().decode(envVariable).toString(Charsets.UTF_8)
}

signing {
    useInMemoryPgpKeys(getPrivateKey(), getAndCheckEnv("GPG_SECRET_KEY_PASSWORD"))
    sign(publishing.publications)
}

val javadocJar by tasks.creating(Jar::class) {
    from(tasks.dokkaJavadoc)
    archiveBaseName.set("javadoc")
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    from(kotlin.sourceSets.main.get().kotlin)
    archiveBaseName.set("sources")
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("main") {
            from(components["kotlin"])
            artifact(javadocJar)
            artifact(sourcesJar)
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
                    tag.set(GITHUB_REF)
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

kotlin.target.compilations.all {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(getAndCheckEnv("SONATYPE_USERNAME"))
            password.set(getAndCheckEnv("SONATYPE_PASSWORD"))
        }
    }
}

dependencies {
    api("io.ktor:ktor-client-cio:2.2.3")
    api("io.ktor:ktor-client-content-negotiation:2.2.3")
    api("io.ktor:ktor-client-logging:2.2.3")
    api("io.ktor:ktor-serialization-kotlinx-json:2.2.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
}