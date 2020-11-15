package com.github.stephenott.camunda.rest.formio.forms

import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.camunda.bpm.engine.rest.exception.InvalidRequestException
import org.camunda.bpm.engine.rest.impl.DefaultProcessEngineRestServiceImpl
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/process-definition")
class FormioStartEventRestService() : DefaultProcessEngineRestServiceImpl() {
    @GET
    @Path("/key/{key}/forms")
    @Produces(MediaType.APPLICATION_JSON)
    fun processDefKey(@PathParam("key") processDefKey: String): Response {
        val rootResourcePath = getRelativeEngineUri(null).toASCIIString() //@TODO review if this is needed...
        val processEngine = FormioFormRestUtils.getEngine(null, objectMapper, rootResourcePath)

        val def: ProcessDefinitionEntity = processEngine.repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefKey)
                .latestVersion()
                .withoutTenantId()
                .singleResult() as ProcessDefinitionEntity

        checkStartFormAuthz(processEngine, def) //@TODO validate that this works...

        val formKey = processEngine.formService.getStartFormKey(def.id)

        return FormioFormRestUtils.getSchemaResponse(processEngine, def.deploymentId, formKey, objectMapper)
    }

    @GET
    @Path("/{id}/forms")
    @Produces(MediaType.APPLICATION_JSON)
    fun processDefId(@PathParam("id") processDefId: String): Response {
        val rootResourcePath = getRelativeEngineUri(null).toASCIIString() //@TODO review if this is needed...
        val processEngine = FormioFormRestUtils.getEngine(null, objectMapper, rootResourcePath)

        val def: ProcessDefinitionEntity = processEngine.repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefId)
                .latestVersion()
                .withoutTenantId()
                .singleResult() as ProcessDefinitionEntity

        checkStartFormAuthz(processEngine, def) //@TODO validate that this works...

        val formKey = processEngine.formService.getStartFormKey(def.id)

        return FormioFormRestUtils.getSchemaResponse(processEngine, def.deploymentId, formKey, objectMapper)
    }

    @GET
    @Path("/key/{key}/tenant-id/{tenantId}/forms")
    @Produces(MediaType.APPLICATION_JSON)
    fun processDefKeyTenant(@PathParam("key") processDefKey: String, @PathParam("tenantId") tenantId: String): Response {
        val rootResourcePath = getRelativeEngineUri(null).toASCIIString() //@TODO review if this is needed...
        val processEngine = FormioFormRestUtils.getEngine(null, objectMapper, rootResourcePath)

        val def: ProcessDefinitionEntity = processEngine.repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefKey)
                .latestVersion()
                .tenantIdIn(tenantId)
                .singleResult() as ProcessDefinitionEntity

        checkStartFormAuthz(processEngine, def) //@TODO validate that this works...

        val formKey = processEngine.formService.getStartFormKey(def.id)

        return FormioFormRestUtils.getSchemaResponse(processEngine, def.deploymentId, formKey, objectMapper)
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