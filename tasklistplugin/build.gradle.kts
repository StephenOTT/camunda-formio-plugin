val kotlinVersion: String by project
val spekVersion: String by project
val camundaVersion: String by project

plugins {
    application
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
}


dependencies {
    compileOnly(platform("org.camunda.bpm:camunda-bom:$camundaVersion"))
    compileOnly("org.camunda.bpm:camunda-engine")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("org.camunda.bpm:camunda-engine-plugin-spin")
    implementation("org.camunda.spin:camunda-spin-dataformat-json-jackson")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

    compileOnly("org.camunda.bpm.webapp:camunda-webapp:$camundaVersion:classes")
    compileOnly("javax:javaee-api:6.0")

    // Used for parsing URIs @TODO review for better usage
    implementation("org.apache.httpcomponents:fluent-hc:4.5.12")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}