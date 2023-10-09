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
import router from './router';
import { useNavigationStore } from './store/NavigationStore';

import VueApexCharts from "vue3-apexcharts";

// Composables
import { createApp } from 'vue'
import { defineComponent } from 'vue';
import { createPinia } from 'pinia'

// Plugins
import { registerPlugins } from '@/plugins'

const app = createApp(App)

const pinia = createPinia()

async function loadUserPlugins() {

    app.use(router);
    app.use(pinia);

    app.use(VueApexCharts);

    registerPlugins(app)

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

    app.mount('#app')

    }

loadUserPlugins()