<template>
    <v-container fluid class="pa-0">
        <v-card :variant="cardStyle ? cardStyle : 'elevated'">
            <v-list nav class="pt-0">
                <DescriptionElement             v-if="SubmodelElementObject.embeddedDataSpecifications && SubmodelElementObject.embeddedDataSpecifications.length > 0 && SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" :descriptionObject="SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" :descriptionTitle="'Definition'" :small="true"></DescriptionElement>
                <v-divider                      v-if="SubmodelElementObject.embeddedDataSpecifications && SubmodelElementObject.embeddedDataSpecifications.length > 0 && SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" class="mt-2"></v-divider>
                <MultiLanguageProperty          v-if="SubmodelElementObject.modelType == 'MultiLanguageProperty'"           :multiLanguagePropertyObject="SubmodelElementObject"></MultiLanguageProperty>
                <Property                       v-if="SubmodelElementObject.modelType == 'Property'"                        :propertyObject="SubmodelElementObject" @updateValue="updatePropertyValue"></Property>
                <File                           v-if="SubmodelElementObject.modelType == 'File'"                            :fileObject="SubmodelElementObject"></File>
                <Blob                           v-if="SubmodelElementObject.modelType == 'Blob'"                            :blobObject="SubmodelElementObject"></Blob>
                <Operation                      v-if="SubmodelElementObject.modelType == 'Operation'"                       :operationObject="SubmodelElementObject"></Operation>
                <ReferenceElement               v-if="SubmodelElementObject.modelType == 'ReferenceElement'"                :referenceElementObject="SubmodelElementObject"></ReferenceElement>
                <Range                          v-if="SubmodelElementObject.modelType == 'Range'"                           :rangeObject="SubmodelElementObject"></Range>
                <RelationshipElement            v-if="SubmodelElementObject.modelType == 'RelationshipElement'"             :relationshipElementObject="SubmodelElementObject"></RelationshipElement>
                <AnnotatedRelationshipElement   v-if="SubmodelElementObject.modelType == 'AnnotatedRelationshipElement'"    :annotatedRelationshipElementObject="SubmodelElementObject"></AnnotatedRelationshipElement>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import DescriptionElement from './DescriptionElement.vue';

import Property                     from '../SubmodelElements/Property.vue';
import MultiLanguageProperty        from '../SubmodelElements/MultiLanguageProperty.vue';
import File                         from '../SubmodelElements/File.vue';
import Blob                         from '../SubmodelElements/Blob.vue';
import Operation                    from '../SubmodelElements/Operation.vue';
import ReferenceElement             from '../SubmodelElements/ReferenceElement.vue';
import Range                        from '../SubmodelElements/Range.vue';
import RelationshipElement          from '../SubmodelElements/RelationshipElement.vue';
import AnnotatedRelationshipElement from '../SubmodelElements/AnnotatedRelationshipElement.vue';

export default defineComponent({
    name: 'SubmodelELementWrapper',
    components: {
        // UI Components
        DescriptionElement,

        // SubmodelElements
        Property,
        MultiLanguageProperty,
        File,
        Blob,
        Operation,
        ReferenceElement,
        Range,
        RelationshipElement,
        AnnotatedRelationshipElement,
    },
    props: ['SubmodelElementObject', 'cardStyle'],

    methods: {
        // Function to update the value of a property
        updatePropertyValue(submodelElement: any) {
            this.$emit('updateValue', submodelElement);
        },
    },
});
</script>
