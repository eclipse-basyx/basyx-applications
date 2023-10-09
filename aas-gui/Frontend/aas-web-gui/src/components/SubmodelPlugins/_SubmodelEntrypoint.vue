<template>
    <v-container fluid class="pa-0">
        <!-- List of all available Submodel and SubmodelElement Plugins matched by their SemanticID -->
        <template v-if="SelectedNode && Object.keys(SelectedNode).length > 0 && Object.keys(submodelElementData).length > 0 && submodelElementData.semanticId && submodelElementData.semanticId.keys && submodelElementData.semanticId.keys.length > 0">
            <HTWFuehrungskomponente v-if="checkSemanticId('http://htw-berlin.de/smc_statemachine')" :submodelElementData="submodelElementData" :selectedNode="selectedNode"></HTWFuehrungskomponente>
            <DigitalNameplate v-if="checkSemanticId('https://admin-shell.io/zvei/nameplate/1/0/Nameplate')" :submodelElementData="submodelElementData"></DigitalNameplate>
            <JSONArrayProperty v-if="checkSemanticId('http://iese.fraunhofer.de/prop_jsonarray')" :submodelElementData="submodelElementData"></JSONArrayProperty>
            <!-- Plugins added by the user are dynamically registered here -->
            <component v-for="(plugin, i) in filteredPlugins" :key="i" :is="plugin.name" :submodelElementData="submodelElementData"></component>
        </template>
        <!-- List of all File/Blob-Plugins matched by their mimeType -->
        <template v-if="SelectedNode && Object.keys(SelectedNode).length > 0 && Object.keys(submodelElementData).length > 0 && (submodelElementData.modelType == 'File' || submodelElementData.modelType == 'Blob')">
            <ImagePreview v-if="submodelElementData.mimeType && submodelElementData.mimeType.includes('image')" :submodelElementData="submodelElementData"></ImagePreview>
            <PDFPreview v-if="submodelElementData.mimeType && submodelElementData.mimeType.includes('pdf')" :submodelElementData="submodelElementData"></PDFPreview>
        </template>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';

import HTWFuehrungskomponente from './HTWFuehrungskomponente.vue';
import DigitalNameplate from './DigitalNameplate.vue';
import JSONArrayProperty from './JSONArrayProperty.vue';

import ImagePreview from './ImagePreview.vue';
import PDFPreview from './PDFPreview.vue';

export default defineComponent({
    name: 'SubmodelEntrypoint',
    components: {
        HTWFuehrungskomponente,
        DigitalNameplate,
        JSONArrayProperty,
        ImagePreview,
        PDFPreview,
    },
    props: ['submodelElementData', 'selectedNode'],

    setup() {
        const navigationStore = useNavigationStore()
        const aasStore = useAASStore()

        return {
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
            plugins: [] as Array<any>,
        }
    },

    computed: {
        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        ImportedPlugins() {
            return this.navigationStore.getPlugins;
        },

        // Filtered Plugins
        filteredPlugins() {
            return this.ImportedPlugins.filter((plugin: any) => {
                // console.log(plugin.SemanticID === this.submodelElementData.semanticId.keys[0].value ? 'Plugin found: ' + plugin.SemanticID : 'Plugin not found: ' + plugin.SemanticID);
                return plugin.SemanticID === this.submodelElementData.semanticId.keys[0].value;
            });
        },
    },

    methods: {
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
