<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">AAS Web User Interface</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The AAS Web UI provides a user-friendly interface for viewing and interacting with Asset Administration Shells and their Submodels. The graphical interface enhances accessibility and usability, making it easier for non-technical users to benefit from digital twin technologies.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            It can be extended by plugins, for example to have dedicated visualisations for different Submodels such as the <a class="text-primary" style="text-decoration: none" href="https://industrialdigitaltwin.org/wp-content/uploads/2022/10/IDTA-02006-2-0_Submodel_Digital-Nameplate.pdf" target="_blank">Digital Nameplate</a>.
        </p>
        <!-- User selection for User Interface -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't add the AAS Web UI" value="noUI"></v-radio>
                <v-radio label="Add the AAS Web UI" value="addUI"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/behaviour/time-series">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/visualization/corporate-design">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'UI',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noUI',
        };
    },

    mounted() {
        if (this.isUIEnabled) {
            this.selection = 'addUI';
            // add AAS Web UI to the Docker Compose configuration
            this.addUIToDockerCompose();
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if the User Interface is enabled
        isUIEnabled(): boolean {
            return this.appStore.getUserInterface;
        },

        // check if the AAS Discovery Service is enabled
        isAasDiscoveryEnabled(): boolean {
            return this.appStore.getAasDiscovery;
        },

        // check if the Dashboard Service is enabled
        isDashboardEnabled(): boolean {
            return this.appStore.getDashboard;
        },

        // check if time series data is enabled
        isTimeSeriesDataEnabled(): boolean {
            return this.appStore.getTimeSeriesData;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },
    },

    methods: {
        addUIToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and add it if not
                if (!dockerComposeConfig.services['aas-web-ui']) {
                    dockerComposeConfig.services['aas-web-ui'] = {
                        image: "aaronzi/basyx-aas-ui:SNAPSHOT_14",
                        container_name: "aas-ui",
                        ports: ["3000:3000"],
                        environment: {
                            AAS_REGISTRY_PATH: "http://localhost:8082/shell-descriptors",
                            SUBMODEL_REGISTRY_PATH: "http://localhost:8083/submodel-descriptors",
                            AAS_REPO_PATH: "http://localhost:8081/shells",
                            SUBMODEL_REPO_PATH: "http://localhost:8081/submodels",
                            CD_REPO_PATH: "http://localhost:8081/concept-descriptions"
                        },
                        restart: "always",
                        depends_on: {
                            "aas-env": {
                                condition: "service_healthy"
                            }
                        }
                    };
                    // check if Discovery Service is enabled
                    if (this.isAasDiscoveryEnabled) {
                        dockerComposeConfig.services["aas-web-ui"].environment.AAS_DISCOVERY_PATH = "http://localhost:8084/lookup/shells";
                    }
                    // check if Dashboard is enabled
                    if (this.isDashboardEnabled) {
                        dockerComposeConfig.services["aas-web-ui"].environment.DASHBOARD_SERVICE_PATH = "http://localhost:8085/api/elements";
                    }
                    // check if Time Series Data is enabled
                    if (this.isTimeSeriesDataEnabled) {
                        dockerComposeConfig.services["aas-web-ui"].environment.INFLUXDB_TOKEN = "S18VeAlq042B4naMX31oqIaSGmUmOLAC-DV3VIdkxDJuAhTXLTVFEchyTSmCcUAmB7Wu94KgExzV8gJaDjzR3Q==";
                    }
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeUIFromDockerCompose() {
            // delete the AAS Web UI service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['aas-web-ui'];
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        updateConfig() {
            if (this.selection === 'addUI') {
                // add AAS Web UI to the Docker Compose configuration
                this.addUIToDockerCompose();
                // Add AAS Web UI to the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                // Add AAS Web UI
                basyxConfig.push({
                    id: '562336fb-852c-4041-b1ee-fc88886a4c51',
                    title: 'AAS Web UI',
                    children: [
                        { id: '663f8a96-007e-4df1-8f66-df9cb310fcba', title: 'Config: default', type: 'config' },
                    ],
                });
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateUserInterface(true);
            } else {
                // remove AAS Web UI from the Docker Compose configuration
                this.removeUIFromDockerCompose();
                let basyxConfig = [...this.basyxConfig];
                basyxConfig = basyxConfig.filter((item: any) => item.id !== '562336fb-852c-4041-b1ee-fc88886a4c51');
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateUserInterface(false);
            }
        },
    },
});
</script>