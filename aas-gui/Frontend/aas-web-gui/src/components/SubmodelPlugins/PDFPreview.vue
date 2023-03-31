<template>
    <v-container fluid class="pa-0">
        <v-card class="pdfCard" style="transform: translateY(0px)">
            <!-- PDF Preview -->
            <iframe v-if="useIFrame" :src="localPathValue" width="100%" height="600px" frameBorder="0" style="margin-bottom: -5px"></iframe>
            <vue-pdf-embed ref="pdfPreview" v-if="pdfData && !useIFrame" :source="pdfData" />
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';
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
            useIFrame: true,
            pdfData: '',
        }
    },

    mounted() {
        this.localPathValue = this.getLocalPath(this.submodelElementData.value)
        this.errorLoadingImage = false;

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
            this.localPathValue = this.getLocalPath(this.submodelElementData.value)
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
    },

    methods: {
        // Function to prepare the PDF Link for the PDF Preview
        getLocalPath(path: string): string {
            this.useIFrame = true;
            if (!path) return '';
            // check if Link starts with '/'
            if (path.startsWith('/')) {
                path = this.SelectedAAS.endpoints[0].address.replace('/aas', '') + '/files' + path;
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
                    this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'Error! Server responded with: ' + error }); // Show Error Snackbar
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