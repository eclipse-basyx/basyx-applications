<template>
    <v-container fluid class="pa-0">
        <v-card height="calc(100vh - 105px)" color="rgba(0,0,0,0)" elevation="0">
            <v-card-title style="padding: 15px 16px 16px">Visualization</v-card-title>
            <v-divider></v-divider>
            <v-card-text style="height: calc(100vh - 170px); overflow-y: auto">
                <!-- TODO: Add Widget configuration and visualization here -->
                <!-- TODO: Add Plugins matched on SemanticId's here -->
                <SubmodelEntrypoint></SubmodelEntrypoint>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../mixins/RequestHandling';

import SubmodelEntrypoint from './SubmodelPlugins/_SubmodelEntrypoint.vue';

export default defineComponent({
    name: 'ComponentVisualization',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        SubmodelEntrypoint, // Submodel Plugin Entrypoint Component
    },
    mixins: [RequestHandling],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            submodelElementData: {} as any, // SubmodelElement Data
        }
    },

    mounted() {
    },

    watch: {
        // Resets the SubmodelElementView when the Registry Server changes
        registryServerURL() {
            if (!this.registryServerURL) {
                this.submodelElementData = {};
            }
        },

        // Resets the SubmodelElementView when the AAS changes
        SelectedAAS() {
            this.submodelElementData = {};
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
        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.store.getters.getSelectedNode;
        },

        // Get the real-time object from the store
        RealTimeObject() {
            return this.store.getters.getRealTimeObject;
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
            // console.log('SubmodelElement Data: ', this.submodelElementData)
        },
    },
});
</script>
