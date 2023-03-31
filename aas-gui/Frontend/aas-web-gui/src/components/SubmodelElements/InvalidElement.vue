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
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';

export default defineComponent({
    name: 'InvalidElement',
    props: ['invalidElementObject'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            submodelElements: [
                'Submodel',
                'SubmodelElementCollection',
                'Property',
                'MultiLanguageProperty',
                'File',
                'Operation',
                'ReferenceElement',
            ] as string[],
        }
    },
});
</script>
