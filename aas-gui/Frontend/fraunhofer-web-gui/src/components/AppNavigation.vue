<template>
    <v-container fluid class="ma-0 pa-0">
        <!-- Desktop App Bar -->
        <v-app-bar v-if="!isMobile" app>
            <!-- Logo in the App Bar -->
            <v-img src="@/assets/Logo.jpg" max-width="40px"></v-img>
            <!-- Title of the Webapp -->
            <v-toolbar-title class="primary--text ml-5" style="font-weight: bold">AAS Web GUI</v-toolbar-title>
            <!-- Registry Server URL Input -->
            <v-text-field outlined rounded dense hide-details class="mx-5" style="max-width: 400px" label="Registry Server URL" v-model="serverURL">
                <template v-slot:append>
                    <v-btn x-small rounded color="primary" class="buttonText--text" style="margin-top: 2px; margin-right: -12px" @click.native.stop="connectToServer()" :loading="loading">Connect</v-btn>
                </template>
            </v-text-field>
            <v-spacer></v-spacer>
            <!-- I4.0 Platform Image -->
            <v-img src="@/assets/I40.png" max-width="260px" :style="{filter: isDark ? 'invert(1)' : 'invert(0)'}"></v-img>
            <!-- Switch to change between local and remote network (http <-> https) -->
            <v-switch v-if="1==2" class="ml-10" hide-details label="Local Network" @change="changeNetwork" v-model="local"></v-switch>
            <!-- Switch to change between dark and light theme -->
            <v-switch class="ml-5" hide-details label="Theme" @change="changeTheme" v-model="dark" :value="isDark"></v-switch>
        </v-app-bar>
        <!-- Mobile App Bar -->
        <v-app-bar v-else app>
            <!-- Logo in the App Bar -->
            <v-img src="@/assets/Logo.jpg" max-width="40px"></v-img>
            <!-- Title of the Webapp -->
            <v-toolbar-title class="primary--text ml-5" style="font-weight: bold">AAS Web GUI</v-toolbar-title>
            <v-spacer></v-spacer>
            <!-- Settings Button -->
            <v-btn icon @click="settingsDialog = true"><v-icon>mdi-cog</v-icon></v-btn>
        </v-app-bar>
        <v-dialog v-model="settingsDialog" fullscreen persistant>
            <v-card color="card" tile>
                <v-toolbar>
                    <v-toolbar-title>Settings</v-toolbar-title>
                    <v-spacer></v-spacer>
                    <v-btn icon @click="settingsDialog = false"><v-icon>mdi-close</v-icon></v-btn>
                </v-toolbar>
                <v-card-text>
                    <v-row align="center" justify="center">
                        <v-col cols="12">
                            <!-- Registry Server URL Input -->
                            <v-text-field outlined rounded dense hide-details class="mt-5" style="max-width: 400px" label="Registry Server URL" v-model="serverURL">
                                <template v-slot:append>
                                    <v-btn x-small rounded color="primary" class="buttonText--text" style="margin-top: 2px; margin-right: -12px" @click.native.stop="connectToServer()" :loading="loading">Connect</v-btn>
                                </template>
                            </v-text-field>
                            <!-- Switch to change between local and remote network (http <-> https) -->
                            <v-switch v-if="1==2" class="mt-5" hide-details label="Local Network" @change="changeNetwork" v-model="local"></v-switch>
                            <!-- Switch to change between dark and light theme -->
                            <v-switch class="mt-5" hide-details label="Theme" @change="changeTheme" v-model="dark" :value="isDark"></v-switch>
                            <!-- I4.0 Platform Image -->
                            <v-img class="mt-12" src="@/assets/I40.png" max-width="276px" :style="{filter: isDark ? 'invert(1)' : 'invert(0)'}"></v-img>
                        </v-col>
                    </v-row>
                </v-card-text>
            </v-card>
        </v-dialog>
        <!-- Footer with Copyright and Developer Info (LIcense needed here?) -->
        <v-footer app padless>
            <v-col v-if="isMobile" class="text-center" cols="12">{{ new Date().getFullYear() }} — <strong>HTW Berlin ©</strong></v-col>
            <v-col v-else class="text-center" cols="12">{{ new Date().getFullYear() }} — <strong>HTW Berlin ©</strong> - <span style="font-size: 12px">developed by Aaron Zielstorff</span></v-col>
        </v-footer>
        <!-- global snackbar -->
        <v-snackbar v-model="Snackbar.status" :color="Snackbar.color" :timeout="Snackbar.timeout" :centered="!isMobile" :bottom="isMobile">
            <span class="buttonText--text">{{ Snackbar.text }}</span>
            <template v-slot:action="{ attrs }">
                <v-btn :color="Snackbar.btnColor" rounded outlined v-bind="attrs" @click="closeSnackbar()">Close</v-btn>
            </template>
        </v-snackbar>
    </v-container>
