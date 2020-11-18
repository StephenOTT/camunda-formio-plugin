package com.github.stephenott.camunda.tasklist.plugin

import com.fasterxml.jackson.databind.JsonNode
import org.apache.http.client.utils.URLEncodedUtils
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.BadUserRequestException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.exception.NotFoundException
import org.camunda.bpm.engine.exception.NullValueException
import org.camunda.bpm.engine.rest.exception.InvalidRequestException
import org.camunda.bpm.tasklist.plugin.spi.impl.AbstractTasklistPlugin
import org.camunda.bpm.tasklist.resource.AbstractTasklistPluginResource
import org.camunda.bpm.tasklist.resource.AbstractTasklistPluginRootResource
import org.camunda.spin.Spin
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class FormsEndpointTasklistPlugin : AbstractTasklistPlugin() {
    override fun getId(): String {
        return ID
    }

    override fun getResourceClasses(): Set<Class<*>> {
        val classes: MutableSet<Class<*>> = HashSet()
        classes.add(FormsRootResource::class.java)
        return classes
    }

    companion object {
        const val ID = "forms-plugin"
    }
}

@Path("plugin/" + FormsEndpointTasklistPlugin.ID)
class FormsRootResource : AbstractTasklistPluginRootResource(FormsEndpointTasklistPlugin.ID) {

    //https://localhost:8080/camunda/api/tasklist/plugin/forms-plugin/default/process-definition/myProcId/forms
    @Path("{engineName}/process-definition/{processDefinitionId}/forms")
    fun getStartEventForms(@PathParam("engineName") engineName: String, @PathParam("processDefinitionId") processDefinitionId: String): StartEventFormsResource {
        return subResource(StartEventFormsResource(engineName, processDefinitionId), engineName)
    }

    //https://localhost:8080/camunda/api/tasklist/plugin/forms-plugin/default/task/myTaskId/forms
    @Path("{engineName}/task/{taskId}/forms")
    fun getUserTaskForms(@PathParam("engineName") engineName: String, @PathParam("taskId") taskId: String): UserTaskFormsResource {
        return subResource(UserTaskFormsResource(engineName, taskId), engineName)
    }

}

class StartEventFormsResource(engineName: String, private val processDefinitionId: String) : AbstractTasklistPluginResource(engineName) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getStartEventForms(): Response {
        val definition = kotlin.runCatching {
            processEngine.repositoryService.getProcessDefinition(processDefinitionId)
        }.getOrThrow() //@TODO refactor for better error handling

        if (definition != null) {
            val startFormkey: String? = processEngine.formService.getStartFormKey(definition.id)
            if (startFormkey != null) {
                val response = FormResourceUtils.getSchemaResponse(processEngine, definition.deploymentId, startFormkey)
                return response
            } else {
                throw InvalidRequestException(Response.Status.BAD_REQUEST, "No Form Key was found")
            }
        } else {
            throw InvalidRequestException(Response.Status.BAD_REQUEST, "Could not find or access process def")
        }
    }
}

class UserTaskFormsResource(engineName: String, private val taskId: String) : AbstractTasklistPluginResource(engineName) {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserTaskForms(): Response {

        val userTask = kotlin.runCatching {
            processEngine.taskService.createTaskQuery().taskId(taskId).initializeFormKeys().singleResult()
        }.getOrElse {
            throw InvalidRequestException(Response.Status.NOT_FOUND, "Task does not exist or are not authorized to view task.")
        } //@TODO refactor for better error handling

        val definition = kotlin.runCatching {
            // @TODO If you can view the task, should you need the authorization to view the definition?
            processEngine.repositoryService.getProcessDefinition(userTask.processDefinitionId)
        }.getOrThrow() //@TODO refactor for better error handling

        if (definition != null) {
            val userTaskFormkey: String? = processEngine.formService.getTaskFormKey(userTask.processDefinitionId, userTask.taskDefinitionKey)
            if (userTaskFormkey != null) {
                val response = FormResourceUtils.getSchemaResponse(processEngine, definition.deploymentId, userTaskFormkey)
                return response
            } else {
                throw InvalidRequestException(Response.Status.BAD_REQUEST, "No Form Key was found")
            }
        } else {
            throw InvalidRequestException(Response.Status.BAD_REQUEST, "Could not find or access process definition")
        }
    }
}

object FormResourceUtils {
    fun getSchemaResponse(processEngine: ProcessEngine, deploymentId: String, formKey: String): Response {
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
                    String(processEngine.repositoryService.getDeploymentResources(deploymentId).single {
                        it.name == fileName
                        //@TODO should have check for validating that it was a `.json` file?
                    }.bytes)
                }.getOrElse {
                    throw IllegalArgumentException("Formio Deployment key/file could not be found in deployment.")
                }

                //@TODO get sub forms and put them into the JsonNode

                val schemas = mapOf(
                        Pair("parent", Spin.JSON(schema).unwrap()), //@TODO do we need spin or can we find a way to access the original ObjectMapper?
                        Pair("subForms", listOf<Map<String, JsonNode>>()) //@TODO
                )

                Response.ok(schemas).build()
            } else {
                throw InvalidRequestException(Response.Status.NOT_FOUND, "Did not find any form deployment configuration")
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
}