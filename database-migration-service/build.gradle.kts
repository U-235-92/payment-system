plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
//	Spring web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

//	Spring data
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

//	DB drivers
	runtimeOnly("org.postgresql:postgresql")

//	Flyway
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-database-postgresql")

//	Util
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

//	Test
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
