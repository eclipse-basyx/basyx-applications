<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">AAS Dashboard</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The AAS Dashboard provides a user-friendly display of time series data. Historical and real-time data can be viewed at a glance. The aggregation of multiple data sources into a single view is beneficial for monitoring machine and plant behaviour.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            Enabling this feature adds a service that allows you to configure your own dashboard to meet your requirements. Configurations are persisted using MongDB.
        </p>
        <!-- User selection for Dashboard -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't add AAS Dashboard" value="noDashboard"></v-radio>
                <v-radio label="Add AAS Dashboard" value="addDashboard"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/visualization/corporate-design">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/deployment/integration">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'Dashboard',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noDashboard',
        };
    },

    mounted() {
        if (this.isDashboardEnabled) {
            this.selection = 'addDashboard';
            // add the default Dashboard Service configuration
            this.addDefaultDashboardConfig();
            // add Dashboard to the Docker Compose configuration
            this.addDashboardToDockerCompose();
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if the Dashboard is enabled
        isDashboardEnabled(): boolean {
            return this.appStore.getDashboard;
        },

        // check if MongoDB is enabled
        isMongoDBEnabled(): boolean {
            return this.appStore.getMongoDB;
        },

        // check if the User Interface is enabled
        isUIEnabled(): boolean {
            return this.appStore.getUserInterface;
        },

        // get the dashboard configuration object from the store
        dashboardConfigObject() {
            return this.appStore.getDashboardConfig;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },
    },

    methods: {
        addDefaultDashboardConfig() {
            // create an aas-dashboard.yml File
            const dashboardConfig = {
                "server": {
                    "port": "8085"
                },
                "spring": {
                    "data": {
                        "mongodb": {
                            "host": "${DB_HOST:mongo}",
                            "database": "${DB_NAME:aas-dashboard}",
                            "authentication-database": "admin",
                            "username": "${DB_USERNAME:mongoAdmin}",
                            "password": "${DB_PASSWORD:mongoPassword}"
                        }
                    }
                },
                "basyx": {
                    "cors": {
                        "allowed-origins": "*",
                        "allowed-methods": "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD"
                    },
                    "externalurl": "http://localhost:8085"
                }
            };
            // create config object
            let configObject = {
                id: '0d036b87-c61f-4959-9011-ecb684af5110',
                name: 'aas-dashboard.yml',
                value: dashboardConfig,
            }
            // check if the aas-dashboard.yml file already exists
            if (!this.dashboardConfigObject) {
                this.appStore.setDashboardConfig(configObject);
            }
        },

        removeDashboardConfig() {
            // remove the aas-dashboard.yml file
            if (this.dashboardConfigObject) {
                this.appStore.setDashboardConfig(undefined);
            }
        },

        addDashboardToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and add it if not
                if (!dockerComposeConfig.services['dashboard-api']) {
                    dockerComposeConfig.services['dashboard-api'] = {
                        image: "aaronzi/basyx-dashboard-api:SNAPSHOT_02",
                        container_name: "dashboard-api",
                        ports: ["8085:8085"],
                        volumes: ["./basyx/aas-dashboard.yml:/application.yml"],
                        restart: "always",
                        depends_on: {
                            "mongo": {
                                condition: "service_healthy"
                            }
                        }
                    };
                    // check if mongoDB is already present in the docker-compose.yml file and add it if not
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
                    // check if the AAS Web UI is enabled and add the DASHBOARD_SERVICE_PATH to the environment variables
                    if (this.isUIEnabled) {
                        dockerComposeConfig.services['aas-web-ui'].environment.DASHBOARD_SERVICE_PATH = "http://localhost:8085/api/elements";
                    }
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeDashboardFromDockerCompose() {
            // delete the Dashboard API service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['dashboard-api'];
            if (this.isUIEnabled) {
                delete dockerComposeConfig.services['aas-web-ui'].environment.DASHBOARD_SERVICE_PATH;
            }
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        removeMongoDBFromDockerCompose() {
            // delete the MongoDB service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['mongo'];
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        updateConfig() {
            if (this.selection === 'addDashboard') {
                // add the default Dashboard Service configuration
                this.addDefaultDashboardConfig();
                // add Dashboard to the Docker Compose configuration
                this.addDashboardToDockerCompose();
                // add Dashboard API to the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                // Add Dashboard API
                basyxConfig.push({
                    id: 'ebee24da-6e60-45c6-8bc1-e5dfa7c052bc',
                    title: 'Dashboard API',
                    children: [
                        { id: '8a13ce5f-8036-426e-ae2a-3c9f6751573d', title: 'Config: default', type: 'config' },
                        { id: '9a36c0f2-5e72-43ea-a8a3-778da3e164c0', title: 'MongoDB Integration', type: 'db' },
                    ],
                });
                // add MongoDB if not already present
                if (!this.basyxConfig.some((item: any) => item.id === 'e007e23b-1588-4b64-b17f-69f475684842')) {
                    basyxConfig.push({
                        id: 'e007e23b-1588-4b64-b17f-69f475684842',
                        title: 'MongoDB',
                        children: [
                            { id: '68624679-d4c7-4d7f-b87b-3eebc81c1f11', title: 'Config: default', type: 'config' },
                        ],
                    });
                }
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateDashboard(true);
            } else {
                // remove the Dashboard Service configuration
                this.removeDashboardConfig();
                // remove Dashboard API from the Docker Compose configuration
                this.removeDashboardFromDockerCompose();
                // remove Dashboard API from the BaSyx configuration
                let basyxConfig = [...this.basyxConfig];
                basyxConfig = basyxConfig.filter((item: any) => item.id !== 'ebee24da-6e60-45c6-8bc1-e5dfa7c052bc');
                if (!this.isMongoDBEnabled) {
                    basyxConfig = basyxConfig.filter((item: any) => item.id !== 'e007e23b-1588-4b64-b17f-69f475684842');
                    // remove MongoDB from the Docker Compose configuration
                    this.removeMongoDBFromDockerCompose();
                }
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateDashboard(false);
            }
        },
    },
});
</script>