package com.github.stephenott.camunda.rest.formio.forms

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.utils.URLEncodedUtils
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.exception.NotFoundException
import org.camunda.bpm.engine.exception.NullValueException
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.rest.exception.InvalidRequestException
import org.camunda.bpm.engine.rest.impl.AbstractRestProcessEngineAware
import java.lang.IllegalArgumentException
import javax.ws.rs.core.Response

class FormioFormRestUtils {
    companion object {
        fun getSchemaResponse(processEngine: ProcessEngine, deploymentId: String, formKey: String, objectMapper: ObjectMapper): Response {
            try {
                val formKeyParams = kotlin.runCatching {
                    val parsed = URLEncodedUtils.parse(formKey.substringAfter("?"), Charsets.UTF_8)
                    parsed.associateBy({ it.name }, { it.value })

                }.getOrElse {
                    throw InvalidRequestException(Response.Status.BAD_REQUEST, "Unable to parse formKey: ${it.message}")
                }

                return if (formKeyParams.containsKey("deployment")) {
                    val fileName = formKeyParams["deployment"]

                    if (fileName.isNullOrEmpty()) {
                        throw InvalidRequestException(Response.Status.BAD_REQUEST, "Formio deployment key cannot be empty.")
                    }

                    val schema = kotlin.runCatching {
                        processEngine.repositoryService.getDeploymentResources(deploymentId).single {
                            it.name == fileName
                            //@TODO should have check for validating that it was a `.json` file?
                        }.bytes
                    }.getOrElse {
                        throw IllegalArgumentException("Formio Deployment key/file could not be found in deployment.")
                    }

                    //@TODO get sub forms and put them into the JsonNode

                    val schemas = mapOf(
                            Pair("parent", objectMapper.readTree(schema)),
                            Pair("subForms", listOf<Map<String, JsonNode>>()) //@TODO
                    )

                    Response.ok(objectMapper.writeValueAsString(schemas)).build()
                } else {
                    Response.ok(objectMapper.writeValueAsString(listOf<Pair<String, JsonNode>>())).build()
                }

            } catch (e: NotFoundException) {
                throw InvalidRequestException(Response.Status.NOT_FOUND, e.message)
            } catch (e: NullValueException) {
                throw InvalidRequestException(Response.Status.BAD_REQUEST, e.message)
            } catch (e: AuthorizationException) {
                throw InvalidRequestException(Response.Status.FORBIDDEN, e.message)
            } catch (e: BadUserRequestException) {
                throw InvalidRequestException(Response.Status.BAD_REQUEST, e.message)
            }
        }


        fun getEngine(engineName: String?, mapper: ObjectMapper, relRoot: String): ProcessEngine {
            return object : AbstractRestProcessEngineAware(engineName, mapper) {
                val engine = processEngine
            }.also {
                it.setRelativeRootResourceUri(relRoot)
            }.engine
        }

        fun checkStartFormAuthz(engine: ProcessEngine, processDefinition: ProcessDefinitionEntity) {
            if ((engine.processEngineConfiguration as ProcessEngineConfigurationImpl).isAuthorizationEnabled) {
//            val processEngineConfiguration: ProcessEngineConfigurationImpl = engine.processEngineConfiguration as ProcessEngineConfigurationImpl
//            val deploymentCache = processEngineConfiguration.deploymentCache
//            val processDefinition = deploymentCache.findDeployedProcessDefinitionById(processDefinitionId)
                kotlin.runCatching {
                    for (checker in (engine.processEngineConfiguration as ProcessEngineConfigurationImpl).commandCheckers) {
                        checker.checkReadProcessDefinition(processDefinition)
                    }
                }.onFailure {
                    if (it is AuthorizationException) {
                        throw InvalidRequestException(Response.Status.FORBIDDEN, it.message)
                    } else {
                        throw it
                    }
                }
            }
        }
    }
}