<template>
    <v-container fluid class="pa-0">
        <!-- Header -->
        <v-card class="mb-4">
            <v-card-title>
                <div class="text-subtitle-1">{{ "Handover Documentation:" }}</div>
            </v-card-title>
        </v-card>
        <!-- Documents -->
        <v-expansion-panels v-model="panel">
            <v-expansion-panel v-for="(document, index) in documents" :key="document.idShort">
                <v-expansion-panel-title>
                    <v-list-item class="pa-0">
                        <template v-slot:prepend>
                            <v-icon size="small">mdi-file-outline</v-icon>
                        </template>
                        <v-list-item-title>{{ document.idShort }}</v-list-item-title>
                    </v-list-item>
                </v-expansion-panel-title>
                <v-divider v-if="panel === index"></v-divider>
                <v-expansion-panel-text>
                    <template v-for="documentVersion in document.documentVersions" :key="documentVersion.idShort">
                        <!-- General Document Information (DocumentVersion) -->
                        <v-table>
                            <tbody>
                                <tr v-for="(versionPropertie, index) in documentVersion.value" :key="versionPropertie.idShort" :class="index % 2 === 0 ? 'bg-tableEven' : 'bg-tableOdd'">
                                    <td>
                                        <div class="text-subtitleText text-caption">
                                            <span>{{ versionPropertie.idShort }}</span>
                                            <v-tooltip v-if="versionPropertie.description && versionPropertie.description.length > 0" activator="parent" open-delay="600" transition="slide-y-transition" max-width="360px" location="bottom">
                                                <div v-for="(description, i) in versionPropertie.description" :key="i" class="text-caption"><span class="font-weight-bold">{{ description.language + ': ' }}</span>{{ description.text }}</div>
                                            </v-tooltip>
                                        </div>
                                    </td>
                                    <td>
                                        <!-- MultiLanguageProperties -->
                                        <template v-if="versionPropertie.modelType == 'MultiLanguageProperty'">
                                            <v-list-item class="pl-0">
                                                <v-list-item-title class="text-caption">{{ versionPropertie.value[0].text }}</v-list-item-title>
                                            </v-list-item>
                                        </template>
                                        <!-- Default -->
                                        <span v-else class="text-caption">{{ versionPropertie.value }}</span>
                                    </td>
                                </tr>
                            </tbody>
                        </v-table>
                        <!-- Switcher for File Preview/Digital File -->
                        <v-row justify="center" class="mt-3">
                            <v-col cols="auto">
                                <v-btn-toggle color="primary" variant="outlined" divided density="compact" v-model="documentVersion.fileToggle">
                                    <v-btn value="preview">
                                        <span>Preview File</span>
                                    </v-btn>
                                    <v-btn value="digital">
                                        <span>Digital File</span>
                                    </v-btn>
                                </v-btn-toggle>
                            </v-col>
                        </v-row>
                        <!-- File Preview (PreviewFile) -->
                        <template v-if="documentVersion.fileToggle === 'preview'">
                            <div class="mt-3" v-if="documentVersion.previewFile">
                                <ImagePreview v-if="documentVersion.previewFile.contentType && documentVersion.previewFile.contentType.includes('image')" :submodelElementData="documentVersion.previewFile"></ImagePreview>
                                <PDFPreview v-if="documentVersion.previewFile.contentType && documentVersion.previewFile.contentType.includes('pdf')" :submodelElementData="documentVersion.previewFile"></PDFPreview>
                                <CADPreview v-if="documentVersion.previewFile.contentType && (documentVersion.previewFile.contentType.includes('sla') || documentVersion.previewFile.contentType.includes('stl') || documentVersion.previewFile.contentType.includes('model') || documentVersion.previewFile.contentType.includes('obj') || documentVersion.previewFile.contentType.includes('gltf'))" :submodelElementData="documentVersion.previewFile"></CADPreview>
                            </div>
                            <v-alert v-else text="No preview file found!" class="mt-3" density="compact" type="warning" variant="outlined"></v-alert>
                        </template>
                        <!-- File Preview (DigitalFile) -->
                        <template v-else-if="documentVersion.fileToggle === 'digital'">
                            <div class="mt-3" v-if="documentVersion.digitalFile">
                                <ImagePreview v-if="documentVersion.digitalFile.contentType && documentVersion.digitalFile.contentType.includes('image')" :submodelElementData="documentVersion.digitalFile"></ImagePreview>
                                <PDFPreview v-if="documentVersion.digitalFile.contentType && documentVersion.digitalFile.contentType.includes('pdf')" :submodelElementData="documentVersion.digitalFile"></PDFPreview>
                                <CADPreview v-if="documentVersion.digitalFile.contentType && (documentVersion.digitalFile.contentType.includes('sla') || documentVersion.digitalFile.contentType.includes('stl') || documentVersion.digitalFile.contentType.includes('model') || documentVersion.digitalFile.contentType.includes('obj') || documentVersion.digitalFile.contentType.includes('gltf'))" :submodelElementData="documentVersion.digitalFile"></CADPreview>
                            </div>
                            <v-alert v-else text="No digital file found!" class="mt-3" density="compact" type="warning" variant="outlined"></v-alert>
                        </template>
                    </template>
                    <!-- DocumentClassifications -->
                    <v-card variant="outlined" class="mt-3">
                        <v-table>
                            <thead>
                                <tr v-if="document.documentClassifications.length > 0">
                                    <th v-for="classificationProperty in document.documentClassifications[0].value" :key="classificationProperty.idShort">
                                        <v-list-item class="pl-0">
                                            <v-list-item-title class="text-caption">{{ classificationProperty.idShort }}</v-list-item-title>
                                        </v-list-item>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(documentClassification, index) in document.documentClassifications" :key="documentClassification.idShort" :class="index % 2 === 0 ? 'bg-tableOdd' : 'bg-tableEven'">
                                    <td v-for="classificationProperty in documentClassification.value" :key="classificationProperty.idShort">
                                        <!-- MultiLanguageProperties -->
                                        <template v-if="classificationProperty.modelType == 'MultiLanguageProperty'">
                                            <v-list-item class="pl-0">
                                                <span class="text-caption text-subtitleText">{{ classificationProperty.value[0].text }}</span>
                                            </v-list-item>
                                        </template>
                                        <!-- Default -->
                                        <span v-else class="text-caption text-subtitleText">{{ classificationProperty.value }}</span>
                                    </td>
                                </tr>
                            </tbody>
                        </v-table>
                    </v-card>
                    <!-- DocumentIds -->
                    <v-card variant="outlined" class="mt-3">
                        <v-table>
                            <thead>
                                <tr v-if="document.documentIds.length > 0">
                                    <th v-for="idProperty in document.documentIds[0].value" :key="idProperty.idShort">
                                        <v-list-item class="pl-0">
                                            <v-list-item-title class="text-caption">{{ idProperty.idShort }}</v-list-item-title>
                                        </v-list-item>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(documentId, index) in document.documentIds" :key="documentId.idShort" :class="index % 2 === 0 ? 'bg-tableOdd' : 'bg-tableEven'">
                                    <td v-for="idProperty in documentId.value" :key="idProperty.idShort">
                                        <!-- MultiLanguageProperties -->
                                        <template v-if="idProperty.modelType == 'MultiLanguageProperty'">
                                            <v-list-item class="pl-0">
                                                <span class="text-caption text-subtitleText">{{ idProperty.value[0].text }}</span>
                                            </v-list-item>
                                        </template>
                                        <!-- Default -->
                                        <span v-else class="text-caption text-subtitleText">{{ idProperty.value }}</span>
                                    </td>
                                </tr>
                            </tbody>
                        </v-table>
                    </v-card>
                </v-expansion-panel-text>
            </v-expansion-panel>
        </v-expansion-panels>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

