package com.github.stephenott.camunda.formio

import org.camunda.bpm.engine.BpmnParseException
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.form.handler.*
import org.camunda.bpm.engine.impl.form.type.StringFormType
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl
import org.camunda.bpm.engine.impl.util.xml.Element
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class FormioParseListener(
        val FORMIO_PROPERTY_PREFIX: String = "formio_",
        val FORMIO_DEPLOYMENT_KEY: String = "deployment",
        val FORMIO_PATH_KEY: String = "path",
        val FORMIO_TRANSIENT_KEY: String = "transient",
        val FORMIO_VAR_KEY: String = "var",
        val FORMIO_BASE_FORM_KEY: String = "embedded:/forms/formio.html",
        val FORMIO_SERVER_VALIDATION_KEY: String = "validation",
        val FORM_KEY_PATH: String = "http://camunda.org/schema/1.0/bpmn:formKey",
) : AbstractBpmnParseListener() {

    protected fun buildFormKeyFromExtensionProperties(element: Element, activity: ActivityImpl): String? {
        val props: Map<String, String>? = element.element("extensionElements")
                ?.element("properties")
                ?.elements("property")
                ?.filter { it.attribute("name").startsWith(FORMIO_PROPERTY_PREFIX) }
                ?.associateBy(
                        { it.attribute("name") },
                        { it.attribute("value") }
                )
                ?.mapKeys {
                    it.key.substringAfter(FORMIO_PROPERTY_PREFIX)
                }

        if (!props.isNullOrEmpty()){

            var formKey: String = "${FORMIO_BASE_FORM_KEY}?"

            formKey = if (props.containsKey(FORMIO_DEPLOYMENT_KEY) && props.getValue(FORMIO_DEPLOYMENT_KEY).isNotBlank()){
                val value = props[FORMIO_DEPLOYMENT_KEY]
                formKey.plus("${FORMIO_DEPLOYMENT_KEY}=${value}")

            } else if (props.containsKey(FORMIO_PATH_KEY) && props.getValue(FORMIO_VAR_KEY).isNotBlank()){
                val value = props[FORMIO_PATH_KEY]
                formKey.plus("${FORMIO_PATH_KEY}=${value}")
            } else {
                throw BpmnParseException("Formio Extension property configuration must have a deployment or a path property. (${activity.activityId})", element)
            }

            if (props.containsKey(FORMIO_VAR_KEY) && props.getValue(FORMIO_VAR_KEY).isNotBlank()){
                val value = props[FORMIO_VAR_KEY]
                formKey = formKey.plus("&${FORMIO_VAR_KEY}=${value}")
            }

            if (props.containsKey(FORMIO_TRANSIENT_KEY)){
                val value = props[FORMIO_TRANSIENT_KEY]?: "false"
                formKey = formKey.plus("&${FORMIO_TRANSIENT_KEY}=${value}")
            }

            return formKey

        } else {
            return null
        }
    }

    protected fun hasFormioServerValidation(element: Element): Boolean{
        val props: Map<String, String>? = element.element("extensionElements")
                ?.element("properties")
                ?.elements("property")
                ?.filter { it.attribute("name").startsWith(FORMIO_PROPERTY_PREFIX) }
                ?.associateBy(
                        { it.attribute("name") },
                        { it.attribute("value") }
                )
                ?.mapKeys {
                    it.key.substringAfter(FORMIO_PROPERTY_PREFIX)
                }

        return if (!props.isNullOrEmpty()){
            props[FORMIO_SERVER_VALIDATION_KEY].toBoolean()
        } else {
            false
        }
    }

    protected fun createFormioServerValidation(handler: DefaultFormHandler){
        val prop = handler::class.memberProperties.single { it.name == "formFieldHandlers" }.also { it.isAccessible = true } as KMutableProperty1<DefaultFormHandler, ArrayList<FormFieldHandler>>

        val handlersList = prop.get(handler)

        val ffH = FormFieldHandler().apply {
            id = "formio"
            setType(StringFormType())
            validationHandlers = listOf(FormFieldValidationConstraintHandler().apply {
                name = "formio"
                validator = FormioFormFieldValidator()
            })
        }
        handlersList.add(ffH)
    }


    override fun parseUserTask(userTaskElement: Element, scope: ScopeImpl, activity: ActivityImpl) {
        val formKey = buildFormKeyFromExtensionProperties(userTaskElement, activity)
        if (activity.activityBehavior is UserTaskActivityBehavior){
            if (formKey != null){
                val exp = Context.getProcessEngineConfiguration().expressionManager.createExpression(formKey)
                (activity.activityBehavior as UserTaskActivityBehavior).taskDefinition.formKey = exp

                if (hasFormioServerValidation(userTaskElement)){
                    createFormioServerValidation(((activity.activityBehavior as UserTaskActivityBehavior).taskDefinition.taskFormHandler as DelegateTaskFormHandler).formHandler as DefaultFormHandler)
                }
            }
        }
    }

    protected fun modifyStartEventFormKey(formKey: String, handler: DefaultStartFormHandler){
        val formKeyExp = Context.getProcessEngineConfiguration().expressionManager.createExpression(formKey)
        val formKeyProp = handler::class.memberProperties.single { it.name == "formKey" }.also { it.isAccessible = true } as KMutableProperty1<DefaultStartFormHandler, Expression>
        formKeyProp.set(handler, formKeyExp)
    }

    override fun parseStartEvent(startEventElement: Element, scope: ScopeImpl, startEventActivity: ActivityImpl) {
        if (startEventActivity.properties.toMap().getValue("type") == "startEvent" && scope is ProcessDefinitionEntity){
            val formKey = buildFormKeyFromExtensionProperties(startEventElement, startEventActivity)

            if (formKey != null){
                if (scope.startFormHandler is DelegateStartFormHandler){
                    if ((scope.startFormHandler as DelegateStartFormHandler).formHandler is DefaultStartFormHandler){
                        modifyStartEventFormKey(formKey, (scope.startFormHandler as DelegateStartFormHandler).formHandler as DefaultStartFormHandler)

                        if (hasFormioServerValidation(startEventElement)){
                            createFormioServerValidation((scope.startFormHandler as DelegateStartFormHandler).formHandler as DefaultFormHandler)
                        }
                    }
                }
            }
        }
    }
}