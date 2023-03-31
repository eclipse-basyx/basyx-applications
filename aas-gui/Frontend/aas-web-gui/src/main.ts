/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Components
import App from './App.vue'
import router from './router';
import store from './store';

import VueApexCharts from "vue3-apexcharts";

// Composables
import { createApp } from 'vue'
import { defineComponent } from 'vue';

// Plugins
import { registerPlugins } from '@/plugins'

const app = createApp(App)

async function loadUserPlugins() {

    app.use(router);
    app.use(store);

    app.use(VueApexCharts);

    registerPlugins(app)

    // Load all components in the components folder
    const pluginFiles = import.meta.glob('./UserPlugins/*.vue');
    const files = Object.keys(pluginFiles);

    let plugins = [] as Array<object>;

    await Promise.all(files.map(async (path) => {
        const componentName = path.replace('./UserPlugins/', '').replace('.vue', '');
        const component: any = await pluginFiles[path]();
        app.component(componentName, (component.default || component) as ReturnType<typeof defineComponent>);
        plugins.push({ name: componentName as string, SemanticID: component.default.SemanticID as string });
    }));

    store.dispatch('dispatchPlugins', plugins);

    app.mount('#app')

    }

loadUserPlugins()