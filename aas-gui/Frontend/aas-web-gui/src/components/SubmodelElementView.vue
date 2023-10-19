<template>
    <v-container fluid class="pa-0">
        <v-card color="rgba(0,0,0,0)" elevation="0">
            <v-card-title style="padding: 15px 16px 16px">Element Details</v-card-title>
            <v-divider></v-divider>
            <v-card-text style="overflow-y: auto; height: calc(100vh - 170px)">
                <!-- Detailed View of the selected SubmodelElement (e.g. Property, Operation, etc.) -->
                <v-card v-if="submodelElementData && Object.keys(submodelElementData).length > 0">
                    <v-list nav>
                        <!-- SubmodelELement Identification -->
                        <IdentificationElement :identificationObject="submodelElementData" :modelType="submodelElementData.modelType" :idType="'Identification (ID)'" :nameType="'idShort'"></IdentificationElement>
                        <v-divider v-if="submodelElementData.description && submodelElementData.description.length > 0" class="mt-2"></v-divider>
                        <!-- SubmodelELement Description -->
                        <DescriptionElement v-if="submodelElementData.description && submodelElementData.description.length > 0" :descriptionObject="submodelElementData.description" :descriptionTitle="'Description'" :small="false"></DescriptionElement>
                        <v-divider v-if="submodelElementData.semanticId && submodelElementData.semanticId.keys && submodelElementData.semanticId.keys.length > 0" class="mt-2"></v-divider>
                        <!-- SubmodelELement SemanticID -->
                        <SemanticID v-if="submodelElementData.semanticId && submodelElementData.semanticId.keys && submodelElementData.semanticId.keys.length > 0" :semanticIdObject="submodelElementData.semanticId" :semanticTitle="'SemanticID'"></SemanticID>
                    </v-list>
                    <v-divider></v-divider>
                    <v-list nav class="px-4 pt-0 pb-0"><!-- SubmodelELement Representation for different modelTypes -->
                        <Submodel                   v-if="submodelElementData.modelType      === 'Submodel'"                    :submodelObject="submodelElementData"></Submodel>
                        <SubmodelElementCollection  v-else-if="submodelElementData.modelType === 'SubmodelElementCollection'"   :submodelElementCollectionObject="submodelElementData"></SubmodelElementCollection>
                        <SubmodelElementList        v-else-if="submodelElementData.modelType === 'SubmodelElementList'"         :submodelElementListObject="submodelElementData"></SubmodelElementList>
                        <Property                   v-else-if="submodelElementData.modelType === 'Property'"                    :propertyObject="submodelElementData" @updateValue="initializeView()"></Property>
                        <MultyLanguageProperty      v-else-if="submodelElementData.modelType === 'MultiLanguageProperty'"       :multiLanguagePropertyObject="submodelElementData"></MultyLanguageProperty>
                        <Operation                  v-else-if="submodelElementData.modelType === 'Operation'"                   :operationObject="submodelElementData"></Operation>
                        <File                       v-else-if="submodelElementData.modelType === 'File'"                        :fileObject="submodelElementData" @updatePath="initializeView()"></File>
                        <Blob                       v-else-if="submodelElementData.modelType === 'Blob'"                        :blobObject="submodelElementData" @updateBlob="initializeView"></Blob>
                        <ReferenceElement           v-else-if="submodelElementData.modelType === 'ReferenceElement'"            :referenceElementObject="submodelElementData"></ReferenceElement>
                        <Range                      v-else-if="submodelElementData.modelType === 'Range'"                       :rangeObject="submodelElementData"></Range>
                        <Entity                     v-else-if="submodelElementData.modelType === 'Entity'"                      :entityObject="submodelElementData"></Entity>
                        <InvalidElement             v-else                                                                      :invalidElementObject="submodelElementData"></InvalidElement>
                    </v-list>
                    <!-- ConceptDescription -->
                    <v-divider v-if="submodelElementData.embeddedDataSpecifications && submodelElementData.embeddedDataSpecifications.length > 0" class="mt-5"></v-divider>
                    <v-list v-if="submodelElementData.embeddedDataSpecifications && submodelElementData.embeddedDataSpecifications.length > 0" class="px-4 pt-0 pb-0">
                        <v-list-item class="pa-1">
                            <v-list-item-title class="text-subtitle-2 mt-2">{{ 'Concept Description: ' }}</v-list-item-title>
                        </v-list-item>
                        <ConceptDescription v-if="submodelElementData.embeddedDataSpecifications && submodelElementData.embeddedDataSpecifications.length > 0" :conceptDescriptionObject="submodelElementData.embeddedDataSpecifications"></ConceptDescription>
                    </v-list>
                    <!-- Last Sync -->
                    <v-divider class="mt-5"></v-divider>
                    <v-list class="py-0">
                        <v-list-item>
                            <v-list-item-subtitle>
                                <span class="text-caption">{{ 'Last sync: ' }}</span>
                                <span class="text-caption" :class="submodelElementData.timestamp == 'no sync' ? 'text-error' : 'text-subtitleText'">{{ submodelElementData.timestamp }}</span>
                            </v-list-item-subtitle>
                        </v-list-item>
                    </v-list>
                </v-card>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

import IdentificationElement    from './UIComponents/IdentificationElement.vue';
import DescriptionElement       from './UIComponents/DescriptionElement.vue';
import SemanticID               from './UIComponents/SemanticID.vue';
import ConceptDescription       from './UIComponents/ConceptDescription.vue';

