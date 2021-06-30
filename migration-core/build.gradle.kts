import Publishing.configureMavenPublication

plugins {
    id("kotlin")
    id("maven-publish")
    id("signing")
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.strikt.core)
    testImplementation(libs.mockk.core)
    testImplementation(libs.robolectric)
}

// Build sources too
java {
    withSourcesJar()
}

// Add name and version to manifest
tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
}

publishing {
    publications {
        create(
            "release",
            configureMavenPublication(
                "migration-core",
                "A Kotlin library to enable easier program migrations, inspired by AndroidX Room",
                "https://github.com/boswelja/android-migration",
                project.configurations.implementation.get().allDependencies
            ) {
                artifact("$buildDir/libs/${project.name}-${project.version}.jar")
                artifact("$buildDir/libs/${project.name}-${project.version}-sources.jar")
            }
        )
    }
    repositories(Publishing.repositories)
}

// Create signing config
ext["signing.keyId"] = Publishing.signingKeyId
ext["signing.password"] = Publishing.signingPassword
ext["signing.secretKeyRingFile"] = Publishing.signingSecretKeyring
signing {
    sign(publishing.publications)
}

// Make publish task depend on assembleRelease
tasks.named("publishReleasePublicationToSonatypeRepository") {
    dependsOn("assembleRelease")
}
