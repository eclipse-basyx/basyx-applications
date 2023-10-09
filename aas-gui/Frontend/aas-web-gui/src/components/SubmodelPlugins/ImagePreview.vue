<template>
    <v-container fluid class="pa-0">
        <v-card>
            <!-- Image File Preview -->
            <v-img v-if="submodelElementData.modelType == 'File'" :src="localPathValue" max-width="100%" max-height="100%" contain @error="errorLoadingImage = true"></v-img>
            <!-- Image Blob Preview -->
            <v-img v-if="submodelElementData.modelType == 'Blob'" :src="Base64Image" max-width="100%" max-height="100%" contain @error="errorLoadingImage = true"></v-img>
            <!-- Error Message -->
            <v-alert v-if="errorLoadingImage" text="No Image found at given Path!" density="compact" type="warning" variant="outlined"></v-alert>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useAASStore } from '@/store/AASDataStore';

export default defineComponent({
    name: 'ImagePreview',
    props: ['submodelElementData'],

    setup() {
        const theme = useTheme()
        const aasStore = useAASStore()

        return {
            theme, // Theme Object
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            localPathValue: '', // Path to the File when it is embedded to the AAS
            Base64Image: '',    // Base64 Image String
            errorLoadingImage: false,
        }
    },

    mounted() {
        if (this.submodelElementData.modelType == 'File') {
            this.localPathValue = this.getLocalPath(this.submodelElementData.value)
        } else if (this.submodelElementData.modelType == 'Blob') {
            this.Base64Image =`data:${this.submodelElementData.mimetype};base64,${this.submodelElementData.value}`;
        }
        this.errorLoadingImage = false;
    },

    watch: {
        submodelElementData() {
            if (this.submodelElementData.modelType == 'File') {
                this.localPathValue = this.getLocalPath(this.submodelElementData.value)
            } else if (this.submodelElementData.modelType == 'Blob') {
                this.Base64Image = `data:${this.submodelElementData.mimetype};base64,${this.submodelElementData.value}`;
            }
            this.errorLoadingImage = false;
        },
    },

    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
    },

    methods: {
        // Function to prepare the Image Link for the Image Preview
        getLocalPath(path: string): string {
            if (!path) return '';
            // check if Link starts with '/'
            if (path.startsWith('/')) {
                path = this.SelectedAAS.endpoints[0].protocolInformation.href.replace('/aas', '') + '/files' + path;
            }
            return path;
        }
    },
});
</script>