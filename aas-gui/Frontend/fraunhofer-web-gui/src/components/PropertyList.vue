<template>
    <v-container fluid class="ma-0 pa-0">
        <v-card v-show="!minimized" tile elevation="0" :height="isMobile ? '' : 'calc(100vh - 112px)'" color="card" class="propCard">
            <v-card-title v-if="!isMobile" class="header">Properties</v-card-title>
            <v-divider v-if="!isMobile"></v-divider>
            <v-card-text style="overflow-y: auto;" :style="{height: isMobile ? '' : 'calc(100vh - 176px)' }">
                <v-card v-if="propertyData && propertyData != {} && propertyData != null && !cleared">
                    <v-card-title class="px-2 py-0">
                        <v-list-item style="overflow-x: hidden">
                            <v-list-item-content>
                                <v-list-item-title class="primary--text" style="font-size: 20px">{{ propertyData.idShort }}</v-list-item-title>
                                <v-list-item-subtitle style="font-size: 12px">Last updated: {{ propertyData.timestamp }}</v-list-item-subtitle>
                            </v-list-item-content>
                            <v-chip outlined color="primary" small>{{ propertyData.modelType.name }}</v-chip>
                        </v-list-item>
                    </v-card-title>
                    <v-divider></v-divider>
                    <v-card-text class="pa-0">
                        <v-list rounded>
                            <!-- ID -->
                            <v-list-item link v-if="propertyData.modelType.name === 'SubmodelDescriptor'" style="overflow-x: hidden">
                                <v-list-item-content>
                                    <v-list-item-subtitle class="primary--text font-weight-bold">ID {{ propertyData.identification ? ' (' + propertyData.identification.idType + '):' : ':' }}</v-list-item-subtitle>
                                </v-list-item-content>
                                <v-text-field v-if="propertyData.identification" readonly filled rounded outlined hide-details dense :value="propertyData.identification.id"></v-text-field>
                            </v-list-item>
                            <!-- DataType -->
                            <v-list-item v-if="propertyData.modelType.name === 'Property'">
                                <v-row align="center">
                                    <v-col cols="auto">
                                        <div class="primary--text font-weight-bold">Type:</div>
                                    </v-col>
                                    <v-col cols="auto">
                                        <v-chip small label outlined><div class="primary--text">{{ propertyData.valueType }}</div></v-chip>
                                    </v-col>
                                    <v-spacer></v-spacer>
                                    <v-col cols="auto">
                                        <v-checkbox hide-details label="auto-renew" style="margin-top: -2px" @change="enableRenew" :value="autoRenew" :disabled="enableCmd"></v-checkbox>
                                    </v-col>
                                    <v-col cols="auto" class="px-0">
                                        <v-icon color="primary">mdi-autorenew</v-icon>
                                    </v-col>
                                </v-row>
                            </v-list-item>
                            <!-- Value -->
                            <v-list-item v-if="propertyData.modelType.name === 'Property'">
                                <v-row align="center">
                                    <v-col cols="auto">
                                        <div class="primary--text font-weight-bold">Value:</div>
                                    </v-col>
                                    <v-col cols="auto">
                                        <v-text-field v-if="propertyData && propertyData.valueType != 'boolean'" :readonly="!enableCmd" filled rounded outlined hide-details dense :value="propertyData.value" @input="updateValue" :type="propertyData.valueType == 'string' ? 'text' : 'number'" @keyup.native.enter="sendData()">
                                            <template v-slot:append>
                                                <v-btn v-if="enableCmd" @click="sendData()" x-small rounded color="primary" style="margin-top: 2px; margin-right: -12px"><v-icon small>mdi-upload</v-icon></v-btn>
                                            </template>
                                        </v-text-field>
                                        <v-row v-if="propertyData && propertyData.valueType == 'boolean'">
                                            <v-switch :readonly="!enableCmd" :label="propertyData.value.toString()" @change="updateValue" :input-value="propertyData.value" :false-Value="false" :true-value="true"></v-switch>
                                            <v-btn v-if="enableCmd" @click="sendData()" x-small rounded color="primary" style="margin-top: 22px; margin-left: 14px"><v-icon small>mdi-upload</v-icon></v-btn>
                                        </v-row>
                                    </v-col>
                                    <v-col cols="auto">
                                        <v-checkbox label="En. Cmd." v-model="enableCmd" :disabled="autoRenew"></v-checkbox>
                                    </v-col>
                                </v-row>
                            </v-list-item>
                            <!-- File -->
                            <div v-else-if="propertyData.modelType.name === 'File'">
                                <v-list-item >
                                    <v-row align="center">
                                        <v-col cols="auto">
                                            <div class="primary--text font-weight-bold">Link:</div>
                                        </v-col>
                                        <v-col cols="auto">
                                            <v-text-field v-if="propertyData" :readonly="!enableCmd" filled rounded outlined hide-details dense :value="propertyData.value" @input="updateValue" @keyup.native.enter="sendData()">
                                                <template v-slot:append>
                                                    <v-btn v-if="enableCmd" @click="sendData()" x-small rounded color="primary" style="margin-top: 2px; margin-right: -12px"><v-icon small>mdi-upload</v-icon></v-btn>
                                                </template>
                                            </v-text-field>
                                        </v-col>
                                        <v-col cols="auto">
                                            <v-checkbox label="En. Cmd." v-model="enableCmd"></v-checkbox>
                                        </v-col>
                                    </v-row>
                                </v-list-item>
                                <!-- Image -->
                                <v-row v-if="['jpg', 'jpeg', 'tiff', 'gif', 'bmp', 'png', 'svg', 'avif', 'webp', 'bif'].includes(propertyData.mimeType.replace('application/', ''))">
                                    <v-col cols="12">
                                        <img :src="propertyData.value" style="object-fit: contain" width="100%">
                                    </v-col>
                                </v-row>
                                <!-- Downloadable File -->
                                <v-row v-if="['pdf', 'txt', 'xml', 'json', 'doc', 'docx', 'odt', 'rtf', 'tex', 'wpd'].includes(propertyData.mimeType.replace('application/', ''))" justify="center">
                                    <v-col cols="auto">
                                        <v-btn color="primary" class="buttonText--text" rounded :href="propertyData.value" target="_blank">Download {{ propertyData.mimeType.replace('application/', '').toUpperCase() }} File</v-btn>
                                    </v-col>
                                </v-row>
                            </div>
                            <!-- MultiLanguageProperty -->
                            <v-list-item v-else-if="propertyData.modelType.name === 'MultiLanguageProperty'">
                                <v-row v-for="(mlpValue, i) in propertyData.value" :key="i" align="center">
                                    <v-col cols="auto">
                                        <div class="primary--text font-weight-bold">{{ "Value [" + mlpValue.language + "]:" }}</div>
                                    </v-col>
                                    <v-col cols="auto">
                                        <v-text-field disabled filled rounded outlined hide-details dense :value="mlpValue.text"></v-text-field>
                                    </v-col>
                                </v-row>
                            </v-list-item>
                            <!-- Operation -->
                            <div v-else-if="propertyData.modelType.name === 'Operation'">
                                <v-card flat class="ma-2 elevatedCard">
                                    <v-container class="ma-0 pa-0" fluid v-for="variableType in variableTypes" :key="variableType.id">
                                        <template v-if="propertyData[variableType.type].length > 0">
                                            <v-card-title class="subtitle-1 pa-3">{{ variableType.name }}:</v-card-title>
                                            <v-card-text class="pb-0">
                                                <div v-for="(variable, i) in propertyData[variableType.type]" :key="variable.id">
                                                    <v-card class="pa-3 mb-2">
                                                        <div v-if="variable.value.description && variable.value.description.length > 0" class="mb-1">
                                                            <div v-for="(description, j) in variable.value.description" :key="j" style="font-size: 12px">{{ "[" + description.language + "] " + description.text }}</div>
                                                        </div>
                                                        <v-row align="center">
                                                            <v-col cols="auto">
                                                                <v-chip small label outlined><div class="primary--text">{{ variable.value.valueType }}</div></v-chip>
                                                            </v-col>
                                                            <v-col cols="auto">
                                                                <!-- Input Variables -->
                                                                <template v-if="variableType.type == 'inputVariables'">
                                                                    <v-text-field v-if="variable.value.valueType != 'boolean'" v-model="inputVariableArray[i]" :label="variable.value.idShort" filled rounded outlined hide-details dense :type="variable.value.valueType == 'string' ? 'text' : 'number'"></v-text-field>
                                                                    <v-row v-if="variable.value.valueType == 'boolean'">
                                                                        <v-switch :label="variable.value.idShort" v-model="inputVariableArray[i]" :false-Value="false" :true-value="true"></v-switch>
                                                                    </v-row>
                                                                </template>
                                                                <!-- In-/Output Variables -->
                                                                <template v-if="variableType.type == 'inoutputVariables'">
                                                                    <v-text-field v-if="variable.value.valueType != 'boolean'" v-model="inoutputVariableArray[i]" :label="variable.value.idShort" filled rounded outlined hide-details dense :type="variable.value.valueType == 'string' ? 'text' : 'number'"></v-text-field>
                                                                    <v-row v-if="variable.value.valueType == 'boolean'">
                                                                        <v-switch :label="variable.value.idShort" v-model="inoutputVariableArray[i]" :false-Value="false" :true-value="true"></v-switch>
                                                                    </v-row>
                                                                </template>
                                                                <!-- Output Variables -->
                                                                <template v-if="variableType.type == 'outputVariables'">
                                                                    <v-text-field v-if="variable.value.valueType != 'boolean'" v-model="outputVariableArray[i]" disabled :label="variable.value.idShort" filled rounded outlined hide-details dense :type="variable.value.valueType == 'string' ? 'text' : 'number'"></v-text-field>
                                                                    <v-row v-if="variable.value.valueType == 'boolean'">
                                                                        <v-switch :label="variable.value.idShort" v-model="outputVariableArray[i]" :false-Value="false" :true-value="true"></v-switch>
                                                                    </v-row>
                                                                </template>
                                                            </v-col>
                                                        </v-row>
                                                    </v-card>
                                                </div>
                                            </v-card-text>
                                        </template>
                                    </v-container>
                                    <v-divider></v-divider>
                                    <!-- Execute Button -->
                                    <v-card-actions>
                                        <v-spacer></v-spacer>
                                        <v-btn color="primary" outlined @click="clearInputs">Clear</v-btn>
                                        <v-btn color="primary" class="buttonText--text" @click="executeOperation">Execute</v-btn>
                                    </v-card-actions>
                                </v-card>
                            </div>
                        </v-list>
                    </v-card-text>
                </v-card>
            </v-card-text>
        </v-card>
        <!-- minimized window -->
        <v-card v-show="minimized" tile elevation="0" height="calc(100vh - 114px)" color="card" class="propCard2">
            <v-card-title class="px-1 py-3 header" style="margin-top: 2px; margin-bottom: 2px">
                <v-icon class="ml-2 mt-1 mb-2" color="card" style="margin-right: 4px;">mdi-window-maximize</v-icon>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text style="overflow-y: auto; height: calc(100vh - 176px)">
                <div class="title" style="transform: rotate(-90deg); transform-origin: top left; white-space: nowrap; margin-top: 94px; margin-left: -2px">Properties</div>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script>
