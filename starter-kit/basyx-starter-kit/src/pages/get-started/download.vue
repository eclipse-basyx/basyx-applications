<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Download Your BaSyx Setup</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Your BaSyx setup is now complete. Click on <code class="text-subheader">Download</code> and extract the zip file to your device. Please note that Docker must be running to start the BaSyx Containers!
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            Run the following command in a terminal to start the BaSyx Containers:
        </p>
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8 pr-1 py-1">
            <v-row align="center">
                <v-col>
                    <code class="text-subheader font-weight-medium">docker-compose up -d</code>
                </v-col>
                <v-spacer></v-spacer>
                <v-fade-transition>
                    <v-col v-if="copyIcon === 'mdi-clipboard-check-outline'" cols="auto" class="pr-0">
                        <span class="text-subheader">copied</span>
                    </v-col>
                </v-fade-transition>
                <v-col cols="auto">
                    <v-btn variant="plain" :icon="copyIcon" color="normalText" @click="copyToClipboard()"></v-btn>
                </v-col>
            </v-row>
        </v-alert>
        <!-- Download Button -->
        <v-btn class="mb-8" variant="tonal" block append-icon="mdi-download" color="success" @click="downloadAsZip()">Download BaSyx Setup</v-btn>
        <!-- Navigation to previous page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/deployment/context-path">Back</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';
import JSZip from 'jszip';

interface aasWebUI {
    image: string;
    container_name: string;
    ports: string[];
    environment: {
        AAS_DISCOVERY_PATH?: string;
        AAS_REGISTRY_PATH: string;
        SUBMODEL_REGISTRY_PATH: string;
        AAS_REPO_PATH: string;
        SUBMODEL_REPO_PATH: string;
        CD_REPO_PATH: string;
        PRIMARY_COLOR?: string;
        LOGO_PATH?: string;
    };
    volumes?: string[];
    restart: string;
    depends_on: any;
};

export default defineComponent({
    name: 'RBAC',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            dockerComposeFile: undefined as any | undefined,
            readmeFile: '',
            copyIcon: 'mdi-clipboard-outline'
        };
    },

    mounted() {
        this.createReadme();
    },

    computed: {
        // check if the AAS Discovery Service is enabled
        isAasDiscoveryEnabled(): boolean {
            return this.appStore.getAasDiscovery;
        },

        // check if MQTT is enabled
        isMQTTEnabled(): boolean {
            return this.appStore.getMQTT || this.appStore.getMQTTBroker;
        },

        // check if time series data is enabled
        isTimeSeriesDataEnabled(): boolean {
            return this.appStore.getTimeSeriesData;
        },

        // get the logo from the store
        logoStore(): File[] | undefined {
            return this.appStore.getLogo;
        },

        // get the icon from the store
        iconStore(): File[] | undefined {
            return this.appStore.getAppIcon;
        },

        // get the telegraf configuration file from the store
        telegrafConfigStore(): File[] | undefined {
            return this.appStore.getTelegrafConf;
        },

        // get the AAS Files from the store
        aasFilesStore(): File[] | undefined {
            return this.appStore.getAasFiles;
        },

        // check if the Dashboard is enabled
        isDashboardEnabled(): boolean {
            return this.appStore.getDashboard;
        },
    },

    methods: {
        createReadme() {
            // create a README.md file
            const readme = `# BaSyx Setup
This is your BaSyx setup. To run the BaSyx containers, you need to have Docker installed on your device.

## How to run the BaSyx containers
1. Extract the zip file on your device.
2. Open a terminal and navigate to the extracted folder.
3. Run the following command to start the BaSyx containers:
\`\`\`
docker-compose up -d
\`\`\`

## Access the BaSyx containers
- AAS Environment: [http://localhost:8081](http://localhost:8081)
- AAS Registry: [http://localhost:8082](http://localhost:8082)
- Submodel Registry: [http://localhost:8083](http://localhost:8083)

## Include your own Asset Administration Shells
To include your own Asset Administration Shells, you can either put them in the \`aas\` folder or upload them via the AAS Web UI.`;
            this.readmeFile = readme;
        },


        downloadAsZip() {
            // create a zip file with the BaSyx setup
            const zip = new JSZip();

            // create an empty aas folder
            const aasFolder = zip.folder('aas');

            // add the AAS files to the aas folder
            if (this.aasFilesStore && this.aasFilesStore.length > 0) {
                this.aasFilesStore.forEach((file) => {
                    aasFolder?.file(file.name, file);
                });
            }

            // create basyx folder
            const basyxFolder = zip.folder('basyx');

            // add the aas-env.properties file to the aas folder
            basyxFolder?.file('aas-env.properties', this.appStore.getAasEnvConfigAsString.value);

            // add the aas-registry.yml file to the basyx folder
            basyxFolder?.file('aas-registry.yml', this.appStore.getAasRegistryConfigAsString.value);

            // add the sm-registry.yml file to the basyx folder
            basyxFolder?.file('sm-registry.yml', this.appStore.getSubmodelRegistryConfigAsString.value);

            // add the aas-discovery.properties file to the basyx folder if the AAS Discovery Service is enabled
            if (this.isAasDiscoveryEnabled) {
                basyxFolder?.file('aas-discovery.properties', this.appStore.getAasDiscoveryConfigAsString.value);
            }

            // add the aas-dashboard.yml file to the basyx folder if the Dashboard is enabled
            if (this.isDashboardEnabled) {
                basyxFolder?.file('aas-dashboard.yml', this.appStore.getDashboardConfigAsString.value);
            }

            // create a mosquitto folder if MQTT is enabled
            if (this.isMQTTEnabled) {
                const mosquittoFolder = zip.folder('mosquitto');
                // create config folder in mosquitto folder
                const configFolder = mosquittoFolder?.folder('config');
                configFolder?.file('mosquitto.conf', this.appStore.getMosquittoConfig.value);
            }

            // create a logo folder if a logo and icon were uploaded
            if (this.logoStore && this.iconStore) {
                const logoFolder = zip.folder('logo');

                // add the logo and icon to the logo folder
                logoFolder?.file(this.logoStore[0].name, this.logoStore[0]);
                logoFolder?.file(this.iconStore[0].name, this.iconStore[0]);
            }

            // create telegraf folder if time series data is enabled
            if (this.isTimeSeriesDataEnabled) {
                const telegrafFolder = zip.folder('telegraf');
                if (this.telegrafConfigStore) {
                    telegrafFolder?.file(this.telegrafConfigStore[0].name, this.telegrafConfigStore[0]);
                }
            }

            // add the docker-compose.yml file to the zip file
            zip.file('docker-compose.yml', this.appStore.getDockerComposeConfigAsString.value);

            // add the README.md file to the zip file
            zip.file('README.md', this.readmeFile);

            // generate the zip file
            zip.generateAsync({ type: 'blob' }).then((content) => {
                // create a download link
                const a = document.createElement('a');
                const url = URL.createObjectURL(content);
                a.href = url;
                a.download = 'basyx-setup.zip';
                a.click();
                URL.revokeObjectURL(url);
            });
        },

        copyToClipboard() {
            // copy the command to start the BaSyx containers to the clipboard
            navigator.clipboard.writeText('docker-compose up -d');
            // change the icon to checkmark for 2 seconds
            this.copyIcon = 'mdi-clipboard-check-outline';
            setTimeout(() => {
                this.copyIcon = 'mdi-clipboard-outline';
            }, 2000);
        },
    },
});
</script>