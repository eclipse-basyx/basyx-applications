<template>
    <v-container fluid class="pa-0">
        <v-card height="calc(100vh - 105px)" color="card" elevation="0">
            <v-card-title style="padding: 15px 16px 16px">
                <!-- TODO: Add Searchfield to filter the Treeview -->
                AAS Treeview
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text style="height: calc(100vh - 170px); overflow-y: auto">
                <!-- Spinner for loading State -->
                <v-row v-if="loading" justify="center" class="ma-3">
                    <v-col cols="auto">
                        <v-progress-circular :size="70" :width="7" indeterminate></v-progress-circular>
                    </v-col>
                </v-row>
                <!-- TODO: Replace with Vuetify Treeview Component when it get's released in Q1 2023 -->
                <VTreeview v-else v-for="item in submodelData" :key="item.id" class="root" :item="item" :depth="0"></VTreeview>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../mixins/RequestHandling';

import VTreeview from './UIComponents/VTreeview.vue';

export default defineComponent({
    name: 'AASTreeview',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        VTreeview,
    },
    mixins: [RequestHandling],

    setup () {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            submodelData: [] as Array<any>, // Treeview Data
        }
    },

    watch: {
        // initialize Treeview when AAS gets selected or changes
        SelectedAAS: {
            deep: true,
            handler() {
                // console.log('Selected AAS: ', this.SelectedAAS);
                this.initializeTree();
            }
        },

        // Resets the Treeview when the Registry Server changes
        registryServerURL() {
            if(!this.registryServerURL) {
                this.submodelData = [];
            }
        },

        // change the submodelData Object when the updated Node changes
        updatedNode() {
            this.updateNode(this.updatedNode);
        },
    },


    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // gets loading State from Store
        loading() {
            return this.store.getters.getLoadingState;
        },

        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

        // get the updated Treeview Node from Store
        updatedNode() {
            return this.store.getters.getUpdatedNode;
        },
    },

    methods: {
        // Function to get the Submodels from the selected AAS (retrieved from the AAS with the provided endpoint)
        initializeTree() {
            // return if no endpoints are available
            if(!this.SelectedAAS || !this.SelectedAAS.endpoints || this.SelectedAAS.endpoints.length === 0 || !this.SelectedAAS.endpoints[0].address) {
                this.store.dispatch('getSnackbar', { status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'AAS with no (valid) Endpoint selected!' });
                return;
            }
            if(this.loading) return; // return if loading state is true -> prevents multiple requests
            this.store.dispatch('dispatchLoadingState', true); // set loading state to true
            this.submodelData = []; // reset Treeview Data
            // retrieve AAS from endpoint
            let path = this.SelectedAAS.endpoints[0].address + '/submodels';
            let context = 'retrieving AAS Data';
            let disableMessage = false;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                this.store.dispatch('dispatchLoadingState', false); // set loading state to false
                if (response.success) { // execute if the Request was successful
                    // prepare data for treeview
                    this.submodelData = this.prepareTreeviewData(response.data);
                    // console.log('Treeview Data: ', this.submodelData);
                } else { // execute if the Request failed
                    this.submodelData = [];
                }
            });
        },

        // Function to prepare the Datastructure for the Treeview
        prepareTreeviewData(data: any) {
            // console.log('Data: ', data);
            // iterate over all elements in the current level of the tree (Submodels, SubmodelElements [e.g. SubmodelElementCollections, Properties])
            data.forEach((element: any) => {
                // give the Element a unique ID
                element.id = this.UUID();
                // set the active State of each Element
                element.isActive = false;
                // set the Path of each Element
                if(element.modelType.name == 'Submodel') {
                    element.path = 'submodels/' + element.idShort + '/submodel';
                } else if(element.parent.modelType.name == 'Submodel') {
                    element.path = element.parent.path + '/submodelElements/' + element.idShort;
                } else {
                    element.path = element.parent.path + '/' + element.idShort;
                }
                // check if the Element has Children
                if(element.submodelElements && element.submodelElements.length > 0) { // check for SubmodelElements
                    element.submodelElements.forEach((submodelElement: any) => {
                        submodelElement.parent = element; // set the Parent of the SubmodelElement
                    });
                    // if the Element has Children, call the Function again with the Children as Data
                    element.children = this.prepareTreeviewData(element.submodelElements);
                    element.showChildren = false; // set showChildren to false (for the Treeview Component)
                } else if(element.value && Array.isArray(element.value) && element.value.length > 0 && element.modelType.name == 'SubmodelElementCollection') { // check for Values
                    element.value.forEach((submodelElement: any) => {
                        submodelElement.parent = element; // set the Parent of the SubmodelElement
                    });
                    // if the Element has Children, call the Function again with the Children as Data
                    element.children = this.prepareTreeviewData(element.value);
                    element.showChildren = false; // set showChildren to false (for the Treeview Component)
                }
            });
            return data;
        },

        // Function to select a Property
        updateNode(updatedNode: any) {
            // console.log('Updated Node: ', updatedNode);
            // change the isActive State of the selected Node in the Treeview Data (submodelData)
            this.submodelData = this.changeActiveState(this.submodelData, updatedNode);

        },

        // Function to change the isActive State of a Node in the Treeview Data (submodelData)
        changeActiveState(data: any, updatedNode: any) {
            // iterate over all elements in the current level of the tree (Submodels, SubmodelElements [e.g. SubmodelElementCollections, Properties])
            data.forEach((element: any) => {
                // check if the Element has Children
                if(element.children && element.children.length > 0) { // check for SubmodelElements
                    // if the Element has Children, call the Function again with the Children as Data
                    element.children = this.changeActiveState(element.children, updatedNode);
                }
                // check if the Element is the updated Node
                if (element.id === updatedNode.id) {
                    // set isActive State of the updated node
                    element.isActive = updatedNode.isActive;
                } else {
                    // set isActive State of all other nodes to false
                    element.isActive = false;
                }
            });
            return data;
        },

        // generate a unique ID (UUID)
        UUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
    },
});
</script>
