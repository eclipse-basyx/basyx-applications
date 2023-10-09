<template>
    <v-container>
        <!-- Main App Bar -->
        <v-app-bar class="px-3" color="appBar">
            <v-row class="mx-0" align="center">
                <v-tooltip location="bottom" :open-delay="600">
                    <template v-slot:activator="{ props }">
                        <v-card @click="openDashboard()" flat v-bind="props" color="appBar">
                            <v-row align="center">
                                <v-col class="ml-1 my-1">
                                    <!-- Logo in the App Bar -->
                                    <img :src="imagePath" style="min-height: 42px; max-height: 42px; margin-top: 8px">
                                </v-col>
                                <v-col class="mr-3">
                                    <!-- App Bar Title -->
                                    <v-app-bar-title class="text-primary" style="font-weight: bold">AAS Web UI</v-app-bar-title>
                                </v-col>
                            </v-row>
                        </v-card>
                    </template>
                    <span v-if="WidgetFeatureActive">{{ 'Dashboard' }}</span>
                </v-tooltip>
                <!-- Menu Toggle (Desktop) -->
                <v-menu v-if="!isMobile" :close-on-content-click="false" v-model="mainMenu">
                    <template v-slot:activator="{ props: menu }">
                        <v-tooltip text="Main Menu" location="bottom" :open-delay="600">
                            <template v-slot:activator="{ props: tooltip }">
                                <v-app-bar-nav-icon class="ml-3" v-bind="mergeProps(menu, tooltip)"></v-app-bar-nav-icon>
                            </template>
                        </v-tooltip>
                    </template>
                    <!-- Main Menu Component -->
                    <MainMenu @closeMenu="mainMenu = false"></MainMenu>
                </v-menu>
                <v-spacer></v-spacer>
                <!-- Settings-Menu for Auto-Sync and Sync-Interval -->
                <AutoSync></AutoSync>
                <!-- Platform I 4.0 Logo -->
                <v-img v-if="!isMobile" src="I40.png" max-width="260px" :style="{filter: isDark ? 'invert(1)' : 'invert(0)'}">
                    <template #sources>
                        <source srcset="@/assets/I40.png">
                    </template>
                </v-img>
                <!-- Menu Toggle (Mobile) -->
                <v-dialog v-if="isMobile" fullscreen v-model="mainMenu" :z-index="9993">
                    <template v-slot:activator="{ props: menu }">
                        <v-tooltip text="Main Menu" location="bottom" :open-delay="600">
                            <template v-slot:activator="{ props: tooltip }">
                                <v-app-bar-nav-icon v-bind="mergeProps(menu, tooltip)"></v-app-bar-nav-icon>
                            </template>
                        </v-tooltip>
                    </template>
                    <v-card color="card">
                        <v-toolbar color="appBar" elevation="3" class="mb-3">
                            <v-toolbar-title>Settings</v-toolbar-title>
                            <v-spacer></v-spacer>
                            <v-toolbar-items>
                                <v-btn icon="mdi-close" @click="mainMenu = false" class="mr-3"></v-btn>
                            </v-toolbar-items>
                        </v-toolbar>
                        <!-- Auto-Sync and Theme Settings in Mobile View -->
                        <v-row justify="center" style="max-height: 128px !important">
                            <v-col cols="12" class="text-center">
                                <ThemeSwitch></ThemeSwitch>
                            </v-col>
                        </v-row>
                        <v-divider></v-divider>
                        <!-- Main Menu Component -->
                        <MainMenu @closeMenu="mainMenu = false"></MainMenu>
                    </v-card>
                </v-dialog>
                <!-- Settings Menu -->
                <Settings v-if="!isMobile"></Settings>
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
        <v-footer app class="bg-appBar text-center d-flex flex-column py-0">
            <v-list-item class="px-1">
                <v-list-item-title>
                    <div>{{ new Date().getFullYear() }} — <strong>HTW Berlin ©</strong> - <span style="font-size: 12px">developed by Aaron Zielstorff, MIT License</span></div>
                </v-list-item-title>
            </v-list-item>
        </v-footer>

        <!-- left Side Menu with the AAS List -->
        <v-navigation-drawer width="336" color="appNavigation" :rail="drawerState" class="leftMenu" v-if="mainWindowOpen && !isMobile">
            <AASList />
        </v-navigation-drawer>

        <!-- Mobile Menu -->
        <v-menu transition="slide-y-reverse-transition" v-model="mobileMenu" v-if="isMobile && !mainMenu" style="z-index: 9992">
            <template v-slot:activator="{ props }">
                <v-btn v-bind="props" :icon="mobileMenu ? 'mdi-close' : 'mdi-dots-vertical'" :color="mobileMenu ? '' : 'primary'" style="position: fixed; bottom: 50px; right: 10px; z-index: 9990"></v-btn>
            </template>
            <div class="mr-1 mb-6">
                <v-row v-for="menuEntry in menuEntries" :key="menuEntry.id" justify="end" align="center">
                    <v-col cols="auto">
                        <div>{{ menuEntry.name }}</div>
                    </v-col>
                    <v-col cols="auto" class="py-1">
                        <v-btn :icon="menuEntry.icon" @click="openWindow(menuEntry.route)" :active="menuEntry.route == $route.path" style="z-index: 9990" size="small"></v-btn>
                    </v-col>
                </v-row>
            </div>
        </v-menu>

    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { mergeProps } from 'vue'
