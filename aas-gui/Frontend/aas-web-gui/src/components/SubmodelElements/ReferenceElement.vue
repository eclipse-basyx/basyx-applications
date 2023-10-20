<template>
    <v-container fluid class="pa-0">
        <v-list-item class="px-1 pb-1 pt-0">
            <v-list-item-title class="text-subtitle-2 mt-2">{{ 'Reference: ' }}</v-list-item-title>
        </v-list-item>
        <v-card color="elevatedCard" v-if="referenceElementObject">
            <!-- Value of the Reference -->
            <v-list nav class="bg-elevatedCard pt-0">
                <template v-for="(value, i) in referenceValue" :key="i">
                    <v-list-item>
                        <!-- Tooltip with Reference ID -->
                        <v-tooltip activator="parent" open-delay="600" transition="slide-x-transition">
                            <div class="text-caption"><span class="font-weight-bold">{{ '(' + value.type + ') ' }}</span>{{ value.value }}</div>
                        </v-tooltip>
                        <!-- Reference Title -->
                        <template v-slot:title>
                            <div v-html="'Description:'" class="text-subtitle-2 mt-2"></div>
                        </template>
                        <!-- Reference Representation -->
                        <template v-slot:subtitle>
                            <div class="pt-2">
                                <v-chip label size="x-small" border class="mr-2">{{ value.type }}</v-chip>
                                <span v-html="value.value"></span>
                            </div>
                        </template>
                    </v-list-item>
                    <v-divider v-if="i < referenceValue.length - 1" class="mt-3"></v-divider>
                </template>
            </v-list>
            <v-divider></v-divider>
            <!-- Action Buttons for Reference Jump -->
            <v-list nav class="bg-elevatedCard pa-0">
                <v-list-item>
                    <template v-slot:append>
                        <!-- Jump-Button -->
                        <v-btn @click="jumpToReferencedElement(referencedAAS, referenceValue, referencedSubmodel)" size="small" class="text-buttonText" color="primary" :loading="loading" :disabled="disabled">Jump</v-btn>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAASStore } from '@/store/AASDataStore';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

export default defineComponent({
    name: 'ReferenceElement',
    mixins: [SubmodelElementHandling],
    props: ['referenceElementObject'],

    setup() {
        const aasStore = useAASStore()

        return {
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            referenceValue: [] as Array<any>, // Value of the Reference (Array of Reference Keys)
            loading: false, // Loading State of the Jump-Button (loading when checking if referenced element exists in one of the registered AAS)
            disabled: true, // Disabled State of the Jump-Button (disabled when referenced element does not exist in one of the registered AAS)
            referencedAAS: Object as any, // AAS in which the referenced Element is included (if it exists)
            referencedSubmodel: Object as any, // Submodel in which the referenced Element is included (if it exists)
        }
    },

    mounted() {
        // console.log('ReferenceElement Mounted:', this.referenceElementObject);
        this.referenceValue = this.referenceElementObject.value.keys;
        this.validateReference();
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.referenceValue = [];
            }
        },

        // Watch for changes in the referenceElementObject
        referenceElementObject: {
            deep: true,
            handler() {
                this.referenceValue = this.referenceElementObject.value.keys;
                this.validateReference();
            }
        },
    },

    computed: {
        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
    },

    methods: {
        // Function to check if the referenced Element exists
        validateReference() {
            this.loading = true;
            this.checkReference(this.referenceValue)
                .then(({ success, aas, submodel }) => {
                    // console.log('checkReference: ', success, aas, submodel);
                    if (success) {
                        this.referencedAAS = aas;
                        this.referencedSubmodel = submodel;
                        this.disabled = false;
                    }
                    this.loading = false;
                })
                .catch(error => {
                    // Handle any errors
                    console.error('Error:', error);
                    this.loading = false;
                });
        },
    },
});
</script>
