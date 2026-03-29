plugins {
	java
	id("org.springframework.boot") version "4.0.3"
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
}

repositories {
	maven {
		url = uri("https://maven.aliyun.com/repository/public/")
	}
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux:4.0.3")
	implementation("org.springframework.boot:spring-boot-webclient")

	compileOnly("org.projectlombok:lombok:1.18.42")
	annotationProcessor("org.projectlombok:lombok:1.18.42")
	testCompileOnly("org.projectlombok:lombok:1.18.42")
	testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test:3.8.2")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation(platform("org.springframework.ai:spring-ai-bom:2.0.0-M4"))
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
