<template>
    <v-container fluid class="pa-0">
        <v-card color="elevatedCard" class="mt-4">
            <v-list v-if="submodelObject.submodelElements.length > 0" nav class="bg-elevatedCard">
                <div v-for="(SubmodelElement, i) in submodelObject.submodelElements" :key="i">
                    <v-list-item class="pt-0">
                        <v-list-item-title class="pt-2">
                            <!-- SubmodelElementCollection -->
                            <v-alert v-if="SubmodelElement.modelType.name == 'SubmodelElementCollection'" :text="SubmodelElement.idShort" density="compact" variant="outlined" border="start">
                                <template v-slot:prepend>
                                    <v-chip label size="x-small" border color="primary">{{ SubmodelElement.modelType.name }}</v-chip>
                                </template>
                                <template v-slot:append>
                                    <v-badge :content="SubmodelElement.value.length" inline></v-badge>
                                </template>
                            </v-alert>
                            <!-- Property -->
                            <v-text-field v-else-if="SubmodelElement.modelType.name == 'Property'" :label="SubmodelElement.idShort" density="compact" variant="outlined" v-model="SubmodelElement.value" readonly hide-details>
                                <!-- Current Value -->
                                <template v-slot:prepend-inner>
                                    <v-chip label size="x-small" border color="primary">{{ SubmodelElement.valueType }}</v-chip>
                                </template>
                            </v-text-field>
                            <!-- MultiLanguageProperty -->
                            <DescriptionElement v-else-if="SubmodelElement.modelType.name == 'MultiLanguageProperty'" :descriptionObject="SubmodelElement.value" :descriptionTitle="SubmodelElement.idShort" :small="false" style="margin-top: -12px"></DescriptionElement>
                            <!-- Operation -->
                            <v-alert v-else-if="SubmodelElement.modelType.name == 'Operation'" :text="SubmodelElement.idShort" density="compact" variant="tonal" border="start">
                                <template v-slot:prepend>
                                    <v-chip label size="x-small" border color="primary">{{ SubmodelElement.modelType.name }}</v-chip>
                                </template>
                                <template v-slot:append>
                                    <v-icon style="margin-right: 5px">mdi-lightning-bolt-circle</v-icon>
                                </template>
                            </v-alert>
                            <!-- File -->
                            <v-text-field v-else-if="SubmodelElement.modelType.name == 'File'" :label="SubmodelElement.idShort" density="compact" variant="outlined" v-model="SubmodelElement.value" readonly hide-details>
                                <template v-slot:prepend-inner>
                                    <v-chip label size="x-small" border color="primary">{{ SubmodelElement.modelType.name }}</v-chip>
                                </template>
                                <template v-slot:append-inner>
                                    <v-btn :disabled="!SubmodelElement.value" size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="downloadFile(SubmodelElement.value)">
                                        <v-icon>mdi-download</v-icon>
                                    </v-btn>
                                </template>
                            </v-text-field>
                            <!-- Blob -->
                            <v-text-field v-else-if="SubmodelElement.modelType.name == 'Blob'" :label="SubmodelElement.idShort" density="compact" variant="outlined" v-model="SubmodelElement.value" readonly hide-details>
                                <template v-slot:prepend-inner>
                                    <v-chip label size="x-small" border color="primary">{{ SubmodelElement.modelType.name }}</v-chip>
                                </template>
                            </v-text-field>
                            <!-- InvalidElement -->
                            <v-alert v-else text="Invalid SubmodelElement!" density="compact" type="warning" variant="outlined"></v-alert>
                        </v-list-item-title>
                    </v-list-item>
                    <v-divider v-if="i < submodelObject.submodelElements.length -1" class="mt-2 mb-1"></v-divider>
                </div>
            </v-list>
            <v-list nav class="bg-elevatedCard pt-0" v-else>
                <v-list-item>
                    <v-list-item-title class="pt-2">
                        <v-alert text="Submodel doesn't contain any SubmodelElements!" density="compact" type="warning" variant="outlined"></v-alert>
                    </v-list-item-title>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import DescriptionElement from '../UIComponents/DescriptionElement.vue';

export default defineComponent({
    name: 'Submodel',
    components: {
        DescriptionElement,
    },
    props: ['submodelObject'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
        }
    },

    mounted() {
    },

    watch: {
    },

    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },
    },

    methods: {
        // Function to download a file
        downloadFile(link: string) {
            // open new tab with file
            window.open(this.getLocalPath(link), '_blank');
        },

        // Function to prepare the Image Link for the Image Preview
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
