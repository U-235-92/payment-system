import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("org.openapi.generator") version "7.18.0"
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0"

val dependencyVersionMap = mapOf(
//	Observability
	"opentelemetry-api" to "1.59.0",
	"opentelemetry-sdk" to "1.59.0",
	"opentelemetry-annotations" to "2.25.0",

//	Logging
	"logstash-encoder" to "9.0",

//	Spring
	"spring-boot-starter-aop" to "4.0.0-M2",

//	Test
	"testcontainers" to "2.0.3",
	"wiremock-spring-boot" to "4.0.9",

//	Mapping
	"mapstruct" to "1.6.3",

//	OpenApi
	"springdoc-openapi" to "3.0.2"
)

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

sourceSets { // Источники исходников для проекта
	main {
		java {
			srcDirs(
				"${rootDir}/src/main/java",
				"${rootDir}/build/generated/openapi/client-contracts/src/main/java",
				"${rootDir}/build/generated/openapi/controller-contracts/src/main/java"
			)
		}
	}
}

dependencies {
//	Spring web
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

//	Spring kafka
	implementation("org.springframework.boot:spring-boot-starter-kafka")

//	Spring AOP
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

//	Spring validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

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

//	Mapping
	implementation("org.mapstruct:mapstruct:${dependencyVersionMap.getValue("mapstruct")}")
	annotationProcessor("org.mapstruct:mapstruct-processor:${dependencyVersionMap.getValue("mapstruct")}")

//	Logback JSON encoder
	implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

//	Util
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

//	Test
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-grafana")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-kafka")
	testAnnotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.wiremock.integrations:wiremock-spring-boot:${dependencyVersionMap.getValue("wiremock-spring-boot")}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn("generateContracts")
}

tasks.register("generateContracts") {
	dependsOn("generateClientContracts")
	dependsOn("generateControllerContracts")
}

tasks.register<GenerateTask>("generateClientContracts") {
	inputSpec.set("$rootDir/openapi/components-specification.yaml") // Источник спецификации
	outputDir.set("$rootDir/build/generated/openapi/client-contracts") // Путь куда генерировать исходники
	ignoreFileOverride.set("$rootDir/openapi/openapi-generator-java-sources.ignore") // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
	generatorName.set("java") // Использовать генератор Java для создания исходников на этом языке
	library.set("restclient") // Без явного указания библиотеки генератор Java (выше) настроен на работу с okhttp-gson, по этой причине инструкция serializationLibrary работать не будет (игнорируется) и все DTO начинают использовать библиотеку gson для JSON! Эта инструкция явно указывает использование нужной библиотеки API, которая использует Jackson для JSON
	modelPackage.set("aq.project.dto") // Название пакета модели
	apiPackage.set("aq.project.client") // Название пакета api/controllers
	configOptions.set(mapOf(
		"useBeanValidation" to "true", // Использовать JSR валидацию
		"useJakartaEe" to "true", // Использовать Jakarta EE в Spring
		"sourceFolder" to "src/main/java", // Source папка для сгенерированного кода
		"hideGenerationTimestamp" to "true", // Убрать из сгенерированных исходников отметку времени
		"openApiNullable" to "false", // Не добавлять зависимость на jackson-databind-nullable для всех свойств, отмеченных как nullable: true
		"dateLibrary" to "java8", // Использовать современную модель даты и времени в Java
		"generateApiTests" to "false", // Не генерировать тесты для API
		"generateApiDocumentation" to "false", // Не генерировать документацию для API
		"serializationLibrary" to "jackson",
		"useTags" to "true"
	))
}

tasks.register<GenerateTask>("generateControllerContracts") {
	inputSpec.set("$rootDir/openapi/components-specification.yaml") // Источник спецификации
	outputDir.set("$rootDir/build/generated/openapi/controller-contracts") // Путь куда генерировать исходники
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