<template>
    <v-container fluid class="pa-0">
        <v-card color="elevatedCard" v-if="invalidElementObject" class="mt-4">
            <v-list nav class="bg-elevatedCard pt-0">
                <!-- Alert when SubmodelElement is invalid -->
                <v-list-item>
                    <v-list-item-title class="pt-2">
                        <v-alert text="Invalid SubmodelElement!" density="compact" type="warning" variant="outlined"></v-alert>
                    </v-list-item-title>
                </v-list-item>
                <!-- Show Blob of the current SubmodelElement -->
                <v-list-item class="px-2 py-0">
                    <v-card style="height: 300px; overflow: auto" class="pa-2">
                        <pre>{{ invalidElementObject }}</pre>
                    </v-card>
                </v-list-item>
                <v-divider class="mt-3"></v-divider>
                <!-- Info listing all available SubmodelElements -->
                <v-list-item class="px-3 py-0">
                    <v-list-item-subtitle class="pt-2">{{ 'The selected SubmodelElement is either non existend or not yet implemented.' }}</v-list-item-subtitle>
                    <template v-slot:append>
                        <!-- Tooltip showing all available SubmodelElements -->
                        <v-tooltip open-delay="600" transition="slide-x-transition">
                            <template v-slot:activator="{ props }">
                                <v-icon v-bind="props">mdi-information-outline</v-icon>
                            </template>
                            <div>
                                <span class="font-weight-bold">Available SubmodelElements:</span>
                                <ul class="px-3">
                                    <li v-for="(submodelElement, i) in submodelElements" :key="i">{{ submodelElement }}</li>
                                </ul>
                            </div>
                        </v-tooltip>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'InvalidElement',
    props: ['invalidElementObject'],

    setup() {

        return {
        }
    },

    data() {
        return {
            submodelElements: [
                'Submodel',
                'SubmodelElementCollection',
                'SubmodelElementList',
                'Property',
                'MultiLanguageProperty',
                'File',
                'Blob',
                'Operation',
                'ReferenceElement',
            ] as string[],
        }
    },
});
</script>
