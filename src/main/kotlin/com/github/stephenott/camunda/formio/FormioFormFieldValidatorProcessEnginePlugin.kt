package com.github.stephenott.camunda.formio

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

/**
 * Plugin for managing Server-side validations for Formio form submissions
 */
open class FormioFormFieldValidatorProcessEnginePlugin(
        val formioUrl: String = "http://localhost:8081/validate",
        val validationTimeout: Int = 10000,
        val validationHandler: FormioValidationHandler = SimpleFormioValidationHandler(formioUrl, validationTimeout)
) : ProcessEnginePlugin {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customFormFieldValidators == null) {
            processEngineConfiguration.customFormFieldValidators = mapOf()
        }
        processEngineConfiguration.customFormFieldValidators["formio"] = FormioFormFieldValidator::class.java
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}