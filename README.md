# Camunda Formio Plugin

Provides client-side and server-side integration for using Camunda Embedded Forms with Formio Forms.

Using Formio for a Start Form:

![Start Form](./doc/StartForm-Config.png)


Using Formio for User Tasks:

![User Task Form](./doc/UserTask-Config.png)

If you want to add server side validation, you add a Validation Constraint with the name `formio`.  See the Server Validation 
section for more details.


## What does it do?

Allows you to configure *start-event* forms and *user-task* forms with a `formkey` that will load formio forms in Camunda Tasklist webapp.


## Configure the BPMN

Example of Configuring a Start Form:

`embedded:/forms/formio.html?deployment=MyStartForm.json&var=submission&transient=true`

In this example, we use the `deployment` parameter to direct the use of the `MyStartForm.json` form schema which is found in the BPMN Deployment resources. 
We use the `var` parameter to direct the name of the process variable that will be created to store the form submission. 
We use the `transient` parameter to direct the process variable holding the form submission get created as a 
[transient variable](https://docs.camunda.org/manual/7.13/user-guide/process-engine/variables/#transient-variables) 


![start form configuration](./doc/StartForm-Config.png)

```xml
<bpmn:startEvent id="Event_0emfvgy"
                 camunda:formKey="embedded:/forms/formio.html?deployment=MyStartForm.json&#38;var=submission&#38;transient=true">
    <bpmn:outgoing>Flow_1ax32ut</bpmn:outgoing>
</bpmn:startEvent>
```


Example of Configuring a User Task:

`embedded:/forms/formio.html?deployment=MyUT1.json&var=subWithServerValidation

![start form configuration](./doc/UserTask-Config.png)

```xml
<bpmn:userTask id="Activity_1xq7c62" name="Typical Form with Server Validation"
               camunda:formKey="embedded:/forms/formio.html?deployment=MyUT1.json&#38;var=subWithServerValidation">
    <bpmn:extensionElements>
        <camunda:formData>
            <camunda:formField id="FormField_0t7u03d" type="string">
                <camunda:validation>
                    <camunda:constraint name="formio"/>
                </camunda:validation>
            </camunda:formField>
        </camunda:formData>
    </bpmn:extensionElements>
    <bpmn:incoming>Flow_0069xxd</bpmn:incoming>
    <bpmn:outgoing>Flow_18xtnen</bpmn:outgoing>
</bpmn:userTask>
```


### Configuration Options:

The following are the parameters that can be passed in the `formKey`:

| Parameter | Required? | Default | Description |
|---------------|-------------------|--------------|-----------------------|
|deployment=    | Yes or use path= | - | The .json file name in the deployment created through the API. |
|path=          | Yes or use deployment= | - | The file system path to the .json file. |
|transient=     | No | false | If true, then variable will be submitted as Transient, and thus not saved to database.  Use a Script Task or listener in the transaction to post-process the submission into the desired variable. |
|var=           | No | if start form then "startForm_submission", if user task then "`[taskId]`_submission"| Define a custom variable name for the form submission.  Will be submitted as a Process Variable.|

Examples:

1. `embedded:/forms/formio.html?deployment=MyUT1.json?transient=true&var=myCustomSubmission`
1. `embedded:/forms/formio.html?deployment=MyUT1.json?var=UT1-Submission`
1. `embedded:/forms/formio.html?path=/forms/MyStartForm.json` (where the MyStartForm.json was placed in the `src/main/webapp/forms` folder)


## Installation

### Camunda SpringBoot Deployment

WIP: Add the dependency to your Camunda Webapp deployment.

### Typical Camunda Deployment

WIP: Add the dependency to your Camunda Webapp deployment.


## Submission Storage

When a successful submission occurs, a `json` variable will be created as a Process Instance Variable. 

Use [Camunda SPIN library](https://docs.camunda.org/manual/7.13/reference/spin/json/) to access the json variable properties.

Example in a gateway expression: `${someSubmission.prop('data').prop('someFieldKey').value()}`


### Resolving User Task `taskId` to more meaningful values

Very often the taskID (which is typically a UUID) will not be very meaningful.  If you require more meaningful variable names 
consider using the `var` parameter to set a custom variable name or use the Activity ID (the `id` property when you are in the Modeler on a user task activity).


## Building Forms:

You can build your form and copy the JSON from: [https://formio.github.io/formio.js/app/builder](https://formio.github.io/formio.js/app/builder)


### Submitting a Form as a BPMN Error

Coming soon!


### Submitting a Form as a BPMN Escalation

Coming soon!


## Deploying your Forms
 
### REST API Form Deployment

Forms can be deployed through the REST API.  The form JSON must be part of the same deployment as the BPMN.
 
If changes need to be made to the form, you must deploy a new .json file along with the BPMN.
 
An example of using Postman to deploy:
 
![Deployment in Postman](./doc/Deployment.png)

When using REST API Form deployments, use the `deployment` parameter in the form key such as: 
`embedded:/forms/formio.html?deployment=MyUT1.json`

### File System Forms Deployment (or other URLs)
 
Forms can be deployed on the file system and made available to the web application.  The common way to do this is 
through `src/main/webapp/forms` (or whatever folder you like within `webapp`).
 
If you want to make changes to the JSON, you can modify the JSON file without having to make a new BPMN deployment.
 
If you do not want to use the file system, you can deploy to another URL within the same domain as Camunda Webapps.  Then 
you can set your `formKey` to something like: `embedded:/forms/formio.html?deployment=http://example.com/forms/MyUT1.json`
 
The benefit of having your form/JSON outside of the BPMN deployment is you are not required to redeploy the BPMN each 
time you make changes to the form, but in many use cases you will want to tie your BPMN and forms together 
within the same deployment for versioning purposes.
 
## Example Submission
 
![Cockpit](./doc/Cockpit2.png)
 
 ```json
{
  "data": {
    "firstName": "SomeFirstName",
    "lastName": "SomeLastName",
    "email": "",
    "phoneNumber": {
      "value": "",
      "maskName": "US"
    },
    "select": [],
    "address": {},
    "dueDate": "2020-08-18T12:00:00-04:00",
    "birthdate": "00/00/0000",
    "submit": false
  },
  "metadata": {
    "timezone": "America/Montreal",
    "offset": -240,
    "origin": "http://localhost:8080",
    "referrer": "http://localhost:8080/camunda/app/welcome/default/",
    "browserName": "Netscape",
    "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1.2 Safari/605.1.15",
    "pathName": "/camunda/app/tasklist/default/",
    "onLine": true
  },
  "state": "submitted"
}
```


## Variable Fetching in Formio

Formio will fetch variables based on configurations in the component configuration.

Under the API tab of a component create a custom property with the following format:

**key:** `fetchVariable`  **value:** `variableName` (recommended not to use spaces in variable names)

Once you get your variable, use the Default Value Population feature to populate your field with the data returned from the variable fetch.


## Default Value Population

Use the Custom Default Value Javascript feature in Formio to parse the returned variables.

Variables get stored in `$scope.camForm.formioVariables`

In the Custom Default Value configuration you can use: `let variables = angular.element('#task-form-formio').scope().camForm.formioVariables` 
to access the object of variables.  Variables names are the Keys.

If the variable is type `Json` it is automatically available as a JS Object.

### Example

Set the default value of a Text field "First Name" with the `firstName` property that was submitted in the Start Form (a formio form submission)

In the First Name field's Custom Default Value configuration use the following: 

```js
let variables = angular.element('#task-form-formio').scope().camForm.formioVariables

value = variables.postProcessed_submission.data.firstName
```

The code `angular.element('#task-form-formio').scope()` is re-capturing the Camunda task form angular scope from the `<form id="task-form-formio>` element.

Formio based submissions place the form submission data inside of the `data` object.

Common use case would be to set the First Name field as read-only if it is for display purposes.

1. Configure the Component: 

   ![Build1](./doc/Form-Build-1.png)

1. Go to the Data tab: 

   ![Build2](./doc/Form-Build-2.png)

1. Scroll down to Custom Default Value: 

   ![Build3](./doc/Form-Build-3.png)

1. Add your JS logic for selecting your default value: 

   ![Build4](./doc/Form-Build-4.png)

1. Go to the API tab: 

   ![Build5](./doc/Form-Build-5.png)

1. Create a Custom property with key `fetchVariable`, and the value of the variable you want to work within your JS in step 4.
 
   ![Build6](./doc/Form-Build-6.png)
   

## Server Validation (Validating submissions against the schema on the server)

This section is a WORK IN PROGRESS / WIP: Validations are functional, but easy configuration is still ongoing

From submissions can optionally be enabled with server-side validation by the Formio server-side validation.

To enable server-side validation of a *start-form* or *user-task*, add a "validation constraint" in the form fields configuration. 
Set any field type, and create a constraint with the key/name "formio":

![server config](./doc/UserTask-Config.png)

By default, a lightweight local [form-validation-server](https://github.com/StephenOTT/Form-Validation-Server) 
(validating formio schemas) will be deployed (`localhost:8081/validate`) along with Camunda Webapps.

If you want to use your own deployment of the [form-validation-server](https://github.com/StephenOTT/Form-Validation-Server), 
configure the camunda-formio process engine plugin with the url to the validation endpoint.
