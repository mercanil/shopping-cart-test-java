plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")

}

group = "com.siriusxm.example.cart"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Minimal Spring Boot - just for DI and testing
    implementation("org.springframework.boot:spring-boot-starter")

    // JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Functional programming
    implementation("io.vavr:vavr:0.10.4")

    // Testing with Spring
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}