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
                    <!-- Configure AAS Repository URL -->
                    <v-text-field variant="outlined" density="compact" hide-details class="my-3" label="AAS Repository URL" v-model="AASRepoURL" @keydown.native.enter="connectToEnvironment('AAS')">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="connectToEnvironment('AAS')" :loading="loadingAASRepo">Connect</v-btn>
                        </template>
                    </v-text-field>
                    <!-- Configure Submodel Repository URL -->
                    <v-text-field variant="outlined" density="compact" hide-details class="my-3" label="Submodel Repository URL" v-model="SubmodelRepoURL" @keydown.native.enter="connectToEnvironment('Submodel')">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="connectToEnvironment('Submodel')" :loading="loadingSubmodelRepo">Connect</v-btn>
                        </template>
                    </v-text-field>
                    <!-- Configure Concept Description Repository URL -->
                    <v-text-field variant="outlined" density="compact" hide-details class="my-3" label="Concept Description Repository URL" v-model="ConceptDescriptionRepoURL" @keydown.native.enter="connectToEnvironment('ConceptDescription')">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="connectToEnvironment('ConceptDescription')" :loading="loadingConceptDescriptionRepo">Connect</v-btn>
                        </template>
                    </v-text-field>
                    <!-- TODO: Update AASX Upload -->
                    <!-- <v-divider v-if="aasServerURL && aasServerURL != ''"></v-divider> -->
                    <!-- AASX Upload to AAS Server -->
                    <!-- <v-file-input v-if="aasServerURL && aasServerURL != ''" variant="outlined" density="compact" :multiple="false" v-model="aasxFile" clearable hide-details class="my-1 mt-3" label="AASX File Upload" accept=".aasx">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="uploadAASXFile()">Upload</v-btn>
                        </template>
                    </v-file-input> -->
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
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useWidgetsStore } from '@/store/WidgetsStore';
import { useTheme } from 'vuetify';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'MainMenu',
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server
    },
    mixins: [RequestHandling],

    setup() {
        const theme = useTheme()
        const navigationStore = useNavigationStore()
        const widgetsStore = useWidgetsStore()

        return {
            theme, // Theme Object
            navigationStore, // NavigationStore Object
            widgetsStore, // WidgetsStore Object
        }
    },

    data() {
        return {
            registryURL: '',                        // Registry URL
            AASRepoURL: '',                         // AAS Repository URL
            SubmodelRepoURL: '',                    // Submodel Repository URL
            ConceptDescriptionRepoURL: '',          // Concept Description Repository URL
            loadingRegistry: false,                 // Loading State of the Registry Connection
            loadingAASRepo: false,                  // Loading State of the AAS Repository Connection
            loadingSubmodelRepo: false,             // Loading State of the Submodel Repository Connection
            loadingConceptDescriptionRepo: false,   // Loading State of the Concept Description Repository Connection
            aasxFile: [] as any,                    // AASX File to upload
        }
    },

    mounted() {
        this.registryURL = this.registryServerURL;
        this.AASRepoURL = this.aasRepoURL;
        this.SubmodelRepoURL = this.submodelRepoURL;
        this.ConceptDescriptionRepoURL = this.conceptDescriptionRepoURL;
    },

    computed: {
        // Check if the current Platform is Mobile
        isMobile() {
            return this.platform.android || this.platform.ios ? true : false;
        },

        // get Platform from store
        platform() {
            return this.navigationStore.getPlatform;
        },

        // get Registry Server URL from Store
        registryServerURL() {
            return this.navigationStore.getRegistryURL;
        },

        // Get the AAS Repository URL from the Store
        aasRepoURL() {
            return this.navigationStore.getAASRepoURL;
        },

        // Get the Submodel Repository URL from the Store
        submodelRepoURL() {
            return this.navigationStore.getSubmodelRepoURL;
        },

        // Get the Concept Description Repository URL from the Store
        conceptDescriptionRepoURL() {
            return this.navigationStore.getConceptDescriptionRepoURL;
        },

        // Get the Activation Status of the Widget Feature
        WidgetFeatureActive() {
            return this.widgetsStore.getWidgetFeatureActive;
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
                let path = this.registryURL + '/shell-descriptors';
                let context = 'connecting to Registry Server'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    this.loadingRegistry = false;
                    if (response.success) {
                        this.navigationStore.dispatchRegistryURL(this.registryURL); // save the URL in the NavigationStore
                        this.checkWidgetApi(); // check if the Widget API is available
                        window.localStorage.setItem('registryURL', this.registryURL); // save the URL in the local storage
                    } else {
                        this.navigationStore.dispatchRegistryURL(''); // clear the Registry URL in the NavigationStore
                        this.navigationStore.dispatchWidgetApiURL(''); // clear the Widget Api URL in the NavigationStore
                        window.localStorage.removeItem('registryURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // Function to connect to the respective Repository
        connectToEnvironment(RepoType: string) {
            // console.log('connect to ' + RepoType + ' Repository: ' + (this as any)[RepoType + 'RepoURL']);
            if ((this as any)[RepoType + 'RepoURL'] != '') {
                (this as any)['loading' + RepoType + 'Repo'] = true;
                let path = (this as any)[RepoType + 'RepoURL'];
                let context = 'connecting to ' + RepoType + ' Repository'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    (this as any)['loading' + RepoType + 'Repo'] = false;
                    if (response.success) {
                        this.navigationStore.dispatchRepoURL(RepoType, (this as any)[RepoType + 'RepoURL']); // save the URL in the NavigationStore
                        window.localStorage.setItem(RepoType + 'RepoURL', (this as any)[RepoType + 'RepoURL']); // save the URL in the local storage
                    } else {
                        this.navigationStore.dispatchRepoURL(RepoType, ''); // clear the URL in the NavigationStore
                        window.localStorage.removeItem(RepoType + 'RepoURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // // Function to upload the AASX File to the AAS-Server (TODO: Update AASX Upload)
        // uploadAASXFile() {
        //     // console.log('upload aasx file: ' + this.aasxFile);
        //     // check if a file is selected
        //     if (this.aasxFile.length == 0) return;

        //     let context = 'uploading AASX File';
        //     let disableMessage = false;
        //     let path = this.serverURL + '/shells/aasx';
        //     var headers = new Headers();
        //     var formData = new FormData();
        //     formData.append("file", this.aasxFile[0]);
        //     // Send Request to upload the file
        //     this.postRequest(path, formData, headers, context, disableMessage).then((response: any) => {
        //         if (response.success) {
        //             this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'success', btnColor: 'buttonText', text: 'AASX-File uploaded.' }); // Show Success Snackbar
        //             this.aasxFile = []; // clear the AASX File
        //             // reload the AAS list
        //             this.navigationStore.dispatchTriggerAASListReload(true);
        //         }
        //     });
        // },

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
                    this.navigationStore.dispatchWidgetApiURL(WidgetApiURL); // save the Widget API URL in the NavigationStore
                    this.widgetsStore.dispatchWidgetFeatureActive(true); // set the Widget Feature Activation Status to true
                } else {
                    this.navigationStore.dispatchWidgetApiURL(''); // clear the Widget API URL in the NavigationStore
                    this.widgetsStore.dispatchWidgetFeatureActive(false); // set the Widget Feature Activation Status to false
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