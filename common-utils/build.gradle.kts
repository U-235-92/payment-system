import org.gradle.kotlin.dsl.invoke

plugins {
    java
    id("maven-publish")
}

group = "aq.payment-system"
version = "3.0.0"

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

sourceSets {
    main {
        java {
            srcDir("$rootDir/src/main/java")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
//    Micrometer
    implementation("io.micrometer:micrometer-core:1.18.0-RC1")

//    Lombok
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

//    Slf4j
    implementation("org.slf4j:slf4j-api:2.0.20")

//    OpenTelemetry
    implementation("io.opentelemetry:opentelemetry-api:1.67.0")

//    Spring AOP
    implementation("org.springframework:spring-aspects:7.0.10")

//    Spring Web
    implementation("org.springframework:spring-web:7.0.10")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            groupId = group.toString()
            version = project.version.toString()
            artifact(tasks.named("jar"))
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

tasks.test {
    useJUnitPlatform()
}