plugins {
	java
	id("open-api-conventions")
	id("java-common-conventions")
	id("spring-common-conventions")
}

group = "aq.payment-system"
version = "1.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("org.flywaydb:flyway-database-postgresql")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
}

springBoot {
	mainClass = "aq.project.UserServiceApplication"
}

sourceSets { // Источники исходников для проекта
	main {
		java {
			srcDirs("${rootDir}/user-service/src/main/java",
				"${rootDir}/user-service/build/generated-sources/openapi/src/main/java")
		}
	}
}

openApiGenerate {
	inputSpec.set("$rootDir/user-service/openapi/user-service-api.yaml") // Источник спецификации
	outputDir.set("$rootDir/user-service/build/generated-sources/openapi") // Путь куда генерировать исходники
	ignoreFileOverride.set("$rootDir/user-service/openapi/openapi-generator-java-sources.ignore") // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
}

extra["springCloudVersion"] = "2025.1.0"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}