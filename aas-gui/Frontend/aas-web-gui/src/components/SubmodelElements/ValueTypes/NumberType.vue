<template>
    <v-list-item class="pt-0">
        <v-list-item-title>
            <v-text-field type="number" variant="outlined" density="compact" clearable @keydown.native.enter="updateValue()" v-model="newNumberValue" hint="greyed out value on the left shows the current value in the AAS" @update:focused="setFocus">
                <!-- Current Value -->
                <template v-slot:prepend-inner>
                    <v-chip v-if="isFocused || numberValue.value != newNumberValue" label size="x-small" border>{{ numberValue.value }}</v-chip>
                    <v-divider v-if="isFocused || numberValue.value != newNumberValue" class="ml-3 mr-1" vertical inset style="margin-top: 8px"></v-divider>
                </template>
                <!-- Update Value Button -->
                <template v-slot:append-inner>
                    <span class="text-subtitleText">{{ unitSuffix(numberValue) }}</span>
                    <v-btn v-if="isFocused" size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="updateValue()">
                        <v-icon>mdi-upload</v-icon>
                    </v-btn>
                </template>
            </v-text-field>
        </v-list-item-title>
    </v-list-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../../mixins/RequestHandling';

export default defineComponent({
    name: 'NumberType',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling],
    props: ['numberValue'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            newNumberValue: '', // new value of the property
            isFocused: false,   // boolean to check if the input field is focused
        }
    },

    mounted() {
        this.newNumberValue = this.numberValue.value;
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.newNumberValue = '';
            }
        },

        // Watch for changes in the propertyObject and update the newPropertyValue if the input field is not focused
        stringValue: {
            deep: true,
            handler() {
                if (!this.isFocused) {
                    this.newNumberValue = this.numberValue.value;
                }
            }
        },
    },

    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.store.getters.getSelectedNode;
        },
    },

    methods: {
        // Function to update the value of the property
        updateValue() {
            // console.log("Update Value: " + this.newPropertyValue);
            let path = this.SelectedAAS.endpoints[0].address + '/' + this.numberValue.path + '/value';
            let content = "'" + this.newNumberValue + "'";
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating ' + this.numberValue.modelType.name + ' "' + this.numberValue.idShort + '"';
            let disableMessage = false;
            // Send Request to update the value of the property
            this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // this.newPropertyValue = ''; // reset input
                    let updatedNumberValue = { ...this.numberValue }; // copy the numberValue
                    updatedNumberValue.value = content.toString().replace(/'/g, ''); // update the value of the numberValue
                    this.$emit('updateValue', updatedNumberValue); // emit event to update the value in the parent component
                }
            });
        },

        // Function to set the focus on the input field
        setFocus(e: boolean) {
            this.isFocused = e;
            if (!e) this.newNumberValue = this.numberValue.value; // set input to current value in the AAS if the input field is not focused
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
