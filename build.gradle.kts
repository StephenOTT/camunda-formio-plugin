
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val developmentOnly: Configuration by configurations.creating
val kotlinVersion: String by project
val spekVersion: String by project

plugins {
    val kotlinVersion = "1.3.50"
    application
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.71"
    id("idea")
    id("org.jetbrains.kotlin.plugin.spring") version "1.3.72"
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

version = "0.1"
//group = "camunda-formio-plugin"

repositories {
    mavenCentral()
    jcenter()
}

configurations {
    developmentOnly
}

dependencies {
    testImplementation(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:2.2.5.RELEASE"))
    implementation(enforcedPlatform("org.camunda.bpm:camunda-bom:7.13.0"))

    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter")

    testImplementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp")
    testImplementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.camunda.bpm:camunda-engine-plugin-spin")
    implementation("org.camunda.spin:camunda-spin-dataformat-json-jackson")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

    runtimeOnly("com.h2database:h2:1.4.200")

    implementation("org.apache.httpcomponents:fluent-hc:4.5.12")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

application {
    mainClassName = "com.github.stephenott.camunda.formio"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            javaParameters = true
        }
    }

    withType<Test> {
        classpath = classpath.plus(configurations["developmentOnly"])
        useJUnitPlatform()
    }

    named<JavaExec>("run") {
        doFirst {
            jvmArgs = listOf("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote")
            classpath = classpath.plus(configurations["developmentOnly"])
        }
    }

    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}