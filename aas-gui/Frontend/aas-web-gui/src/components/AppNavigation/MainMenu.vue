<template>
    <v-container fluid class="pa-0">
        <v-card :min-width="isMobile ? 0 : 700" :flat="isMobile ? true : false" :color="isMobile ? 'card' : ''">
            <v-row>
                <v-col :cols="isMobile ? 12 : 4" :class="isMobile ? 'bg-card' : ''">
                    <v-card variant="flat" style="border-radius: 0px" class="pt-3" color="card">
                        <template v-if="!isMobile">
                            <span class="mx-3 text-primary">General Settings</span>
                            <v-list nav class="pa-0 mx-3 mt-3 bg-card">
                                <v-list-item>
                                    <v-list-item-title>Endpoints</v-list-item-title>
                                    <template v-slot:append>
                                        <v-icon>mdi-chevron-right</v-icon>
                                    </template>
                                </v-list-item>
                            </v-list>
                            <v-divider class="mb-3"></v-divider>
                        </template>
                        <span class="mx-3 text-primary">Switch to</span>
                        <!-- Select the view you want to use -->
                        <v-list nav class="bg-card pa-0" :class="isMobile ? 'mx-3 mt-3' : 'ma-3'">
                            <v-list-item v-if="WidgetFeatureActive" to="/dashboard" @click="closeMenu()">
                                <v-list-item-title>Dashboard</v-list-item-title>
                            </v-list-item>
                            <v-list-item :to="isMobile ? '/aaslist' : '/'" @click="closeMenu()">
                                <v-list-item-title>AAS View</v-list-item-title>
                            </v-list-item>
                        </v-list>
                    </v-card>
                </v-col>
                <v-divider :vertical="isMobile ? false : true" style="margin-left: -12px"></v-divider>
                <v-col :cols="isMobile ? 12: 8" :class="isMobile ? 'pt-0 mb-2 px-6' : 'pt-6'" class="bg-card">
                    <!-- Configure Registry URL -->
                    <v-text-field variant="outlined" density="compact" hide-details class="my-3" label="Registry Server URL" v-model="registryURL" @keydown.native.enter="connectToRegistry()">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="connectToRegistry()" :loading="loadingRegistry">Connect</v-btn>
                        </template>
                    </v-text-field>
                    <!-- Configure AAS-Server URL -->
                    <v-text-field variant="outlined" density="compact" hide-details class="my-3" label="AAS-Server URL" v-model="aasServerURL" @keydown.native.enter="connectToAASServer()">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="connectToAASServer()" :loading="loadingServer">Connect</v-btn>
                        </template>
                    </v-text-field>
                    <v-divider v-if="aasServerURL && aasServerURL != ''"></v-divider>
                    <!-- AASX Upload to AAS Server -->
                    <v-file-input v-if="aasServerURL && aasServerURL != ''" variant="outlined" density="compact" :multiple="false" v-model="aasxFile" clearable hide-details class="my-1 mt-3" label="AASX File Upload" accept=".aasx">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="uploadAASXFile()">Upload</v-btn>
                        </template>
                    </v-file-input>
                </v-col>
            </v-row>
            <!-- Platform I 4.0 Logo -->
            <v-row v-if="isMobile">
                <v-col align="center">
                    <v-img src="I40.png" max-width="260px" :style="{ filter: isDark ? 'invert(1)' : 'invert(0)' }">
                        <template #sources>
                            <source srcset="@/assets/I40.png">
                        </template>
                    </v-img>
                </v-col>
            </v-row>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import { useTheme } from 'vuetify';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'MainMenu',
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server
    },
    mixins: [RequestHandling],

    setup() {
        const store = useStore()
        const theme = useTheme()

        return {
            store, // Store Object
            theme, // Theme Object
        }
    },

    data() {
        return {
            registryURL: '',        // Registry URL
            aasServerURL: '',       // AAS-Server URL
            loadingRegistry: false, // Loading State of the Registry Connection
            loadingServer: false,   // Loading State of the AAS-Server Connection
            aasxFile: [] as any,    // AASX File to upload
        }
    },

    mounted() {
        this.registryURL = this.registryServerURL;
        this.aasServerURL = this.serverURL;
    },

    computed: {
        // Check if the current Platform is Mobile
        isMobile() {
            return this.platform.android || this.platform.ios ? true : false;
        },

        // get Platform from store
        platform() {
            return this.store.getters.getPlatform;
        },

        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

        // Get the AAS Server URL from the Store
        serverURL() {
            return this.store.getters.getAASServerURL;
        },

        // Get the Activation Status of the Widget Feature
        WidgetFeatureActive() {
            return this.store.getters.getWidgetFeatureActive;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark;
        },
    },

    methods: {
        // Function to connect to the Registry Server
        connectToRegistry() {
            // console.log('connect to server: ' + this.registryURL);
            if (this.registryURL != '') {
                this.loadingRegistry = true;
                let path = this.registryURL + '/api/v1/registry';
                let context = 'connecting to Registry Server'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    this.loadingRegistry = false;
                    if (response.success) {
                        this.store.dispatch('dispatchRegistryURL', this.registryURL); // save the URL in the store
                        this.checkWidgetApi(); // check if the Widget API is available
                        window.localStorage.setItem('registryURL', this.registryURL); // save the URL in the local storage
                    } else {
                        this.store.dispatch('dispatchRegistryURL', ''); // clear the Registry URL in the store
                        this.store.dispatch('dispatchWidgetApiURL', ''); // clear the Widget Api URL in the store
                        window.localStorage.removeItem('registryURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // Function to connect to the AAS-Server
        connectToAASServer() {
            // console.log('connect to aas server: ' + this.aasServerURL);
            if (this.aasServerURL != '') {
                this.loadingServer = true;
                let path = this.aasServerURL + '/shells';
                let context = 'connecting to AAS Server'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    this.loadingServer = false;
                    if (response.success) {
                        this.store.dispatch('dispatchAASServerURL', this.aasServerURL); // save the URL in the store
                        window.localStorage.setItem('aasServerURL', this.aasServerURL); // save the URL in the local storage
                    } else {
                        this.store.dispatch('dispatchAASServerURL', ''); // clear the URL in the store
                        window.localStorage.removeItem('aasServerURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // Function to upload the AASX File to the AAS-Server
        uploadAASXFile() {
            // console.log('upload aasx file: ' + this.aasxFile);
            // check if a file is selected
            if (this.aasxFile.length == 0) return;

            let context = 'uploading AASX File';
            let disableMessage = false;
            let path = this.serverURL + '/shells/aasx';
            var headers = new Headers();
            var formData = new FormData();
            formData.append("file", this.aasxFile[0]);
            // Send Request to upload the file
            this.postRequest(path, formData, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    this.store.dispatch('getSnackbar', { status: true, timeout: 4000, color: 'success', btnColor: 'buttonText', text: 'AASX-File uploaded.' }); // Show Success Snackbar
                    this.aasxFile = []; // clear the AASX File
                    // reload the AAS list
                    this.store.dispatch('dispatchTriggerAASListReload', true);
                }
            });
        },

        // Function to check if Widget API is available and set the Widget Feature Activation Status in the store
        checkWidgetApi() {
            let WidgetApiURL = this.registryURL.split(':') as any;
            WidgetApiURL[2] = '4000';
            // join the array to a string
            WidgetApiURL = WidgetApiURL.join(':');
            // check if the Widget API is available
            let path = WidgetApiURL + '/api/getallwidgets';
            let context = 'trying to connect to Widget API';
            let disableMessage = true;
            // Send Request to get all Widgets
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) {
                    this.store.dispatch('dispatchWidgetApiURL', WidgetApiURL); // save the Widget API URL in the store
                    this.store.dispatch('dispatchWidgetFeatureActive', true); // set the Widget Feature Activation Status to true
                } else {
                    this.store.dispatch('dispatchWidgetApiURL', ''); // clear the Widget API URL in the store
                    this.store.dispatch('dispatchWidgetFeatureActive', false); // set the Widget Feature Activation Status to false
                }
            });
        },

        // Function to close the menu
        closeMenu() {
            this.$emit('closeMenu');
        },
    },
});
</script>