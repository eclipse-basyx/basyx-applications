# Demo OPC UA Example for the Databridge

This folder includes all necessary config files to get an example running using the BaSyx Databridge for retrieving asset data via OPC UA.

## AAS Server

The AASServerConfig folder includes a demo AAS which get's loaded on startup of the docker-compose project. When Using the OPC2AAS tool to create an AAS, that AAS XML file should be converted to an AASX file and then put into this folder.

## Databridge

The DatabridgeConfig folder includes all relevant Databridge config files for retrieving data from an OPC UA server. When using the OPC2AAS tool, all output Databridge config files should be put in this folder.

## Logo

This folder can contain a logo you want to be displayed in the AAS Web UI.

## OpcUaServer

Here you can find a demo OPC UA server written in python. You can alter the server by changing the *server.py* program.
