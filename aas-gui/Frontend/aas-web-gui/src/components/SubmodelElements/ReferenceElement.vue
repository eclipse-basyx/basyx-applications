<template>
    <v-container fluid class="pa-0">
        <v-list-item class="px-1 pb-1 pt-0">
            <v-list-item-title class="text-subtitle-2 mt-2">{{ 'Reference: ' }}</v-list-item-title>
        </v-list-item>
        <v-card color="elevatedCard" v-if="referenceElementObject">
            <!-- Value of the Property -->
            <v-list nav class="bg-elevatedCard pt-0">
                <template v-for="(value, i) in referenceValue" :key="i">
                    <v-list-item>
                        <!-- Tooltip with Reference ID -->
                        <v-tooltip activator="parent" open-delay="600" transition="slide-x-transition">
                            <div class="text-caption"><span class="font-weight-bold">{{ '(' + value.type + ')(' + (value.local ? 'local' : 'no-local') + ')[' + value.idType + '] ' }}</span>{{ value.value }}</div>
                        </v-tooltip>
                        <!-- Reference Title -->
                        <template v-slot:title>
                            <div v-html="'Description:'" class="text-subtitle-2 mt-2"></div>
                        </template>
                        <!-- Reference Representation -->
                        <template v-slot:subtitle>
                            <div class="pt-2">
                                <v-chip label size="x-small" border class="mr-2">{{ value.type }}</v-chip>
                                <v-chip label size="x-small" border class="mr-2">{{ value.local ? 'local' : 'no-local' }}</v-chip>
                                <v-chip label size="x-small" border class="mr-2">{{ value.idType }}</v-chip>
                                <span v-html="value.value"></span>
                            </div>
                        </template>
                    </v-list-item>
                    <v-divider v-if="i < referenceValue.length - 1" class="mt-3"></v-divider>
                </template>
            </v-list>
            <v-divider></v-divider>
            <!-- Action Buttons for Reference Jump -->
            <v-list nav class="bg-elevatedCard pa-0">
                <v-list-item>
                    <template v-slot:append>
                        <!-- Jump-Button -->
                        <v-btn @click="jumpToReferencedElement()" size="small" class="text-buttonText" color="primary" :loading="loading" :disabled="disabled">Jump</v-btn>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