import ImagePreview from './ImagePreview.vue';
import PDFPreview from './PDFPreview.vue';
import CADPreview from './CADPreview.vue';

export default defineComponent({
    name: 'HandoverDocumentation',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        ImagePreview,
        PDFPreview,
        CADPreview,
    },
    mixins: [RequestHandling, SubmodelElementHandling],
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
            panel: null as Number | null,
            documents: [] as Array<any>,
        }
    },

    mounted() {
        this.initHandoverDocumentation();
    },

    computed: {
        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
    },

    methods: {
        initHandoverDocumentation() {
            // console.log('Initialize Handover Documentation Plugin: ', this.submodelElementData);
            let submodelElementData = { ...this.submodelElementData };
            submodelElementData = this.calculateSubmodelElementPathes(submodelElementData, this.SelectedNode.path);
            // create array of documents
            let documents = this.submodelElementData.submodelElements.filter((element: any) => {
                return element.semanticId.keys[0].value.includes("0173-1#02-ABI500#001/0173-1#01-AHF579#001");
            });
            documents.forEach((document: any) => {
                this.extractDocumentVersions(document);
                this.extractDocumentIds(document);
                this.extractDocumentClassifications(document);
            });
            // console.log('Documents: ', documents);
            this.documents = documents;
        },

        extractDocumentVersions(document: any) {
            // create an array with every DocumentVersion SubmodelElementCollection
            document.documentVersions = document.value.filter((element: any) => {
                return element.semanticId.keys[0].value.includes("0173-1#02-ABI503#001/0173-1#01-AHF582#001");
            });
            // prepare each DocumentVersion
            document.documentVersions.forEach((documentVersion: any) => {
                // extract the DigitalFile
                documentVersion.digitalFile = documentVersion.value.find((element: any) => {
                    return element.idShort === "DigitalFile";
                });
                // extract the PreviewFile
                documentVersion.previewFile = documentVersion.value.find((element: any) => {
                    return element.idShort === "PreviewFile";
                });
                // filter for relevant versionProperties
                documentVersion.value = documentVersion.value.filter((element: any) => {
                    // return elements with the following idShorts: Language, Title, SubTitle, Summary, KeyWords
                    return element.idShort === "Language" || element.idShort === "Title" || element.idShort === "SubTitle" || element.idShort === "Summary" || element.idShort === "KeyWords";
                });
                documentVersion.fileToggle = 'preview';
            });
        },

        extractDocumentIds(document: any) {
            document.documentIds = document.value.filter((element: any) => {
                return element.semanticId.keys[0].value.includes("0173-1#02-ABI501#001/0173-1#01-AHF580#001");
            });
        },

        extractDocumentClassifications(document: any) {
            document.documentClassifications = document.value.filter((element: any) => {
                return element.semanticId.keys[0].value.includes("0173-1#02-ABI502#001/0173-1#01-AHF581#001*02");
            });
        },
    },
});
</script>
