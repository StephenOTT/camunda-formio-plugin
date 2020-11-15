package com.github.stephenott.camunda.formio

import com.github.stephenott.camunda.rest.formio.forms.FormioStartEventRestService
import com.github.stephenott.camunda.rest.formio.forms.FormioUserTaskRestService
import com.github.stephenott.camunda.tasks.forms.command.GetFormVariablesSecurityProcessEnginePlugin
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.rest.CamundaJerseyResourceConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.ws.rs.ApplicationPath


@SpringBootApplication
@EnableProcessApplication
class Application

fun main(args: Array<String>) = runApplication<Application>(*args).let { Unit }

@Component
class MyPlugin: FormioFormFieldValidatorProcessEnginePlugin()

@Component
class MyFormsSecurityPlugin: GetFormVariablesSecurityProcessEnginePlugin()

@Component
class MyFormioConfigParser: FormioParseListenerProcessEnginePlugin()

@Configuration
class MyCorsConfigs {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowCredentials = true
        configuration.addAllowedOrigin("*")
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

@Component
@ApplicationPath("/engine-rest")
class JerseyConfig(): CamundaJerseyResourceConfig() {
    override fun registerAdditionalResources() {
        this.register(FormioStartEventRestService::class.java)
        this.register(FormioUserTaskRestService::class.java)
    }
}