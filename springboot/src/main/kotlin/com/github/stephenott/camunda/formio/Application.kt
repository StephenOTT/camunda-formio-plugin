package com.github.stephenott.camunda.formio

import com.github.stephenott.camunda.tasks.forms.command.GetFormVariablesSecurityProcessEnginePlugin
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableProcessApplication
class Application

fun main(args: Array<String>) = runApplication<Application>(*args).let { Unit }

@Component
class MyPlugin: FormioFormFieldValidatorProcessEnginePlugin()

@Component
class MyFormsSecurityPlugin: GetFormVariablesSecurityProcessEnginePlugin()

@Component
class MyFormioConfigParser: FormioParseListenerProcessEnginePlugin()