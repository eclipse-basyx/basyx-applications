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
            if(this.SelectedProperty.modelType.name === 'Property' || this.SelectedProperty.modelType.name === 'File') {
                this.$http.get('submodels/' + this.SelectedProperty.root + '/submodel/submodelElements/' + this.SelectedProperty.submodelElementsString + this.SelectedProperty.idShort, {accept: 'application/json'})
                        .then(response => {
                            // console.log('response', response.body);
                            let prop = response.body;
                            prop.timestamp = this.formatDate(new Date());
                            this.$store.dispatch('dispatchRealtimeProp', prop);
                            this.propertyData = prop;
                        });
            } else {
                let prop = this.SelectedProperty;
                prop.timestamp = this.formatDate(new Date());
                this.propertyData = prop;
            }
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
        checkWidth() {
            if (this.$route.name != 'MainWindow') return;
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