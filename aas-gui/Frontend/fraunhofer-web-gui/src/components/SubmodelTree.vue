<template>
    <v-container fluid class="ma-0 pa-0">
        <v-card v-show="!minimized" tile elevation="0" :height="isMobile ? '' : 'calc(100vh - 112px)'" color="card" class="subCard">
            <v-card-title v-if="!isMobile" class="header">Submodels</v-card-title>
            <v-divider v-if="!isMobile"></v-divider>
            <v-card-text style="overflow-y: auto;" :style="{height: isMobile ? '' : 'calc(100vh - 176px)' }">
                <v-row v-if="loading" justify="center">
                    <v-col cols="auto">
                        <v-progress-circular :size="70" :width="7" indeterminate></v-progress-circular>
                    </v-col>
                </v-row>
                <v-treeview v-else :items="submodelData" item-text="idShort" dense rounded hoverable return-object :load-children="fetchChildren" activatable @update:active="selectProp">
                    <template v-slot:prepend="{ item, open }">
                        <v-icon v-if="item.modelType.name === 'SubmodelDescriptor' && !item.children" color="primary">mdi-folder-alert</v-icon>
                        <v-icon v-else-if="item.modelType.name === 'SubmodelDescriptor' && item.children" color="primary">{{ open ? 'mdi-folder-open' : '$folderSubOpen' }}</v-icon>
                        <v-icon v-else-if="item.modelType.name === 'SubmodelElementCollection'" color="primary">$fileSub</v-icon>
                        <v-icon v-else color="primary">mdi-file-code</v-icon>
                    </template>
                    <template v-slot:append="{ item }">
                        <v-text-field v-if="item.identification" readonly filled rounded outlined hide-details dense :value="item.identification.id"></v-text-field>
                        <v-chip v-if="item.modelType && item.modelType.name != 'Submodel' && item.modelType.name != 'SubmodelDescriptor'" outlined color="primary" small>{{ item.modelType.name }}</v-chip>
                    </template>
                </v-treeview>
            </v-card-text>
        </v-card>
        <!-- minimized window -->
        <v-card v-show="minimized" tile elevation="0" height="calc(100vh - 114px)" color="card" class="subCard2">
            <v-card-title class="px-1 py-3 header" style="margin-top: 2px; margin-bottom: 2px">
                <v-icon class="ml-2 mt-1 mb-2" color="card" style="margin-right: 4px;">mdi-window-maximize</v-icon>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text style="overflow-y: auto; height: calc(100vh - 176px)">
                <div class="title" style="transform: rotate(-90deg); transform-origin: top left; white-space: nowrap; margin-top: 102px; margin-left: -2px">Submodels</div>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script>
import Vue          from 'vue';
import { isMobile } from 'mobile-device-detect';

