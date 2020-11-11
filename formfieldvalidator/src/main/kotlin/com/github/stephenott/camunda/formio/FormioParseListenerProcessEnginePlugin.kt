package com.github.stephenott.camunda.formio

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin

open class FormioParseListenerProcessEnginePlugin : ProcessEnginePlugin {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        if (processEngineConfiguration.customPreBPMNParseListeners == null){
            processEngineConfiguration.customPreBPMNParseListeners = mutableListOf()
        }
        processEngineConfiguration.customPreBPMNParseListeners.add(FormioParseListener())
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}