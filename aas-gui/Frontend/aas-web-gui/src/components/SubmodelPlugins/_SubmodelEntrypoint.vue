<template>
    <v-container fluid class="pa-0">
        <!-- List of all available Submodel and SubmodelElement Plugins matched by their SemanticID -->
        <template v-if="Object.keys(submodelElementData).length > 0 && submodelElementData.semanticId && submodelElementData.semanticId.keys && submodelElementData.semanticId.keys.length > 0">
            <HTWFuehrungskomponente v-if="checkSemanticId('http://htw-berlin.de/smc_statemachine')"></HTWFuehrungskomponente>
        </template>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';

import HTWFuehrungskomponente from './HTWFuehrungskomponente.vue';

export default defineComponent({
    name: 'SubmodelEntrypoint',
    components: {
        HTWFuehrungskomponente, // SubmodelElementCollection Submodel Plugin for Führungskomponenten
    },

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

        // Function to check if the SemanticID of a SubmodelElement matches the given SemanticID
        checkSemanticId(semanticId: string): boolean {
            let result = false;
            this.submodelElementData.semanticId.keys.forEach((key: any) => {
                if (key.value === semanticId) {
                    result = true;
                }
            });
            return result;
        },
    },
});
</script>
