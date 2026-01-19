plugins {
	java
	application
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.18.0"
}

group = "aq.payment-system"
version = "1.0.0-SNAPSHOT"
description = "Handles authentication, registration and user info requests"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

application {
	mainClass = "aq.project.IndividualsApiApplication"
}

sourceSets {
	main {
		java {
			srcDirs("${rootDir}/individuals-api/src/main/java",
				"${rootDir}/individuals-api/build/generated-sources/openapi/src/main/java")
		}
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("io.swagger.core.v3:swagger-annotations:2.2.41")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
	implementation("com.google.code.gson:gson:2.13.2")
	implementation("io.gsonfire:gson-fire:1.9.0")
	implementation("com.squareup.okhttp3:okhttp:5.3.2")
	implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
	implementation("com.squareup.okio:okio:3.16.4")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("com.google.code.findbugs:jsr305:3.0.2")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	compileOnly("javax.servlet:javax.servlet-api:4.0.1")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApiGenerate {
	generatorName.set("java")
	inputSpec.set("$rootDir/individuals-api/openapi/individuals-api.yaml")
	outputDir.set("${buildDir}/generated-sources/openapi")
	ignoreFileOverride.set("$rootDir/individuals-api/openapi/openapi-generator-java-sources.ignore")
	configOptions.set(mapOf(
		"skipDefaultInterface" to "false",
		"useBeanValidation" to "true",
		"openApiNullable" to "false",
		"useFeignClientUrl" to "true",
		"useTags" to "true",
		"apiPackage" to "aq.project.api",
		"modelPackage" to "aq.project.dto",
	))
}

tasks.named("compileJava") {
	dependsOn("openApiGenerate")
}

tasks.withType<Test> {
	useJUnitPlatform()
}