import Submodel                     from './SubmodelElements/Submodel.vue';
import SubmodelElementCollection    from './SubmodelElements/SubmodelElementCollection.vue';
import SubmodelElementList          from './SubmodelElements/SubmodelElementList.vue';
import Property                     from './SubmodelElements/Property.vue';
import MultyLanguageProperty        from './SubmodelElements/MultiLanguageProperty.vue';
import Operation                    from './SubmodelElements/Operation.vue';
import File                         from './SubmodelElements/File.vue';
import Blob                         from './SubmodelElements/Blob.vue';
import ReferenceElement             from './SubmodelElements/ReferenceElement.vue';
import Range                        from './SubmodelElements/Range.vue';
import Entity                       from './SubmodelElements/Entity.vue';
import InvalidElement               from './SubmodelElements/InvalidElement.vue';

export default defineComponent({
    name: 'SubmodelElementView',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        SubmodelElementHandling, // Mixin to handle the SubmodelElements

        IdentificationElement,
        DescriptionElement,
        SemanticID,
        ConceptDescription,

        Submodel,
        SubmodelElementCollection,
        SubmodelElementList,
        Property,
        MultyLanguageProperty,
        Operation,
        File,
        Blob,
        ReferenceElement,
        Range,
        Entity,
        InvalidElement,
    },
    mixins: [RequestHandling, SubmodelElementHandling],

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
            submodelElementData: {} as any, // SubmodelElement Data
            requestInterval: null as any, // interval to send requests to the AAS
        }
    },

    mounted() {
        if (this.autoSync.state) {
            // create new interval
            this.requestInterval = setInterval(() => {
                if (Object.keys(this.SelectedNode).length > 0) {
                    this.initializeView();
                }
            }, this.autoSync.interval);
        } else {
            this.initializeView();
        }
    },

    beforeUnmount() {
        clearInterval(this.requestInterval); // clear old interval
    },

    watch: {
        // Resets the SubmodelElementView when the Registry Server changes
        registryServerURL() {
            if (!this.registryServerURL) {
                this.submodelElementData = {};
                // also reset the realTimeObject in the store
                this.aasStore.dispatchRealTimeObject(this.submodelElementData);
            }
        },

        // Resets the SubmodelElementView when the AAS changes
        SelectedAAS() {
            this.submodelElementData = {};
            // also reset the realTimeObject in the store
            this.aasStore.dispatchRealTimeObject(this.submodelElementData);
        },

        // Watch for changes in the selected Node and (re-)initialize the Component
        SelectedNode: {
            deep: true,
            handler() {
                // clear old submodelElementData
                this.submodelElementData = {};
                this.initializeView(); // initialize list
            }
        },

        // watch for changes in the autoSync state and create or clear the requestInterval
        autoSync: {
            deep: true,
            handler() {
                if (this.autoSync.state) {
                    clearInterval(this.requestInterval); // clear old interval
                    // create new interval
                    this.requestInterval = setInterval(() => {
                        if (Object.keys(this.SelectedNode).length > 0) {
                            this.initializeView();
                        }
                    }, this.autoSync.interval);
                } else {
                    clearInterval(this.requestInterval);
                }
            }
        },
    },

    computed: {
        // get Registry Server URL from Store
        registryServerURL() {
            return this.navigationStore.getRegistryURL;
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        // Get the auto-sync state from the store
        autoSync() {
            return this.navigationStore.getAutoSync;
        },
    },

    methods: {
        // Initialize the Component
        initializeView() {
            // console.log('Selected Node: ', this.SelectedNode);
            // Check if a Node is selected
            if (Object.keys(this.SelectedNode).length == 0) {
                this.submodelElementData = {}; // Reset the SubmodelElement Data when no Node is selected
                return;
            }
            // Request the selected SubmodelElement
            let path = this.SelectedNode.path;
            let context = 'retrieving SubmodelElement';
            let disableMessage = true;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) { // execute if the Request was successful
                    response.data.timestamp = this.formatDate(new Date()); // add timestamp to the SubmodelElement Data
                    response.data.path = this.SelectedNode.path; // add the path to the SubmodelElement Data
                    // console.log('SubmodelElement Data: ', response.data)
                    this.submodelElementData = response.data;
                } else { // execute if the Request failed
                    // show the static SubmodelElement Data from the store if the Request failed (the timestamp should show that the data is outdated)
                    this.submodelElementData = {}; // Reset the SubmodelElement Data when Node couldn't be retrieved
                    if(Object.keys(this.SelectedNode).length == 0) {
                        // don't copy the static SubmodelElement Data if no Node is selected or Node is invalid
                        this.navigationStore.dispatchSnackbar({ status: true, timeout: 60000, color: 'error', btnColor: 'buttonText', text: 'No valid SubmodelElement under the given Path' }); // Show Error Snackbar
                        return;
                    }
                    this.submodelElementData = { ...this.SelectedNode }; // copy the static SubmodelElement Data from the store
                    this.submodelElementData.timestamp = 'no sync';
                    this.submodelElementData.path = this.SelectedNode.path; // add the path to the SubmodelElement Data
                }
                // console.log('SubmodelElement Data (SubmodelElementView): ', this.submodelElementData)
                // add SubmodelElement Data to the store (as RealTimeDataObject)
                this.aasStore.dispatchRealTimeObject(this.submodelElementData);
            });
        },
    },
});
</script>
