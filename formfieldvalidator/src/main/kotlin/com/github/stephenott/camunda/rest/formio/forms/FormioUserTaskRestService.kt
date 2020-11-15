package com.github.stephenott.camunda.rest.formio.forms

import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity
import org.camunda.bpm.engine.rest.exception.InvalidRequestException
import org.camunda.bpm.engine.rest.impl.DefaultProcessEngineRestServiceImpl
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/task")
class FormioUserTaskRestService() : DefaultProcessEngineRestServiceImpl() {
    @GET
    @Path("/{id}/forms")
    @Produces(MediaType.APPLICATION_JSON)
    fun task(@PathParam("id") taskId: String): Response {
        val rootResourcePath = getRelativeEngineUri(null).toASCIIString() //@TODO review if this is needed...
        val processEngine = FormioFormRestUtils.getEngine(null, objectMapper, rootResourcePath)

        val task = processEngine.taskService.createTaskQuery().taskId(taskId).singleResult()

        val def = (task as TaskEntity).processDefinition

        checkUserTaskFormAuthz(processEngine, def) //@TODO validate that this works...

        val formKey = processEngine.formService.getTaskFormKey(def.id, task.taskDefinitionKey)

        return FormioFormRestUtils.getSchemaResponse(processEngine, def.deploymentId, formKey, objectMapper)
    }

    fun checkUserTaskFormAuthz(engine: ProcessEngine, processDefinition: ProcessDefinitionEntity) {
       //@TODO Refactor to support User Task Authz
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