</template>

<script>
import Vue          from 'vue';
import { isMobile } from 'mobile-device-detect';

export default {
    name: 'AppNavigation',
    data: () => ({
        dark: false,
        local: false,
        serverURL: '',
        loading: false,
        settingsDialog: false,
    }),

    mounted() {
        // set theme according to users preferred theme
        if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
            this.$vuetify.theme.dark = true;
        } else {
            this.$vuetify.theme.dark = false;
        }
        // sets the theme variable for the switch on mount
        if(this.$vuetify.theme.dark == true ) {
            this.dark = true;
        } else {
            this.dark = false;
        }
        // sets the local network variable for the switch on mount
        if(window.location.href.substring(0,5) == 'https') {
            this.local = false;
        } else {
            this.local = true;
        }
        // // set the root path for the Webapp -> Path to registry server
        // Vue.url.options.root = 'http://52.170.212.185:4999';
        // auto connect to reigistry server that was saved in local storage
        let RegistryURL = window.localStorage.getItem('registryURL');
        // console.log(RegistryURL);
        if(RegistryURL) {
            this.serverURL = RegistryURL;
            this.connectToServer();
        }
    },

    watch: {
        Snackbar() {
            if(this.Snackbar.status == true) {
                setTimeout(() => this.closeSnackbar(), this.Snackbar.timeout);
            }
        },
    },

    computed: {
        isDark() {
            return this.$vuetify.theme.dark;
        },
        // get snackbar data from store
        Snackbar() {
            return this.$store.getters.snackbar
        },
        isMobile() {
            return isMobile;
        },
    },

    methods: {
        // method to change the theme (dark <-> light)
        changeTheme() {
            if(this.dark) {
                this.$vuetify.theme.dark = true;
            } else {
                this.$vuetify.theme.dark = false;
            }
        },
        // method to change the network (http (for local) <-> https (for remote with ssl certificate))
        changeNetwork() {
            // console.log(this.local)
            if(!this.local) {
                let path = window.location.href.slice(4);
                window.location.replace('https' + path);
            } else {
                let path = window.location.href.slice(5);
                window.location.replace('http' + path);
            }
        },
        // method to connect to the registry server
        connectToServer() {
            if(this.serverURL != '') {
                Vue.url.options.root = this.serverURL;
                this.loading = true;
                this.$http.get('api/v1/registry', {accept: 'application/json'})
                    .then(response => {
                        if(response.body) {
                            this.$store.dispatch('dispatchRegistryServer', this.serverURL);
                            this.$store.dispatch('getSnackbar', {status: true, timeout: 4000, color: 'success', btnColor: 'buttonText', text: 'Successfully connected to Registry Server!' });
                            // write the registry server url to local storage on success
                            window.localStorage.setItem('registryURL', this.serverURL);
                            this.loading = false;
                        } else {
                            this.$store.dispatch('dispatchRegistryServer', null);
                            this.$store.dispatch('getSnackbar', {status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'An Error occured while connecting to Registry Server!' });
                            this.loading = false;
                        }
                    }, () => {
                        this.$store.dispatch('dispatchRegistryServer', null);
                        this.$store.dispatch('getSnackbar', {status: true, timeout: 4000, color: 'error', btnColor: 'buttonText', text: 'No Registry Server found under this URL!' });
                        this.loading = false;
                    });
            }
        },
        closeSnackbar() {
            this.$store.dispatch('getSnackbar', { status: false })
        },
    }
}
</script>