plugins {
    id("org.openapi.generator")
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