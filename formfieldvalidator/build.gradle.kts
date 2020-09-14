import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlinVersion: String by project
val spekVersion: String by project

plugins {
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
}

dependencies {

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation(enforcedPlatform("org.camunda.bpm:camunda-bom:7.13.0"))
    implementation("org.camunda.bpm:camunda-engine")

    implementation("org.camunda.bpm:camunda-engine-plugin-spin")
    implementation("org.camunda.spin:camunda-spin-dataformat-json-jackson")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.3")

    implementation("org.apache.httpcomponents:fluent-hc:4.5.12")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

/**
 * Generate a shadowjar specific for server plugin usage (contains extra deps)
 */
tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()

    archiveClassifier.set("server-plugin")

    from(sourceSets.main.get().output)

    dependencies {
        exclude {
            (it.moduleGroup != "commons-logging")
                    && (it.moduleGroup != "org.jetbrains.kotlin")
                    && (it.moduleGroup != "org.apache.httpcomponents" && it.moduleName != "fluent-hc")
                    && (it.moduleGroup != "org.apache.httpcomponents" && it.moduleName != "httpclient")
        }
    }
}

tasks.create<Copy>("copyShadowJarToDockerFiles"){
    from("$buildDir/libs/")
    include("*-server-plugin.jar")
    into("../docker")
}