val kotlinVersion: String by project
val spekVersion: String by project

plugins {
    application
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlin.plugin.spring")
}


dependencies {
    implementation(enforcedPlatform("org.springframework.boot:spring-boot-dependencies:2.2.5.RELEASE"))
    implementation(enforcedPlatform("org.camunda.bpm:camunda-bom:7.13.0"))

    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter")

    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp")
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest")

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

    implementation(project(":formfieldvalidator"))
    implementation(project(":tasklistplugin"))
}

application {
    mainClassName = "com.github.stephenott.camunda.formio.Application"
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<ProcessResources> {
    // Setup of the formio customizations into the required locations within the springboot app
    from("../common/tasklist"){
        into("META-INF/resources/webjars/camunda/app/tasklist")
    }
    from("../common/forms"){
        into("public/forms")
    }
}

tasks.withType<ProcessResources> {
    // Setup of the formio customizations into the required locations within the springboot app
    from("../bpmn"){
        into("public/forms")
    }
}