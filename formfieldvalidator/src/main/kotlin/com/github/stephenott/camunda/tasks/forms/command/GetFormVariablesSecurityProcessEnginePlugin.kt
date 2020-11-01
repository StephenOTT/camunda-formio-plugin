package com.github.stephenott.camunda.tasks.forms.command

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.FormServiceImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.variable.VariableMap

/**
 * Plugin for replacing the GetTaskFormVariablesCmd with CustomGetTaskFormVariablesCmd.
 * The replacement provides additional security filtering based on Camunda Extension Properties configurations in the bpmn
 */
open class GetFormVariablesSecurityProcessEnginePlugin : ProcessEnginePlugin {

    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
        // Setup a new instance of form service that has the cmd for security filtering of variables
        // @TODO add startup info about form service being overriden and security added to remove variables.
        processEngineConfiguration.formService = object: FormServiceImpl() {
            override fun getTaskFormVariables(taskId: String, formVariables: MutableCollection<String>?, deserializeObjectValues: Boolean): VariableMap {
                return commandExecutor.execute(CustomGetTaskFormVariablesCmd(taskId, formVariables, deserializeObjectValues))
            }
        }
    }

    override fun postInit(processEngineConfiguration: ProcessEngineConfigurationImpl) {
    }

    override fun postProcessEngineBuild(processEngine: ProcessEngine) {
    }
}