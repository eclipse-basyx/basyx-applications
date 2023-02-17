<template>
    <v-container fluid class="pa-0">
        <template v-if="Object.keys(digitalNameplateData).length > 0">
            <v-card class="mb-4">
                <v-card-title>
                    <div class="text-subtitle-1">{{ "Digital Nameplate:" }}</div>
                </v-card-title>
            </v-card>
            <!-- Iterate over all SubmodelElements of the DigitalNameplate -->
            <v-expansion-panels multiple>
                <v-expansion-panel v-for="NameplateElement in digitalNameplateData" :key="NameplateElement.id">
                    <v-expansion-panel-title class="pl-0 py-0">
                        <IdentificationElement :identificationObject="NameplateElement" :modelType="NameplateElement.modelType.name"></IdentificationElement>
                    </v-expansion-panel-title>
                    <v-expansion-panel-text class="pa-0">
                        <!-- Display NameplateElement directly when it is no SubmodelElementCollection -->
                        <SubmodelElementWrapper v-if="NameplateElement.modelType.name != 'SubmodelElementCollection'" :SubmodelElementObject="NameplateElement" @updateValue="updateNameplateValue"></SubmodelElementWrapper>
                        <!-- Display NameplateElement as SubmodelElementCollection when it is a SubmodelElementCollection -->
                        <CollectionWrapper v-else :SubmodelElementObject="NameplateElement" @updateValue="updateNameplateValue"></CollectionWrapper>
                    </v-expansion-panel-text>
                </v-expansion-panel>
            </v-expansion-panels>
        </template>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

import IdentificationElement from '../UIComponents/IdentificationElement.vue';
import DescriptionElement from '../UIComponents/DescriptionElement.vue';

import SubmodelElementWrapper from '../UIComponents/SubmodelElementWrapper.vue';
import CollectionWrapper from '../UIComponents/CollectionWrapper.vue';

export default defineComponent({
    name: 'DigitalNameplate',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        
        // UI Components
        IdentificationElement,
        DescriptionElement,

        // SubmodelElements
        SubmodelElementWrapper,
        CollectionWrapper,
    },
    mixins: [RequestHandling, SubmodelElementHandling],

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
            digitalNameplateData: {} as any, // Object to store the data of the digital nameplate
        }
    },

    mounted() {
        this.initializeDigitalNameplate(); // initialize DigitalNameplate Plugin
    },

    watch: {
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

        // Get the real-time object from the store
        RealTimeObject() {
            return this.store.getters.getRealTimeObject;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },
    },

    methods: {
        // Function to initialize the Digital Nameplate
        initializeDigitalNameplate() {
            // Check if a Node is selected
            if (Object.keys(this.RealTimeObject).length == 0) {
                this.digitalNameplateData = {}; // Reset the DigitalNameplate Data when no Node is selected
                return;
            }
            let digitalNameplateData = { ...this.RealTimeObject }; // create local copy of the Nameplate Object
            let nameplateElements = digitalNameplateData.submodelElements;
            // add pathes to the SubmodelElements
            this.digitalNameplateData = this.prepareDigitalNameplateData(nameplateElements, this.SelectedNode.path + '/submodelElements');
        },

        // Function to prepare the DigitalNameplate Data
        prepareDigitalNameplateData(nameplateElements: Array<any>, path: string = ''): Array<any> {
            // console.log('nameplateElements: ', nameplateElements, this.SelectedNode.path)
            nameplateElements.forEach((submodelElement: any) => {
                submodelElement.id = this.UUID();
                submodelElement.path = path + '/' + submodelElement.idShort;
                if (submodelElement.modelType.name == 'SubmodelElementCollection') {
                    submodelElement.children = this.prepareDigitalNameplateData(submodelElement.value, submodelElement.path);
                }
            });
            return nameplateElements;
        },

        // Function to update the value of a property
        updateNameplateValue(submodelElement: any) {
            // find the SubmodelElement in the DigitalNameplate Data and replace it with the updated SubmodelElement
            this.digitalNameplateData.forEach((element: any, index: number) => {
                if (element.id == submodelElement.id) {
                    this.digitalNameplateData[index] = submodelElement;
                }
            });
        },
    },
});
</script>

<style>
.v-expansion-panel-text__wrapper {
    padding: 0 !important;
}
</style>