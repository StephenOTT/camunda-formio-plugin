package com.github.stephenott.camunda.formio

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

open class FormioFieldValidatorProcessEnginePlugin(
        private val formioUrl: String,
        private val formioProviderClass: FormioProvider
): ProcessEnginePlugin {

    companion object {
        var formioUrl: String = "http://localhost:8081"
        var formioProvider: FormioProvider = SimpleFormioProvider()
    }

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customFormFieldValidators == null){
            processEngineConfiguration.customFormFieldValidators = mapOf()
        }
        processEngineConfiguration.customFormFieldValidators["formio"] = FormioFieldValidator::class.java

        FormioFieldValidatorProcessEnginePlugin.formioUrl = this.formioUrl
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}