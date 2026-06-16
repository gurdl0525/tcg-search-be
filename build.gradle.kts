plugins {
    // Java
    kotlin("jvm") version "2.3.21"
    kotlin("kapt") version "2.3.21"

    // Spring
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"

    // jpa
    kotlin("plugin.jpa") version "2.3.21"

    // Docs
    id("org.jetbrains.dokka") version "2.2.0"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "TCG search backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator:4.1.0")

    // validator
    implementation("org.springframework.boot:spring-boot-starter-validation:4.1.0")

    // spring
    implementation("org.springframework.boot:spring-boot-starter-webmvc:4.1.0")

    // security
    implementation("org.springframework.boot:spring-boot-starter-security:4.1.0")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.21")

    // jackson
    implementation("tools.jackson.module:jackson-module-kotlin:3.1.4")

    // database
    runtimeOnly("org.postgresql:postgresql:42.7.11")

    // ORM
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:4.1.0")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("jakarta.annotation:jakarta.annotation-api")

    // query DSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")

    // migration
    implementation("org.springframework.boot:spring-boot-starter-flyway:4.1.0")
    implementation("org.flywaydb:flyway-database-postgresql:12.4.0")

    // documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")


    // tests
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:4.1.0")
    testImplementation("org.springframework.security:spring-security-test:7.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.3.21")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("javax.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
