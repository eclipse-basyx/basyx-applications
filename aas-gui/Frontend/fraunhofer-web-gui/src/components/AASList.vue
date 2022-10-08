<template>
    <v-container fluid class="ma-0 pa-0">
        <v-card v-show="!minimized" tile elevation="0" :height="isMobile ? '' : 'calc(100vh - 112px)'" color="card" v-resize="checkWidth" ref="aasCard" class="aasCard">
            <v-card-title class="pa-3 header">
                <!-- searchfield -->
                <v-combobox outlined hide-details dense label="Search for AAS ID..." item-text="idShort" clearable @update:search-input="filterAAS"></v-combobox>
                <v-btn v-if="!isMobile" class="ml-2" icon style="margin-right: -4px" @click="minimize()"><v-icon>mdi-window-restore</v-icon></v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text style="overflow-y: auto;" :style="{height: isMobile ? '' : 'calc(100vh - 176px)' }" class="pa-0">
                <v-hover open-delay="600" close-delay="4000" v-model="hover">
                    <v-list shaped color="card" class="pb-0">
                        <!-- AAS-Items -->
                        <v-list-item
                            @click="selectAAS(item)" :disabled="loading" @mouseenter="showAASDetails(item)" v-for="item in aasData" :key="item.identification.id"
                            class="listItem mb-2" style="border-top: solid; border-right: solid; border-bottom: solid; border-width: 1px;"
                            :style="{ 'border-color': isSelected(item) ? '#009374 !important' : (isDark ? '#686868 !important' : '#ABABAB !important') }"
                        >
                            <v-overlay :value="isSelected(item) ? true : false" absolute color="primary" opacity="0.1"></v-overlay>
                            <v-list-item-avatar>
                                <v-icon color="primary">mdi-robot-industrial</v-icon>
                            </v-list-item-avatar>
                            <v-list-item-content>
                                <v-list-item-title class="primary--text">{{ item.idShort }}</v-list-item-title>
                                <v-list-item-subtitle>{{ item.identification.id }}</v-list-item-subtitle>
                            </v-list-item-content>
                        </v-list-item>
                    </v-list>
                </v-hover>
                <!-- Details for hovered AAS -->
                <v-expand-transition>
                    <v-card v-if="hover && detailsObject" class="transition-fast-in-fast-out v-card--reveal" tile>
                        <v-divider></v-divider>
                        <v-card-title class="px-0 py-0">
                            <v-list-item style="overflow-x: hidden">
                                <v-list-item-content>
                                    <v-list-item-title class="primary--text" style="font-size: 20px">{{ detailsObject.idShort }}</v-list-item-title>
                                    <v-list-item-subtitle style="font-size: 12px">{{ 'Identification (' + detailsObject.identification.idType + '): ' + detailsObject.identification.id }}</v-list-item-subtitle>
                                </v-list-item-content>
                                <v-chip outlined color="primary" small>{{ "AAS" }}</v-chip>
                            </v-list-item>
                        </v-card-title>
                        <v-divider v-if="detailsObject.description && detailsObject.description.length > 0"></v-divider>
                        <v-card-text v-if="detailsObject.description && detailsObject.description.length > 0">
                            <div v-for="(description, i) in detailsObject.description" :key="i">{{ "Description [" + description.language + "]: " + description.text }}</div>
                        </v-card-text>
                        <v-divider></v-divider>
                        <v-card-title class="px-0 py-0">
                            <v-list-item style="overflow-x: hidden">
                                <v-list-item-content>
                                    <v-list-item-title class="primary--text" style="font-size: 20px">{{ detailsObject.asset.idShort }}</v-list-item-title>
                                    <v-list-item-subtitle style="font-size: 12px">{{ 'Identification (' + detailsObject.asset.identification.idType + '): ' + detailsObject.asset.identification.id }}</v-list-item-subtitle>
                                </v-list-item-content>
                                <v-chip outlined color="primary" small>{{ "Asset" }}</v-chip>
                            </v-list-item>
                        </v-card-title>
                        <v-divider v-if="detailsObject.asset.description && detailsObject.asset.description.length > 0"></v-divider>
                        <v-card-text v-if="detailsObject.asset.description && detailsObject.asset.description.length > 0">
                            <div v-for="(description, i) in detailsObject.asset.description" :key="i">{{ "Description [" + description.language + "]: " + description.text }}</div>
                        </v-card-text>
                    </v-card>
                </v-expand-transition>
            </v-card-text>
        </v-card>
        <!-- minimized window -->
        <v-card v-show="minimized" tile elevation="0" height="calc(100vh - 114px)" color="card" class="aasCard2">
            <v-card-title class="px-1 py-3 header" style="margin-top: 2px; margin-bottom: 2px">
                <v-btn class="ml-2" icon style="margin-right: 4px" @click="minimize()"><v-icon>mdi-window-maximize</v-icon></v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text style="overflow-y: auto; height: calc(100vh - 176px)">
                <div class="title" style="transform: rotate(-90deg); transform-origin: top left; white-space: nowrap; margin-top: 266px; margin-left: -2px">Asset Admininstration Shells</div>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script>
