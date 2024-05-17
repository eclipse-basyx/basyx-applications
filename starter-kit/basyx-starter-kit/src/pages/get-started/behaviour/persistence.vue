<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Persistence Backend</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Ensuring data persistence with minimal risk of loss is critical for digital twins throughout an asset's lifecycle. This becomes even more important as AAS data evolves over time. Direct integration with a database, in this case <a class="text-primary" style="text-decoration: none" href="https://www.mongodb.com/compatibility/docker" target="_blank">MongoDB</a>, protects data from loss during both planned and unplanned shutdowns.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            MongoDB's integration with the AAS Environment, the AAS and Submodel Registry as well as the AAS Discovery Service, achieves this essential persistence.
        </p>
        <!-- User selection for Persistence Backend -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't add MongoDB persistence Backend" value="noPersistence"></v-radio>
                <v-radio label="Add MongoDB persistence Backend" value="addMongoDB"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/application">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/behaviour/mqtt">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'Persitence',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noPersistence',
        };
    },

    mounted() {
        if (this.isMongoDBEnabled) {
            this.selection = 'addMongoDB';
            // add MongoDB to the Docker Compose configuration
            this.addMongoDBToDockerCompose();
            // update aas-env.properties to include MongoDB
            this.addMongoDBToAasEnvConfig();
            // update aas-registry.yml to include MongoDB
            this.addMongoDBToAasRegistryConfig();
            // update sm-registry.yml to include MongoDB
            this.addMongoDBToSubmodelRegistryConfig();
            // update aas-discovery.properties to include MongoDB
            if (this.isAasDiscoveryEnabled) {
                this.addMongoDBToAasDiscoveryConfig();
            }
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if MongoDB is enabled
        isMongoDBEnabled(): boolean {
            return this.appStore.getMongoDB;
        },

        // check if the AAS Discovery Service is enabled
        isAasDiscoveryEnabled(): boolean {
            return this.appStore.getAasDiscovery;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },

        // get the aas-env.properties config object from the store
        aasEnvConfigObject() {
            return this.appStore.getAasEnvConfig;
        },

        // get the aas-registry.yml config object from the store
        aasRegistryConfigObject() {
            return this.appStore.getAasRegistryConfig;
        },

        // get the sm-registry.yml config object from the store
        submodelRegistryConfigObject() {
            return this.appStore.getSubmodelRegistryConfig;
        },

        // get the aas-discovery.properties config object from the store
        aasDiscoveryConfigObject() {
            return this.appStore.getAasDiscoveryConfig;
        },
    },

    methods: {
        addMongoDBToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes a MongoDB configuration and add it if not
                if (!dockerComposeConfig.services['mongo']) {
                    dockerComposeConfig.services['mongo'] = {
                        image: "mongo:5.0.10",
                        container_name: "mongo",
                        environment: {
                            MONGO_INITDB_ROOT_USERNAME: "mongoAdmin",
                            MONGO_INITDB_ROOT_PASSWORD: "mongoPassword"
                        },
                        restart: "always",
                        healthcheck: {
                            test: "mongo",
                            interval: "10s",
                            timeout: "5s",
                            retries: 5
                        }
                    };
                }
                // add mongo to the depends_on section of the aas-env
                dockerComposeConfig.services['aas-env'].depends_on['mongo'] = { condition: 'service_healthy' };
                // check if the aas-registry and sm-registry services already include a depends_on section and add it if not
                dockerComposeConfig.services['aas-registry'].depends_on = dockerComposeConfig.services['aas-registry'].depends_on || {};
                dockerComposeConfig.services['aas-registry'].depends_on['mongo'] = { condition: 'service_healthy' };
                // add mongo to the depends_on section of the aas-registry and sm-registry
                dockerComposeConfig.services['sm-registry'].depends_on = dockerComposeConfig.services['sm-registry'].depends_on || {};
                dockerComposeConfig.services['sm-registry'].depends_on['mongo'] = { condition: 'service_healthy' };
                // change the images of the aas-registry and sm-registry services to use the mongo version
                dockerComposeConfig.services['aas-registry'].image = "eclipsebasyx/aas-registry-log-mongodb:2.0.0-milestone-02";
                dockerComposeConfig.services['sm-registry'].image = "eclipsebasyx/submodel-registry-log-mongodb:2.0.0-milestone-02";
                // add the MongoDB service to the depends_on section of the aas-discovery service if it is enabled
                if (this.isAasDiscoveryEnabled) {
                    // check if the aas-discovery service already includes a depends_on section and add it if not
                    dockerComposeConfig.services['aas-discovery'].depends_on = dockerComposeConfig.services['aas-discovery'].depends_on || {};
                    dockerComposeConfig.services['aas-discovery'].depends_on['mongo'] = { condition: 'service_healthy' };
                }
                // update the docker-compose.yml config object in the store
                dockerComposeConfigObject.value = dockerComposeConfig;
                this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
            }
        },

        removeMongoDBFromDockerCompose() {
            // delete the MongoDB service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['mongo'];
            // remove mongo from the depends_on section of the aas-env
            delete dockerComposeConfig.services['aas-env'].depends_on['mongo'];
            // remove mongo from the depends_on section of the aas-registry and sm-registry (and delete the depends_on section if it is empty)
            delete dockerComposeConfig.services['aas-registry'].depends_on['mongo'];
            if (Object.keys(dockerComposeConfig.services['aas-registry'].depends_on).length === 0) {
                delete dockerComposeConfig.services['aas-registry'].depends_on;
            }
            delete dockerComposeConfig.services['sm-registry'].depends_on['mongo'];
            if (Object.keys(dockerComposeConfig.services['sm-registry'].depends_on).length === 0) {
                delete dockerComposeConfig.services['sm-registry'].depends_on;
            }
            // change the images of the aas-registry and sm-registry services back to the default version
            dockerComposeConfig.services['aas-registry'].image = "eclipsebasyx/aas-registry-log-mem:2.0.0-milestone-02";
            dockerComposeConfig.services['sm-registry'].image = "eclipsebasyx/submodel-registry-log-mem:2.0.0-milestone-02";
            // remove the MongoDB service from the depends_on section of the aas-discovery service if it is enabled
            if (this.isAasDiscoveryEnabled) {
                delete dockerComposeConfig.services['aas-discovery'].depends_on['mongo'];
                if (Object.keys(dockerComposeConfig.services['aas-discovery'].depends_on).length === 0) {
                    delete dockerComposeConfig.services['aas-discovery'].depends_on;
                }
            }
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        addMongoDBToAasEnvConfig() {
            // console.log('addMongoDBToAasEnvConfig', this.aasEnvConfigObject);
            let aasEnvConfigObject = { ...this.aasEnvConfigObject };
            let aasEnvConfig = aasEnvConfigObject.value;
            // check if the aasEnvConfigObject already includes a MongoDB configuration and add it if not
            if (!aasEnvConfig['spring.data.mongodb.host']) {
                aasEnvConfig['spring.data.mongodb.host'] = "mongo";
            }
            if (!aasEnvConfig['spring.data.mongodb.database']) {
                aasEnvConfig['spring.data.mongodb.database'] = "aas-env";
            }
            if (!aasEnvConfig['spring.data.mongodb.authentication-database']) {
                aasEnvConfig['spring.data.mongodb.authentication-database'] = "admin";
            }
            if (!aasEnvConfig['spring.data.mongodb.username']) {
                aasEnvConfig['spring.data.mongodb.username'] = "mongoAdmin";
            }
            if (!aasEnvConfig['spring.data.mongodb.password']) {
                aasEnvConfig['spring.data.mongodb.password'] = "mongoPassword";
            }
            // change basyx.backend to MongoDB
            aasEnvConfig['basyx.backend'] = "MongoDB";
            // update the aas-env.properties config object in the store
            aasEnvConfigObject.value = aasEnvConfig;
            this.appStore.setAasEnvConfig(aasEnvConfigObject);
        },

        removeMongoDBFromAasEnvConfig() {
            let aasEnvConfigObject = { ...this.aasEnvConfigObject };
            let aasEnvConfig = aasEnvConfigObject.value;
            delete aasEnvConfig['spring.data.mongodb.host'];
            delete aasEnvConfig['spring.data.mongodb.database'];
            delete aasEnvConfig['spring.data.mongodb.authentication-database'];
            delete aasEnvConfig['spring.data.mongodb.username'];
            delete aasEnvConfig['spring.data.mongodb.password'];
            aasEnvConfig['basyx.backend'] = "InMemory";
            aasEnvConfigObject.value = aasEnvConfig;
            this.appStore.setAasEnvConfig(aasEnvConfigObject);
        },

        addMongoDBToAasRegistryConfig() {
            // console.log('addMongoDBToAasRegistryConfig', this.aasRegistryConfigObject);
            let aasRegistryConfigObject = { ...this.aasRegistryConfigObject };
            let aasRegistryConfig = aasRegistryConfigObject.value;
            // check if the aasRegistryConfigObject already includes a MongoDB configuration and add it if not
            if (!aasRegistryConfig['spring']) {
                aasRegistryConfig['spring'] = {
                    data: {
                        mongodb: {
                            uri: "mongodb://mongoAdmin:mongoPassword@mongo:27017"
                        }
                    }
                };
            }
            // update the aas-registry.yml config object in the store
            aasRegistryConfigObject.value = aasRegistryConfig;
            this.appStore.setAasRegistryConfig(aasRegistryConfigObject);
        },

        removeMongoDBFromAasRegistryConfig() {
            let aasRegistryConfigObject = { ...this.aasRegistryConfigObject };
            let aasRegistryConfig = aasRegistryConfigObject.value;
            delete aasRegistryConfig['spring'];
            aasRegistryConfigObject.value = aasRegistryConfig;
            this.appStore.setAasRegistryConfig(aasRegistryConfigObject);
        },

        addMongoDBToSubmodelRegistryConfig() {
            let submodelRegistryConfigObject = { ...this.submodelRegistryConfigObject };
            let submodelRegistryConfig = submodelRegistryConfigObject.value;
            // check if the submodelRegistryConfigObject already includes a MongoDB configuration and add it if not
            if (!submodelRegistryConfig['spring']) {
                submodelRegistryConfig['spring'] = {
                    data: {
                        mongodb: {
                            uri: "mongodb://mongoAdmin:mongoPassword@mongo:27017"
                        }
                    }
                };
            }
            // update the sm-registry.yml config object in the store
            submodelRegistryConfigObject.value = submodelRegistryConfig;
            this.appStore.setSubmodelRegistryConfig(submodelRegistryConfigObject);
        },

        removeMongoDBFromSubmodelRegistryConfig() {
            let submodelRegistryConfigObject = { ...this.submodelRegistryConfigObject };
            let submodelRegistryConfig = submodelRegistryConfigObject.value;
            delete submodelRegistryConfig['spring'];
            submodelRegistryConfigObject.value = submodelRegistryConfig;
            this.appStore.setSubmodelRegistryConfig(submodelRegistryConfigObject);
        },

        addMongoDBToAasDiscoveryConfig() {
            let aasDiscoveryConfigObject = { ...this.aasDiscoveryConfigObject };
            let aasDiscoveryConfig = aasDiscoveryConfigObject.value;
            // check if the aasDiscoveryConfigObject already includes a MongoDB configuration and add it if not
            if (!aasDiscoveryConfig['spring.data.mongodb.host']) {
                aasDiscoveryConfig['spring.data.mongodb.host'] = "mongo";
            }
            if (!aasDiscoveryConfig['spring.data.mongodb.database']) {
                aasDiscoveryConfig['spring.data.mongodb.database'] = "aas-env";
            }
            if (!aasDiscoveryConfig['spring.data.mongodb.authentication-database']) {
                aasDiscoveryConfig['spring.data.mongodb.authentication-database'] = "admin";
            }
            if (!aasDiscoveryConfig['spring.data.mongodb.username']) {
                aasDiscoveryConfig['spring.data.mongodb.username'] = "mongoAdmin";
            }
            if (!aasDiscoveryConfig['spring.data.mongodb.password']) {
                aasDiscoveryConfig['spring.data.mongodb.password'] = "mongoPassword";
            }
            // change basyx.backend to MongoDB
            aasDiscoveryConfig['basyx.backend'] = "MongoDB";
            // update the aas-env.properties config object in the store
            aasDiscoveryConfigObject.value = aasDiscoveryConfig;
            this.appStore.setAasDiscoveryConfig(aasDiscoveryConfigObject);
        },

        removeMongoDBFromAasDiscoveryConfig() {
            let aasDiscoveryConfigObject = { ...this.aasDiscoveryConfigObject };
            let aasDiscoveryConfig = aasDiscoveryConfigObject.value;
            delete aasDiscoveryConfig['spring.data.mongodb.host'];
            delete aasDiscoveryConfig['spring.data.mongodb.database'];
            delete aasDiscoveryConfig['spring.data.mongodb.authentication-database'];
            delete aasDiscoveryConfig['spring.data.mongodb.username'];
            delete aasDiscoveryConfig['spring.data.mongodb.password'];
            aasDiscoveryConfig['basyx.backend'] = "InMemory";
            aasDiscoveryConfigObject.value = aasDiscoveryConfig;
            this.appStore.setAasEnvConfig(aasDiscoveryConfigObject);
        },

        updateConfig() {
            if (this.selection === 'addMongoDB') {
                // add MongoDB to the Docker Compose configuration
                this.addMongoDBToDockerCompose();
                // update aas-env.properties to include MongoDB
                this.addMongoDBToAasEnvConfig();
                // update aas-registry.yml to include MongoDB
                this.addMongoDBToAasRegistryConfig();
                // update sm-registry.yml to include MongoDB
                this.addMongoDBToSubmodelRegistryConfig();
                // update aas-discovery.properties to include MongoDB
                if (this.isAasDiscoveryEnabled) {
                    this.addMongoDBToAasDiscoveryConfig();
                }
                // add MongoDB to the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                // update AAS Environment, AAS Registry and Submodel Registry -> add child MongoDB Integration
                basyxConfig.forEach((item: any) => {
                    if (item.id === '5289baf4-21a8-4c22-8361-8a4b8bfb5716') {
                        item.children.push({ id: 'fb062d00-f7b4-48a5-856d-957e50c78066', title: 'MongoDB Integration', type: 'db' });
                    }
                    if (item.id === '59cc5124-3a8d-46ae-a622-fe044606d096') {
                        item.children.push({ id: 'fb062d00-a97e-495e-8cf3-eb9fb3e21e79', title: 'MongoDB Integration', type: 'db' });
                    }
                    if (item.id === 'dd16fbe2-bb12-4a0a-9d60-630e02607c66') {
                        item.children.push({ id: 'fb062d00-871d-4bea-a007-eff11b9ff042', title: 'MongoDB Integration', type: 'db' });
                    }
                    if (item.id === 'da6a3af1-f998-487a-8092-2847b0ec74e0') {
                        item.children.push({ id: 'fb062d00-403d-4704-a139-ad30b27594c1', title: 'MongoDB Integration', type: 'db' });
                    }
                });
                // Add MongoDB component
                basyxConfig.push({
                    id: 'e007e23b-1588-4b64-b17f-69f475684842',
                    title: 'MongoDB',
                    children: [
                        { id: '68624679-d4c7-4d7f-b87b-3eebc81c1f11', title: 'Config: default', type: 'config' },
                    ],
                });
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateMongoDB(true);
            } else {
                // remove MongoDB from the Docker Compose configuration
                this.removeMongoDBFromDockerCompose();
                // remove MongoDB from aas-env.properties
                this.removeMongoDBFromAasEnvConfig();
                // remove MongoDB from aas-registry.yml
                this.removeMongoDBFromAasRegistryConfig();
                // remove MongoDB from sm-registry.yml
                this.removeMongoDBFromSubmodelRegistryConfig();
                // remove MongoDB from aas-discovery.properties
                if (this.isAasDiscoveryEnabled) {
                    this.removeMongoDBFromAasDiscoveryConfig();
                }
                let basyxConfig = [...this.basyxConfig];
                basyxConfig = basyxConfig.filter((item: any) => item.id !== 'e007e23b-1588-4b64-b17f-69f475684842');
                basyxConfig.forEach((item: any) => {
                    item.children = item.children.filter((child: any) => !child.id.includes('fb062d00'));
                });
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateMongoDB(false);
            }
        },
    },
});
</script>