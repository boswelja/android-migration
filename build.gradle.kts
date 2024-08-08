plugins {
    id("com.android.library") version "8.5.1" apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.0.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.8.3" apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

group = "io.github.boswelja.migration"
version = findProperty("version") ?: "0.1.0"

nexusPublishing {
    repositories {
        sonatype {
            val ossrhUsername: String? by project
            val ossrhPassword: String? by project
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl
                .set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}
