<template>
    <v-list-item class="pt-0">
        <template v-slot:title>
            <v-switch inset density="compact" v-model="newBooleanValue" color="primary" messages="greyed out value on the right shows the current value in the AAS">
                <template v-slot:label>
                    <span style="display: inline; white-space: nowrap">{{ booleanValue.value }}</span>
                </template>
            </v-switch>
        </template>
        <template v-slot:append>
            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="updateValue()">
                <v-icon>mdi-upload</v-icon>
            </v-btn>
        </template>
    </v-list-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../../mixins/RequestHandling';

export default defineComponent({
    name: 'BooleanType',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling],
    props: ['booleanValue'],

    setup() {
        const aasStore = useAASStore()

        return {
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            newBooleanValue: '', // new value of the property
            isFocused: false,   // boolean to check if the input field is focused
        }
    },

    mounted() {
        this.newBooleanValue = this.booleanValue.value;
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.newBooleanValue = '';
            }
        },

        // Watch for changes in the booleanValue and update the newBooleanValue if the input field is not focused
        booleanValue: {
            deep: true,
            handler() {
                if (!this.isFocused) {
                    this.newBooleanValue = this.booleanValue.value;
                }
            }
        },
    },

    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
    },

    methods: {
        // Function to update the value of the property
        updateValue() {
            // console.log("Update Value: " + this.newPropertyValue);
            let path = this.booleanValue.path + '/$value';
            let content = JSON.stringify(this.newBooleanValue);
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating ' + this.booleanValue.modelType + ' "' + this.booleanValue.idShort + '"';
            let disableMessage = false;
            // Send Request to update the value of the property
            this.patchRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // this.newPropertyValue = ''; // reset input
                    let updatedBooleanValue = { ...this.booleanValue }; // copy the booleanValue
                    updatedBooleanValue.value = content.toString().replace(/'/g, ''); // update the value of the booleanValue
                    this.$emit('updateValue', updatedBooleanValue); // emit event to update the value in the parent component
                }
            });
        },

        // Function to set the focus on the input field
        setFocus(e: boolean) {
            this.isFocused = e;
            if (!e) this.newBooleanValue = this.booleanValue.value; // set input to current value in the AAS if the input field is not focused
        },

        // Get the Unit from the EmbeddedDataSpecification of the Property (if available)
        unitSuffix(prop: any) {
            if (prop.embeddedDataSpecifications && prop.embeddedDataSpecifications.length > 0 && prop.embeddedDataSpecifications[0].dataSpecificationContent && prop.embeddedDataSpecifications[0].dataSpecificationContent.unit) {
                return prop.embeddedDataSpecifications[0].dataSpecificationContent.unit;
            } else {
                return '';
            }
        },
    },
});
</script>
