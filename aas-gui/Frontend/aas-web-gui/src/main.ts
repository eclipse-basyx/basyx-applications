/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Types
interface PluginType {
    name: string;
    SemanticID: string;
}

// Components
import App from './App.vue'
import { createAppRouter } from './router';
import { useNavigationStore } from './store/NavigationStore';
import { useEnvStore } from './store/EnvironmentStore';
import { useAuthStore } from './store/AuthStore';

import VueApexCharts from "vue3-apexcharts";

// Composables
import { createApp } from 'vue'
import { defineComponent } from 'vue';
import { createPinia } from 'pinia'

// Plugins
import { registerPlugins } from '@/plugins'
import Keycloak from 'keycloak-js';
import { KeycloakOnLoad } from 'keycloak-js';

const app = createApp(App)

const pinia = createPinia()

async function loadUserPlugins() {
    const router = await createAppRouter();

    app.use(router);
    app.use(pinia);
    app.use(VueApexCharts);
    
    const envStore = useEnvStore();  // Get the store instance
    await envStore.fetchConfig();  // make sure to await fetchConfig

    // create keycloak instance
    if (envStore.getKeycloakUrl !== "" && envStore.getKeycloakRealm !== "" && envStore.getKeycloakClientId !== "") {
        initKeycloak(envStore.getKeycloakUrl, envStore.getKeycloakRealm, envStore.getKeycloakClientId);
    }

    await registerPlugins(app)

    // Load all components in the components folder
    const pluginFiles = import.meta.glob('./UserPlugins/*.vue');
    const files = Object.keys(pluginFiles);

    let plugins = [] as Array<PluginType>;

    await Promise.all(files.map(async (path) => {
        const componentName = path.replace('./UserPlugins/', '').replace('.vue', '');
        const component: any = await pluginFiles[path]();
        app.component(componentName, (component.default || component) as ReturnType<typeof defineComponent>);
        plugins.push({ name: componentName, SemanticID: component.default.SemanticID });
    }));

    const navigationStore = useNavigationStore();  // Get the store instance
    navigationStore.dispatchPlugins(plugins);  // Update the plugins state
    app.mount('#app');
}

function initKeycloak(keycloakUrl: string, keycloakRealm: string, keycloakClientId: string) {
    let keycloak: Keycloak | null = null;

    let initOptions = {
        url: keycloakUrl,
        realm: keycloakRealm,
        clientId: keycloakClientId,
        onLoad: 'login-required' as KeycloakOnLoad
    };

    try {
        keycloak = new Keycloak(initOptions);
    } catch (error) {
        console.error("Failed to initialize Keycloak, running without authentication.", error);
    }

    keycloak?.init({ onLoad: initOptions.onLoad }).then(async (auth) => {
        if (!auth) {
            window.location.reload();
        } else {
            console.info("Authenticated");
            const authStore = useAuthStore();
            authStore.setToken(keycloak.token);
            authStore.setRefreshToken(keycloak.refreshToken);
            setInterval(() => {
                keycloak.updateToken(70).then((refreshed) => {
                    if (refreshed) {
                        console.log('Token refreshed');
                        authStore.setToken(keycloak.token);
                        authStore.setRefreshToken(keycloak.refreshToken);
                    } else {
                        const exp = keycloak?.tokenParsed?.exp as number;
                        const timeScew = keycloak?.timeSkew as number;
                        console.log('Token not refreshed, valid for ' + Math.round(exp + timeScew - new Date().getTime() / 1000) + ' seconds');
                    }
                }).catch(() => {
                    console.log('Failed to refresh token');
                });
            }, 60000);
        }
    }).catch((error) => {
        console.error("Failed to authenticate with Keycloak, running without authentication.", error);
    });
}

loadUserPlugins();
