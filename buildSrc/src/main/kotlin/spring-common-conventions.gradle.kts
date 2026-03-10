plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val dependencyVersionMap = mapOf(
    "logstash-encoder" to "9.0",
    "spring-boot-starter-aop" to "4.0.0-M2",
    "testcontainers" to "2.0.3",
    "opentelemetry-annotations" to "2.25.0"
)

dependencies {
//    Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:${dependencyVersionMap.getValue("opentelemetry-annotations")}")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("net.logstash.logback:logstash-logback-encoder:${dependencyVersionMap.getValue("logstash-encoder")}")

//    Util
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

//    Common spring
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop:${dependencyVersionMap.getValue("spring-boot-starter-aop")}")

//    Test
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:${dependencyVersionMap.getValue("testcontainers")}")
    testImplementation("org.testcontainers:testcontainers-grafana")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}