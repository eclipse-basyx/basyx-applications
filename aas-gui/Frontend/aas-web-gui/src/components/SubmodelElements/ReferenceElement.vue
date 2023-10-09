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
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

export default defineComponent({
    name: 'ReferenceElement',
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['referenceElementObject'],

    setup() {
        const navigationStore = useNavigationStore()
        const aasStore = useAASStore()

        return {
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
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
        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        // get Registry URL from Store
        registryURL() {
            return this.navigationStore.getRegistryURL;
        },

        // Get the Submodel Repository URL from the Store
        submodelRepoURL() {
            return this.navigationStore.getSubmodelRepoURL;
        },
    },

    methods: {
        // Function to check if the referenced Element exists
        checkReference() {
            // console.log('checkReference: ', this.referenceValue);
            this.loading = true;
            // Request all registered AAS
            let path = this.registryURL + '/api/v3.0/shell-descriptors';
            let context = 'retrieving AAS Data';
            let disableMessage = false;
            try {
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    if (response.success && response.data.result && response.data.result.length > 0) { // execute if the Registry Server responded successfully with Shells
                        let aasList = response.data.result;
                        // console.log('AAS List: ', aasList, 'Reference Value: ', this.referenceValue[0])
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
            } catch {
                this.loading = false;
            }
        },

        // Function to check if the referenced AAS exists
        checkReferenceAAS(aasList: Array<any>) {
            aasList.forEach((aas: any) => {
                // check if the referenced AAS is in the current AAS
                if (aas.id == this.referenceValue[0].value) {
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
                // console.log('AAS: ', aas)
                // Request the Submodel Descriptors from the AAS
                let path = aas.endpoints[0].protocolInformation.href + '/submodel-refs';
                let context = 'retrieving Submodel References';
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then(async (response: any) => {
                    if (response.success) { // execute if the Request was successful
                        // console.log('Submodel References: ', response.data.result)
                        let submodelList = response.data.result;
                        submodelList.forEach((submodel: any) => {
                            // console.log('Submodel: ', submodel)
                            // check if submodel.keys[0].value == this.referenceValue[0].value
                            if (submodel.keys[0].value == this.referenceValue[0].value) {
                                // console.log('Submodel found. AAS: ', aas, 'Submodel: ',  submodel);
                                this.referencedAAS = aas;
                                this.referencedSubmodel = submodel;
                                this.disabled = false;
                            }
                        });
                    }
                });


                // // check if the referenced Submodel is in the current AAS
                // if (aas.submodels && aas.submodels.length > 0) { // The following code only works if the Submodels are registered in the AAS
                //     aas.submodels.forEach((submodel: any) => {
                //         if (submodel.id == this.referenceValue[0].value) {
                //             // console.log('Submodel found. AAS: ', aas, 'Submodel: ',  submodel);
                //             this.referencedAAS = aas;
                //             this.referencedSubmodel = submodel;
                //             this.disabled = false;
                //         }
                //     });
                // } else { // if there are no submodels registered for that AAS, try to request the Submodels from the AAS directly
                //     let path = aas.endpoints[0].protocolInformation.href + '/submodels';
                //     let context = 'retrieving Submodels';
                //     let disableMessage = false;
                //     this.getRequest(path, context, disableMessage).then((response: any) => {
                //         if (response.success) { // execute if the Registry Server is found
                //             let submodelList = response.data;
                //             submodelList.forEach((submodel: any) => {
                //                 if (submodel.id == this.referenceValue[0].value) {
                //                     // console.log('Submodel found. AAS: ', aas, 'Submodel: ',  submodel);
                //                     this.referencedAAS = aas;
                //                     this.referencedSubmodel = submodel;
                //                     this.disabled = false;
                //                 }
                //             });
                //         }
                //     });
                // }
            });
        },

        // Function to jump to the referenced Element
        jumpToReferencedElement() {
            // console.log('jumpToReferencedElement. AAS: ', this.referencedAAS, 'Submodel: ', this.referencedSubmodel);
            let aas = this.referencedAAS.endpoints[0].protocolInformation.href;
            if(this.referencedSubmodel && Object.keys(this.referencedSubmodel).length > 0) { // if the referenced Element is a Submodel or SubmodelElement
                let path = this.submodelRepoURL + '/' + this.URLEncode(this.referencedSubmodel.keys[0].value);
                if(this.referenceValue.length > 1) { // this is the layer directly under the Submodel
                    path += '/submodel-elements/' + this.referenceValue[1].value;
                }
                let promise; // Promise to wait for the SubmodelElementList to be requested (if it exists)
                if(this.referenceValue.length > 2) { // this is the layer under either a SubmodelElementCollection or SubmodelElementList
                    promise = new Promise<void>((resolve, reject) => {
                        this.referenceValue.forEach((SubmodelElement: any, index: number) => {
                            if(index > 1) {
                                // check if the type of the SubmodelElement with index - 1 is a SubmodelElementList
                                if(this.referenceValue[index - 1].type == 'SubmodelElementList') {
                                    // console.log('SubmodelElementList: ', this.referenceValue[index - 1])
                                    // check in which position of the list the element is (list needs to be requested to get the position)
                                    let listPath = path
                                    let context = 'retrieving SubmodelElementList';
                                    let disableMessage = false;
                                    this.getRequest(listPath, context, disableMessage).then((response: any) => {
                                        if (response.success) { // execute if the Request was successful
                                            let list = response.data;
                                            list.value.forEach((element: any, i: number) => {
                                                if(element.idShort == SubmodelElement.value) {
                                                    path += encodeURIComponent('[') + i + encodeURIComponent(']');
                                                }
                                            });
                                            resolve();
                                        }
                                    }).catch((error: any) => {
                                        // console.error('Error with getRequest:', error);
                                        reject(error);
                                    });
                                } else {
                                    path += '.' + SubmodelElement.value;
                                }
                            }
                        });
                        if (this.referenceValue.every((SubmodelElement: any, index: number) => index <= 1 || this.referenceValue[index - 1].type != 'SubmodelElementList')) {
                            resolve();  // Resolve immediately if none of the elements are SubmodelElementList
                        }
                    });
                } else {
                    promise = Promise.resolve();
                }

                promise.then(() => {
                    // set the AAS Endpoint and SubmodelElement path in the aas and path query parameters using the router
                    this.$router.push({ query: { aas: aas, path: path } });
                    // dispatch the AAS set by the ReferenceElement to the store
                    this.aasStore.dispatchSelectedAAS(this.referencedAAS);
                    // Request the referenced SubmodelElement
                    let elementPath = path;
                    let context = 'retrieving SubmodelElement';
                    let disableMessage = true;
                    this.getRequest(elementPath, context, disableMessage).then((response: any) => {
                        if (response.success) { // execute if the Request was successful
                            response.data.timestamp = this.formatDate(new Date()); // add timestamp to the SubmodelElement Data
                            response.data.path = path; // add the path to the SubmodelElement Data
                            response.data.isActive = true; // add the isActive Property to the SubmodelElement Data
                            // console.log('SubmodelElement Data: ', response.data)
                            // dispatch the SubmodelElementPath set by the URL to the store
                            this.aasStore.dispatchNode(response.data); // set the updatedNode in the AASStore
                            this.aasStore.dispatchInitTreeByReferenceElement(true); // set the initTreeByReferenceElement in the AASStore to true to init + expand the Treeview on the referenced Element
                        } else { // execute if the Request failed
                            if (Object.keys(response.data).length == 0) {
                                // don't copy the static SubmodelElement Data if no Node is selected or Node is invalid
                                this.navigationStore.dispatchSnackbar({ status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'No valid SubmodelElement under the given Path' }); // Show Error Snackbar
                                return;
                            }
                            this.aasStore.dispatchNode({});
                        }
                    });
                }).catch(error => {
                    console.error('Error:', error);
                    // Handle error as needed
                });
            } else { // if the referenced Element is an AAS
                // set the AAS Endpoint in the aas query parameter using the router
                this.$router.push({ query: { aas: aas } });
                // dispatch the AAS set by the ReferenceElement to the store
                this.aasStore.dispatchSelectedAAS(this.referencedAAS);
            }
        },
    },
});
</script>
