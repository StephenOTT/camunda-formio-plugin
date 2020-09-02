package com.github.stephenott.camunda.formio

import org.apache.http.HttpStatus
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.form.validator.FormFieldConfigurationException
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode

class SimpleFormioProvider: FormioProvider {
    override fun validate(schema: SpinJsonNode, submission: SpinJsonNode): SpinJsonNode {
        // --> CLEAN private validations: https://github.com/formio/formio/blob/d0c08a27482a0ed67612616a7017b77dfa5d6738/src/resources/FormResource.js#L25
        val url = FormioFieldValidatorProcessEnginePlugin.formioUrl

        val requestBody: SpinJsonNode = SpinJsonNode.JSON("{}").prop("schema", schema).prop("submission", submission)
        requestBody.prop("submission").prop("data").prop("firstName", "steve")

        val response = kotlin.runCatching {
            Request.Post(url).body(StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON))
                    .socketTimeout(10000)
                    .connectTimeout(10000)
                    .execute().returnResponse()

        }.getOrElse {
            throw FormFieldValidationException("Request Failure!!", "Could not validate with Formio Server.", it)
        }

        val responseBody: String = response.entity.content.bufferedReader(Charsets.UTF_8).use { it.readText() }

        if (response.statusLine.statusCode == HttpStatus.SC_ACCEPTED){
            return Spin.JSON(responseBody)
        } else {
            // FormFieldConfiguration needed to be used because it is the only exception that returned a proper response with a body...
            throw FormFieldConfigurationException("Submission Validation returned false", responseBody)
//            throw FormFieldValidationException("detail!!", responseBody)
        }
    }
}