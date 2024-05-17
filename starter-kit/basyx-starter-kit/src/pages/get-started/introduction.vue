<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Introduction</h1>
        <!-- Alert for BaSyx Version -->
        <v-alert color="alertCard">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">You are reading the documentation for BaSyx v2!</div>
                </v-col>
            </v-row>
            <ul style="margin-left: 54px; margin-top: 8px">
                <li class="font-weight-medium text-subheader">BaSyx v1 support has ended on Jul 03, 2023. For EOL support, send us a <a class="text-primary" style="text-decoration: none" href="mailto:basyx-dev@eclipse.org">mail</a>.</li>
                <li class="font-weight-medium text-subheader">New to BaSyx v2? You are in the right place!</li>
            </ul>
        </v-alert>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- BaSyx Starter Kit -->
        <h2 class="text-header">BaSyx Starter Kit</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The BaSyx Starter Kit is a web tool designed to simplify the configuration and deployment of BaSyx. It guides you through a series of questions aimed at understanding your specific Digital Twin use case. Based on your answers, it generates a customised BaSyx setup that's tailored to your needs.
        </p>
        <v-row justify="center" class="mt-5">
            <v-col cols="auto">
                <v-btn variant="tonal" color="primary" append-icon="mdi-rocket-launch" size="large" to="/get-started/application">Let's start</v-btn>
            </v-col>
        </v-row>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- What is BaSyx -->
        <h2 class="text-header">What is BaSyx?</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Eclipse BaSyx is an open source platform designed to facilitate the development and use of digital twins in the context of Industry 4.0. At its core, BaSyx serves as a middleware that bridges the gap between assets in the manufacturing environment and their digital representations.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            With a strong focus on interoperability, BaSyx supports the <a class="text-primary" style="text-decoration: none" href="https://www.plattform-i40.de/IP/Redaktion/DE/Downloads/Publikation/AAS-ReadingGuide_202201.pdf?__blob=publicationFile&v=1" target="_blank">Asset Administration Shell (AAS)</a>, a key enabler for the use of standardised digital twins. This enables the creation of detailed digital representations that reflect the characteristics and behaviour of their real-world counterparts, providing a unified interface for their management and integration into digital ecosystems.
        </p>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- Off the Shelf Components -->
        <h2 class="text-header">Off-the-Shelf Components</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Eclipse BaSyx simplifies the journey towards Industry 4.0 with its Off-the-Shelf components, designed to jumpstart the deployment of digital twins and their integration into digital ecosystems. Available for direct download from <a class="text-primary" style="text-decoration: none" href="https://hub.docker.com/u/eclipsebasyx" target="_blank">Docker Hub</a>, these components offer a ready-to-use foundation that can be customised to meet your requirements.
        </p>
        <p class="text-normalText mt-3 mb-8 text-subtitle-1">
            These components are containerised, making them highly portable and easy to deploy in a variety of environments, from local development setups to cloud-based platforms.
        </p>
        <!-- Alert for Prerequisites -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mb-8">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="primary">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">Prerequisites</div>
                </v-col>
            </v-row>
            <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                To deploy BaSyx components, you need to have <a class="text-primary" style="text-decoration: none" href="https://docs.docker.com/get-docker/" target="_blank">Docker</a> installed on your system. If you haven't already, download and install Docker to get started with BaSyx.
            </p>
        </v-alert>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app'

export default defineComponent({
    name: 'Introduction',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
        };
    },

    mounted() {
        this.addDefaultDockerComposeConfig();
        this.addDefaultAasEnvConfig();
        this.addDefaultAasRegistryConfig();
        this.addDefaultSubmodelRegistryConfig();
    },

    computed: {
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
    },

    methods: {
        addDefaultDockerComposeConfig() {
            let dockerComposeConfig = {
                version: "3",
                services: {
                    "aas-env": {
                        image: "eclipsebasyx/aas-environment:2.0.0-milestone-02",
                        container_name: "aas-env",
                        volumes: [
                            "./aas:/application/aas",
                            "./basyx/aas-env.properties:/application/application.properties"
                        ],
                        ports: ["8081:8081"],
                        restart: "always",
                        depends_on: {
                            "aas-registry": {
                                condition: "service_healthy"
                            },
                            "sm-registry": {
                                condition: "service_healthy"
                            }
                        }
                    },
                    "aas-registry": {
                        image: "eclipsebasyx/aas-registry-log-mem:2.0.0-milestone-02",
                        container_name: "aas-registry",
                        ports: ["8082:8080"],
                        volumes: ["./basyx/aas-registry.yml:/workspace/config/application.yml"],
                        restart: "always"
                    },
                    "sm-registry": {
                        image: "eclipsebasyx/submodel-registry-log-mem:2.0.0-milestone-02",
                        container_name: "sm-registry",
                        ports: ["8083:8080"],
                        volumes: ["./basyx/sm-registry.yml:/workspace/config/application.yml"],
                        restart: "always"
                    }
                }
            };
            // create config object
            let configObject = {
                id: '1d54e0af-ea5e-4f14-9f58-a589eaf06f9f',
                name: 'docker-compose.yml',
                value: dockerComposeConfig,
            }
            // check if the docker-compose.yml file already exists
            if (!this.dockerComposeConfigObject) {
                this.appStore.setDockerComposeConfig(configObject);
            }
        },

        addDefaultAasEnvConfig() {
            // create an aas-env.properties File
            const aasEnvConfig = {
                "server.port": "8081",
                "basyx.backend": "InMemory",
                "basyx.environment": "file:aas",
                "basyx.cors.allowed-origins": "*",
                "basyx.cors.allowed-methods": "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD",
                "basyx.aasrepository.feature.registryintegration": "http://aas-registry:8080",
                "basyx.submodelrepository.feature.registryintegration": "http://sm-registry:8080",
                "basyx.externalurl": "http://localhost:8081"
            };
            // create config object
            let configObject = {
                id: '9b71a749-09f8-4e8d-bfbc-ea1025577a1c',
                name: 'aas-env.properties',
                value: aasEnvConfig,
            }
            // check if the aas-env.properties file already exists
            if (!this.aasEnvConfigObject) {
                this.appStore.setAasEnvConfig(configObject);
            }
        },

        addDefaultAasRegistryConfig() {
            // create an aas-registry.yml File
            let aasRegistryConfig = {
                basyx: {
                    cors: {
                        "allowed-origins": "*",
                        "allowed-methods": "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD"
                    }
                }
            };
            // create config object
            let configObject = {
                id: '1fb2422d-e1ca-405e-9480-195cb16b9ac0',
                name: 'aas-registry.yml',
                value: aasRegistryConfig,
            }
            // check if the aas-registry.yml file already exists
            if (!this.aasRegistryConfigObject) {
                this.appStore.setAasRegistryConfig(configObject);
            }
        },

        addDefaultSubmodelRegistryConfig() {
            // create a sm-registry.yml File
            let submodelRegistryConfig = {
                basyx: {
                    cors: {
                        "allowed-origins": "*",
                        "allowed-methods": "GET,POST,PATCH,DELETE,PUT,OPTIONS,HEAD"
                    }
                }
            };
            // create config object
            let configObject = {
                id: '79594332-56a3-4e91-9c3b-7d0b436d433a',
                name: 'sm-registry.yml',
                value: submodelRegistryConfig,
            }
            // check if the sm-registry.yml file already exists
            if (!this.submodelRegistryConfigObject) {
                this.appStore.setSubmodelRegistryConfig(configObject);
            }
        },
    },
});
</script>