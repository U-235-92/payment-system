import gradle.kotlin.dsl.accessors._0430f2283e40a71ba65f409311bb6762.openApiGenerate
import gradle.kotlin.dsl.accessors._d7c1cb8291fcf7e869bfba85a0dc6ae2.implementation

plugins {
    id("org.openapi.generator")
}

val dependencyVersionMap = mapOf(
    "feign-core" to "13.6",
    "feign-jackson" to "13.6",
    "feign-slf4j" to "13.6",
    "feign-form-spring" to "3.8.0",
    "jakarta-annotation" to "3.0.0",
    "jakarta-validation" to "3.1.1"
)

dependencies {
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
}

openApiGenerate {
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

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}