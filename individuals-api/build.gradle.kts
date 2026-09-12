import org.gradle.internal.extensions.stdlib.capitalized
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("org.openapi.generator") version "7.18.0"
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "aq.payment-system"
version = "1.0.0-dev"

extra["springCloudVersion"] = "2025.1.0"

val artifact = "individuals-api-service"

val yamlExtension = ".yaml"

val openApiSpecificationBuildPath = "$rootDir/build/generated/openapi"
val openApiSpecificationYamlPath = "$rootDir/openapi/individuals-api-service-api-specification.yaml"
val openApiIgnore = "$rootDir/openapi/_openapi.ignore"

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

repositories {
	mavenCentral()
	maven {
		name = System.getProperty("LOCAL_REPO_NAME")
		url = uri(System.getProperty("LOCAL_REPO_URL"))
		isAllowInsecureProtocol = true
		credentials {
			username = System.getProperty("LOCAL_REPO_USERNAME")
			password = System.getProperty("LOCAL_REPO_PASSWORD")
		}
	}
}

springBoot {
	mainClass = "aq.project.IndividualsApiApplication"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

sourceSets {
	main {
		java {
			srcDir("${rootDir}/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/currency-rate-service/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/individuals-api-service/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/person-service/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/transaction-service/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/wallet-service/src/main/java")
		}
	}
}

configurations {
	create("apiSpec")
}

val dependencyVersionMap = mapOf(
//	Keycloak
	"keycloak-admin-client" to "26.0.8",
	"testcontainers-keycloak" to "4.1.1",

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

//	OpenApi
	"springdoc-openapi" to "3.0.2",

//	Currency rate service API specification
	"currency-rate-service-api-specification" to "1.0.7-dev",

//	Person service API specification
	"person-service-api-specification" to "1.0.4-dev",

//	Transaction service API specification
	"transaction-service-api-specification" to "1.0.3-dev",

//	Wallet service API specification
	"wallet-service-api-specification" to "1.0.1-dev"
)

dependencies {
//	Keycloak
	implementation("org.keycloak:keycloak-admin-client:${dependencyVersionMap.getValue("keycloak-admin-client")}")

//	Spring common
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

//	Spring webflux
	implementation("org.springframework.boot:spring-boot-starter-webflux")

//	Spring security
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

//	Test
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers:${dependencyVersionMap.getValue("testcontainers")}")
	testImplementation("org.testcontainers:testcontainers-grafana")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("testcontainers-keycloak")}")
	testImplementation("org.wiremock.integrations:wiremock-spring-boot:${dependencyVersionMap.getValue("wiremock-spring-boot")}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

//	OpenApi
	implementation("tools.jackson.core:jackson-core")
	implementation("tools.jackson.core:jackson-databind")
	implementation("com.fasterxml.jackson.core:jackson-annotations")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${dependencyVersionMap.getValue("springdoc-openapi")}")

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

//	Currency rate service api
	"apiSpec"("aq.payment-system:currency-rate-service-api-specification:${dependencyVersionMap.getValue("currency-rate-service-api-specification")}")

//	Person service api
	"apiSpec"("aq.payment-system:person-service-api-specification:${dependencyVersionMap.getValue("person-service-api-specification")}")

//	Transaction service api
	"apiSpec"("aq.payment-system:transaction-service-api-specification:${dependencyVersionMap.getValue("transaction-service-api-specification")}")

//	Wallet service api
	"apiSpec"("aq.payment-system:wallet-service-api-specification:${dependencyVersionMap.getValue("wallet-service-api-specification")}")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("compileJava") {
	dependsOn("genApi")
}

tasks.register("genApi") {
	dependsOn(genApiContractTask)
	dependsOn("genApiControllers")
}

val openApiDir = File("./openapi")
val openApiYamlSpecFiles = openApiDir.listFiles { spec -> spec.name.endsWith(yamlExtension) } ?: emptyArray<File>()

val genApiContractTask = openApiYamlSpecFiles.map { file ->
	val serviceName = "${file.name.substring(0, file.name.lastIndexOf("service"))}service"
	val serviceNameParts = serviceName.split("-").map { it.capitalized() }
	val genApiClientTask = "gen${serviceNameParts.joinToString(separator = "", transform = { it })}Client"
	val clientPackage = serviceNameParts.joinToString(separator = "_", transform = { it.lowercase() })
	tasks.register<GenerateTask>(genApiClientTask) {
		dependsOn("fetchExternalApi")
		inputSpec.set("$rootDir/openapi/${file.name}")  // Источник спецификации
		outputDir.set("$openApiSpecificationBuildPath/$serviceName") // Путь куда генерировать исходники
		ignoreFileOverride.set(openApiIgnore) // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
		generatorName.set("spring") // Использовать генератор java для создания исходников на этом языке
		library.set("spring-http-interface") // Без явного указания библиотеки генератор Java (выше) настроен на работу с okhttp-gson, по этой причине инструкция serializationLibrary работать не будет (игнорируется) и все DTO начинают использовать библиотеку gson для JSON! Эта инструкция явно указывает использование нужной библиотеки API, которая использует Jackson для JSON
		invokerPackage.set("aq.project") // Устанавливает название корневого пакета для клиентов, dto и других сгенерированных артефактов
		apiPackage.set("aq.project.$clientPackage") // Название пакета api/controllers
		modelPackage.set("aq.project.dto") // Название пакета модели
		apiNameSuffix.set("ApiClient")
		configOptions.set(mapOf(
			"useBeanValidation" to "true", // Использовать JSR валидацию
			"useJakartaEe" to "true", // Использовать Jakarta EE в Spring
			"sourceFolder" to "src/main/java", // Source папка для сгенерированного кода
			"hideGenerationTimestamp" to "true", // Убрать из сгенерированных исходников отметку времени
			"dateLibrary" to "java8", // Использовать современную модель даты и времени в Java
			"generateApiTests" to "false", // Не генерировать тесты для API
			"generateApiDocumentation" to "false", // Не генерировать документацию для API
			"interfaceOnly" to "true", // Генерация только интерфейсов контроллеров по спецификации в .yaml файле
			"generateApis" to "false",
			"generateSupportingFiles" to "false",
			"generateModels" to "true",
			"serializationLibrary" to "jackson",
			"openApiNullable" to "false",
			"useTags" to "true",
			"includeHttpRequestContext" to "false",
			"reactive" to "true"
		))
	}
}

tasks.register<GenerateTask>("genApiControllers") {
	dependsOn("fetchExternalApi")
	inputSpec.set(openApiSpecificationYamlPath) // Источник спецификации
	outputDir.set("$openApiSpecificationBuildPath/$artifact") // Путь куда генерировать исходники
	ignoreFileOverride.set(openApiIgnore) // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
	generatorName.set("spring") // Использовать генератор Java для создания исходников на этом языке
	library.set("spring-boot") // Без явного указания библиотеки генератор Java (выше) настроен на работу с okhttp-gson, по этой причине инструкция serializationLibrary работать не будет (игнорируется) и все DTO начинают использовать библиотеку gson для JSON! Эта инструкция явно указывает использование нужной библиотеки API, которая использует Jackson для JSON
	invokerPackage.set("aq.project") // Устанавливает название корневого пакета для клиентов, dto и других сгенерированных артефактов
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
		"useTags" to "true",
		"reactive" to "true"
	))
}

tasks.register("purgeExternalApi") {
	val dir = File("./openapi")
	for(file in dir.listFiles()) {
		if(file.name.endsWith(yamlExtension) && !file.name.startsWith(artifact)) {
			file.delete()
		}
	}
}

tasks.register<Copy>("fetchExternalApi") {
	dependsOn("purgeExternalApi")
	from(configurations["apiSpec"]
		.map { apiSpecification -> zipTree(apiSpecification)
			.matching { include("*$yamlExtension") } })
	into("$rootDir/openapi")
}