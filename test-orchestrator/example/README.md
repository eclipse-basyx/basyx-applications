# Java Test Orchestrator Example

This project is a Java-based Test Orchestrator microservice packaged as a Docker image and orchestrated via Docker Compose. It integrates with BaSyx AAS components and an MQTT broker.

---

## 🔧 Prerequisites

- Docker
- Docker Compose
- Git (to clone the repo)

---

This folder contains an example setup for using the **Test Orchestrator**.

## Getting Started

### 1. **Clone the Repository**

```bash
git clone https://gitlab.rz.htw-berlin.de/OpenBasys/java-testorchestrator.git
cd example
```


### 2. Run the Project

```bash
docker-compose up -d
```
### 3. Access the User Interface

After the application starts, open your browser and navigate to:
```bash
http://localhost:9080
```
The Test Orchestrator UI should now be accessible.

## Testing Options

You can test submodels in three different ways:

### 1. Test Using JSON Input

#### Standard Submodel Validation
To validate your submodel against an IDTA-registered standard, provide the JSON input of the AAS or standalone submodel in the `aasFile` field.

#### Custom Submodel Validation
To validate against a custom reference:
- Copy and paste your submodel in JSON format in the `aasFile` field.
- Copy and paste the reference submodel JSON in the `customAASFile` field.
- Click the `Execute` button.

![Test_Orchestrator_JSON](./logo/Test_Orchestrator_JSON.png)

Validation results will be displayed in [the Test Orchestrator Module section](#test-orchestrator-module).


---

### 2. Test Using API Links

#### Standard Submodel Validation
Provide the API endpoint of your AAS or standalone submodel in the `inputAASLink` field.

#### Custom Submodel Validation
- Provide the API URL of the submodel you want to test in the `inputAASLink` field.
- Provide the API URL of the reference submodel in the `customAASLink` field.
- Click the `Execute` button.

![Test_Orchestrator_API](./logo/Test_Orchestrator_API.png)

Validation results will be displayed in [the Test Orchestrator Module section](#test-orchestrator-module).

---

### 3. Test by Uploading Files

- Upload a valid AAS or submodel JSON file via the web interface.
- The system will automatically validate all submodels contained in the uploaded AAS.

![Test_Orchestrator_Upload](./logo/Test_Orchestrator_Upload.png)


Validation results will be displayed in [the Test Orchestrator Module section](#test-orchestrator-module).

---

## Test Orchestrator Module

Navigate to the **Test Orchestrator** module to:
- View all test results and statistics.
- Download validation results:
  - For individual submodels.
  - For all tested submodels as a single JSON file.

![Test_Orchestrator_Module](./logo/Test_Orchestrator_Module.png)


![Module_Visu](./logo/Module_Visu.PNG)

