plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlinx.kover")
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(21)
    jvm()
    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.boswelja.migration"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

kover {
    reports {
        verify {
            rule("Code line coverage") {
                minBound(90)
            }
        }
    }
}

group = "io.github.boswelja.migration"
version = findProperty("version") ?: "0.1.0"

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            val ossrhUsername: String? by project
            val ossrhPassword: String? by project
            name = "sonatype"
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])

        pom {
            name.set("migration-core")
            description.set("A Kotlin library to enable easier program migrations, inspired by AndroidX Room")
            url.set("https://github.com/boswelja/kotlin-migration")
            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://github.com/boswelja/kotlin-migration/blob/main/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("boswelja")
                    name.set("Jack Boswell")
                    email.set("boswelja@outlook.com")
                    url.set("https://github.com/boswelja")
                }
            }
            scm {
                connection.set("scm:git:github.com/boswelja/kotlin-migration.git")
                developerConnection.set("scm:git:ssh://github.com/boswelja/kotlin-migration.git")
                url.set("https://github.com/boswelja/kotlin-migration")
            }
        }
    }
}

detekt {
    config.from("$rootDir/config/detekt/detekt.yml")
}
