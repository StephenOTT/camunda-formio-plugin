# Docker Setup

Manual local setup of docker image using released resources:

1. Download the resources from the release: forms.zip, tasklist.zip, formfieldvalidation-*-server-plugin.jar
1. Unzip the .zip files and Place the resources into the docker folder
1. build the image (expose 8080)


## Build Instructions

1. run `shadowJar` task in `formfieldvalidator` project
1. run `copyShadowJarToDockerFiles` task in `formfieldvalidator` project
1. run `setupDockerFiles` task in `root` project
1. run docker build and run
