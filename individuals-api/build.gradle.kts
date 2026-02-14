plugins {
	java
	application
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.18.0"
}

group = "aq.payment-system"
version = "1.0.0"
description = "Handles authentication, registration and user info requests"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

application {
	mainClass = "aq.project.IndividualsApiApplication"
}

springBoot {
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

val dependencyVersionMap = mapOf(
	"feign-core" to "13.6",
	"feign-jackson" to "13.6",
	"feign-slf4j" to "13.6",
	"feign-form-spring" to "3.8.0",
	"keycloak-admin-client" to "26.0.8",
	"springdoc-openapi-starter-webmvc-ui" to "2.6.0",
	"swagger-annotations" to "2.2.41",
	"okhttp" to "5.3.2",
	"logging-interceptor" to "5.3.2",
	"okio" to "3.16.4",
	"javax-annotation" to "1.3.2",
	"javax-validation" to "2.0.1.Final",
	"jsr305" to "3.0.2",
	"spring-boot-starter-aop" to "4.0.0-M2",
	"logstash-encoder" to "9.0",
	"servlet-api" to "4.0.1",
	"testcontainers" to "2.0.3",
	"tc-keycloak" to "4.1.1"
)

dependencies {
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.core:jackson-core")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	implementation("io.github.openfeign:feign-core:${dependencyVersionMap.getValue("feign-core")}")
	implementation("io.github.openfeign:feign-jackson:${dependencyVersionMap.getValue("feign-jackson")}")
	implementation("io.github.openfeign:feign-slf4j:${dependencyVersionMap.getValue("feign-slf4j")}")

	implementation("io.github.openfeign.form:feign-form-spring:${dependencyVersionMap.getValue("feign-form-spring")}")

	implementation("org.keycloak:keycloak-admin-client:${dependencyVersionMap.getValue("keycloak-admin-client")}")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${dependencyVersionMap.getValue("springdoc-openapi-starter-webmvc-ui")}")

	implementation("io.swagger.core.v3:swagger-annotations:${dependencyVersionMap.getValue("swagger-annotations")}")

	implementation("com.squareup.okhttp3:okhttp:${dependencyVersionMap.getValue("okhttp")}")
	implementation("com.squareup.okhttp3:logging-interceptor:${dependencyVersionMap.getValue("logging-interceptor")}")
	implementation("com.squareup.okio:okio:${dependencyVersionMap.getValue("okio")}")

	implementation("javax.annotation:javax.annotation-api:${dependencyVersionMap.getValue("javax-annotation")}")
	implementation("javax.validation:validation-api:${dependencyVersionMap.getValue("javax-validation")}")

	implementation("com.google.code.findbugs:jsr305:${dependencyVersionMap.getValue("jsr305")}")

	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

	implementation("io.micrometer:micrometer-registry-prometheus")

	implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

	compileOnly("javax.servlet:javax.servlet-api:${dependencyVersionMap.getValue("servlet-api")}")

	compileOnly("org.projectlombok:lombok")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers:${dependencyVersionMap.getValue("testcontainers")}")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("tc-keycloak")}")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

openApiGenerate {
	generatorName.set("java")
	library.set("feign")
	inputSpec.set("$rootDir/individuals-api/openapi/individuals-api.yaml")
	outputDir.set("${rootDir}/individuals-api/build/generated-sources/openapi")
	ignoreFileOverride.set("$rootDir/individuals-api/openapi/openapi-generator-java-sources.ignore")
	configOptions.set(mapOf(
		"skipDefaultInterface" to "false",
		"useBeanValidation" to "true",
		"openApiNullable" to "false",
		"useFeignClientUrl" to "true",
		"useTags" to "true",
		"apiPackage" to "aq.project.api",
		"modelPackage" to "aq.project.dto",
		"serializationLibrary" to "jackson",
		"useJakartaEe" to "true",
		"additionalModelTypeAnnotations" to "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)"
	))
}

tasks.named("compileJava") {
	dependsOn("openApiGenerate")
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}