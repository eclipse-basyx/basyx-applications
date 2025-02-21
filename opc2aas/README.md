# OPC2AAS

OPC2AAS is an Asset Administration Shell microservice represent through the AAS API (operation SubmodelElement) that translates OPC UA Node Structures to Asset Administration Shells.

## OPC UA Testserver

To test the functionality of the OPC2AAS service, you can use the provided OPC UA testserver. It is a docker container that provides a simple OPC UA server with a few nodes. The docker image is available at [aaronzi/demo-opc-server](https://hub.docker.com/r/aaronzi/demo-opc-server).

### Prerequisites

Make sure you have Docker installed on your system.

### Starting the example

Run the following command to start the test server together with OPC2AAS and the necessary Eclipse BaSyx components:

```bash
docker-compose up -d
```

After the services are started, you can access the AAS Web UI at [http://localhost:9080](http://localhost:9080).
From there you are able to access and use the OPC2AAS service.

## OPC2AAS Service

### Usage

The `AASfromOPC` operation in the **CreationSubmodel** has the following input parameters:

| Variable                 | Description                                                                            | Input values                          |
|--------------------------|----------------------------------------------------------------------------------------|---------------------------------------|
| `aasIdShort`             | The idShort of the AAS that will be created                                            | GeneratedAAS                          |
| `opcNodeId`              | The NodeId of the node from which the OPC2AAS service should start                     | ns=2;i=1                              |
| `opcServerUrl`           | The URL of the OPC UA server that should be used                                       | opc.tcp://opcserver:4840              |
| `opcUSername` (optional) | The username that will be used to authenticate with the OPC UA server                  | test                                  |
| `opcPassword` (optional) | The password that will be used to authenticate with the OPC UA server                  | test                                  |
| `submodelRepoUrl`        | The URL of the BaSyx Submodel Repository where the AASs Submodels will be uploaded to  | http://aas-environment:8081/submodels |

After providing the inputs and executing the operation, the service will generate seven files in the **OutputSubmodel** if the connection to the OPC UA Node is successful.

### Generated Files

The following files are generated that can also be downloaded:

| File Name              | Description                                                                                                |
|------------------------|------------------------------------------------------------------------------------------------------------|
| **aas**                | Generated AASX file.                                                                                       |
| **consumerFile**       | DataBridge configuration file for the OPC UA Consumer.                                                     |
| **extractvalue**       | Transformer configuration file to extract the OPC UA value from the Java Node Object.                      |
| **jsonatatransformer** | Transformer configuration file.                                                                            |
| **jacksontransformer** | Transformer configuration file.                                                                            |
| **aasserver**          | DataBridge configuration file for the AAS Environment datasink.                                            |
| **route**              | DataBridge configuration file for the routes between the OPC UA Consumer and the AAS Environment datasink. |
