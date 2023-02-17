<template>
    <v-container fluid class="pa-0">
        <v-list-item class="px-1 pb-1 pt-0">
            <v-list-item-title class="text-subtitle-2 mt-2">{{ 'Value: ' }}</v-list-item-title>
        </v-list-item>
        <v-card color="elevatedCard" v-if="propertyObject">
            <!-- Value of the Property -->
            <v-list nav class="bg-elevatedCard pt-0">
                <!-- valueType -->
                <v-list-item class="pb-0">
                    <v-list-item-title>
                        <span class="text-caption">{{ 'Value Type: ' }}</span>
                        <v-chip label size="x-small" border color="primary">{{ propertyObject.valueType }}</v-chip>
                    </v-list-item-title>
                </v-list-item>
                <!-- Non Boolean Representation -->
                <v-list-item class="pt-0" v-if="propertyObject.valueType != 'boolean'">
                    <v-list-item-title>
                        <!-- String and Number Value Textfield -->
                        <v-text-field variant="outlined" density="compact" clearable @keydown.native.enter="updateValue()" v-model="newPropertyValue" :type="propertyObject.valueType == 'string' || propertyObject.valueType == 'none' ? 'text' : 'number'" hint="greyed out value on the left shows the current value in the AAS" @update:focused="setFocus">
                            <!-- Current Value -->
                            <template v-slot:prepend-inner>
                                <v-chip v-if="isFocused || propertyObject.value != newPropertyValue" label size="x-small" border style="margin-top: 2px">{{ propertyObject.value }}</v-chip>
                                <v-divider v-if="isFocused || propertyObject.value != newPropertyValue" class="ml-3 mr-1" vertical inset style="margin-top: 4px"></v-divider>
                            </template>
                            <!-- Update Value Button -->
                            <template v-slot:append-inner>
                                <span class="text-subtitleText">{{ unitSuffix(propertyObject) }}</span>
                                <v-btn v-if="isFocused" size="small" variant="elevated" color="primary" class="text-buttonText" style="margin-top: -2.5px; right: -6px" @click.stop="updateValue()">
                                    <v-icon>mdi-upload</v-icon>
                                </v-btn>
                            </template>
                        </v-text-field>
                    </v-list-item-title>
                </v-list-item>
                <!-- Boolean Representation -->
                <v-list-item class="pt-0" v-else>
                    <template v-slot:title>
                        <!-- Boolean Value Switch -->
                        <v-switch inset density="compact" v-model="booleanProp" color="primary" messages="greyed out value on the right shows the current value in the AAS">
                            <!-- Current Value -->
                            <template v-slot:label>
                                <span style="display: inline; white-space: nowrap">{{ propertyObject.value }}</span>
                            </template>
                        </v-switch>
                    </template>
                    <!-- Update Value Button -->
                    <template v-slot:append>
                        <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="margin-top: -2.5px; right: -6px" @click.stop="updateValue()">
                            <v-icon>mdi-upload</v-icon>
                        </v-btn>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'Property',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling],
    props: ['propertyObject'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            newPropertyValue:   '',             // new value of the property
            booleanProp:        false,          // boolean value of the property (if valueType is boolean)
            isFocused:          false,          // boolean to check if the input field is focused
        }
    },

    mounted() {
        // set the booleanProp to the current value if the valueType is boolean
        if (this.propertyObject.valueType == 'boolean') {
            this.booleanProp = this.propertyObject.value;
        } else {
            this.newPropertyValue = this.propertyObject.value;
        }
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.newPropertyValue = '';
            }
        },

        // Watch for changes in the propertyObject and update the newPropertyValue if the input field is not focused
        propertyObject: {
            deep: true,
            handler() {
                if (!this.isFocused) {
                    this.newPropertyValue = this.propertyObject.value;
                }
            }
        },
    },

    computed: {
        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

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
            let path = this.SelectedAAS.endpoints[0].address + '/' + this.propertyObject.path + '/value';
            let content = this.propertyObject.valueType == 'boolean' ? "'" + this.booleanProp + "'" : "'" + this.newPropertyValue + "'";
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating ' + this.propertyObject.modelType.name + ' "' + this.propertyObject.idShort + '"';
            let disableMessage = false;
            // Send Request to update the value of the property
            this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // this.newPropertyValue = ''; // reset input
                    let updatedPropertyObject = { ...this.propertyObject }; // copy the propertyObject
                    updatedPropertyObject.value = content.toString().replace(/'/g, ''); // update the value of the propertyObject
                    this.$emit('updateValue', updatedPropertyObject); // emit event to update the value in the parent component
                }
            });
        },

        // Function to set the focus on the input field
        setFocus(e: boolean) {
            this.isFocused = e;
            if(!e) this.newPropertyValue = this.propertyObject.value; // set input to current value in the AAS if the input field is not focused
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
