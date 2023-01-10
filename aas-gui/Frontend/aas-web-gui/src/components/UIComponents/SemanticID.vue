<template>
    <v-container fluid class="pa-0">
        <v-list-item>
            <!-- Tooltip with SemanticId -->
            <v-tooltip activator="parent" open-delay="600" transition="slide-x-transition">
                <div v-for="(semanticId, i) in semanticIdObject.keys" :key="i" class="text-caption"><span class="font-weight-bold">{{ '(' + semanticId.type + ')(' + (semanticId.local ? 'local' : 'no-local') + ')[' + semanticId.idType + '] ' }}</span>{{ semanticId.value }}</div>
            </v-tooltip>
            <!-- SemanticIId Title -->
            <template v-slot:title>
                <div v-html="semanticTitle + ':'" class="text-subtitle-2 mt-2"></div>
            </template>
            <!-- SemanticId List -->
            <template v-slot:subtitle>
                <div v-for="(semanticId, i) in semanticIdObject.keys" :key="i" class="pt-2">
                    <v-chip label size="x-small" border class="mr-2">{{ semanticId.type }}</v-chip>
                    <v-chip label size="x-small" border class="mr-2">{{ semanticId.local ? 'local' : 'no-local' }}</v-chip>
                    <v-chip label size="x-small" border class="mr-2">{{ semanticId.idType }}</v-chip>
                    <span v-html="semanticId.value"></span>
                </div>
            </template>
        </v-list-item>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';

export default defineComponent({
    name: 'SemanticID',
    props: ['semanticIdObject', 'semanticTitle'],
});
</script>
