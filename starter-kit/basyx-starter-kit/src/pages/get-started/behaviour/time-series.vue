<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Time Series Data</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            In the context of digital twins and AAS in particular, time series data offers significant advantages in the operation of assets. These include, but are not limited to, gaining insight into patterns, trends and anomalies in asset behaviour. This information is essential for predictive maintenance to minimise downtime and extend asset life.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            <a class="text-primary" style="text-decoration: none" href="https://industrialdigitaltwin.org/wp-content/uploads/2023/03/IDTA-02008-1-1_Submodel_TimeSeriesData.pdf" target="_blank">Time Series Data</a> is also an AAS submodel specification. A distinction is made between data within the AAS (internal segment), data from files (external segment) and data from time series databases (linked segment). The latter requires a database and a tool to collect data from the asset.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            Here, <a class="text-primary" style="text-decoration: none" href="https://www.influxdata.com/" target="_blank">InfluxDB</a> is used as a time series database in conjunction with <a class="text-primary" style="text-decoration: none" href="https://www.influxdata.com/time-series-platform/telegraf/" target="_blank">Telegraf</a> as a tool for the collection of metrics.
        </p>
        <!-- User selection for Time Series Data -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't include time series data from a linked database" value="noTSD"></v-radio>
                <v-radio label="Include time series data from a linked database" value="addTSD"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Configuration for Time Series Data -->
        <v-slide-y-transition>
            <div v-if="selection === 'addTSD'">
                <v-divider class="mt-12 mb-8"></v-divider>
                <!-- Telegraf Configuration -->
                <h2 class="text-header">Metrics Collection</h2>
                <p class="text-normalText mt-8 mb-5 text-subtitle-1">
                    Telegraf is configured using a configuration file. This file contains the necessary information about the asset endpoints that provide data to be stored in the time series database.
                </p>
                <p class="text-normalText mt-3 mb-6 text-subtitle-1">
                    The <a class="text-primary" style="text-decoration: none" href="https://docs.influxdata.com/influxdb/latest/write-data/no-code/use-telegraf/" target="_blank">Telegraf configuration file</a> must be created beforehand. This can be done either <a class="text-primary" style="text-decoration: none" href="https://docs.influxdata.com/influxdb/v2/write-data/no-code/use-telegraf/auto-config/" target="_blank">automatically</a> or by <a class="text-primary" style="text-decoration: none" href="https://docs.influxdata.com/influxdb/v2/write-data/no-code/use-telegraf/manual-config/" target="_blank">hand</a>.
                </p>
                <v-file-input variant="solo-filled" prepend-inner-icon="$file" prepend-icon="" label="Upload telegraf.conf File" density="compact" v-model="telegrafConfigFile" @update:modelValue="addTelegrafConf()" hide-details></v-file-input>
                <v-divider class="mt-12 mb-8"></v-divider>
                <!-- Telegraf Configuration -->
                <h2 class="text-header mb-8">MQTT Metrics</h2>
                <!-- Alert for MQTT Broker -->
                <v-alert color="alertCard">
                    <v-row align="center">
                        <v-col cols="auto" class="pr-0">
                            <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                        </v-col>
                        <v-col>
                            <div class="font-weight-medium text-header">How to collect MQTT metrics?</div>
                        </v-col>
                    </v-row>
                    <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                        If you want to collect data published by MQTT clients within a time series database, this data must first be sent to an MQTT broker so that Telegraf can subscribe to this data. If you don't have a MQTT broker yet, you can check the following box to include the Eclipse Mosquitto Broker in your setup.
                    </p>
                </v-alert>
                <v-checkbox class="mt-8" label="Add Mosquitto MQTT broker" v-model="brokerCheckbox" @update:modelValue="updateBrokerConfig"></v-checkbox>
            </div>
        </v-slide-y-transition>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/behaviour/mqtt">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/visualization/ui">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'TimeSeries',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noTSD',
            brokerCheckbox: false,
            telegrafConfigFile: undefined as File[] | undefined,
        };
    },

    mounted() {
        if (this.isTimeSeriesDataEnabled) {
            this.selection = 'addTSD';
            // add influxDB to the Docker Compose configuration
            this.addInfluxDBToDockerCompose();
            // add telegraf to the Docker Compose configuration
            this.addTelegrafToDockerCompose();
        }
        if (this.isMQTTBrokerEnabled) {
            this.brokerCheckbox = true;
            // add default Mosquitto Service configuration
            this.addDefaultMosquittoConfig();
            // add Mosquitto to the Docker Compose configuration
            this.addMosquittoToDockerCompose();
        }
        if (this.telegrafConfigStore) {
            this.telegrafConfigFile = this.telegrafConfigStore;
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if time series data is enabled
        isTimeSeriesDataEnabled(): boolean {
            return this.appStore.getTimeSeriesData;
        },

        // check if MQTT broker is enabled
        isMQTTBrokerEnabled(): boolean {
            return this.appStore.getMQTTBroker;
        },

        // check if MQTT is enabled
        isMQTTEnabled(): boolean {
            return this.appStore.getMQTT;
        },

        // check if the User Interface is enabled
        isUIEnabled(): boolean {
            return this.appStore.getUserInterface;
        },

        // get the telegraf configuration file from the store
        telegrafConfigStore(): File[] | undefined {
            return this.appStore.getTelegrafConf;
        },

        // get the mosquitto.conf config object from the store
        mosquittoConfigObject() {
            return this.appStore.getMosquittoConfig;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },
    },

    methods: {
        addInfluxDBToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the influxDB configuration and add it if not
                if (!dockerComposeConfig.services['influxdb']) {
                    dockerComposeConfig.services['influxdb'] = {
                        image: "influxdb:2",
                        container_name: "influxdb",
                        ports: [
                            "8086:8086",
                            "9999:9999"
                        ],
                        volumes: [
                            "./influxdb/data:/var/lib/influxdb2",
                            "./influxdb/config:/etc/influxdb2"
                        ],
                        environment: [
                            "DOCKER_INFLUXDB_INIT_MODE=setup",
                            "DOCKER_INFLUXDB_INIT_USERNAME=admin",
                            "DOCKER_INFLUXDB_INIT_PASSWORD=influxpassword",
                            "DOCKER_INFLUXDB_INIT_ORG=basyx",
                            "DOCKER_INFLUXDB_INIT_BUCKET=basyx",
                            "DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=S18VeAlq042B4naMX31oqIaSGmUmOLAC-DV3VIdkxDJuAhTXLTVFEchyTSmCcUAmB7Wu94KgExzV8gJaDjzR3Q=="
                        ],
                        restart: "always",
                    };
                    // check if the AAS Web UI is enabled and add the INFLUXDB_TOKEN to the environment variables
                    if (this.isUIEnabled) {
                        dockerComposeConfig.services['aas-web-ui'].environment.INFLUXDB_TOKEN = "S18VeAlq042B4naMX31oqIaSGmUmOLAC-DV3VIdkxDJuAhTXLTVFEchyTSmCcUAmB7Wu94KgExzV8gJaDjzR3Q==";
                    }
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeInfluxDBFromDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject includes the influxDB configuration and remove it if present
                if (dockerComposeConfig.services['influxdb']) {
                    delete dockerComposeConfig.services['influxdb'];
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
                if (this.isUIEnabled) {
                    delete dockerComposeConfig.services['aas-web-ui'].environment.INFLUXDB_TOKEN;
                }
            }
        },

        addTelegrafToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the telegraf configuration and add it if not
                if (!dockerComposeConfig.services['telegraf']) {
                    dockerComposeConfig.services['telegraf'] = {
                        image: "telegraf:1.29.1",
                        container_name: "telegraf",
                        volumes: ["./telegraf/telegraf.conf:/etc/telegraf/telegraf.conf:ro"],
                        hostname: "basyx_host",
                        restart: "always",
                        depends_on: ["influxdb"]
                    };
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeTelegrafFromDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject includes the telegraf configuration and remove it if present
                if (dockerComposeConfig.services['telegraf']) {
                    delete dockerComposeConfig.services['telegraf'];
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        addDefaultMosquittoConfig() {
            // create mosquitto.conf File
            const mosquittoConfig = `# Default listener port for unencrypted connections (usually 1883)
listener 1883 0.0.0.0

# Path to the directory where persistence information is stored.
# Remove or comment out to disable persistence.
persistence true
persistence_location /mosquitto/data/

# Log settings
log_dest file /mosquitto/log/mosquitto.log
log_type all

# Connection settings
# Setting for maximum concurrent connections. -1 means unlimited.
max_connections -1

# Security settings
# Uncomment and set these for enabling username-password authentication.
#allow_anonymous false
#password_file /mosquitto/config/mosquitto_passwd

# Other settings like SSL/TLS, ACLs, etc., can also be configured as needed.
allow_anonymous true
            `;
            // create config object
            let configObject = {
                id: '517a0e1a-212f-42d3-bf3a-b6bfff6047db',
                name: 'mosquitto.conf',
                value: mosquittoConfig,
            }
            // check if the mosquitto.conf file already exists
            if (!this.mosquittoConfigObject) {
                this.appStore.setMosquittoConfig(configObject);
            }
        },

        removeMosquittoConfig() {
            // remove the mosquitto.conf file
            if (this.mosquittoConfigObject) {
                this.appStore.setMosquittoConfig(undefined);
            }
        },

        addMosquittoToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the Mosquitto configuration and add it if not
                if (!dockerComposeConfig.services['mosquitto']) {
                    dockerComposeConfig.services['mosquitto'] = {
                        image: "eclipse-mosquitto:2.0.15",
                        container_name: "mosquitto",
                        ports: ["1883:1883"],
                        volumes: ["./mosquitto:/mosquitto"],
                        restart: "always",
                        healthcheck: {
                            test: ["CMD-SHELL", "mosquitto_sub -p 1883 -t 'topic' -C 1 -E -i probe -W 3"],
                            interval: "5s",
                            timeout: "10s",
                            start_period: "1s",
                            retries: 3
                        }
                    };
                    dockerComposeConfigObject.value = dockerComposeConfig;
                    this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
                }
            }
        },

        removeMosquittoFromDockerCompose() {
            // delete the Mosquitto service from the docker-compose.yml config
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value;
            delete dockerComposeConfig.services['mosquitto'];
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        addTelegrafConf() {
            // add telegraf configuration file to the store
            // console.log(this.telegrafConfigFile);
            this.appStore.setTelegrafConf(this.telegrafConfigFile);
        },

        updateConfig() {
            if (this.selection === 'addTSD') {
                // add influxDB to the Docker Compose configuration
                this.addInfluxDBToDockerCompose();
                // add telegraf to the Docker Compose configuration
                this.addTelegrafToDockerCompose();
                let basyxConfig = [...this.basyxConfig];
                // Add InfluxDB
                basyxConfig.push({
                    id: 'cbd3567b-8ccc-4798-a501-1f97535912f7',
                    title: 'InfluxDB',
                    children: [
                        { id: 'd9ba7309-ad5b-42a5-a814-bac774500cc1', title: 'Config: default', type: 'config' },
                    ],
                });
                // Add Telegraf
                basyxConfig.push({
                    id: '0d9187db-6b75-4431-a589-d12238233f96',
                    title: 'Telegraf',
                    children: [
                        { id: '7d501e92-82f4-4003-bbda-39daeb685c5e', title: 'Config: default', type: 'config' },
                    ],
                });
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateTimeSeriesData(true);
            } else {
                // remove influxDB from the Docker Compose configuration
                this.removeInfluxDBFromDockerCompose();
                // remove Telegraf from the Docker Compose configuration
                this.removeTelegrafFromDockerCompose();
                let basyxConfig = [...this.basyxConfig];
                basyxConfig = basyxConfig.filter((item: any) => item.id !== 'cbd3567b-8ccc-4798-a501-1f97535912f7');
                basyxConfig = basyxConfig.filter((item: any) => item.id !== '0d9187db-6b75-4431-a589-d12238233f96');
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateTimeSeriesData(false);
            }
        },

        updateBrokerConfig() {
            if (this.brokerCheckbox) {
                // add Mosquitto to the Docker Compose configuration
                this.addMosquittoToDockerCompose();
                // add default Mosquitto Service configuration
                this.addDefaultMosquittoConfig();
                let basyxConfig = [...this.basyxConfig];
                // Add Mosquitto MQTT Broker if not already present
                if (!this.basyxConfig.some((item: any) => item.id === '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc')) {
                    basyxConfig.push({
                        id: '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc',
                        title: 'MQTT Broker',
                        children: [
                            { id: 'b4696d54-ad40-4cd1-b769-9cdce6133f9d', title: 'Config: default', type: 'config' },
                        ],
                    });
                }
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateMQTTBroker(true);
            } else {
                let basyxConfig = [...this.basyxConfig];
                if (!this.isMQTTEnabled) {
                    // remove Mosquitto Service configuration
                    this.removeMosquittoConfig();
                    // remove Mosquitto from the Docker Compose configuration
                    this.removeMosquittoFromDockerCompose();
                    basyxConfig = basyxConfig.filter((item: any) => item.id !== '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc');
                    this.appStore.updateBasyxConfig(basyxConfig);
                }
                this.appStore.updateMQTTBroker(false);
            }
        },
    },
});
</script>