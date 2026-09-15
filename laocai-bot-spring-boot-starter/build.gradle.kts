plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
    kotlin("kapt") version "2.4.10"
    `java-library`
    `maven-publish`
}

group = "indi.kyson"
version = "0.0.1-SNAPSHOT"

description = "Laocai Bot Spring Boot Starter"

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
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("tools.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")
    implementation("org.springframework.boot:spring-boot-webclient")

    kapt("org.springframework.boot:spring-boot-configuration-processor:4.1.0")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "laocai-bot-spring-boot-starter"
            from(components["java"])
        }
    }
}
