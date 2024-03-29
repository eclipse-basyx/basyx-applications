<template>
    <v-container fluid class="pa-0">
        <div v-for="(referenceKey, j) in referenceKeys" :key="j">
            <v-list-item class="px-1 pb-1 pt-0">
                <v-list-item-title class="text-subtitle-2 mt-2">{{ capitalizeFirstLetter(referenceKey) + ': ' }}</v-list-item-title>
            </v-list-item>
            <v-card color="elevatedCard" v-if="relationshipElementObject">
                <!-- Value of the Property -->
                <v-list nav class="bg-elevatedCard pt-0">
                    <template v-for="(value, i) in getReference[referenceKey as 'first' | 'second']" :key="i">
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
                        <v-divider v-if="i < getReference[referenceKey as 'first' | 'second'].length - 1" class="mt-3"></v-divider>
                    </template>
                </v-list>
                <v-divider></v-divider>
                <!-- Action Buttons for Reference Jump -->
                <v-list nav class="bg-elevatedCard pa-0">
                    <v-list-item>
                        <template v-slot:append>
                            <!-- Jump-Button -->
                            <v-btn @click="jumpToReferencedElement(getReferencedAAS[referenceKey as 'first' | 'second'], getReference[referenceKey as 'first' | 'second'], getReferencedSubmodel[referenceKey as 'first' | 'second'])" size="small" class="text-buttonText" color="primary" :loading="getLoadingState[referenceKey as 'first' | 'second']" :disabled="getDisabledState[referenceKey as 'first' | 'second']">Jump</v-btn>
                        </template>
                    </v-list-item>
                </v-list>
            </v-card>
        </div>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAASStore } from '@/store/AASDataStore';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

export default defineComponent({
    name: 'RelationshipElement',
    mixins: [SubmodelElementHandling],
    props: ['relationshipElementObject'],

    setup() {
        const aasStore = useAASStore()

        return {
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            referenceKeys: ['first', 'second'], // Keys of the References
            firstReference: [] as Array<any>, // Value of the first Reference (Array of Reference Keys)
            secondReference: [] as Array<any>, // Value of the second Reference (Array of Reference Keys)
            firstLoading: false, // Loading State of the first Jump-Button (loading when checking if referenced element exists in one of the registered AAS)
            secondLoading: false, // Loading State of the second Jump-Button (loading when checking if referenced element exists in one of the registered AAS)
            firstDisabled: true, // Disabled State of the first Jump-Button (disabled when referenced element does not exist in one of the registered AAS)
            secondDisabled: true, // Disabled State of the second Jump-Button (disabled when referenced element does not exist in one of the registered AAS)
            firstReferencedAAS: Object as any, // first AAS in which the referenced Element is included (if it exists)
            secondReferencedAAS: Object as any, // second AAS in which the referenced Element is included (if it exists)
            firstReferencedSubmodel: Object as any, // first Submodel in which the referenced Element is included (if it exists)
            secondReferencedSubmodel: Object as any, // second Submodel in which the referenced Element is included (if it exists)
        }
    },

    mounted() {
        // console.log('RelationshipElement Mounted:', this.relationshipElementObject);
        this.firstReference = this.relationshipElementObject.first.keys;
        this.secondReference = this.relationshipElementObject.second.keys;
        this.validateReference('first');
        this.validateReference('second');
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.firstReference = [];
                this.secondReference = [];
            }
        },

        // Watch for changes in the referenceElementObject
        referenceElementObject: {
            deep: true,
            handler() {
                this.firstReference = this.relationshipElementObject.first.keys;
                this.secondReference = this.relationshipElementObject.second.keys;
                this.validateReference('first');
                this.validateReference('second');
            }
        },
    },

    computed: {
        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        // Get the referenceObject based on the referenceKey
        getReference() {
            return {
                first: this.firstReference,
                second: this.secondReference,
            };
        },
        
        // Get the referencedAAS based on the referenceKey
        getReferencedAAS() {
            return {
                first: this.firstReferencedAAS,
                second: this.secondReferencedAAS,
            };
        },

        // Get the referencedSubmodel based on the referenceKey
        getReferencedSubmodel() {
            return {
                first: this.firstReferencedSubmodel,
                second: this.secondReferencedSubmodel,
            };
        },

        // Get the loadingState based on the referenceKey
        getLoadingState() {
            return {
                first: this.firstLoading,
                second: this.secondLoading,
            };
        },

        // Get the disabledState based on the referenceKey
        getDisabledState() {
            return {
                first: this.firstDisabled,
                second: this.secondDisabled,
            };
        },
    },

    methods: {
        // Function to check if the referenced Element exists
        validateReference(reference: string) {
            (this as any)[reference + 'Loading'] = true;
            this.checkReference((this as any)[reference + 'Reference'])
                .then(({ success, aas, submodel }) => {
                    // console.log('checkReference: ', success, aas, submodel);
                    if (success) {
                        (this as any)[reference + 'ReferencedAAS'] = aas;
                        (this as any)[reference + 'ReferencedSubmodel'] = submodel;
                        (this as any)[reference + 'Disabled'] = false;
                    }
                    (this as any)[reference + 'Loading'] = false;
                })
                .catch(error => {
                    // Handle any errors
                    console.error('Error:', error);
                    (this as any)[reference + 'Loading'] = false;
                });
        },
    },
});
</script>
