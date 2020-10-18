package com.github.stephenott.camunda.formio

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

/**
 * Plugin for managing Server-side validations for Formio form submissions
 */
open class FormioFormFieldValidatorProcessEnginePlugin @JvmOverloads constructor(
        var validationUrl: String = "http://localhost:8081/validate",
        var validationTimeout: Int = 10000,
        var validationHandler: FormioValidationHandler? = null
) : ProcessEnginePlugin {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (validationHandler == null){
            validationHandler = SimpleFormioValidationHandler(validationUrl, validationTimeout)
        }

        if (processEngineConfiguration.customFormFieldValidators == null) {
            processEngineConfiguration.customFormFieldValidators = mutableMapOf()
        }
        processEngineConfiguration.customFormFieldValidators["formio"] = FormioFormFieldValidator::class.java
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}