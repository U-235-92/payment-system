plugins {
	java
	id("java-common-conventions")
	id("spring-common-conventions")
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
	"keycloak-admin-client" to "26.0.8",
	"tc-keycloak" to "4.1.1"
)

dependencies {
	implementation("org.keycloak:keycloak-admin-client:${dependencyVersionMap.getValue("keycloak-admin-client")}")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation(project(":common-dto"))

	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("tc-keycloak")}")
}

springBoot {
	mainClass = "aq.project.IndividualsApiApplication"
}