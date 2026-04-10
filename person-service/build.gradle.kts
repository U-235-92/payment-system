plugins {
	java
	id("java-library")
	id("io.freefair.lombok") version "9.2.0"
	id("org.openapi.generator") version "7.18.0"
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
//	Mapping
	"mapstruct" to "1.6.3",

//	Hibernate
	"hibernate-envers" to "7.2.6.Final",

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

//	OpenApi
	"feign-core" to "13.6",
	"feign-jackson" to "13.6",
	"feign-slf4j" to "13.6",
	"feign-form-spring" to "3.8.0",
	"jakarta-annotation" to "3.0.0",
	"jakarta-validation" to "3.1.1"
)

dependencies {
//	Spring common
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

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

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

//	Mapping
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

//	Test
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers:${dependencyVersionMap.getValue("testcontainers")}")
	testImplementation("org.testcontainers:testcontainers-grafana")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//	OpenApi
	implementation("com.fasterxml.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.core:jackson-core")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("io.github.openfeign:feign-core:${dependencyVersionMap.getValue("feign-core")}")
	implementation("io.github.openfeign:feign-jackson:${dependencyVersionMap.getValue("feign-jackson")}")
	implementation("io.github.openfeign:feign-slf4j:${dependencyVersionMap.getValue("feign-slf4j")}")
	implementation("io.github.openfeign.form:feign-form-spring:${dependencyVersionMap.getValue("feign-form-spring")}")
	implementation("jakarta.annotation:jakarta.annotation-api:${dependencyVersionMap.getValue("jakarta-annotation")}")
	implementation("jakarta.validation:jakarta.validation-api:${dependencyVersionMap.getValue("jakarta-validation")}")

//	Observability
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
	implementation("io.opentelemetry:opentelemetry-exporter-otlp")
	implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:${dependencyVersionMap.getValue("opentelemetry-annotations")}")
	implementation("io.micrometer:micrometer-registry-prometheus")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

//	Util
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

springBoot {
	mainClass = "aq.project.PersonServiceApplication"
}

repositories {
	mavenCentral()
}

sourceSets { // Источники исходников для проекта
	main {
		java {
			srcDirs("${rootDir}/src/main/java",
				"${rootDir}/build/generated/openapi/src/main/java")
		}
	}
}

openApiGenerate {
	inputSpec.set("$rootDir/openapi/components-specification.yaml") // Источник спецификации
	outputDir.set("$rootDir/build/generated/openapi") // Путь куда генерировать исходники
	ignoreFileOverride.set("$rootDir/openapi/openapi-generator-java-sources.ignore") // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
	generatorName.set("java") // Использовать генератор Java для создания исходников на этом языке
	library.set("feign") // Без явного указания библиотеки генератор Java (выше) настроен на работу с okhttp-gson, по этой причине инструкция serializationLibrary работать не будет (игнорируется) и все DTO начинают использовать библиотеку gson для JSON! Эта инструкция явно указывает использование нужной библиотеки API, которая использует Jackson для JSON
	modelPackage.set("aq.project.dto") // Название пакета модели
//	apiPackage.set("aq.project.api") // Закомментировано, чтобы исключить генерацию API/Контроллеров
	configOptions.set(mapOf(
		"useBeanValidation" to "true", // Использовать JSR валидацию
		"useJakartaEe" to "true", // Использовать Jakarta EE в Spring
		"sourceFolder" to "src/main/java", // Source папка для сгенерированного кода
		"hideGenerationTimestamp" to "true", // Убрать из сгенерированных исходников отметку времени
		"openApiNullable" to "false", // Не добавлять зависимость на jackson-databind-nullable для всех свойств, отмеченных как nullable: true
		"dateLibrary" to "java8", // Использовать современную модель даты и времени в Java
		"generateApiTests" to "false", // Не генерировать тесты для API
		"generateApiDocumentation" to "false", // Не генерировать документацию для API
		"serializationLibrary" to "jackson", // Библиотека сериализации для JSON
		"additionalModelTypeAnnotations" to "@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)" // Добавить ко всем классам сгенерированных моделей аннотацию jackson @JsonIgnoreProperties(ignoreUnknown = true)
	))
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn("openApiGenerate")
}

extra["springCloudVersion"] = "2025.1.0"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}