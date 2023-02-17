<template>
    <v-container fluid class="pa-0">
        <!-- List of all available Submodel and SubmodelElement Plugins matched by their SemanticID -->
        <template v-if="Object.keys(submodelElementData).length > 0 && submodelElementData.semanticId && submodelElementData.semanticId.keys && submodelElementData.semanticId.keys.length > 0">
            <HTWFuehrungskomponente v-if="checkSemanticId('http://htw-berlin.de/smc_statemachine')"></HTWFuehrungskomponente>
            <DigitalNameplate v-if="checkSemanticId('https://admin-shell.io/zvei/nameplate/1/0/Nameplate')"></DigitalNameplate>
            <HelloWorldPlugin v-if="checkSemanticId('http://hello.world.de/plugin_submodel')"></HelloWorldPlugin>
            <JSONArrayProperty v-if="checkSemanticId('http://iese.fraunhofer.de/prop_jsonarray')"></JSONArrayProperty>
        </template>
        <!-- List of all File-Plugins matched by theyr mimeType -->
        <template v-if="Object.keys(submodelElementData).length > 0 && submodelElementData.modelType.name == 'File'">
            <ImagePreview v-if="submodelElementData.mimeType && submodelElementData.mimeType.includes('image')"></ImagePreview>
            <PDFPreview v-if="submodelElementData.mimeType && submodelElementData.mimeType.includes('pdf')"></PDFPreview>
        </template>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';

import HTWFuehrungskomponente from './HTWFuehrungskomponente.vue';
import DigitalNameplate from './DigitalNameplate.vue';
import HelloWorldPlugin from './HelloWorldPlugin.vue';
import JSONArrayProperty from './JSONArrayProperty.vue';

import ImagePreview from './ImagePreview.vue';
import PDFPreview from './PDFPreview.vue';

export default defineComponent({
    name: 'SubmodelEntrypoint',
    components: {
        HTWFuehrungskomponente, // SubmodelElementCollection Plugin for Führungskomponenten
        DigitalNameplate, // Submodel Plugin for Digital Nameplates
        HelloWorldPlugin, // Submodel Plugin used as HelloWorld Example
        JSONArrayProperty, // Property Plugin to visualize JSON Arrays

        ImagePreview, // File Plugin for Image Preview
        PDFPreview, // File Plugin for PDF Preview
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
