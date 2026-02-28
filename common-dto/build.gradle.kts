plugins {
    java
    id("open-api-conventions")
    id("java-common-conventions")
}

group = "aq.project"
version = "1.0.0"

sourceSets { // Источники исходников для проекта
    main {
        java {
            srcDirs("${rootDir}/common-dto/src/main/java",
                "${rootDir}/common-dto/build/generated-sources/openapi/src/main/java")
        }
    }
}

openApiGenerate {
    inputSpec.set("$rootDir/common-dto/openapi/dto-specification.yaml") // Источник спецификации
    outputDir.set("$rootDir/common-dto/build/generated-sources/openapi") // Путь куда генерировать исходники
    ignoreFileOverride.set("$rootDir/common-dto/openapi/openapi-generator-java-sources.ignore") // Источник, в котором указано, какие файлы следует игнорировать в процессе генерации исходников
}