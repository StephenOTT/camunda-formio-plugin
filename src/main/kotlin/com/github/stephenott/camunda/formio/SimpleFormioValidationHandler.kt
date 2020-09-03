package com.github.stephenott.camunda.formio

import org.apache.http.HttpStatus
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.camunda.bpm.engine.impl.form.validator.FormFieldConfigurationException
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode

class SimpleFormioValidationHandler(
        override val validationUrl: String,
        override val validationTimeout: Int
) : FormioValidationHandler {

    override fun validate(schema: SpinJsonNode, submission: SpinJsonNode): SpinJsonNode {

        val requestBody: SpinJsonNode = SpinJsonNode.JSON("{}")
                .prop("schema", schema)
                .prop("submission", submission)

        val response = kotlin.runCatching {
            Request.Post(validationUrl)
                    .body(StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON))
                    .socketTimeout(validationTimeout)
                    .connectTimeout(validationTimeout)
                    .execute()
                    .returnResponse()

        }.getOrElse {
            // FormFieldConfiguration needed to be used because it is the only exception that returned a proper response with a body...
            throw FormFieldConfigurationException("Request Failure!!", "Unable to reach Formio validation server.", it)
        }

        val responseBody: String = response.entity.content.bufferedReader(Charsets.UTF_8).use { it.readText() }

        if (response.statusLine.statusCode == HttpStatus.SC_ACCEPTED) {
            return Spin.JSON(responseBody)

        } else {
            // FormFieldConfiguration needed to be used because it is the only exception that returned a proper response with a body...
            throw FormFieldConfigurationException("Submission Validation returned false", responseBody)
        }
    }
}