export default defineComponent({
    name: 'ReferenceElement',
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['referenceElementObject'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            referenceValue: [] as Array<any>, // Value of the Reference (Array of Reference Keys)
            loading: false, // Loading State of the Jump-Button (loading when checking if referenced element exists in one of the registered AAS)
            disabled: true, // Disabled State of the Jump-Button (disabled when referenced element does not exist in one of the registered AAS)
            referencedAAS: Object as any, // AAS in which the referenced Element is included (if it exists)
            referencedSubmodel: Object as any, // Submodel in which the referenced Element is included (if it exists)
        }
    },

    mounted() {
        // console.log('ReferenceElement Mounted:', this.referenceElementObject);
        this.referenceValue = this.referenceElementObject.value.keys;
        this.checkReference();
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.referenceValue = [];
            }
        },

        // Watch for changes in the referenceElementObject
        referenceElementObject: {
            deep: true,
            handler() {
                this.referenceValue = this.referenceElementObject.value.keys;
                this.checkReference();
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

        // get Registry URL from Store
        registryURL() {
            return this.store.getters.getRegistryURL;
        },
    },

    methods: {
        // Function to check if the referenced Element exists
        checkReference() {
            // console.log('checkReference: ', this.referenceValue);
            this.loading = true;
            // Request all registered AAS
            let path = this.registryURL + '/api/v1/registry';
            let context = 'retrieving AAS Data';
            let disableMessage = false;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) { // execute if the Registry Server responded successfully
                    let aasList = response.data;
                    // check if the first key of the reference is an AAS or a Submodel
                    if(this.referenceValue[0].type == 'AssetAdministrationShell') {
                        this.checkReferenceAAS(aasList);
                    }
                    if(this.referenceValue[0].type == 'Submodel') {
                        this.checkReferenceSubmodel(aasList);
                    }
                }
                this.loading = false;
            });
        },

        // Function to check if the referenced AAS exists
        checkReferenceAAS(aasList: Array<any>) {
            aasList.forEach((aas: any) => {
                // check if the referenced AAS is in the current AAS
                if (aas.identification.id == this.referenceValue[0].value) {
                    // console.log('AAS found. AAS: ', aas);
                    this.referencedAAS = aas;
                    this.disabled = false;
                }
            });
        },

        // Function to check if the referenced Submodel (+ SubmodelElement) exists
        checkReferenceSubmodel(aasList: Array<any>) {
            // check if one of the registered AAS has the referenced Submodel
            aasList.forEach((aas: any) => {
                // check if the referenced Submodel is in the current AAS
                if (aas.submodels && aas.submodels.length > 0) { // The following code only works if the Submodels are registered in the AAS
                    aas.submodels.forEach((submodel: any) => {
                        if (submodel.identification.id == this.referenceValue[0].value) {
                            // console.log('Submodel found. AAS: ', aas, 'Submodel: ',  submodel);
                            this.referencedAAS = aas;
                            this.referencedSubmodel = submodel;
                            this.disabled = false;
                        }
                    });
                } else { // if there are no submodels registered for that AAS, try to request the Submodels from the AAS directly
                    let path = aas.endpoints[0].address + '/submodels';
                    let context = 'retrieving Submodels';
                    let disableMessage = false;
                    this.getRequest(path, context, disableMessage).then((response: any) => {
                        if (response.success) { // execute if the Registry Server is found
                            let submodelList = response.data;
                            submodelList.forEach((submodel: any) => {
                                if (submodel.identification.id == this.referenceValue[0].value) {
                                    // console.log('Submodel found. AAS: ', aas, 'Submodel: ',  submodel);
                                    this.referencedAAS = aas;
                                    this.referencedSubmodel = submodel;
                                    this.disabled = false;
                                }
                            });
                        }
                    });
                }
            });
        },

        // Function to jump to the referenced Element
        jumpToReferencedElement() {
            // console.log('jumpToReferencedElement. AAS: ', this.referencedAAS, 'Submodel: ', this.referencedSubmodel);
            let aas = this.referencedAAS.endpoints[0].address;
            if(this.referencedSubmodel && Object.keys(this.referencedSubmodel).length > 0) { // if the referenced Element is a Submodel or SubmodelElement
                let path = 'submodels/' + this.referencedSubmodel.idShort + '/submodel';
                if(this.referenceValue.length > 1) {
                    path += '/submodelElements/' + this.referenceValue[1].value;
                }
                if(this.referenceValue.length > 2) {
                    this.referenceValue.forEach((SubmodelElement: any, index: number) => {
                        if(index > 1) {
                            path += '/' + SubmodelElement.value;
                        }
                    });
                }
                // set the AAS Endpoint and SubmodelElement path in the aas and path query parameters using the router
                this.$router.push({ query: { aas: aas, path: path } });
                // dispatch the AAS set by the ReferenceElement to the store
                this.store.dispatch('dispatchSelectedAAS', this.referencedAAS);
                // Request the referenced SubmodelElement
                let elementPath = aas + '/' + path;
                let context = 'retrieving SubmodelElement';
                let disableMessage = true;
                this.getRequest(elementPath, context, disableMessage).then((response: any) => {
                    if (response.success) { // execute if the Request was successful
                        response.data.timestamp = this.formatDate(new Date()); // add timestamp to the SubmodelElement Data
                        response.data.path = path; // add the path to the SubmodelElement Data
                        response.data.isActive = true; // add the isActive Property to the SubmodelElement Data
                        // console.log('SubmodelElement Data: ', response.data)
                        // dispatch the SubmodelElementPath set by the URL to the store
                        this.store.dispatch('dispatchNode', response.data); // set the updatedNode in the Store
                        this.store.dispatch('dispatchInitTreeByReferenceElement', true); // set the initTreeByReferenceElement in the Store to true to init + expand the Treeview on the referenced Element
                    } else { // execute if the Request failed
                        if (Object.keys(response.data).length == 0) {
                            // don't copy the static SubmodelElement Data if no Node is selected or Node is invalid
                            this.store.dispatch('getSnackbar', { status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'No valid SubmodelElement under the given Path' }); // Show Error Snackbar
                            return;
                        }
                        this.store.dispatch('dispatchNode', {});
                    }
                });
            } else { // if the referenced Element is an AAS
                // set the AAS Endpoint in the aas query parameter using the router
                this.$router.push({ query: { aas: aas } });
                // dispatch the AAS set by the ReferenceElement to the store
                this.store.dispatch('dispatchSelectedAAS', this.referencedAAS);
            }
        },
    },
});
</script>
