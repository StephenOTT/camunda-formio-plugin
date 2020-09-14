FROM camunda/camunda-bpm-platform:tomcat-7.13.0

# Remove examples
RUN rm -r /camunda/webapps/examples
RUN rm -r /camunda/webapps/camunda-invoice
# Remove docs site
RUN rm -r /camunda/webapps/docs

COPY ./tasklist/scripts/. /camunda/webapps/camunda/app/tasklist/scripts
COPY ./tasklist/styles/. /camunda/webapps/camunda/app/tasklist/styles
COPY ./forms/. /camunda/webapps/forms/

COPY ./formfieldvalidator-*-server-plugin.jar /camunda/lib/formfieldvalidator-server-plugin.jar

COPY ./bpm-platform.xml /camunda/conf/bpm-platform.xml