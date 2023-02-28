<template>
    <v-container fluid class="pa-0">
        <v-card>
            <v-list nav class="pt-0">
                <DescriptionElement v-if="SubmodelElementObject.embeddedDataSpecifications && SubmodelElementObject.embeddedDataSpecifications.length > 0 && SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" :descriptionObject="SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" :descriptionTitle="'Definition'" :small="true"></DescriptionElement>
                <v-divider v-if="SubmodelElementObject.embeddedDataSpecifications && SubmodelElementObject.embeddedDataSpecifications.length > 0 && SubmodelElementObject.embeddedDataSpecifications[0].dataSpecificationContent.definition" class="mt-2"></v-divider>
                <MultiLanguageProperty v-if="SubmodelElementObject.modelType.name == 'MultiLanguageProperty'" :multiLanguagePropertyObject="SubmodelElementObject"></MultiLanguageProperty>
                <Property v-if="SubmodelElementObject.modelType.name == 'Property'" :propertyObject="SubmodelElementObject" @updateValue="updatePropertyValue"></Property>
                <File v-if="SubmodelElementObject.modelType.name == 'File'" :fileObject="SubmodelElementObject"></File>
                <Operation v-if="SubmodelElementObject.modelType.name == 'Operation'" :operationObject="SubmodelElementObject"></Operation>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import IdentificationElement from './IdentificationElement.vue';
import DescriptionElement from './DescriptionElement.vue';

import Property from '../SubmodelElements/Property.vue';
import MultiLanguageProperty from '../SubmodelElements/MultiLanguageProperty.vue';
import File from '../SubmodelElements/File.vue';
import Operation from '../SubmodelElements/Operation.vue';

export default defineComponent({
    name: 'SubmodelELementWrapper',
    components: {
        // UI Components
        IdentificationElement,
        DescriptionElement,

        // SubmodelElements
        Property,
        MultiLanguageProperty,
        File,
        Operation,
    },
    props: ['SubmodelElementObject'],

    data() {
        return {
        }
    },

    mounted() {
    },

    watch: {
    },

    computed: {
    },

    methods: {
        // Function to update the value of a property
        updatePropertyValue(submodelElement: any) {
            this.$emit('updateValue', submodelElement);
        },
    },
});
</script>
