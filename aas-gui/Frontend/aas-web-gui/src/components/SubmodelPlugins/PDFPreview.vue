<template>
    <v-container fluid class="pa-0">
        <v-card class="pdfCard" style="transform: translateY(0px)">
            <!-- PDF File Preview -->
            <iframe v-if="useIFrame && submodelElementData.modelType == 'File'" :src="localPathValue" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
            <vue-pdf-embed ref="pdfPreview" v-if="pdfData && !useIFrame && submodelElementData.modelType == 'File'" :source="pdfData" />
            <!-- PDF Blob Preview -->
            <vue-pdf-embed ref="pdfPreview" v-if="submodelElementData.modelType == 'Blob'" :source="Base64PDF" />
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import VuePdfEmbed from 'vue-pdf-embed'

export default defineComponent({
    name: 'PDFPreview',
    components: {
        VuePdfEmbed,
    },
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

        let timeoutId: ReturnType<typeof setTimeout> | null = null;
        let lastWidth = 0;
        const pdfCard = document.querySelector('.pdfCard') as HTMLElement;
        const observer = new ResizeObserver(entries => {
            const { width, height } = entries[0].contentRect;
            if (width !== lastWidth && height === pdfCard.clientHeight) {
                lastWidth = width;
                if (timeoutId !== null) {
                    clearTimeout(timeoutId);
                }
                timeoutId = setTimeout(() => {
                    // force render of vue-pdf-embed
                    let pdfComponent = this.$refs.pdfPreview as any;
                    if (pdfComponent) {
                        // console.log('rendering pdf');
                        pdfComponent.render();
                    }
                }, 1000);
            }
        });
        observer.observe(pdfCard);
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
            // check if Link starts with '/'
            if (path.startsWith('/')) {
                path = this.SelectedNode.path + '/attachment';
            }
            // check if path has not .pdf at the end and instead /File
            if (!path.endsWith('.pdf') && path.endsWith('/File')) {
                this.useIFrame = false;
                this.getPDFData();
            }
            return path;
        },

        // Function to fetch raw PDF data
        getPDFData() {
            // console.log('getPDFData: ', this.submodelElementData.value);
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