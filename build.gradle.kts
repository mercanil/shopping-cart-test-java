plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")
    id("org.sonarqube") version "6.3.1.5724"
}

sonar {
    properties {
        property("sonar.projectKey", "mercanil_shopping-cart-test-java")
        property("sonar.organization", "mercanil")
    }
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
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.vavr:vavr:0.10.4")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.withType<Test>())

    reports {
        xml.required.set(true)
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform {
        includeTags("integration")
    }
}