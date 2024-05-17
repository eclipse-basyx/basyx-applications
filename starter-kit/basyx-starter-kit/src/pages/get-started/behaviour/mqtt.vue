<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">MQTT Eventing</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The BaSyx AAS Environment inherently facilitates <a class="text-primary" style="text-decoration: none" href="https://mqtt.org/" target="_blank">MQTT</a> eventing, seamlessly enabling event-driven responses to alterations within your digital twins' data.
        </p>
        <p class="text-normalText mt-3 mb-2 text-subtitle-1">
            When using this feature, BaSyx will act as a publishing MQTT client transmitting AAS data to a MQTT broker. In this case the <a class="text-primary" style="text-decoration: none" href="https://mosquitto.org/" target="_blank">Eclipse Mosquitto</a> broker is used.
        </p>
        <!-- User selection for MQTT Eventing -->
        <v-alert color="primary" variant="outlined" class="bg-alertCard mt-8 mb-8">
            <v-radio-group v-model="selection" color="primary" hide-details @update:modelValue="updateConfig" class="text-normalText font-weight-medium">
                <v-radio label="Don't add MQTT eventing feature" value="noMQTT"></v-radio>
                <v-radio label="Add MQTT eventing feature" value="addMQTT"></v-radio>
            </v-radio-group>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/behaviour/persistence">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/behaviour/time-series">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'MQTT',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noMQTT',
        };
    },

    mounted() {
        if (this.isMQTTEnabled) {
            this.selection = 'addMQTT';
            // add default Mosquitto Service configuration
            this.addDefaultMosquittoConfig();
            // add Mosquitto to the Docker Compose configuration
            this.addMosquittoToDockerCompose();
            // update aas-env.properties to include MQTT
            this.addMQTTToAasEnvConfig();
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if MQTT is enabled
        isMQTTEnabled(): boolean {
            return this.appStore.getMQTT;
        },

        // check ig MQTT Broker is enabled
        isMQTTBrokerEnabled(): boolean {
            return this.appStore.getMQTTBroker;
        },

        // get the mosquitto.conf config object from the store
        mosquittoConfigObject() {
            return this.appStore.getMosquittoConfig;
        },

        // get the aas-env.properties config object from the store
        aasEnvConfigObject() {
            return this.appStore.getAasEnvConfig;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },
    },

    methods: {
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
                    // add mosquitto to the depends_on section of the aas-env
                    dockerComposeConfig.services['aas-env'].depends_on['mosquitto'] = { condition: 'service_healthy' };
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
            // remove mosquitto from the depends_on section of the aas-env
            delete dockerComposeConfig.services['aas-env'].depends_on['mosquitto'];
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        addMQTTToAasEnvConfig() {
            // console.log('addMQTTToAasEnvConfig', this.aasEnvConfigObject);
            let aasEnvConfigObject = { ...this.aasEnvConfigObject };
            let aasEnvConfig = aasEnvConfigObject.value;
            // check if the aasEnvConfigObject already includes a MQTT configuration and add it if not
            if (!aasEnvConfig['mqtt.clientId']) {
                aasEnvConfig['mqtt.clientId'] = "AAS-Env-8081";
            }
            if (!aasEnvConfig['mqtt.hostname']) {
                aasEnvConfig['mqtt.hostname'] = "mosquitto";
            }
            if (!aasEnvConfig['mqtt.port']) {
                aasEnvConfig['mqtt.port'] = "1883";
            }
            if (!aasEnvConfig['basyx.aasrepository.feature.mqtt.enabled']) {
                aasEnvConfig['basyx.aasrepository.feature.mqtt.enabled'] = true;
            }
            if (!aasEnvConfig['basyx.submodelrepository.feature.mqtt.enabled']) {
                aasEnvConfig['basyx.submodelrepository.feature.mqtt.enabled'] = true;
            }
            // update the aas-env.properties config object in the store
            aasEnvConfigObject.value = aasEnvConfig;
            this.appStore.setAasEnvConfig(aasEnvConfigObject);
        },

        removeMQTTFromAasEnvConfig() {
            // console.log('removeMQTTFromAasEnvConfig', this.aasEnvConfigObject);
            let aasEnvConfigObject = { ...this.aasEnvConfigObject };
            let aasEnvConfig = aasEnvConfigObject.value;
            // remove MQTT configuration from aas-env.properties
            delete aasEnvConfig['mqtt.clientId'];
            delete aasEnvConfig['mqtt.hostname'];
            delete aasEnvConfig['mqtt.port'];
            delete aasEnvConfig['basyx.aasrepository.feature.mqtt.enabled'];
            delete aasEnvConfig['basyx.submodelrepository.feature.mqtt.enabled'];
            // update the aas-env.properties config object in the store
            aasEnvConfigObject.value = aasEnvConfig;
            this.appStore.setAasEnvConfig(aasEnvConfigObject);
        },

        updateConfig() {
            if (this.selection === 'addMQTT') {
                // add default Mosquitto Service configuration
                this.addDefaultMosquittoConfig();
                // add Mosquitto to the Docker Compose configuration
                this.addMosquittoToDockerCompose();
                // update aas-env.properties to include MQTT
                this.addMQTTToAasEnvConfig();
                let basyxConfig = [...this.basyxConfig];
                // update AAS Environment,  -> add MQTT Eventing
                basyxConfig.forEach((item: any) => {
                    if (item.id === '5289baf4-21a8-4c22-8361-8a4b8bfb5716') {
                        item.children.push({ id: '3e347723-fe5e-48cb-bb92-6ffa45219f6e', title: 'MQTT Eventing', type: 'communication' });
                    }
                });
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
                this.appStore.updateMQTT(true);
            } else {
                // remove Mosquitto Service configuration
                this.removeMosquittoConfig();
                // remove Mosquitto from the Docker Compose configuration
                this.removeMosquittoFromDockerCompose();
                // remove MQTT from aas-env.properties
                this.removeMQTTFromAasEnvConfig();
                let basyxConfig = [...this.basyxConfig];
                if (!this.isMQTTBrokerEnabled) {
                    basyxConfig = basyxConfig.filter((item: any) => item.id !== '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc');
                }
                basyxConfig.forEach((item: any) => {
                    item.children = item.children.filter((child: any) => child.id !== '3e347723-fe5e-48cb-bb92-6ffa45219f6e');
                });
                this.appStore.updateBasyxConfig(basyxConfig);
                this.appStore.updateMQTT(false);
            }
        },
    },
});
</script>