package com.github.stephenott.camunda

import com.github.stephenott.camunda.formio.FormioFieldValidatorProcessEnginePlugin
import com.github.stephenott.camunda.formio.SimpleFormioProvider
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableProcessApplication
class Application

fun main(args: Array<String>) = runApplication<Application>(*args).let { Unit }

@Component
class MyPlugin: FormioFieldValidatorProcessEnginePlugin("http://localhost:8081/validate", SimpleFormioProvider())