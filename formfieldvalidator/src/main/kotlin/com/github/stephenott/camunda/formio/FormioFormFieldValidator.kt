package com.github.stephenott.camunda.formio

import org.apache.http.client.utils.URLEncodedUtils
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.impl.cfg.CompositeProcessEnginePlugin
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.form.handler.DefaultStartFormHandler
import org.camunda.bpm.engine.impl.form.handler.DelegateStartFormHandler
import org.camunda.bpm.engine.impl.form.validator.FormFieldConfigurationException
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.model.bpmn.impl.instance.StartEventImpl
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode

class FormioFormFieldValidator : FormFieldValidator {

    override fun validate(submittedValue: Any?, validatorContext: FormFieldValidatorContext): Boolean {
        val submission: SpinJsonNode = kotlin.runCatching {
            validatorContext.submittedValues.values.single { it is SpinJsonNode } as SpinJsonNode
        }.getOrElse {
            throw FormFieldConfigurationException(it.message, "Unable to find a JSON variable in the Tasklist submission", it)
        }

        val formKey = getFormKey(validatorContext.variableScope)

        if (formKey.isNullOrBlank()){
            throw FormFieldConfigurationException("Form Key was blank but Formio Server Validation is enabled on form")
        }

        val formKeyParams = kotlin.runCatching {
            val parsed = URLEncodedUtils.parse(formKey.substringAfter("?"), Charsets.UTF_8)
            parsed.associateBy({ it.name }, { it.value })

        }.getOrElse {
            throw FormFieldConfigurationException("formKey: ${formKey}", "Unable to parse/understand provided formKey parameters", it)
        }

        val validationResponse: SpinJsonNode = if (formKeyParams.containsKey("deployment")) {
            val fileName = formKeyParams["deployment"]

            if (fileName.isNullOrEmpty()) {
                throw FormFieldConfigurationException(formKey, "Invalid 'deployment' param provided.")
            }

            val deploymentId = getDeploymentId(validatorContext.variableScope)
            val formSchema = Spin.JSON(Context.getCommandContext().deploymentManager.findDeploymentById(deploymentId).getResource(fileName).bytes.toString(Charsets.UTF_8))

            val plugin = kotlin.runCatching {
                // Check if is in springboot
                (Context.getProcessEngineConfiguration().processEnginePlugins.first { it is CompositeProcessEnginePlugin } as CompositeProcessEnginePlugin).plugins
                        .single { it is FormioFormFieldValidatorProcessEnginePlugin } as FormioFormFieldValidatorProcessEnginePlugin

            }.getOrElse {
                kotlin.runCatching {
                    // check in normal spot.
                    (Context.getProcessEngineConfiguration().processEnginePlugins.single { it is FormioFormFieldValidatorProcessEnginePlugin } as FormioFormFieldValidatorProcessEnginePlugin)
                }.getOrElse {
                    throw FormFieldConfigurationException("Formio Validation Plugin", "Unable to find Formio Validation Plugin.")
                }
            }

            plugin.validationHandler!!.validate(formSchema, submission)

        } else {
            throw FormFieldConfigurationException("Unsupported parameter. Only Deployment parameter is currently supported for Formio server validation", "Unsupported parameter. Only Deployment parameter is currently supported for Formio server validation")
        }

        submission.prop("data", validationResponse.prop("processed_submission"))

        return true
    }

    private fun getDeploymentId(variableScope: VariableScope): String {
        return when (variableScope) {
            is TaskEntity -> {
                variableScope.processDefinition.deploymentId
            }
            is ExecutionEntity -> {
                variableScope.processDefinition.deploymentId
            }
            else -> {
                throw FormFieldConfigurationException("Could not get deployment id for formio server validator.")
            }
        }
    }

    private fun getFormKey(variableScope: VariableScope): String? {
        return when (variableScope) {
            is TaskEntity -> {
                variableScope.initializeFormKey()
                variableScope.formKey
            }
            //Start Event Scenario
            is ExecutionEntity -> {
                 if (variableScope.bpmnModelElementInstance is StartEventImpl) {
                    // Start Event Form keys cannot be expressions and therefore do not need to be initialized
//                    (variableScope.bpmnModelElementInstance as StartEventImpl).camundaFormKey // @TODO This does not work... why??
                     ((variableScope.processDefinition.startFormHandler as DelegateStartFormHandler).formHandler as DefaultStartFormHandler).formKey.expressionText
                } else {
                    throw IllegalArgumentException("Received a unexpected (${variableScope.bpmnModelElementInstance::class.qualifiedName}) model instance for Formio Server Validation")
                }
            }
            else -> {
                throw IllegalStateException("Did not receive a expected variable scope in Formio Server Validator.")
            }
        }
    }

    private fun getProcessInstanceId(variableScope: VariableScope): String {
        return when (variableScope) {
            is TaskEntity -> {
                variableScope.processInstanceId
            }
            // Start Event Scenario
            is ExecutionEntity -> {
                variableScope.processInstanceId
            }
            else -> {
                throw IllegalStateException("Did not receive a expected variable scope in Formio Server Validator.")
            }
        }
    }
}