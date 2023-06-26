<template>
    <v-list-item class="pt-0">
        <v-list-item-title>
            <v-text-field type="text" variant="outlined" density="compact" clearable @keydown.native.enter="updateValue()" v-model="newStringValue" hint="greyed out value on the left shows the current value in the AAS" @update:focused="setFocus">
                <!-- Current Value -->
                <template v-slot:prepend-inner>
                    <v-chip v-if="isFocused || stringValue.value != newStringValue" label size="x-small" border>{{ stringValue.value }}</v-chip>
                    <v-divider v-if="isFocused || stringValue.value != newStringValue" class="ml-3 mr-1" vertical inset style="margin-top: 8px"></v-divider>
                </template>
                <!-- Update Value Button -->
                <template v-slot:append-inner>
                    <span class="text-subtitleText">{{ unitSuffix(stringValue) }}</span>
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
    name: 'StringType',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling],
    props: ['stringValue'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            newStringValue: '', // new value of the property
            isFocused: false,   // boolean to check if the input field is focused
        }
    },

    mounted() {
        this.newStringValue = this.stringValue.value;
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.newStringValue = '';
            }
        },

        // Watch for changes in the propertyObject and update the newPropertyValue if the input field is not focused
        stringValue: {
            deep: true,
            handler() {
                if (!this.isFocused) {
                    this.newStringValue = this.stringValue.value;
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
            let path = this.SelectedAAS.endpoints[0].address + '/' + this.stringValue.path + '/value';
            let content = "'" + this.newStringValue + "'";
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating ' + this.stringValue.modelType.name + ' "' + this.stringValue.idShort + '"';
            let disableMessage = false;
            // Send Request to update the value of the property
            this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // this.newPropertyValue = ''; // reset input
                    let updatedStringValue = { ...this.stringValue }; // copy the stringValue
                    updatedStringValue.value = content.toString().replace(/'/g, ''); // update the value of the stringValue
                    this.$emit('updateValue', updatedStringValue); // emit event to update the value in the parent component
                }
            });
        },

        // Function to set the focus on the input field
        setFocus(e: boolean) {
            this.isFocused = e;
            if (!e) this.newStringValue = this.stringValue.value; // set input to current value in the AAS if the input field is not focused
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
