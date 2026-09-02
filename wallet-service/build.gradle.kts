import org.gradle.kotlin.dsl.java
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("maven-publish")
	id("org.openapi.generator") version "7.18.0"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0"

extra["springCloudVersion"] = "2025.1.2"

val artifact = "wallet-service"

val specificationArtifactVersion = "1.0.6-dev"
val specificationArtifactJarName = "${artifact}-api-specification-${specificationArtifactVersion}.jar"

val openApiSpecificationYamlPath = "$rootDir/openapi/${artifact}-api-specification.yaml"
val openApiSpecificationBuildPath = "$rootDir/build/generated/openapi"

val envFile = file(".env")
if(envFile.exists()) {
	envFile.forEachLine { line ->
		if(line.isNotBlank() && !line.startsWith("#")) {
			val pair = line.split("=", limit = 2)
			if(pair.size == 2) {
				val key = pair[0].trim()
				val value = pair[1].trim()
				if(key.startsWith("LOCAL_REPO_") && value.isNotBlank()) {
					System.setProperty(key, value)
				}
			}
		}
	}
}

sourceSets { // Источники исходников для проекта
	main {
		java {
			srcDir("$rootDir/src/main/java")
			srcDir("$openApiSpecificationBuildPath/api-contract/src/main/java")
		}
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

springBoot {
	mainClass = "aq.project.WalletServiceApplication"
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

val dependencyVersionMap = mapOf(
//	Mapping
	"mapstruct" to "1.6.3",

//	Observability
	"opentelemetry-api" to "1.59.0",
	"opentelemetry-sdk" to "1.59.0",
	"opentelemetry-annotations" to "2.25.0",
	"logstash-encoder" to "9.0",

//	Spring
	"spring-data-envers" to "4.0.4",
	"spring-boot-starter-aop" to "4.0.0-M2",
	"spring-cloud-starter-openfeign" to "5.0.1",

//	Test
	"testcontainers" to "2.0.3",
	"wiremock-spring-boot" to "4.0.9",
	"testcontainers-keycloak" to "4.1.1",

//	OpenApi
	"springdoc-openapi" to "3.0.2",

//	Sharding
	"shardingsphere" to "5.5.1",
	"atomikos" to "6.0.1",

//	Lombok MapStruct binding
	"lombok-mapstruct-binding" to "0.2.0"
)

dependencies {
//	Spring web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

//	Spring kafka
	implementation("org.springframework.boot:spring-boot-starter-kafka")

//	Spring AOP
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

//	Spring validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

//	Spring data
	implementation("org.springframework.data:spring-data-envers:${dependencyVersionMap.getValue("spring-data-envers")}")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-h2console")

//	DB drivers
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")

//	Flyway
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-flyway")

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")

//	Util
	annotationProcessor("org.projectlombok:lombok")
	compileOnly("org.projectlombok:lombok")

//	Mapping
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

//	Lombok MapStruct binding
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${dependencyVersionMap.getValue("lombok-mapstruct-binding")}")

//	Test
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers:${dependencyVersionMap.getValue("testcontainers")}")
	testImplementation("org.testcontainers:testcontainers-grafana")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-kafka")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("testcontainers-keycloak")}")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	testImplementation("org.wiremock.integrations:wiremock-spring-boot:${dependencyVersionMap.getValue("wiremock-spring-boot")}")

//	Logback JSON encoder
	implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

//	Observability
	implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:${dependencyVersionMap.getValue("opentelemetry-annotations")}")
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
	implementation("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")

//	OpenApi
	implementation("tools.jackson.core:jackson-core")
	implementation("tools.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${dependencyVersionMap.getValue("springdoc-openapi")}")

//	Sharding. Basic
	implementation("org.apache.shardingsphere:shardingsphere-jdbc:${dependencyVersionMap.getValue("shardingsphere")}")
//	Sharding. API
	implementation("org.apache.shardingsphere:shardingsphere-transaction-api:${dependencyVersionMap.getValue("shardingsphere")}")
//	Sharding. Adds ability for using [jdbc:shardingsphere:classpath] as spring.datasource.url property for load sharding datasource settings
	implementation("org.apache.shardingsphere:shardingsphere-infra-url-classpath:${dependencyVersionMap.getValue("shardingsphere")}")
//	Sharding. XA-transactions
	implementation("org.apache.shardingsphere:shardingsphere-transaction-xa-core:${dependencyVersionMap.getValue("shardingsphere")}")
//	Sharding. Implementation of XA-transaction
	implementation("com.atomikos:transactions-jta:${dependencyVersionMap.getValue("atomikos")}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn("genApi")
}

tasks.register<GenerateTask>("genApi") {
	inputSpec.set(openApiSpecificationYamlPath) // Источник спецификации
	outputDir.set("$openApiSpecificationBuildPath/api-contract") // Путь куда генерировать исходники
	generatorName.set("spring") // Использовать генератор Java для создания исходников на этом языке
	library.set("spring-boot") // Без явного указания библиотеки генератор Java (выше) настроен на работу с okhttp-gson, по этой причине инструкция serializationLibrary работать не будет (игнорируется) и все DTO начинают использовать библиотеку gson для JSON! Эта инструкция явно указывает использование нужной библиотеки API, которая использует Jackson для JSON
	modelPackage.set("aq.project.dto") // Название пакета модели
	apiPackage.set("aq.project.controller") // Название пакета api/controllers
	apiNameSuffix.set("RestControllerApi") // Заменяет суффикс (по умолчанию Api) на указанный для сгенерированных интерфейсов контроллеров
	configOptions.set(mapOf(
		"useBeanValidation" to "true", // Использовать JSR валидацию
		"useJakartaEe" to "true", // Использовать Jakarta EE в Spring
		"sourceFolder" to "src/main/java", // Source папка для сгенерированного кода
		"hideGenerationTimestamp" to "true", // Убрать из сгенерированных исходников отметку времени
		"openApiNullable" to "false", // Не добавлять зависимость на jackson-databind-nullable для всех свойств, отмеченных как nullable: true
		"dateLibrary" to "java8", // Использовать современную модель даты и времени в Java
		"generateApiTests" to "false", // Не генерировать тесты для API
		"generateApiDocumentation" to "false", // Не генерировать документацию для API
		"interfaceOnly" to "true", // Генерация только интерфейсов контроллеров по спецификации в .yaml файле
		"useTags" to "true"
	))
}

tasks.register("purgeJarApi") {
	val root = File(openApiSpecificationBuildPath)
	if(root.exists()) {
		for(file in root.listFiles()) {
			if(file.name.endsWith(".jar")) {
				file.delete()
			}
		}
	}
}

tasks.register<Jar>("jarApi") {
	dependsOn("purgeJarApi")
	archiveFileName.set(specificationArtifactJarName)
	destinationDirectory.set(file(openApiSpecificationBuildPath))
	from(openApiSpecificationYamlPath)
}

tasks.named("jar") {
	dependsOn("jarApi")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			artifactId = "${artifact}-api-specification"
			groupId = group.toString()
			version = specificationArtifactVersion
			artifact(tasks.named("jarApi"))
		}
	}
	repositories {
		maven {
			url = uri(System.getProperty("LOCAL_REPO_URL"))
			isAllowInsecureProtocol = true
			credentials {
				username = System.getProperty("LOCAL_REPO_USERNAME")
				password = System.getProperty("LOCAL_REPO_PASSWORD")
			}
		}
	}
}