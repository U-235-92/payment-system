plugins {
	java
	id("java-common-conventions")
	id("spring-common-conventions")
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
	"mapstruct" to "1.6.3"
)

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation(project(":common-dto"))
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")

	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

	runtimeOnly("org.postgresql:postgresql")

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