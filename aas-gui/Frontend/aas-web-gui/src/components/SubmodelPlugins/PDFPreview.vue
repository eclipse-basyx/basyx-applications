<template>
    <v-container fluid class="pa-0">
        <v-card class="pdfCard" style="transform: translateY(0px)">
            <!-- PDF File Preview -->
            <iframe v-if="useIFrame && submodelElementData.modelType == 'File'" :src="localPathValue" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
            <iframe v-if="!useIFrame && submodelElementData.modelType == 'File'" :src="pdfData" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
            <!-- PDF Blob Preview -->
            <iframe v-if="Base64PDF && submodelElementData.modelType == 'Blob'" :src="Base64PDF" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'PDFPreview',
    props: ['submodelElementData'],
    mixins: [RequestHandling],

    setup() {
        const theme = useTheme()
        const navigationStore = useNavigationStore()
        const aasStore = useAASStore()

        return {
            theme, // Theme Object
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            localPathValue: '', // Path to the File when it is embedded to the AAS
            Base64PDF: '',      // Base64 PDF String
            useIFrame: false,
            pdfData: '',
        }
    },

    mounted() {
        if (this.submodelElementData.modelType == 'File') {
            this.localPathValue = this.getLocalPath(this.submodelElementData.value)
        } else if (this.submodelElementData.modelType == 'Blob') {
            this.Base64PDF = `data:${this.submodelElementData.contentType};base64,${this.submodelElementData.value}`;
        }
    },

    watch: {
        submodelElementData() {
            if (this.submodelElementData.modelType == 'File') {
                this.localPathValue = this.getLocalPath(this.submodelElementData.value)
            } else if (this.submodelElementData.modelType == 'Blob') {
                this.Base64PDF = `data:${this.submodelElementData.contentType};base64,${this.submodelElementData.value}`;
            }
        },
    },

    computed: {
        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
    },

    methods: {
        // Function to prepare the PDF Link for the PDF Preview
        getLocalPath(path: string): string {
            this.useIFrame = true;
            if (!path) return '';

            try {
                new URL(path);
                // If no error is thrown, path is a valid URL
                return path;
            } catch {
                // If error is thrown, path is not a valid URL
                if(!path.endsWith('.pdf') && path.endsWith('/File')) {
                    this.useIFrame = false;
                    this.getPDFData();
                    return path;
                } else {
                    return `${this.submodelElementData.path}/attachment`;
                }
            }
        },

        // Function to fetch raw PDF data
        getPDFData() {
            console.log('getPDFData: ', this.submodelElementData.value);
            let path = this.submodelElementData.value;
            fetch(path, { method: 'GET', headers: { 'Content-Type': 'application/octet-stream' } })
                .then(response => response.blob())
                .then(result => {
                    // console.log(result);
                    this.createPDF(result);
                })
                .catch(error => {
                    // console.log('error', error)
                    this.navigationStore.dispatchSnackbar({ status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
                });
        },

        // Function to create PDF Preview
        createPDF(PDFData: Blob) {
            // convert PDFData to data:application/pdf;base64,<BASE64_ENCODED_PDF>
            const reader = new FileReader();
            reader.readAsDataURL(PDFData);
            reader.onloadend = () => {
                const base64data = reader.result as string;
                const pdfDataUri = base64data.replace('data:application/octet-stream', 'data:application/pdf');
                this.pdfData = pdfDataUri;
            };
        },
    },
});
</script>