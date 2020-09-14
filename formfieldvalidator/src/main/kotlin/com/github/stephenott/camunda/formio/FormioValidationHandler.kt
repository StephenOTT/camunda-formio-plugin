package com.github.stephenott.camunda.formio

import org.camunda.spin.json.SpinJsonNode

interface FormioValidationHandler {
    val validationUrl: String
    val validationTimeout: Int

    fun validate(schema: SpinJsonNode, submission: SpinJsonNode): SpinJsonNode
}