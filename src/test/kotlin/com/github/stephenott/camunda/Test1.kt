package com.github.stephenott.camunda

import org.assertj.core.api.Assertions.*
import org.camunda.bpm.engine.ProcessEngine
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.web.WebAppConfiguration
import java.nio.file.Paths


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Test1(
        @Autowired val engine: ProcessEngine
) {
    @BeforeAll
    fun deploy(){
        val deployment = engine.repositoryService.createDeployment()
                .addInputStream("SomeProcess1.bpmn", Paths.get("bpmn","SomeProcess1.bpmn").toFile().inputStream())
                .addInputStream("MyStartForm.json", Paths.get("bpmn", "MyStartForm.json").toFile().inputStream())
                .addInputStream("MyUT1.json", Paths.get("bpmn", "MyUT1.json").toFile().inputStream())
                .addInputStream("ChoicesEnumExample.json", Paths.get("bpmn", "ChoicesEnumExample.json").toFile().inputStream())
                .addInputStream("NoPersistFieldExample.json", Paths.get("bpmn", "NoPersistFieldExample.json").toFile().inputStream())
                .deployWithResult()

        println("Deployment ID: " + deployment.id)
    }

    @Test @Order(1)
    fun contextLoads() {
        println("Engine: " + engine.name)
        assertThat(engine.name).isEqualTo("default")
    }

    @Test @Order(2)
    fun startInstance(){
//        val key = engine.repositoryService.createProcessDefinitionQuery().active().latestVersion().singleResult().key
//        assertThat(key).isEqualTo("someProcess1")

//        val instance = engine.runtimeService.startProcessInstanceByKey("someProcess1")
        Thread.sleep(100000000)

//        val task = engine.taskService.createTaskQuery().processInstanceId(instance.processInstanceId).singleResult()
//        assertThat(task.name).isEqualTo("UT1")
    }

}
