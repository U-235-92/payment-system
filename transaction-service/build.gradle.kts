import org.gradle.internal.extensions.stdlib.capitalized
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

val artifact = "transaction-service"

val yamlExtension = ".yaml"

val specificationArtifactVersion = "1.0.3-dev"
val specificationArtifactJarName = "${artifact}-api-specification-${specificationArtifactVersion}.jar"

val openApiSpecificationYamlPath = "$rootDir/openapi/${artifact}-api-specification.yaml"
val openApiSpecificationBuildPath = "$rootDir/build/generated/openapi"
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

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
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

sourceSets { // Источники исходников для проекта
	main {
		java {
			srcDir("${rootDir}/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/transaction-service/src/main/java")
			srcDir("${openApiSpecificationBuildPath}/wallet-service/src/main/java")
		}
	}
}

configurations {
	create("apiSpec")
}

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
	"testcontainers-keycloak" to "4.1.1",

//	Mapping
	"mapstruct" to "1.6.3",

//	OpenApi
	"springdoc-openapi" to "3.0.2",

//	Transaction service API specification
	"wallet-service-api-specification" to "1.0.1-dev"
)

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
	testImplementation("com.github.dasniko:testcontainers-keycloak:${dependencyVersionMap.getValue("testcontainers-keycloak")}")

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
			"includeHttpRequestContext" to "false"
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
		"useTags" to "true"
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