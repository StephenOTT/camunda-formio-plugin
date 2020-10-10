package com.github.stephenott.camunda.tasks.forms.command

import org.camunda.bpm.engine.impl.cmd.GetTaskFormVariablesCmd
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties

class CustomGetTaskFormVariablesCmd(taskId: String, variableNames: MutableCollection<String>?, deserializeObjectValues: Boolean) : GetTaskFormVariablesCmd(taskId, variableNames, deserializeObjectValues) {

    companion object {
        var allowedVariablesKey: String = "allowed-variables"
        var restrictedVariablesKey: String = "restricted-variables"
    }

    override fun execute(commandContext: CommandContext): VariableMap {
        val result = super.execute(commandContext)

        //@TODO add debug info about variables being filtered

        val utModelInstance = commandContext.taskManager.findTaskById(resourceId).bpmnModelElementInstance
        val props = utModelInstance.extensionElements.elementsQuery.filterByType<CamundaProperties>(CamundaProperties::class.java).singleResult()
        val allowedVars = props.camundaProperties.find {
            it.camundaName == allowedVariablesKey
        }

        val restrictedVars = props.camundaProperties.find {
            it.camundaName == restrictedVariablesKey
        }

        allowedVars?.let { camProp ->
            val varNames = camProp.camundaValue.split(",").map { it.trim() }
            result.entries.removeIf {
                it.key !in varNames
            }
        }

        restrictedVars?.let { camProp->
            val varNames = camProp.camundaValue.split(",").map { it.trim() }
            result.entries.removeIf {
                it.key in varNames
            }
        }

        return result
    }
}