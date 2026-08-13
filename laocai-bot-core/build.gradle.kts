plugins {
    kotlin("jvm") version "2.4.10"
    `java-library`
    `maven-publish`
}

group = "indi.kyson"
version = "0.0.1-SNAPSHOT"

description = "Laocai Bot Core"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

kotlin {
    compilerOptions {
        javaParameters = true
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
    api("org.springframework:spring-webflux")
    api("io.projectreactor:reactor-core")
    api("tools.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.slf4j:slf4j-api")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "laocai-bot-core"
            from(components["java"])
        }
    }
}
