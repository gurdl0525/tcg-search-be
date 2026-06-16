plugins {
    // Java
	kotlin("jvm") version "2.3.21"

    // Spring
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"

    // jpa
	kotlin("plugin.jpa") version "2.3.21"
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
	implementation("org.springframework.boot:spring-boot-starter-actuator")

    // validator
	implementation("org.springframework.boot:spring-boot-starter-validation")

    // spring
	implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // reflection
	implementation("org.jetbrains.kotlin:kotlin-reflect")

    // jackson
	implementation("tools.jackson.module:jackson-module-kotlin")

    // database
	runtimeOnly("org.postgresql:postgresql")

    // ORM
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // migration
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    // documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")


    // tests
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