import { isMobile } from 'mobile-device-detect';
export default {
    name: 'AASList',
    data () {
        return {
            aasDataServer: [],
            aasData: [],
            minimized: false,
            initalMinimize: true,
            observer: null,
            observerMinimized: null,
            oldWidth: document.getElementsByClassName('aasCard')[0] ? document.getElementsByClassName('aasCard')[0].offsetWidth : 0,
            minimizedByButton: false,
            debounce: false,
            hover: false,
            detailsObject: null,
        }
    },

    mounted() {
        if(this.registryServer) this.getAASList();
        let card = document.getElementsByClassName('aasCard')[0];
        this.observer = new ResizeObserver(this.checkWidth).observe(card);
        let cardMinimized = document.getElementsByClassName('aasCard2')[0];
        this.observerMinimized = new ResizeObserver(this.checkWidth).observe(cardMinimized);
        // console.log(this.observer)
        // if(document.getElementsByClassName('aasCard')[0].offsetWidth < 320) {
        //     this.minimize('byDrag')
        // }
    },

    watch: {
        registryServer() {
            if(this.registryServer) {
                this.getAASList();
            } else {
                this.aasData = [];
            }
        },
    },

    computed: {
        isDark() {
            return this.$vuetify.theme.dark;
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
        // gets all registered AAS from the registry server (called when the component is initialized)
        getAASList() {
            this.$http.get('api/v1/registry', {accept: 'application/json'})
                    .then(response => {
                        // console.log(response.body);
                        // sort by idShort (alphabetically)
                        let responseData = response.body.sort((a, b) => {
                            if (a.idShort < b.idShort) {
                                return -1;
                            }
                            if (a.idShort > b.idShort) {
                                return 1;
                            }
                            return 0;
                        });
                        this.aasData = responseData; // changable aas data array for filtering
                        this.aasDataServer = responseData; // unchangable aas data array with the complete dataset
                    });
        },
        // filters the AAS-list by the search-input
        filterAAS(event) {
            // console.log(event);
            if(event === null) { // reset to original data on clear
                this.aasData = this.aasDataServer;
            } else { // filter data on search
                this.aasData = this.aasDataServer.filter(item => {
                    return item.idShort.toLowerCase().includes(event.toLowerCase());
                });
            }
        },
        // show details for the hovered AAS
        showAASDetails(item) {
            // console.log('showDetails: ', item);
            this.detailsObject = item;
        },
        // selects the AAS that was clicked by the user
        selectAAS(selectedAAS) {
            // console.log(selectedAAS);
            this.$store.dispatch('dispatchAAS', selectedAAS);
        },
        // checks if the AAS is selected
        isSelected(item) {
            if (this.$store.getters.selectedAAS === null) {
                return false;
            }
            return this.$store.getters.selectedAAS.idShort === item.idShort;
        },
        checkWidth() {
            let width = 0; 
            if(this.minimized) {
                width = document.getElementsByClassName('aasCard2')[0].offsetWidth;
            } else {
                width = document.getElementsByClassName('aasCard')[0].offsetWidth;
            }
            // console.log(this.oldWidth,width, this.minimizedByButton, this.minimized);
            if(this.oldWidth >= 320 && width < 320 && this.oldWidth != 0 && !this.minimizedByButton) {
                // console.log('minimized');
                this.minimize('byDrag');
                // this.observer.unobserve(document.getElementsByClassName('aasCard')[0])
            } else if(this.oldWidth <= 321 && width > 321 && this.oldWidth != 0 && !this.minimizedByButton) {
                // console.log('maximized');
                this.minimize('byDrag');
                // this.observer.observe(document.getElementsByClassName('aasCard')[0])
            }
            if(!this.debounce) this.minimizedByButton = false;
            this.debounce = false;
            this.oldWidth = width;
            this.initalMinimize = false;
        },
        // minimizes the AAS-list
        minimize(e) {
            // console.log(this.isMobile, e, this.initalMinimize, this.minimized);
            if(this.isMobile) return;
            if(!e) this.minimizedByButton = true;
            if(!e) this.debounce = true;
            if(e && this.initalMinimize) {
                this.initialMinimize = false;
                return;
            }
            this.minimized = !this.minimized;
            // console.log(this.minimized)
            // console.log(document.getElementsByClassName(this.minimized ? 'aasCard' : 'aasCard2')[0])
            let width = document.getElementsByClassName(this.minimized ? 'aasCard' : 'aasCard2')[0].offsetWidth;
            let window = { status: this.minimized, winNr: 0, e: e, width: width };
            this.$emit('minimize', window);
        },
    },
}
</script>

<style>
.v-card--reveal {
    bottom: 0;
    opacity: .96;
    position: absolute;
    width: 100%;
}
</style>