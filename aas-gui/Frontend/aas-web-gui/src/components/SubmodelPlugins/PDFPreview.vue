<template>
    <v-container fluid class="pa-0">
        <v-card style="transform: translateY(0px)">
            <!-- PDF Preview -->
            <iframe :src="localPathValue" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';

export default defineComponent({
    name: 'PDFPreview',

    setup() {
        const theme = useTheme()
        const store = useStore()

        return {
            theme, // Theme Object
            store, // Store Object
        }
    },

    data() {
        return {
            localPathValue: '', // Path to the File when it is embedded to the AAS
            errorLoadingImage: false,
        }
    },

    mounted() {
        this.localPathValue = this.getLocalPath(this.RealTimeObject.value)
        this.errorLoadingImage = false;
    },

    watch: {
        RealTimeObject() {
            this.localPathValue = this.getLocalPath(this.RealTimeObject.value)
            this.errorLoadingImage = false;
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
        // Function to prepare the PDF Link for the PDF Preview
        getLocalPath(path: string): string {
            if (!path) return '';
            // check if Link starts with '/'
            if (path.startsWith('/')) {
                path = this.SelectedAAS.endpoints[0].address.replace('/aas', '') + '/files' + path;
            }
            return path;
        },
    },
});
</script>