import { useTheme } from 'vuetify';
import { useNavigationStore } from '@/store/NavigationStore';
import { useWidgetsStore } from '@/store/WidgetsStore';
import { useEnvStore } from '@/store/EnvironmentStore';
import RequestHandling from '../../mixins/RequestHandling';
import AASList  from './AASList.vue';
import AutoSync from './AutoSync.vue';
import ThemeSwitch from './Settings/ThemeSwitch.vue';
import MainMenu from './MainMenu.vue';
import Settings from './Settings.vue';

export default defineComponent({
    name: 'AppNavigation', // Name of the Component
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server

        AASList,    // Component to display the AAS List
        AutoSync,   // Component to display the Auto-Sync Settings
        ThemeSwitch,// Component to display the Theme Switch
        MainMenu,   // Component to display the Main Menu
        Settings,   // Component to display the Settings Menu
    },
    mixins: [RequestHandling],

    setup () {
        const theme = useTheme()
        const navigationStore = useNavigationStore()
        const widgetsStore = useWidgetsStore()
        const envStore = useEnvStore()

        return {
            theme, // Theme Object
            navigationStore, // NavigationStore Object
            widgetsStore, // WidgetsStore Object
            envStore, // EnvironmentStore Object
        }
    },
    
    data() {
        return {
            loading: false,                 // Variable to show the loading animation on the Connect Button
            registryURL: '',                // Variable to store the Registry Server URL
            AASRepoURL: '',                 // Variable to store the AAS Repository URL
            SubmodelRepoURL: '',            // Variable to store the Submodel Repository URL
            ConceptDescriptionRepoURL: '',  // Variable to store the Concept Description Repository URL
            mainMenu: false,                // Variable to show the Main Menu
            mobileMenu: false,              // Variable to show the Mobile Menu
            menuEntries: [
                { name: 'AAS List',         icon: 'mdi-format-list-text',               route: '/aaslist',                  id: 1 },
                { name: 'AAS Treeview',     icon: 'mdi-format-list-group',              route: '/aastreeview',              id: 2 },
                { name: 'Element Details',  icon: 'mdi-cards-variant',                  route: '/submodelelementview',      id: 3 },
                { name: 'Visualization',    icon: 'mdi-chart-timeline-variant-shimmer', route: '/componentvisualization',   id: 4 },
            ]
        }
    },

    mounted() {
        // check the local storage for a saved theme preference
        let theme = localStorage.getItem('theme');
        if (theme) {
            if (theme == 'dark' || theme == 'light') {
                this.theme.global.name.value = theme;
            } else {
                // sets the Theme according to the Users preferred Theme
                if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                    this.theme.global.name.value = 'dark';
                } else {
                    this.theme.global.name.value = 'light';
                }
            }
        } else {
            // sets the Theme according to the Users preferred Theme
            if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
                this.theme.global.name.value = 'dark';
            } else {
                this.theme.global.name.value = 'light';
            }
        }

        // auto connect to registry server that was saved in local storage
        let RegistryURL = window.localStorage.getItem('registryURL');
        if(RegistryURL) {
            this.registryURL = RegistryURL;
            this.connectToRegistry();
            // console.log('RegistryURL was found in local storage', RegistryURL);
        } else { // if no registry server was saved in local storage, check if an environment variable is set
            if (this.EnvRegistryPath && this.EnvRegistryPath != '') {
                this.registryURL = this.EnvRegistryPath;
                this.connectToRegistry();
            }
        }

        // auto connect to AAS Repository that was saved in local storage
        let aasRepoURL = window.localStorage.getItem('AASRepoURL');
        if(aasRepoURL) {
            this.AASRepoURL = aasRepoURL;
            this.connectToEnvironment('AAS');
            // console.log('AASRepoURL was found in local storage', AASRepoURL);
        } else { // if no aas server was saved in local storage, check if an environment variable is set
            if (this.EnvAASRepoPath && this.EnvAASRepoPath != '') {
                this.AASRepoURL = this.EnvAASRepoPath;
                this.connectToEnvironment('AAS');
            }
        }

        // auto connect to Submodel Repository that was saved in local storage
        let submodelRepoURL = window.localStorage.getItem('SubmodelRepoURL');
        if(submodelRepoURL) {
            this.SubmodelRepoURL = submodelRepoURL;
            this.connectToEnvironment('Submodel');
            // console.log('SubmodelRepoURL was found in local storage', SubmodelRepoURL);
        } else { // if no submodel server was saved in local storage, check if an environment variable is set
            if (this.EnvSubmodelRepoPath && this.EnvSubmodelRepoPath != '') {
                this.SubmodelRepoURL = this.EnvSubmodelRepoPath;
                this.connectToEnvironment('Submodel');
            }
        }

        // auto connect to Concept Description Repository that was saved in local storage
        let conceptDescriptionRepoURL = window.localStorage.getItem('ConceptDescriptionRepoURL');
        if(conceptDescriptionRepoURL) {
            this.ConceptDescriptionRepoURL = conceptDescriptionRepoURL;
            this.connectToEnvironment('ConceptDescription');
            // console.log('ConceptDescriptionRepoURL was found in local storage', ConceptDescriptionRepoURL);
        } else { // if no concept description server was saved in local storage, check if an environment variable is set
            if (this.EnvConceptDescriptionRepoPath && this.EnvConceptDescriptionRepoPath != '') {
                this.ConceptDescriptionRepoURL = this.EnvConceptDescriptionRepoPath;
                this.connectToEnvironment('ConceptDescription');
            }
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
        // Check if the current Platform is Mobile
        isMobile() {
            return this.platform.android || this.platform.ios ? true : false;
        },
        
        // get Platform from store
        platform() {
            return this.navigationStore.getPlatform;
        },

        // get Drawer State from store
        drawerState() { // Computed Property to control the state of the Navigation Drawer (true -> collapsed, false -> extended)
            return this.navigationStore.getDrawerState;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark;
        },

        // get snackbar data from store
        Snackbar() {
            return this.navigationStore.getSnackbar;
        },

        // to check if the MainWindow is the current Route
        mainWindowOpen() {
            return this.$route.name == 'MainWindow' ? true : false;
        },

        // Get the Activation Status of the Widget Feature
        WidgetFeatureActive() {
            return this.widgetsStore.getWidgetFeatureActive;
        },

        // Get the Env Variable for the Registry Server URL from the store
        EnvRegistryPath() {
            return this.envStore.getEnvRegistryPath;
        },

        // Get the Env Variable for the AAS Repo URL from the store
        EnvAASRepoPath() {
            return this.envStore.getEnvAASRepoPath;
        },

        // Get the Env Variable for the Submodel Repo URL from the store
        EnvSubmodelRepoPath() {
            return this.envStore.getEnvSubmodelRepoPath;
        },

        // Get the Env Variable for the Concept Description Repo URL from the store
        EnvConceptDescriptionRepoPath() {
            return this.envStore.getEnvConceptDescriptionRepoPath;
        },

        imagePath() {
            // get a list of files in the public directory with the filename "Logo" when the file extension is unknown not using any nodejs modules
            let files = import.meta.glob('@/assets/Logo/Logo.*');
            let filePaths = Object.keys(files); // get the paths to the files
            // get the first file in the List
            let filePath = filePaths[0];
            // get the file extension
            let fileExtension = filePath.split('.').pop();
            // return the path to the file with the correct file extension
            // console.log('src/assets/Logo/Logo.' + fileExtension);
            let path = 'src/assets/Logo/Logo.' + fileExtension as string;
            return path;
        }
    },

    methods: {
        mergeProps,

        // Function to connect to the Registry
        connectToRegistry() {
            // console.log('connect to registry: ' + this.registryURL);
            if(this.registryURL != '') {
                this.loading = true;
                let path = this.registryURL + '/shell-descriptors';
                let context = 'connecting to Registry Server'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    this.loading = false;
                    if (response.success) {
                        this.navigationStore.dispatchRegistryURL(this.registryURL); // save the URL in the NavigationStore
                        window.localStorage.setItem('registryURL', this.registryURL); // save the URL in the local storage
                        this.checkWidgetApi(); // check if the Widget API is available
                    } else {
                        this.navigationStore.dispatchRegistryURL(''); // clear the URL in the NavigationStore
                        window.localStorage.removeItem('registryURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // Function to connect to the respective Repository
        connectToEnvironment(RepoType: string) {
            // console.log('connect to ' + RepoType + ' Repository: ' + (this as any)[RepoType + 'RepoURL']);
            if ((this as any)[RepoType + 'RepoURL'] != '') {
                (this as any)['loading' + RepoType + 'Repo'] = true;
                let path = (this as any)[RepoType + 'RepoURL'];
                let context = 'connecting to ' + RepoType + ' Repository'
                let disableMessage = false;
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    (this as any)['loading' + RepoType + 'Repo'] = false;
                    if (response.success) {
                        this.navigationStore.dispatchRepoURL(RepoType, (this as any)[RepoType + 'RepoURL']); // save the URL in the NavigationStore
                        window.localStorage.setItem(RepoType + 'RepoURL', (this as any)[RepoType + 'RepoURL']); // save the URL in the local storage
                    } else {
                        this.navigationStore.dispatchRepoURL(RepoType, ''); // clear the URL in the NavigationStore
                        window.localStorage.removeItem(RepoType + 'RepoURL'); // remove the URL from the local storage
                    }
                });
            }
        },

        // Function to check if Widget API is available and set the Widget Feature Activation Status in the store
        checkWidgetApi() {
            let WidgetApiURL = this.registryURL.split(':') as any;
            WidgetApiURL[2] = '4000';
            // join the array to a string
            WidgetApiURL = WidgetApiURL.join(':');
            // check if the Widget API is available
            let path = WidgetApiURL + '/api/getallwidgets';
            let context = 'trying to connect to Widget API';
            let disableMessage = true;
            // Send Request to get all Widgets
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) {
                    this.navigationStore.dispatchWidgetApiURL(WidgetApiURL); // save the Widget API URL in the NavigationStore
                    this.widgetsStore.dispatchWidgetFeatureActive(true); // set the Widget Feature Activation Status to true
                } else {
                    this.navigationStore.dispatchWidgetApiURL(''); // clear the Widget API URL in the NavigationStore
                    this.widgetsStore.dispatchWidgetFeatureActive(false); // set the Widget Feature Activation Status to false
                }
            });
        },

        // Function to close the Snackbar
        closeSnackbar() {
            this.navigationStore.dispatchSnackbar({ status: false });
        },

        // Function to open the Dashboard
        openDashboard() {
            if(this.$route.name != 'Dashboard' && this.WidgetFeatureActive) this.$router.push({ name: 'Dashboard' });
        },

        // Function to open the clicked mobile menu entry
        openWindow(route: string) {
            // get the current query parameters
            let query = this.$route.query;
            // push the new route and include the query parameters
            if(this.$route.path != route) this.$router.push({ path: route, query: query });
        },
    },
});
</script>
