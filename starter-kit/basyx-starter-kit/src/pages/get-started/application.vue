<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Application</h1>
        <!-- Alert for starter kit explanation -->
        <v-alert color="alertCard">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">How does the Starter Kit work?</div>
                </v-col>
            </v-row>
            <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                A selection of components will be made based on your answers to the following questions, which will attempt to determine your use case. The configurations of these components will change along the way, depending on the choices you make. At the end you will be able to download and deploy your customised BaSyx setup.
            </p>
        </v-alert>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- AAS Discovery Service -->
        <h2 class="text-header">AAS Discovery Service</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The AAS Discovery Service allows searching for Asset IDs of corresponding Asset Administration Shells, as well as finding Asset Administration Shell IDs based on given Asset IDs.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            This is useful when working with the <a class="text-primary" style="text-decoration: none" href="https://industrialdigitaltwin.org/wp-content/uploads/2023/04/IDTA-02011-1-0_Submodel_HierarchicalStructuresEnablingBoM.pdf" target="_blank">Hierachical Structures enabling Bills of Material</a> Submodel. You will be able to easily find and navigate to the Asset Administration Shells of the assets included in this Submodel.
        </p>
        <!-- User selection for AAS Discovery Service -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't include the AAS Discovery Service" value="noDiscoveryService"></v-radio>
                <v-radio label="Include the AAS Discovery Service" value="addDiscoveryService"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/introduction">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/behaviour/persistence">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'Application',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noDiscoveryService',
        };
    },

    mounted() {
        if (this.isAasDiscoveryEnabled) {
            this.selection = 'addDiscoveryService';
            // add the default AAS Discovery Service configuration
            this.addDefaultAasDiscoveryConfig();
            // add the AAS Discovery Service to the Docker Compose configuration
            this.addAasDiscoveryToDockerCompose();
            if (this.isMongoDBEnabled) {
                let basyxConfig = [...this.basyxConfig];
                basyxConfig.forEach((item: any) => {
                    if (item.id === 'da6a3af1-f998-487a-8092-2847b0ec74e0') {
                        // check if the MongoDB component is already added
                        let isMongoDBAdded = item.children.some((child: any) => child.id === 'fb062d00-f7b4-48a5-856d-957e50c78066');
                        if (!isMongoDBAdded) {
                            item.children.push({ id: 'fb062d00-f7b4-48a5-856d-957e50c78066', title: 'MongoDB Integration', type: 'db' });
                        }
                    }
                });
                this.appStore.updateBasyxConfig(basyxConfig);
            }
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if the AAS Discovery Service is enabled
        isAasDiscoveryEnabled(): boolean {
            return this.appStore.getAasDiscovery;
        },

        // check if MongoDB is enabled
        isMongoDBEnabled(): boolean {
            return this.appStore.getMongoDB;
        },

        // check if the User Interface is enabled
        isUIEnabled(): boolean {
            return this.appStore.getUserInterface;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },

        // get the aas-discovery.properties config object from the store
        aasDiscoveryConfigObject() {
            return this.appStore.getAasDiscoveryConfig;
        },
    },

    methods: {
        addDefaultAasDiscoveryConfig() {
            // create an aas-discovery.properties File
            const aasDiscoveryConfig = {
                "server.port": "8081",
                "spring.application.name": "AAS Discovery Service",
                "basyx.aasdiscoveryservice.name": "aas-discovery-service",
                "basyx.backend": "InMemory",
                "basyx.cors.allowed-origins": "*",
                "basyx.cors.allowed-methods": "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD"
            };
            // create config object
            let configObject = {
                id: '517a0e1a-212f-42d3-bf3a-b6bfff6047db',
                name: 'aas-discovery.properties',
                value: aasDiscoveryConfig,
            }
            // check if the aas-discovery.properties file already exists
            if (!this.aasDiscoveryConfigObject) {
                this.appStore.setAasDiscoveryConfig(configObject);
            }
        },

        deleteAasDiscoveryConfig() {
            this.appStore.setAasDiscoveryConfig(undefined);
        },

        addAasDiscoveryToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the AAS Discovery Service configuration and add it if not
                if (!dockerComposeConfig.services['aas-discovery']) {
                    dockerComposeConfig.services['aas-discovery'] = {
                        image: "eclipsebasyx/aas-discovery:2.0.0-milestone-02",
                        container_name: "aas-discovery",
                        volumes: ["./basyx/aas-discovery.properties:/application/application.properties"],
                        ports: ["8084:8081"],
                        restart: "always"
                    };
                    // check if MongoDB is enabled and add the MongoDB service to the depends_on section of the aas-discovery service
                    if (this.isMongoDBEnabled) {
                        // check if the aas-discovery service already includes a depends_on section and add it if not
                        dockerComposeConfig.services['aas-discovery'].depends_on = dockerComposeConfig.services['aas-discovery'].depends_on || {};
                        dockerComposeConfig.services['aas-discovery'].depends_on['mongo'] = { condition: 'service_healthy' };
                    }
                    // check if the AAS Web UI is enabled and add the AAS_DISCOVERY_PATH to the environment variables
                    if (this.isUIEnabled) {
                        dockerComposeConfig.services['aas-web-ui'].environment.AAS_DISCOVERY_PATH = "http://localhost:8084/lookup/shells";
                    }
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeAasDiscoveryFromDockerCompose() {
            // delete the AAS Discovery Service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['aas-discovery'];
            // delete the AAS_DISCOVERY_PATH from the AAS Web UI environment variables
            if (this.isUIEnabled) {
                delete dockerComposeConfig.services['aas-web-ui'].environment.AAS_DISCOVERY_PATH;
            }
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        updateConfig() {
            if (this.selection === 'addDiscoveryService') {
                // add the default AAS Discovery Service configuration
                this.addDefaultAasDiscoveryConfig();
                // add the AAS Discovery Service to the Docker Compose configuration
                this.addAasDiscoveryToDockerCompose();
                // add the AAS Discovery Service to the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                basyxConfig.push({
                    id: 'da6a3af1-f998-487a-8092-2847b0ec74e0',
                    title: 'AAS Discovery Service',
                    children: [
                        { id: 'e29de304-900b-4a5d-ae7f-289564a89e82', title: 'Config: default', type: 'config' },

                    ],
                });
                if (this.isMongoDBEnabled) {
                    basyxConfig.forEach((item: any) => {
                        if (item.id === 'da6a3af1-f998-487a-8092-2847b0ec74e0') {
                            // check if the MongoDB component is already added
                            let isMongoDBAdded = item.children.some((child: any) => child.id === 'fb062d00-403d-4704-a139-ad30b27594c1');
                            if (!isMongoDBAdded) {
                                item.children.push({ id: 'fb062d00-403d-4704-a139-ad30b27594c1', title: 'MongoDB Integration', type: 'db' });
                            }
                        }
                    });
                }
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateAasDiscovery(true)
            } else {
                // remove the AAS Discovery Service configuration
                this.deleteAasDiscoveryConfig();
                // remove the AAS Discovery Service from the Docker Compose configuration
                this.removeAasDiscoveryFromDockerCompose();
                // remove the AAS Discovery Service from the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                basyxConfig = basyxConfig.filter((item: any) => item.id !== 'da6a3af1-f998-487a-8092-2847b0ec74e0');
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateAasDiscovery(false)
            }
        },
    },
});
</script>