<template>
    <v-container class="pa-0">
        <!-- Title Bar in the AAS List -->
        <v-card-title class="pl-1 pr-3">
            <v-row align="center">
                <!-- Reload Button -->
                <v-col cols="auto" class="pr-0">
                    <v-btn icon="mdi-reload" variant="plain" @click="reloadList()"></v-btn>
                </v-col>
                <!-- AAS Search Field -->
                <v-col class="pl-1">
                    <v-text-field variant="outlined" density="compact" hide-details label="Search for AAS..." clearable @update:modelValue="filterAASList"></v-text-field>
                </v-col>
            </v-row>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text style="overflow-y: auto" class="pa-0">
            <!-- AAS List -->
            <v-list nav>
                <!-- Single AAS -->
                <v-list-item v-for="AAS in AASData" :key="AAS['identification']['id']" @click="selectAAS(AAS)" class="bg-listItem mb-2" style="border-top: solid; border-right: solid; border-bottom: solid; border-width: 1px" :style="{ 'border-color': isSelected(AAS) ? '#009374 !important' : (isDark ? '#686868 !important' : '#ABABAB !important') }">
                    <!-- Tooltip with idShort and identification id -->
                    <v-tooltip activator="parent" open-delay="600" transition="slide-x-transition">
                        <div class="text-caption"><span class="font-weight-bold">{{ 'idShort: ' }}</span>{{ AAS['idShort'] }}</div>
                        <div class="text-caption"><span class="font-weight-bold">{{ 'Identification ID: ' }}</span>{{ AAS['identification']['id'] }}</div>
                    </v-tooltip>
                    <!-- idShort of the AAS -->
                    <template v-slot:title>
                        <div class="text-primary" style="z-index: 9999">{{ AAS['idShort'] }}</div>
                    </template>
                    <!-- identification id of the AAS -->
                    <template v-slot:subtitle>
                        <div v-html="AAS['identification']['id']"></div>
                    </template>
                    <!-- open Details Button (with Status Badge) -->
                    <template v-slot:append>
                        <!-- Badge that show's the Status of the AAS -->
                        <v-badge :model-value="AAS['status'] && AAS['status'] == 'offline'" icon="mdi-network-strength-4-alert" color="error" text-color="buttonText" inline></v-badge>
                        <!-- Information Button -->
                        <v-btn @click.stop="showAASDetails(AAS)" icon="mdi-information-outline" size="x-small" variant="plain" style="z-index: 9000"></v-btn>
                        <!-- Remove from Registry Button -->
                        <v-btn @click.stop="removeFromRegistry(AAS)" icon="mdi-close" size="x-small" variant="plain" style="z-index: 9000; margin-left: -6px"></v-btn>
                    </template>
                    <v-overlay :model-value="isSelected(AAS)" scrim="primary" style="opacity: 0.2" contained persistent></v-overlay>
                </v-list-item>
            </v-list>
            <!-- AAS Details (only visible if the Information Button is pressed on an AAS) -->
            <AASListDetails :detailsObject="detailsObject" :showDetailsCard="showDetailsCard" @close-details="showDetailsCard = false"/>
        </v-card-text>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, onBeforeMount } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import AASListDetails from './AASListDetails.vue';

