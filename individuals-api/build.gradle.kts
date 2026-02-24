plugins {
	java
	id("open-api-conventions")
	id("java-common-conventions")
	id("spring-common-conventions")
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
	"feign-core" to "13.6",
	"feign-jackson" to "13.6",
	"feign-slf4j" to "13.6",
	"feign-form-spring" to "3.8.0",
	"keycloak-admin-client" to "26.0.8",
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
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("tc-keycloak")}")
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

openApiGenerate {
	inputSpec.set("$rootDir/individuals-api/openapi/individuals-api.yaml")
	outputDir.set("${rootDir}/individuals-api/build/generated-sources/openapi")
	ignoreFileOverride.set("$rootDir/individuals-api/openapi/openapi-generator-java-sources.ignore")
}