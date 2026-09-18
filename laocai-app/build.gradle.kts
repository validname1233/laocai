plugins {
	java
	kotlin("jvm") version "2.4.10"
	kotlin("plugin.spring") version "2.4.10"
	kotlin("kapt") version "2.4.10"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "indi.kyson"
version = "0.0.1-SNAPSHOT"
description = "laocai project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
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
	implementation(project(":laocai-bot-spring-boot-starter"))

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	kapt("org.springframework.boot:spring-boot-configuration-processor:4.1.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("com.embabel.agent:embabel-agent-starter:1.5.1")
	implementation("com.embabel.agent:embabel-agent-starter-openai:1.5.1")
	// Registers Spring AI's OpenAI auto-configuration and creates OpenAiChatModel.
	implementation("org.springframework.ai:spring-ai-starter-model-openai:2.0.0")
	implementation("redis.clients:jedis:7.4.1")
	implementation("com.github.ben-manes.caffeine:caffeine")
}

tasks.withType<Test> {
	useJUnitPlatform()

	testLogging {
        showStandardStreams = true
    }
}
