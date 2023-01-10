<template>
    <v-container>
        <!-- Desktop App Bar -->
        <v-app-bar class="px-3" color="appBar">
            <v-row class="mx-0" align="center">
                <v-app-bar-nav-icon class="mr-3" @click.stop="toggleDrawer"></v-app-bar-nav-icon>
                <!-- Logo in the App Bar -->
                <v-img src="Logo.jpg" max-width="40px">
                    <template #sources>
                        <source srcset="@/assets/Logo.jpg">
                    </template>
                </v-img>
                <!-- App Bar Title -->
                <v-col cols="auto">
                    <v-app-bar-title class="text-primary" style="font-weight: bold">AAS Web UI</v-app-bar-title>
                </v-col>
                <!-- Registry Server URL Input -->
                <v-col>
                    <v-text-field variant="outlined" density="compact" hide-details class="mx-5" style="max-width: 400px" label="Registry Server URL" v-model="registryURL" @keydown.native.enter="connectToServer()">
                        <template v-slot:append-inner>
                            <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="margin-top: -2.5px; right: -6px" @click.stop="connectToServer()" :loading="loading">Connect</v-btn>
                        </template>
                    </v-text-field>
                </v-col>
                <v-spacer></v-spacer>
                <!-- Settings-Menu for Auto-Sync and Sync-Interval -->
                <AutoSync></AutoSync>
                <!-- Platform I 4.0 Logo -->
                <v-img src="I40.png" max-width="260px" :style="{filter: isDark ? 'invert(1)' : 'invert(0)'}">
                    <template #sources>
                        <source srcset="@/assets/I40.png">
                    </template>
                </v-img>
                <!-- Switch to change between dark and light Theme -->
                <v-col cols="auto">
                    <v-switch class="ml-5" label="Theme" @change="toggleTheme" v-model="dark" density="compact" color="primary" hide-details inline append-icon="mdi-theme-light-dark"></v-switch>
                </v-col>
            </v-row>
        </v-app-bar>

        <!-- global Snackbar -->
        <v-snackbar v-model="Snackbar.status" :color="Snackbar.color" :timeout="Snackbar.timeout" location="top">
            <v-card v-if="Snackbar.status === true && Snackbar.color == 'error' && Snackbar.baseError">
                <v-card-title class="text-subtitle-2">{{ Snackbar.baseError }}</v-card-title>
                <v-divider></v-divider>
                <v-card-text style="max-height: 200px; overflow-y: auto; max-width: 590px">
                    <div class="text-subtitleText text-caption">{{ Snackbar.extendedError }}</div>
                </v-card-text>
            </v-card>
            <span v-else class="text-buttonText">{{ Snackbar.text }}</span>
            <template v-slot:actions>
                <v-btn :color="Snackbar.btnColor" variant="plain" @click="closeSnackbar()">
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </template>
        </v-snackbar>

        <!-- App Footer -->
        <v-footer app class="bg-appBar text-center d-flex flex-column">
            <div>{{ new Date().getFullYear() }} — <strong>HTW Berlin ©</strong> - <span style="font-size: 12px">developed by Aaron Zielstorff, MIT License</span></div>
        </v-footer>

        <!-- left Side Menu with the AAS List -->
        <v-navigation-drawer width="336" color="appNavigation" v-model="drawerState" class="leftMenu">
            <AASList />
        </v-navigation-drawer>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import AASList  from './AASList.vue';
import AutoSync from './AutoSync.vue';

export default defineComponent({
    name: 'AppNavigation', // Name of the Component
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server

        AASList,    // Component to display the AAS List
        AutoSync,   // Component to display the Auto-Sync Settings
    },
    mixins: [RequestHandling],

    setup () {
        const theme = useTheme()
        const store = useStore()

        return {
            theme, // Theme Object
            toggleTheme: () => theme.global.name.value = theme.global.current.value.dark ? 'light' : 'dark', // Function to toggle between dark and light Theme

            store, // Store Object
        }
    },
    
    data() {
        return {
            dark: false,        // Variable reflecting the current Theme
            drawerState: true,  // Variable to control the state of the Navigation Drawer
            registryURL: '',    // Variable to store the Registry Server URL
            loading: false,     // Variable to control the loading state of the Connect Button
        }
    },

    mounted() {
        // sets the Theme according to the Users preferred Theme
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            this.theme.global.name.value = 'dark';
        } else {
            this.theme.global.name.value = 'light';
        }
        // sets the theme variable for the switch on startup
        if(this.isDark) {
            this.dark = true;
        } else {
            this.dark = false;
        }
        // auto connect to reigistry server that was saved in local storage
        let RegistryURL = window.localStorage.getItem('registryURL');
        if(RegistryURL) {
            this.registryURL = RegistryURL;
            this.connectToServer();
        }


    },

    watch: {
        // Watch for changes in the Snackbar Object and close it after the Timeout
        Snackbar() {
            if(this.Snackbar.status) {
                setTimeout(() => this.closeSnackbar(), this.Snackbar.timeout);
            }
        },
    },


    computed: {
        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark;
        },

        // get snackbar data from store
        Snackbar() {
            return this.store.getters.snackbar;
        },

    },

    methods: {
        // Function to toggle the Navigation Drawer
        toggleDrawer() {
            this.drawerState = !this.drawerState;
        },

        // Function to connect to the Registry Server
        connectToServer() {
            // console.log('connect to server: ' + this.registryURL);
            if(this.registryURL != '') {
                this.loading = true;
                let path = this.registryURL + '/api/v1/registry';
                let context = 'connecting to Registry Server'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    this.loading = false;
                    if (response.success) {
                        this.store.dispatch('dispatchRegistryURL', this.registryURL); // save the URL in the store
                        window.localStorage.setItem('registryURL', this.registryURL); // save the URL in the local storage
                    } else {
                        this.store.dispatch('dispatchRegistryURL', ''); // clear the URL in the store
                    }
                });
            }
        },

        // Function to close the Snackbar
        closeSnackbar() {
            this.store.dispatch('getSnackbar', { status: false })
        },

    },
});
</script>
