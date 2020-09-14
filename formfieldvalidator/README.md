

## Build instructions for server plugin

The plugin requires additional minimal dependencies.  
Run the `shadowJar` task which will generate a jar with ending: `*-server-plugin.jar`. 
This jar can be dropped into a Camunda engine instance such in Tomcat: `/camunda/lib` 