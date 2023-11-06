<template>
    <v-container fluid class="pa-0">
        <v-card color="elevatedCard" v-for="(conceptDescription, i) in conceptDescriptionObject" :key="i">
            <v-list nav class="bg-elevatedCard pt-0">
                <!-- hasDataSpecification -->
                <SemanticID v-if="conceptDescription.dataSpecification && conceptDescription.dataSpecification.keys && conceptDescription.dataSpecification.keys.length > 0" :semanticIdObject="conceptDescription.dataSpecification" :semanticTitle="'Data Specification'" class="mb-2"></SemanticID>
                <v-divider v-if="conceptDescription.dataSpecificationContent"></v-divider>
                <!-- dataSpecificationContent -->
                <DataSpecificationContent v-if="conceptDescription.dataSpecificationContent" :dataSpecificationObject="conceptDescription.dataSpecificationContent"></DataSpecificationContent>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SemanticID from './SemanticID.vue';
import DataSpecificationContent from './DataSpecificationContent.vue';

export default defineComponent({
    name: 'ConceptDescription',
    components: {
        SemanticID,
        DataSpecificationContent,
    },
    props: ['conceptDescriptionObject'],
});
</script>
