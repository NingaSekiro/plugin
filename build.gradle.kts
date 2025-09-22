plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm")
}

group = "org.aopbuddy"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(
        listOf(
            "java",
            "org.intellij.groovy"
        )
    )
}


dependencies {
    implementation(fileTree("lib") { include("*.jar") })
    implementation("cn.hutool:hutool-all:5.8.40")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"

    }
    intellij {
        updateSinceUntilBuild.set(false)
    }

    patchPluginXml {
        sinceBuild.set("232")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

}
