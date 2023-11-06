<template>
    <v-container fluid class="pa-0">
        <!-- entityType -->
        <v-list-item class="px-1 pb-1 pt-2">
            <v-list-item-title>
                <span class="text-subtitle-2 mr-2">{{ 'Entity Type: ' }}</span>
                <v-chip label size="x-small" border color="primary" style="margin-top: -3px">{{ entityObject.entityType }}</v-chip>
            </v-list-item-title>
        </v-list-item>
        <!-- Group of contained SubmodelElements -->
        <SubmodelElementGroup :smeObject="entityObject" :smeLocator="'statements'" :topMargin="'mt-1'"></SubmodelElementGroup>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SubmodelElementGroup from '../UIComponents/SubmodelElementGroup.vue';

export default defineComponent({
    name: 'Entity',
    components: {
        SubmodelElementGroup,
    },
    props: ['entityObject'],
});
</script>
