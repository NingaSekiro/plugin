plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "org.aopbuddy"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    mavenLocal()
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
    implementation(files("D:\\Code\\aopbuddy\\agent-core\\target\\agent-jar-with-dependencies.jar"))
    implementation("org.aopbuddy:spy-api:1.0")
    implementation("cn.hutool:hutool-all:5.8.40")
    implementation("org.mybatis:mybatis:3.5.16")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.h2database:h2:2.3.232")
    // junit5


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0-M1")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
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
    withType<Test> {
        useJUnitPlatform()
    }

    patchPluginXml {
        sinceBuild.set("222")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    named<org.jetbrains.intellij.tasks.PrepareSandboxTask>("prepareSandbox") {
        from("D:/Vue/mermaid/dist") {
            into(pluginName.get())
        }
    }

}
