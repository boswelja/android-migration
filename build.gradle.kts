buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        classpath("com.android.tools.build:gradle:7.1.0-alpha11")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = Publishing.groupId
version = Publishing.version ?: "0.1.0"

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl
                .set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(Publishing.ossrhUsername)
            password.set(Publishing.ossrhPassword)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
