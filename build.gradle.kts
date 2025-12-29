plugins {
	java
	id("org.springframework.boot") version "3.5.9"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "indi.dkx"
version = "0.0.1-SNAPSHOT"
description = "laocai project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
	all {
		// 1. 全局排除 Spring Boot 默认的日志启动器 (它包含了 Logback)
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
		// 2. 【关键】强制排除 log4j-to-slf4j，解决 "cannot be present with" 那个报错
		exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
		// 3. 顺便确保 Logback 彻底消失 (解决 Multiple SLF4J providers 警告)
		exclude(group = "ch.qos.logback", module = "logback-classic")
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
	// 排除默认的 Logback 依赖
	implementation("org.springframework.boot:spring-boot-starter-webflux:3.5.9")

	// 引入 Log4j2 依赖
	implementation("org.springframework.boot:spring-boot-starter-log4j2:3.5.9")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("dev.langchain4j:langchain4j:1.9.1")
	implementation("dev.langchain4j:langchain4j-open-ai-spring-boot-starter:1.9.1-beta17")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