export default defineComponent({
    name: 'AASList',
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server and the AAS

        AASListDetails, // AAS Details Component
    },
    mixins: [RequestHandling],

    setup () {
        const theme = useTheme()
        const store = useStore()

        return {
            theme, // Theme Object
            store, // Store Object
        }
    },

    data() {
        return {
            AASData: [],                // Variable to store the AAS Data
            unfilteredAASData: [],      // Variable to store the AAS Data before filtering
            detailsObject: {} as any,   // Variable to store the AAS Data of the currently selected AAS
            showDetailsCard: false,     // Variable to store if the Details Card should be shown
        }
    },

    mounted() {
        // Load the AAS List on Startup if the Registry URL is set
        if(this.registryURL !== '') {
            this.getAASData();
        }

        // check if the aas Query is set in the URL and if so load the AAS
        const searchParams = new URL(window.location.href).searchParams;
        const aasEndpoint = searchParams.get('aas');
        if (aasEndpoint) {
            // console.log('AAS Query is set: ', aasEndpoint);
            let aas = {} as any;
            let endpoints = [];
            endpoints.push({ address: aasEndpoint } );
            aas.endpoints = endpoints;
            // dispatch the AAS set by the URL to the store
            this.store.dispatch('dispatchSelectedAAS', aas);
        }
    },

    watch: {
        // Watch the Registry URL for changes and reload the AAS List if the URL changes
        registryURL() {
            if(this.registryURL !== '') {
                this.reloadList();
                this.addConnectionInterval();
            } else {
                this.AASData = [];
            }
        }
    },

    computed: {
        // get Registry URL from Store
        registryURL() {
            return this.store.getters.getRegistryURL;
        },

        // get the selected AAS from Store
        selectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },

        // gets loading State from Store
        loading() {
            return this.store.getters.getLoadingState;
        },
    },

    methods: {
        // Function to get the AAS Data from the Registry Server
        getAASData() {
            let path = this.registryURL + '/api/v1/registry';
            let context = 'retrieving AAS Data';
            let disableMessage = false;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) { // execute if the Registry Server is found
                    // sort data by idetification id (ascending) and store it in the AASData variable
                    let sortedData = response.data.sort((a: { [x: string]: { [x: string]: number; }; }, b: { [x: string]: { [x: string]: number; }; }) => (a['identification']['id'] > b['identification']['id']) ? 1 : -1);
                    // add status online to the AAS Data
                    sortedData.forEach((AAS: any) => {
                        AAS['status'] = 'online';
                    });
                    this.AASData = sortedData; // store the sorted data in the AASData variable
                    this.unfilteredAASData = sortedData; // make a copy of the sorted data and store it in the unfilteredAASData variable
                    this.checkAASStatus(); // check the AAS Status
                } else { // execute if the Registry Server is not found
                    this.store.dispatch('dispatchRegistryURL', ''); // clear the URL in the store
                }
            });
        },

        // Function which adds an Interval to check if the Shells in the Registry Server are still available
        addConnectionInterval() {
            // check if the Registry URL is set
            if(this.registryURL !== '') {
                // add an Interval to check if the Shells in the Registry Server are still available
                setInterval(() => {
                    // Check if the AAS is online
                    this.checkAASStatus();
                }, 30000); // check every 60 seconds
            }
        },

        // Function to check the AAS Status
        checkAASStatus() {
            // console.log('Check AAS Status: ', AAS);
            // iterate over all AAS in the AAS List
            this.AASData.forEach((AAS: any) => {
                let path = AAS.endpoints[0].address + '/submodels';
                let context = 'evaluating AAS Status';
                let disableMessage = true;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    if (response.success) { // execute if the Registry Server is found
                        AAS.status = 'online';
                    } else { // execute if the Registry Server is not found
                        AAS.status = 'offline';
                    }
                });
            });
        },

        // Function to reload the AAS List
        reloadList() {
            this.getAASData();
        },

        // Function to filter the AAS List
        filterAASList(value: string) {
            // console.log('Filter AAS List: ', value);
            // if the Search Field is empty, show all AAS
            if(value === '' || value === null) {
                this.AASData = this.unfilteredAASData;
            } else {
                // filter the AAS List by the Search Field Value
                let filteredAASData = this.unfilteredAASData.filter((AAS: { [x: string]: string; }) => AAS['idShort'].toLowerCase().includes(value.toLowerCase()));
                this.AASData = filteredAASData;
            }
        },

        // Function to select an AAS
        selectAAS(AAS: any) {
            // console.log('Select AAS: ', AAS);
            // return if loading state is true -> prevents multiple requests
            if(this.loading) {
                this.store.dispatch('getSnackbar', { status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'Please wait for the current Request to finish.' });
                return;
            }
            // add aas endpoint as quaery to the router
            this.$router.push({ query: { aas: AAS.endpoints[0].address } });
            // dispatch the selected AAS to the Store
            this.store.dispatch('dispatchSelectedAAS', AAS);
        },

        // checks if the AAS is selected
        isSelected(AAS: any) {
            if (this.selectedAAS === undefined || this.selectedAAS === null || Object.keys(this.selectedAAS).length === 0) {
                return false;
            }
            return this.selectedAAS['endpoints'][0]['address'] === AAS['endpoints'][0]['address'];
        },

        // Function to display the AAS Details
        showAASDetails(AAS: any) {
            // console.log('Show Details: ', AAS);
            this.detailsObject = AAS;
            if(AAS) this.showDetailsCard = true;
        },

        // Function to remove the AAS from the Registry Server
        removeFromRegistry(AAS: any) {
            // console.log('Remove AAS: ', AAS);
            // return if loading state is true -> prevents multiple requests
            if(this.loading) {
                this.store.dispatch('getSnackbar', { status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'Please wait for the current Request to finish.' });
                return;
            }
            // show a confirmation Dialog to delete the AAS
            if(confirm('Are you sure you want to delete the AAS from the Registry?')) {
                // execute if the user confirms the removal
                let path = this.registryURL + '/api/v1/registry/' + AAS.identification.id;
                let context = 'removing AAS from Registry';
                let disableMessage = false;
                this.deleteRequest(path, context, disableMessage).then((response: any) => {
                    if (response.success) { // execute if deletion was successful
                        this.reloadList(); // reload the AAS List
                    }
                });
            }
        },
    },
});
</script>

<style>
.v-card--reveal {
    bottom: 0;
    opacity: .96;
    position: absolute;
    width: 100%;
}
</style>
