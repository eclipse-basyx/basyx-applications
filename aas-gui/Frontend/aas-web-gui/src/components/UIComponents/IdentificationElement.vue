<template>
    <v-container fluid class="pa-0">
        <v-list-item>
            <!-- Tooltip with idShort and identification id -->
            <v-tooltip activator="parent" open-delay="600" transition="slide-x-transition">
                <div v-if="identificationObject.idShort" class="text-caption"><span class="font-weight-bold">{{ 'idShort: ' }}</span>{{ identificationObject['idShort'] }}</div>
                <div v-if="identificationObject && identificationObject.id" class="text-caption">{{ identificationObject['id'] }}</div>
            </v-tooltip>
            <!-- idShort -->
            <template v-slot:title>
                <div v-html="identificationObject.idShort ? identificationObject.idShort : ''" class="text-primary text-subtitle-1"></div>
                <div v-if="identificationObject.id">{{ 'Identification (ID):' }}</div>
            </template>
            <!-- identification id -->
            <template v-slot:subtitle>
                <div v-if="identificationObject.id" v-html="identificationObject.id ? identificationObject.id : ''"></div>
            </template>
            <!-- modelType -->
            <template v-slot:append>
                <v-chip size="x-small" color="primary">{{ modelType }}</v-chip>
            </template>
        </v-list-item>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'IdentificationElement',
    props: ['identificationObject', 'modelType'],
});
</script>
