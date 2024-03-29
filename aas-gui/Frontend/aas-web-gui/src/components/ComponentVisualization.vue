<template>
    <v-container fluid class="pa-0">
        <v-card color="rgba(0,0,0,0)" elevation="0">
            <v-card-title style="padding: 15px 16px 16px">
                <v-row justify="space-between" align="center">
                    <v-col>
                        <div>Visualization</div>
                    </v-col>
                    <v-spacer></v-spacer>
                </v-row>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text v-if="submodelElementData && Object.keys(submodelElementData).length > 0" style="overflow-y: auto; height: calc(100vh - 170px)">
                <!-- Add Plugins matched on SemanticId's inside the SubmodelEntrypoint -->
                <SubmodelEntrypoint :submodelElementData="submodelElementData" :selectedNode="SelectedNodeToTransfer"></SubmodelEntrypoint>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '@/mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

import SubmodelEntrypoint from './SubmodelPlugins/_SubmodelEntrypoint.vue';

export default defineComponent({
    name: 'ComponentVisualization',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        SubmodelElementHandling, // Mixin to handle the SubmodelElements

        SubmodelEntrypoint, // Submodel Plugin Entrypoint Component
    },
    mixins: [RequestHandling, SubmodelElementHandling],

    setup() {
        const navigationStore = useNavigationStore()
        const aasStore = useAASStore()

        return {
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            submodelElementData: {} as any, // SubmodelElement Data
            requestInterval: null as any, // interval to send requests to the AAS
        }
    },

    mounted() {
        // initialize the Component if zhe component got mounted on mobile devices (needed there because it is rendered in a separate view)
        if (Object.keys(this.SelectedNode).length > 0 && this.isMobile) {
            this.initializeView();
        }
    },

    beforeUnmount() {
        clearInterval(this.requestInterval); // clear old interval
    },

    watch: {
        // Resets the submodelElementData when the AAS Registry changes
        aasRegistryServerURL() {
            if (!this.aasRegistryServerURL) {
                this.submodelElementData = {};
            }
        },

        // Resets the submodelElementData when the Submodel Registry changes
        submodelRegistryServerURL() {
            if (!this.submodelRegistryServerURL) {
                this.submodelElementData = {};
            }
        },

        // Resets the submodelElementData when the AAS changes
        SelectedAAS() {
            this.submodelElementData = {};
        },

        // Watch for changes in the SelectedNode and (re-)initialize the Component
        SelectedNode: {
            deep: true,
            handler() {
                // clear old submodelElementData
                this.submodelElementData = {};
                this.initializeView(); // initialize list
            }
        },

        // Watch for changes in the RealTimeDataObject and (re-)initialize the Component
        RealTimeObject: {
            deep: true,
            handler() {
                // clear old submodelElementData
                this.submodelElementData = {};
                this.initializeView(); // initialize list
            }
        },
    },

    computed: {
        // get AAS Registry URL from Store
        aasRegistryServerURL() {
            return this.navigationStore.getAASRegistryURL;
        },

        // get the Submodel Registry URL from Store
        submodelRegistryServerURL() {
            return this.navigationStore.getSubmodelRegistryURL;
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        // return the selected Node with the full path to the SubmodelElement
        SelectedNodeToTransfer() {
            let aas = { ...this.aasStore.getSelectedAAS };
            let node = { ...this.aasStore.getSelectedNode };
            if (aas && aas.endpoints && aas.endpoints[0] && aas.endpoints[0].protocolInformation.href) {
                node.pathFull = aas.endpoints[0].protocolInformation.href + '/' + node.path;
            }
            // console.log('SelectedNodeToTransfer: ', node);
            return node;
        },

        // Get the real-time object from the store
        RealTimeObject() {
            return this.aasStore.getRealTimeObject;
        },

        // Check if the current Platform is Mobile
        isMobile() {
            return this.platform.android || this.platform.ios ? true : false;
        },

        // get Platform from store
        platform() {
            return this.navigationStore.getPlatform;
        },
    },

    methods: {
        // Initialize the Component
        initializeView() {
            // console.log('Selected Node: ', this.RealTimeObject);
            // Check if a Node is selected
            if (Object.keys(this.RealTimeObject).length == 0) {
                this.submodelElementData = {}; // Reset the SubmodelElement Data when no Node is selected
                return;
            }
            this.submodelElementData = { ...this.RealTimeObject }; // create local copy of the SubmodelElement Object
            // console.log('SubmodelElement Data (ComponentVisualization): ', this.submodelElementData);
        },
    },
});
</script>
