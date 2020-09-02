package com.github.stephenott.camunda.formio

import org.camunda.spin.json.SpinJsonNode

interface FormioProvider {
    fun validate(schema: SpinJsonNode, submission: SpinJsonNode): SpinJsonNode
}