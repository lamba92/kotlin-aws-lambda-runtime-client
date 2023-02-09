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
version = GITHUB_REF ?: "1.0-SNAPSHOT"

fun getPrivateKey(): String? {
    val envVariable = System.getenv("GPG_SECRET_KEY") ?: return null
    return runCatching { Paths.get(envVariable).toFile().readText() }
        .getOrNull()
        ?:Base64.getDecoder().decode(envVariable).toString(Charsets.UTF_8)
}

signing {
    useInMemoryPgpKeys(getPrivateKey(), System.getenv("GPG_SECRET_KEY_PASSWORD"))
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
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
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