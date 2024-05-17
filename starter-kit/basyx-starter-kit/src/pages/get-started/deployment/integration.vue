<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">AAS Integration</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            During this step, you are able to provide your own Asset Administration Shell files. They will be included automatically when you start BaSyx after you finished this setup process. Supported AAS file formats are <v-kbd>AASX</v-kbd>, <v-kbd>XML</v-kbd> and <v-kbd>JSON</v-kbd>.
        </p>
        <!-- AAS File Input -->
        <v-file-input variant="solo-filled" prepend-inner-icon="$file" prepend-icon="" label="AAS File Upload" density="compact" v-model="aasFilesInput" multiple :accept="['.aasx', '.xml', '.json']">
            <template v-slot:append-inner>
                <v-btn size="small" variant="tonal" color="primary" style="right: -4px" @click.stop="uploadAasFiles()">Upload</v-btn>
            </template>
        </v-file-input>
        <!-- Uploaded AAS Files -->
        <v-table v-if="uploadedAasFiles.length > 0" style="border-radius: 4px;">
            <thead>
                <tr class="bg-tableOdd">
                    <th class="text-subtitle-1">Name</th>
                    <th class="text-subtitle-1">Format</th>
                    <th style="text-align: right;" class="text-subtitle-1">Size</th>
                    <th style="text-align: right;"></th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(aasFile, index) in uploadedAasFiles" :key="aasFile.name" :class="index % 2 === 0 ? 'tableEven' : 'bg-tableOdd'">
                    <td>{{ aasFile?.name.split('.').slice(0, -1).join('.') }}</td>
                    <td>{{ aasFile?.name && aasFile.name.includes('.') ? aasFile.name.split('.').pop()?.toUpperCase() : '' }}</td>
                    <td style="text-align: right;">{{ Math.round(aasFile.size / 1024) }} KB</td>
                    <td style="text-align: right;">
                        <v-btn icon @click="uploadedAasFiles.splice(index, 1)" variant="plain" size="small">
                            <v-icon>mdi-delete</v-icon>
                        </v-btn>
                    </td>
                </tr>
            </tbody>
        </v-table>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- BaSyx Component Integrations -->
        <h2 class="text-header">BaSyx Component Integrations</h2>
        <p class="text-normalText mt-8 mb-8 text-subtitle-1">
            One advantage of BaSyx is the integration of components with each other. This includes but is not limited to the automatic registration of digital twins after they are added to the environment.
        </p>
        <!-- Alert for Integrations -->
        <v-alert color="alertCard" class="mb-8">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">Important note</div>
                </v-col>
            </v-row>
            <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                Currently, used Asset Administration Shells are not automatically added to the AAS Discovery Service. This feature will be added in the future to streamline AAS discovery in your applications.
            </p>
        </v-alert>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/visualization/dashboard">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/deployment/context-path">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'Integration',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            aasFilesInput: undefined as File[] | undefined,
            uploadedAasFiles: [] as File[],
        };
    },

    watch: {
        uploadedAasFiles: {
            handler: function (val) {
                this.appStore.setAasFiles(val);
            },
            deep: true,
        },
    },

    mounted() {
        if (this.aasFilesStore && this.aasFilesStore.length > 0) {
            this.uploadedAasFiles = [...this.aasFilesStore]
        }
    },

    computed: {
        // get the AAS Files from the store
        aasFilesStore(): File[] | undefined {
            return this.appStore.getAasFiles;
        },
    },

    methods: {
        uploadAasFiles() {
            if (this.aasFilesInput) {
                this.uploadedAasFiles.push(...this.aasFilesInput);
                // console.log(this.uploadedAasFiles);
                this.aasFilesInput = undefined;
            }
        },
    },
});
</script>