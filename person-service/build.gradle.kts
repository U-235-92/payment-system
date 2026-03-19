plugins {
	java
	id("java-common-conventions")
	id("spring-common-conventions")
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
	"mapstruct" to "1.6.3",
	"hibernate-envers" to "7.2.6.Final",
	"opentelemetry-api" to "1.59.0",
	"opentelemetry-sdk" to "1.59.0",
	"opentelemetry-annotations" to "2.25.0",
	"spring-data-envers" to "4.0.4"
)

dependencies {
//	DTO
	implementation(project(":common-dto"))

//	Spring data
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.data:spring-data-envers:${dependencyVersionMap.getValue("spring-data-envers")}")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")

//	Spring web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

//	Mapping
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

// Test
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
}

springBoot {
	mainClass = "aq.project.PersonServiceApplication"
}

extra["springCloudVersion"] = "2025.1.0"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}