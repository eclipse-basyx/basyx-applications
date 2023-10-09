<template>
    <v-container fluid class="pa-0">
        <v-card color="elevatedCard" v-if="operationObject" class="mt-4">
            <!-- Operation with Variable(s) -->
            <v-list nav class="bg-elevatedCard py-0" v-if="operationObject.inputVariables.length > 0 || operationObject.inoutputVariables.length > 0 || operationObject.outputVariables.length > 0">
                <!-- List with the Variable Types -->
                <v-container class="ma-0 pa-0" fluid v-for="variableType in variableTypes" :key="variableType.id">
                    <template v-if="operationObject[variableType.type].length > 0">
                        <!-- Title of the Variable Type -->
                        <v-list-item class="px-1 pb-1 pt-0">
                            <v-list-item-title class="text-subtitle-2 mt-2">{{ variableType.name + ':' }}</v-list-item-title>
                        </v-list-item>
                        <!-- List with the Fields belonging to the Variable Type -->
                        <v-card v-for="(variable, i) in operationObject[variableType.type]" :key="i" class="mb-3">
                            <!-- Variable Description -->
                            <DescriptionElement v-if="variable.value && variable.value.description" :descriptionObject="variable.value.description" :descriptionTitle="'Description'" :small="true"></DescriptionElement>
                            <v-divider v-if="variable.value && variable.value.description" class="mt-1"></v-divider>
                            <!-- Variable Value -->
                            <v-list-item>
                                <v-list-item-title class="pt-2">
                                    <!-- Input Field containing the Variable Value (non boolean) -->
                                    <v-text-field v-if="variable.value.valueType != 'boolean'" v-model="operationVariables[variableType.type][i]" variant="outlined" density="compact" hide-details clearable :type="isNumber(variable.value.valueType) ? 'number' : 'text'" :label="variable.value.idShort" :readonly="variableType.type == 'outputVariables'">
                                        <template v-slot:prepend-inner>
                                            <!-- Variable Type -->
                                            <v-chip label size="x-small" border color="primary">{{ variable.value.valueType }}</v-chip>
                                        </template>
                                    </v-text-field>
                                    <!-- Switch containing the Variable Value (boolean) -->
                                    <v-switch v-else v-model="operationVariables[variableType.type][i]" :label="variable.value.idShort" color="primary" inset hide-details density="compact" :readonly="variableType.type == 'outputVariables'"></v-switch>
                                </v-list-item-title>
                            </v-list-item>
                        </v-card>
                    </template>
                </v-container>
            </v-list>
            <!-- Warning when Operation has no variable(s) -->
            <v-list nav class="bg-elevatedCard pt-0" v-else>
                <v-list-item>
                    <v-list-item-title class="pt-2">
                        <v-alert text="Operation doesn't contain a Variable!" density="compact" type="warning" variant="outlined"></v-alert>
                    </v-list-item-title>
                </v-list-item>
            </v-list>
            <v-divider></v-divider>
            <!-- Action Buttons for the Operation -->
            <v-list nav class="bg-elevatedCard pa-0">
                <v-list-item>
                    <template v-slot:append>
                        <!-- Clear-Button -->
                        <v-btn @click="clearFields()" size="small" variant="outlined" color="primary" class="mr-3">clear</v-btn>
                        <!-- Execute-Button -->
                        <v-btn @click="executeOperation()" size="small" class="text-buttonText" color="primary" :loading="loading">execute</v-btn>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

import DescriptionElement from '../UIComponents/DescriptionElement.vue';

export default defineComponent({
    name: 'Operation',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        SubmodelElementHandling, // Mixin to handle the SubmodelElements

        DescriptionElement,
    },
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['operationObject'],

    setup() {
        const navigationStore = useNavigationStore()
        const aasStore = useAASStore()

        return {
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            variableTypes: [ // Variable Types for the Operation
                { type: 'inputVariables',       name: 'Input Variables',        id: 0 }, // write
                { type: 'inoutputVariables',    name: 'In-/Output Variables',   id: 1 }, // read/write
                { type: 'outputVariables',      name: 'Output Variables',       id: 2 }, // read
            ] as Array<any>,
            operationVariables: {
                inputVariables:     [], // Array containing the local State of the Input Variables
                inoutputVariables:  [], // Array containing the local State of the In-/Output Variables
                outputVariables:    [], // Array containing the local State of the Output Variables
            } as any,
            loading: false, // Loading State of the Operation
        }
    },

    mounted() {
        // console.log(this.operationObject)
        this.initializeOperation(this.operationObject);
    },

    watch: {
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
        // initialize Operation DataType
        initializeOperation(operationObject: any) {
            operationObject.inputVariables.forEach(() => {
                this.operationVariables.inputVariables.push(null);
            });
            operationObject.inoutputVariables.forEach(() => {
                this.operationVariables.inoutputVariables.push(null);
            });
            operationObject.outputVariables.forEach(() => {
                this.operationVariables.outputVariables.push(null);
            });
        },

        // Function to clear all Fields
        clearFields() {
            this.operationVariables.inputVariables = [];
            this.operationVariables.inoutputVariables = [];
            this.operationVariables.outputVariables = [];
            this.initializeOperation(this.operationObject);
        },

        // Function to execute the Operation
        executeOperation() {
            // create Array containing the Input Variables which will will be send to the Server
            let inputVariables = this.operationObject.inputVariables;
            inputVariables.forEach((inputVariable: any, i: number) => {
                inputVariable.value.value = this.operationVariables.inputVariables[i]
            })
            // create Array containing the In-/Output Variables which will will be send to the Server
            let inoutputVariables = this.operationObject.inoutputVariables;
            inoutputVariables.forEach((inoutputVariable: any, i: number) => {
                inoutputVariable.value.value = this.operationVariables.inoutputVariables[i]
            })
            // console.log('executeOperation: ', inputVariables, inoutputVariables);
            let path = this.operationObject.path + '/invoke';
            let content = {
                inputArguments:     inputVariables,
                inoutputArguments:  inoutputVariables,
                requestId:          this.UUID(),
                timeout:            60000, // 60 second timeout
            };
            let body = JSON.stringify(content);
            let headers = { 'accept': 'application/json', 'Content-Type': 'application/json' };
            let context = 'invoking ' + this.operationObject.modelType + ' "' + this.operationObject.idShort + '"';
            let disableMessage = false;
            this.loading = true;
            this.postRequest(path, body, headers, context, disableMessage).then((response: any) => {
                this.loading = false;
                if (response.success) {
                    // clear the operationVariables which will be filled with the new values
                    this.operationVariables.inoutputVariables = [];
                    this.operationVariables.outputVariables = [];
                    // fill the operationVariables with the new values
                    response.data.inoutputArguments.forEach((inoutputVariable: any) => {
                        this.operationVariables.inoutputVariables.push(inoutputVariable.value.value);
                    });
                    response.data.outputArguments.forEach((outputVariable: any) => {
                        this.operationVariables.outputVariables.push(outputVariable.value.value);
                    });
                    // check the operationResult, if success is false, show an error message
                    if (response.data.operationResult && !response.data.operationResult.success) {
                        this.errorHandler(response.data.operationResult, context);
                    } else {
                        this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'success', btnColor: 'buttonText', text: 'Operation executed successfully.' }); // Show Success Snackbar
                    }
                }
            });
        },

        // generate random uuid
        UUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },

    },
});
</script>
