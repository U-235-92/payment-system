import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("maven-publish")
	id("org.openapi.generator") version "7.18.0"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0-dev"

extra["springCloudVersion"] = "2025.1.2"

/////////////////////////////////////
// BLOCK OF BUILD SCRIPT VARIABLES //
/////////////////////////////////////
val artifact = "payment-provider-service"

val specificationArtifactVersion = "1.0.8-dev"
val specificationArtifactJarName = "${artifact}-api-specification-${specificationArtifactVersion}.jar"

val openApiSpecificationYamlPath = "$rootDir/openapi/${artifact}-api-specification.yaml"

val openApiSpecificationBuildPath = "$rootDir/build/generated/openapi"

//////////////////////////////////////////////////////////
// LOAD OF LOCAL MAVEN REPOSITORY CONNECTION PROPERTIES //
//////////////////////////////////////////////////////////
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

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

sourceSets {
	main {
		java {
			srcDir("${rootDir}/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/api-contract/src/main/java")
		}
	}
}

//////////////////////////
// PROJECT DEPENDENCIES //
//////////////////////////
val dependencyVersionMap = mapOf(
//	OpenApi
	"springdoc-openapi" to "3.0.2",

//	Resilience4j
	"resilience4j" to "2.4.0",

//	Spring AOP
	"spring-boot-starter-aop" to "4.0.0-M2",

//	Mapping
	"mapstruct" to "1.6.3",

//	Logging
	"logstash-encoder" to "9.0",

//	Test
	"wiremock-spring-boot" to "4.0.9",

//	Spring data envers
	"spring-data-envers" to "4.0.4",

//	Lombok MapStruct binding
	"lombok-mapstruct-binding" to "0.2.0"
)

dependencies {
//	Spring web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

//	Spring kafka
	implementation("org.springframework.boot:spring-boot-starter-kafka")

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security")

//	Spring data
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.springframework.data:spring-data-envers:${dependencyVersionMap.getValue("spring-data-envers")}")

//	Spring AOP
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

//	Spring validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

//	Resilience4j
	implementation("io.github.resilience4j:resilience4j-spring-boot4:${dependencyVersionMap.getValue("resilience4j")}")

//	Mapping
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

//	Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

//	Lombok MapStruct binding
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${dependencyVersionMap.getValue("lombok-mapstruct-binding")}")

//	Logback JSON encoder
	implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

//	OpenApi
	implementation("tools.jackson.core:jackson-core")
	implementation("tools.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${dependencyVersionMap.getValue("springdoc-openapi")}")

//	Observability
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

//	Test
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-grafana")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.springframework.boot:spring-boot-resttestclient:4.0.2")
	testImplementation("org.apache.httpcomponents.client5:httpclient5")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
	testImplementation("org.wiremock.integrations:wiremock-spring-boot:${dependencyVersionMap.getValue("wiremock-spring-boot")}")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

////////////////////
// BLOCK OF TASKS //
////////////////////
/////////////
// TESTING //
/////////////
tasks.withType<Test> {
	useJUnitPlatform()
}

//////////////////////
// COMPILE OVERRIDE //
//////////////////////
tasks.named("compileJava") {
	dependsOn("genApi")
}

//////////////////////////////////////////
// GENERATION OF OPEN API SPECIFICATION //
//////////////////////////////////////////
tasks.register<GenerateTask>("genApi") {
	inputSpec.set(openApiSpecificationYamlPath) // Источник спецификации
	outputDir.set("${openApiSpecificationBuildPath}/api-contract") // Путь куда генерировать исходники
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
//////////////////////////////////////////////////////////////////////////////////////////
// PURGE A JAR WHICH CONTAINS OPEN API SPECIFICATION WHICH WAS CREATED BY [jarApi] TASK //
//////////////////////////////////////////////////////////////////////////////////////////
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

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// CREATING JAR WHICH CONTAINS OPEN API SPECIFICATION WHICH IS READY TO SEND TO LOCAL MAVEN REPOSITORY //
/////////////////////////////////////////////////////////////////////////////////////////////////////////
tasks.register<Jar>("jarApi") {
	dependsOn("purgeJarApi")
	archiveFileName.set(specificationArtifactJarName)
	destinationDirectory.set(file(openApiSpecificationBuildPath))
	from(openApiSpecificationYamlPath)
}

//////////////////
// JAR OVERRIDE //
//////////////////
tasks.named("jar") {
	dependsOn("jarApi")
}

///////////////////////////////////////////////////
// PUBLISH ARTIFACTS INTO LOCAL MAVEN REPOSITORY //
///////////////////////////////////////////////////
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