export default {
    name: 'SubmodelTree',
    data() {
        return {
            submodelData: [], // data for the treeview
            minimized: false,
            observer: null,
            observerMinimized: null,
            oldWidth: document.getElementsByClassName('subCard')[0] ? document.getElementsByClassName('subCard')[0].offsetWidth : 0,
        }
    },

    watch: {
        // initialize treeview when aas gets selected or changes
        SelectedAAS: {
            deep: true,
            handler() {
                // console.log('Selected AAS: ', this.SelectedAAS);
                this.initializeTree();
            }
        },
        registryServer() {
            if(!this.registryServer) {
                this.submodelData = [];
            }
        },
    },

    mounted() {
        let card = document.getElementsByClassName('subCard')[0];
        this.observer = new ResizeObserver(this.checkWidth).observe(card);
        let cardMinimized = document.getElementsByClassName('subCard2')[0];
        this.observerMinimized = new ResizeObserver(this.checkWidth).observe(cardMinimized);
        if(document.getElementsByClassName('subCard')[0].offsetWidth < 310) {
            this.minimize('byDrag')
        }
    },

    computed: {
        // get selected aas from store
        SelectedAAS() {
            return this.$store.getters.selectedAAS;
        },
        // gets loading state from store
        loading() {
            return this.$store.getters.getLoadingState;
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
        // initialize treeview with submodels
        initializeTree() {
            if(this.loading) return;
            this.$store.dispatch('dispatchLoading', true);
            this.submodelData = []; // clear treeview data on initialize
            if(!this.SelectedAAS || !this.SelectedAAS.endpoints[0] || !this.SelectedAAS.endpoints[0].address) return // check if endpoint is valid
            Vue.url.options.root = this.SelectedAAS.endpoints[0].address; // update url root to selected endpoint of aas
            // build root layer with submodels
            this.$http.get('submodels', {accept: 'application/json'})
                    .then(response => {
                        let finishPromise = new Promise((resolve) => {
                            // console.log('response: ', response.body);
                            response.body.forEach( (sub, index, array) => {
                                this.checkSubmodel(sub)
                                    .then( result => {
                                        // console.log(result);
                                        if(result) sub.children = [];
                                        sub.id = this.UUID();
                                        this.submodelData.push(sub);
                                        if (index === array.length -1) resolve();
                                    });
                            });
                        });
                        finishPromise.then(() => {
                            // console.log('submodelData: ', this.submodelData);
                            this.$store.dispatch('dispatchLoading', false);
                        });
                    });
        },
        // check if submodel has props
        async checkSubmodel(sub) {
            return new Promise(resolve => {
                this.$http.get('submodels/' + sub.idShort + '/submodel', {accept: 'application/json'})
                        .then(response => {
                            // console.log(response.body, sub);
                            if(response.body.submodelElements.length > 0) {
                                resolve(true);
                            } else {
                                resolve(false);
                            }
                        });
            });
        },
        // fetch children of submodel
        async fetchChildren(parent) {
            // console.log('fetchChildren', parent);
            // if a submodel folder is clicked (first layer beneath root)
            if(parent.modelType.name == 'SubmodelDescriptor' || parent.modelType.name == 'Submodel') {
                await this.$http.get('submodels/' + parent.idShort + '/submodel', {accept: 'application/json'})
                        .then(response => {
                            // console.log(response.body);
                            response.body.submodelElements.forEach(sub => {
                                sub.id = this.UUID();
                                sub.root = parent.idShort;
                                sub.parentID = '';
                                sub.submodelElementsString = '';
                                // console.log(sub);
                                if(sub.modelType.name == 'SubmodelElementCollection') {
                                    // console.log('SubmodelElementCollection', sub);
                                    sub.children = [];
                                }
                            });
                            parent.children = response.body.submodelElements;
                        });
            }
            // if a submodel collection folder is clicked (from second layer beneath root until tip of branch ist reached)
            if(parent.modelType.name == 'SubmodelElementCollection') {
                await this.$http.get('submodels/' + parent.root + '/submodel/submodelElements/' + parent.parentID + parent.idShort, {accept: 'application/json'})
                        .then(response => {
                            // console.log(response.body);
                            response.body.value.forEach(col => {
                                if(col.modelType.name == 'SubmodelElementCollection') {
                                    // console.log('SubmodelElementCollection', col);
                                    col.children = [];
                                }
                                // console.log(col);
                                col.id = this.UUID();
                                col.root = parent.root;
                                if(parent.parentID){
                                    col.parentID = parent.parentID + parent.idShort + '/';
                                } else {
                                    col.parentID = parent.idShort + '/';
                                }
                                col.submodelElementsString = parent.submodelElementsString + parent.idShort + '/';
                            });
                            // console.log(response.body.value);
                            parent.children = response.body.value;
                        });
            }
        },
        // generate random uuid
        UUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
        // select a property (node) in the treeview for display on the right side
        selectProp(item) {
            // console.log('selectProp', item[0]);
            this.$store.dispatch('dispatchSelectedProp', item[0]);
        },
        checkWidth() {
            let width = 0; 
            if(this.minimized) {
                width = document.getElementsByClassName('subCard2')[0].offsetWidth;
            } else {
                width = document.getElementsByClassName('subCard')[0].offsetWidth;
            }
            // console.log(this.oldWidth,width);
            if(this.oldWidth >= 310 && width < 310 && this.oldWidth != 0) {
                // console.log('minimized');
                this.minimize('byDrag');
            } else if(this.oldWidth <= 311 && width > 311 && this.oldWidth != 0) {
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