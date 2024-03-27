<template>
    <v-container fluid class="pa-0">
        <v-expansion-panels multiple class="mt-3">
            <v-expansion-panel v-for="(submodelElement, index) in localSubmodelElementData" :key="submodelElement.id">
                <v-expansion-panel-title color="elevatedCard">
                    <span v-if="submodelElement.idShort">{{ submodelElement.idShort }}</span>
                    <span v-else>{{ 'Element ' + (index + 1) }}</span>
                </v-expansion-panel-title>
                <v-expansion-panel-text>
                    <GenericDataVisu v-if="Array.isArray(submodelElement.value) && submodelElement.value.length > 0" :submodelElementData="submodelElement.value"></GenericDataVisu>
                    <v-list v-else nav class="px-4 pt-0 pb-0">
                        <!-- SubmodelELement Representation for different modelTypes -->
                        <Property v-if="submodelElement.modelType === 'Property'" :propertyObject="submodelElement"></Property>
                        <MultyLanguageProperty v-else-if="submodelElement.modelType === 'MultiLanguageProperty'" :multiLanguagePropertyObject="submodelElement"></MultyLanguageProperty>
                        <Operation v-else-if="submodelElement.modelType === 'Operation'" :operationObject="submodelElement"></Operation>
                        <File v-else-if="submodelElement.modelType === 'File'" :fileObject="submodelElement"></File>
                        <Blob v-else-if="submodelElement.modelType === 'Blob'" :blobObject="submodelElement"></Blob>
                        <ReferenceElement v-else-if="submodelElement.modelType === 'ReferenceElement'" :referenceElementObject="submodelElement"></ReferenceElement>
                        <Range v-else-if="submodelElement.modelType === 'Range'" :rangeObject="submodelElement"></Range>
                        <Entity v-else-if="submodelElement.modelType === 'Entity'" :entityObject="submodelElement"></Entity>
                        <RelationshipElement v-else-if="submodelElement.modelType === 'RelationshipElement'" :relationshipElementObject="submodelElement"></RelationshipElement>
                        <AnnotatedRelationshipElement v-else-if="submodelElement.modelType === 'AnnotatedRelationshipElement'" :annotatedRelationshipElementObject="submodelElement"></AnnotatedRelationshipElement>
                        <InvalidElement v-else :invalidElementObject="submodelElement"></InvalidElement>
                    </v-list>
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

import IdentificationElement from '../UIComponents/IdentificationElement.vue';
import DescriptionElement from '../UIComponents/DescriptionElement.vue';

import SubmodelElementWrapper from '../UIComponents/SubmodelElementWrapper.vue';
import CollectionWrapper from '../UIComponents/CollectionWrapper.vue';

import SubmodelElementList from '../SubmodelElements/SubmodelElementList.vue';
import Property from '../SubmodelElements/Property.vue';
import MultyLanguageProperty from '../SubmodelElements/MultiLanguageProperty.vue';
import Operation from '../SubmodelElements/Operation.vue';
import File from '../SubmodelElements/File.vue';
import Blob from '../SubmodelElements/Blob.vue';
import ReferenceElement from '../SubmodelElements/ReferenceElement.vue';
import Range from '../SubmodelElements/Range.vue';
import Entity from '../SubmodelElements/Entity.vue';
import RelationshipElement from '../SubmodelElements/RelationshipElement.vue';
import AnnotatedRelationshipElement from '../SubmodelElements/AnnotatedRelationshipElement.vue';
import InvalidElement from '../SubmodelElements/InvalidElement.vue';

export default defineComponent({
    name: 'GenericDataVisu',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        // UI Components
        IdentificationElement,
        DescriptionElement,

        // SubmodelElements
        SubmodelElementWrapper,
        CollectionWrapper,

        SubmodelElementList,
        Property,
        MultyLanguageProperty,
        Operation,
        File,
        Blob,
        ReferenceElement,
        Range,
        Entity,
        RelationshipElement,
        AnnotatedRelationshipElement,
        InvalidElement,
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
            localSubmodelElementData: [] as Array<any>, // SubmodelElement Data
            conceptDescription: {}, // ConceptDescription Object
        }
    },

    watch: {
        submodelElementData: {
            handler() {
                this.initializeSubmodelElementData();
            },
            deep: true,
        },
    },

    mounted() {
        this.initializeSubmodelElementData();
    },

    methods: {
        // Initialize the SubmodelElement Data
        initializeSubmodelElementData() {
            // console.log('SubmodelElementData: ', this.submodelElementData)
            if (Object.keys(this.submodelElementData).length == 0) {
                this.localSubmodelElementData = []; // Reset the SubmodelElement Data when no Node is selected
                return;
            }
            let submodelElementData = [ ...this.submodelElementData ];
            // console.log('SubmodelElementData: ', submodelElementData)
            submodelElementData.forEach((submodelElement: any) => {
                // console.log('ModelType: ', submodelElement);
                if (submodelElement.modelType === 'SubmodelElementList') {
                    // add embeddedDataSpecifications to every value of the SubmodelElementList
                    submodelElement.value.forEach((value: any) => {
                        value.embeddedDataSpecifications = submodelElement.embeddedDataSpecifications;
                    });
                }
            });
            this.localSubmodelElementData = submodelElementData;
        },
        
    },
});
</script>