import { isMobile } from 'mobile-device-detect';
export default {
    name: 'PropertyList',
    data() {
        return {
            propertyData: null, // data for the property list
            requestInterval: null, // interval for renewing the property value
            autoRenew: false, // if the property value should be renewed automatically
            minimized: false,
            observer: null,
            observerMinimized: null,
            oldWidth: document.getElementsByClassName('propCard')[0] ? document.getElementsByClassName('propCard')[0].offsetWidth : 0,
            cleared: false,
            enableCmd: false,
            inputValue: null,
            variableTypes: [
                { type: 'inputVariables', name: 'Input Variables', id: 0 },
                { type: 'inoutputVariables', name: 'In-/Output Variables', id: 1 },
                { type: 'outputVariables', name: 'Output Variables', id: 2 },
            ],
            inputVariableArray: [],
            inoutputVariableArray: [],
            outputVariableArray: [],
        }
    },

    watch: {
        // initialize property list when prop gets selected or changes
        SelectedProperty: {
            deep: true,
            handler() {
                // console.log('Selected Prop: ', this.SelectedProperty);
                clearInterval(this.requestInterval); // clear old interval
                this.autoRenew = false;
                this.initializeList(); // initialize list
                this.cleared = false;
                this.enableCmd = false;
            }
        },
        registryServer() {
            if(!this.registryServer) {
                this.cleared = true;
            }
        },
    },

    mounted() {
        let card = document.getElementsByClassName('propCard')[0];
        this.observer = new ResizeObserver(this.checkWidth).observe(card);
        let cardMinimized = document.getElementsByClassName('propCard2')[0];
        this.observerMinimized = new ResizeObserver(this.checkWidth).observe(cardMinimized);
        if(document.getElementsByClassName('propCard')[0].offsetWidth < 200) {
            this.minimize('byDrag')
        }
    },

    computed: {
        SelectedProperty() {
            return this.$store.getters.getSelectedProp;
        },
        // get registry server route from store
        registryServer() {
            return this.$store.getters.getRegistryServer;
        },
        isMobile() {
            return isMobile;
        },
    },

    methods: {
        // initialize property list with data from selected property
        initializeList() {
            // console.log('initializeList', this.SelectedProperty);
            if(!this.SelectedProperty) {
                this.propertyData = null;
                return;
            }
            if(this.SelectedProperty.modelType.name === 'Property' || this.SelectedProperty.modelType.name === 'File' || this.SelectedProperty.modelType.name === 'MultiLanguageProperty' || this.SelectedProperty.modelType.name === 'Operation') {
                this.$http.get('submodels/' + this.SelectedProperty.root + '/submodel/submodelElements/' + this.SelectedProperty.submodelElementsString + this.SelectedProperty.idShort, {accept: 'application/json'})
                        .then(response => {
                            // console.log('response', response.body);
                            let prop = response.body;
                            prop.timestamp = this.formatDate(new Date());
                            prop.id = this.UUID();
                            this.$store.dispatch('dispatchRealtimeProp', prop);
                            // initialize Operation DataType
                            if(this.SelectedProperty.modelType.name === 'Operation') {
                                this.initializeOperation(prop);
                            } else {
                                this.propertyData = prop;
                            }
                        });
            } else {
                let prop = this.SelectedProperty;
                prop.timestamp = this.formatDate(new Date());
                this.propertyData = prop;
            }
        },
        // initialize Operation DataType
        initializeOperation(prop) {
            prop.inputVariables.forEach(() => {
                this.inputVariableArray.push(null);
            });
            prop.inoutputVariables.forEach(() => {
                this.inoutputVariableArray.push(null);
            });
            prop.outputVariables.forEach(element => {
                this.outputVariableArray.push(element.value.value);
            });
            this.propertyData = prop;
        },
        updateValue(e) {
            // console.log('updateValue', e);
            this.inputValue = e;
        },
        // method to write data to the AAS
        sendData() {
            // console.log('sendData', this.propertyData, this.inputValue);
            if(!this.enableCmd) return;
            // console.log(this.propertyData, this.SelectedProperty);
            if(this.inputValue === null) this.inputValue = this.propertyData.value;
            this.$http.put('submodels/' + this.SelectedProperty.root + '/submodel/submodelElements/' + this.SelectedProperty.submodelElementsString + this.SelectedProperty.idShort + '/value', "'" + this.inputValue + "'", {'accept': 'application/json', 'content-type': 'application/json'})
                    .then(() => {
                        // console.log('response', response.body);
                        this.$store.dispatch('getSnackbar', {status: true, timeout: 2000, color: 'success', btnColor: 'buttonText', text: 'Property updated' });
                    }, () => {
                        this.$store.dispatch('getSnackbar', {status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'Error updating Property!' });
                    });
        },
        //auto renew property data
        enableRenew(e) {
            // console.log('enableRenew', e);
            this.autoRenew = e;
            if(e) {
                this.requestInterval = setInterval(() => {
                    this.initializeList();
                }, 1000);
            } else {
                clearInterval(this.requestInterval);
            }
        },
        // convert date element to digits
        padTo2Digits(num) {
            return num.toString().padStart(2, '0');
        },
        // convert js date object to string
        formatDate(date) {
            return (
                [
                    date.getFullYear(),
                    this.padTo2Digits(date.getMonth() + 1),
                    this.padTo2Digits(date.getDate()),
                ].join('-') +
                ' ' +
                [
                    this.padTo2Digits(date.getHours()),
                    this.padTo2Digits(date.getMinutes()),
                    this.padTo2Digits(date.getSeconds()),
                ].join(':')
            );
        },
        // clear Operation Inputs
        clearInputs() {
            // console.log('clearInputs');
            this.inputVariableArray = new Array(this.inputVariableArray.length).fill(null);
            this.inoutputVariableArray = new Array(this.inoutputVariableArray.length).fill(null);
            this.outputVariableArray = new Array(this.outputVariableArray.length).fill(null);
        },
        // execute Operation
        executeOperation() {
            // console.log('executeOperation', this.inputVariableArray, this.inoutputVariableArray, this.outputVariableArray);
            // console.log('submodels/' + this.SelectedProperty.root + '/submodel/submodelElements/' + this.SelectedProperty.submodelElementsString + this.SelectedProperty.idShort + '/invoke');
            let operationPath = 'submodels/' + this.SelectedProperty.root + '/submodel/submodelElements/' + this.SelectedProperty.submodelElementsString + this.SelectedProperty.idShort + '/invoke';
            let requestObject = {};
            requestObject.requestId = this.UUID();
            // console.log(this.propertyData.inputVariables);
            let inputVariables = this.propertyData.inputVariables;
            inputVariables.forEach((element, i) => {
                element.value.value = this.inputVariableArray[i];
            });
            let inoutputVariables = this.propertyData.inoutputVariables;
            inoutputVariables.forEach((element, i) => {
                element.value.value = this.inoutputVariableArray[i];
            });
            requestObject.inputArguments = inputVariables;
            requestObject.inoutputArguments = inoutputVariables;
            requestObject.timeout = 60000;
            // console.log('requestObject', requestObject);
            this.$http.post(operationPath, requestObject, {'accept': 'application/json', 'content-type': 'application/json'})
                    .then(response => {
                        // console.log('response', response.body);
                        this.outputVariableArray = [];
                        response.body.outputArguments.forEach(element => {
                            this.outputVariableArray.push(element.value.value);
                        });
                        this.inoutputVariableArray = [];
                        response.body.inoutputArguments.forEach(element => {
                            this.inoutputVariableArray.push(element.value.value);
                        });
                        this.$store.dispatch('getSnackbar', {status: true, timeout: 2000, color: 'success', btnColor: 'buttonText', text: 'Operation was successfully executed!' });
                    }, () => {
                        this.$store.dispatch('getSnackbar', {status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'Error executing Operation!' });
                    });
        },
        // generate random uuid
        UUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
        checkWidth() {
            let width = 0; 
            if(this.minimized) {
                width = document.getElementsByClassName('propCard2')[0].offsetWidth;
            } else {
                width = document.getElementsByClassName('propCard')[0].offsetWidth;
            }
            // console.log(this.oldWidth,width);
            if(this.oldWidth >= 200 && width < 200 && this.oldWidth != 0) {
                // console.log('minimized');
                this.minimize('byDrag');
            } else if(this.oldWidth <= 201 && width > 201 && this.oldWidth != 0) {
                this.minimize('byDrag');
            }
            this.oldWidth = width;
        },
        // changes to minimized state
        minimize() {
            if(this.isMobile) return;
            this.minimized = !this.minimized;
        },
    },
}
</script>