package com.github.stephenott.camunda.formio

import org.apache.http.client.utils.URLEncodedUtils
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.form.validator.FormFieldConfigurationException
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidator
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidatorContext
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode

class FormioFormFieldValidator: FormFieldValidator {
    override fun validate(submittedValue: Any?, validatorContext: FormFieldValidatorContext): Boolean {

        val taskEntity = (validatorContext.variableScope as TaskEntity)

        val submission: SpinJsonNode = kotlin.runCatching {
            validatorContext.submittedValues.values.single { it is SpinJsonNode } as SpinJsonNode

        }.getOrElse {
            throw FormFieldConfigurationException(it.message, "Unable to find a JSON variable in the Tasklist submission", it)
        }

        taskEntity.initializeFormKey()
        val formKey: String = taskEntity.formKey

        val formKeyParams = kotlin.runCatching {
            val parsed = URLEncodedUtils.parse(formKey.substringAfter("?"), Charsets.UTF_8)
            parsed.associateBy({it.name}, {it.value})

        }.getOrElse {
            throw FormFieldConfigurationException("formKey: ${formKey}", "Unable to parse/understand provided formKey parameters", it)
        }

        val validationResponse: SpinJsonNode = if (formKeyParams.containsKey("deployment")){
            val fileName = formKeyParams["deployment"]

            if (fileName.isNullOrEmpty()){
                throw FormFieldConfigurationException(formKey, "Invalid 'deployment' param provided.")
            }

            val deploymentId = Context.getCommandContext().deploymentManager.findDeploymentIdsByProcessInstances(listOf(taskEntity.processInstanceId)).single()
            val formSchema = Spin.JSON(Context.getCommandContext().deploymentManager.findDeploymentById(deploymentId).getResource(fileName).bytes.toString(Charsets.UTF_8))

            val plugin = kotlin.runCatching {
                Context.getProcessEngineConfiguration().processEnginePlugins.single { it is FormioFormFieldValidatorProcessEnginePlugin } as FormioFormFieldValidatorProcessEnginePlugin

            }.getOrElse {
                throw FormFieldConfigurationException("Formio Validation Plugin", "Unable to find Formio Validation Plugin.")
            }

            plugin.validationHandler.validate(formSchema, submission)

        } else {
            throw FormFieldConfigurationException("Unsupported parameter. Only Deployment parameter is currently supported for Formio server validation", "Unsupported parameter. Only Deployment parameter is currently supported for Formio server validation")
        }

        submission.prop("data", validationResponse.prop("processed_submission"))

        return true
    }
}