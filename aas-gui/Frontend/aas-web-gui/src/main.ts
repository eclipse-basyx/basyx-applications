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

// Plugins
import { registerPlugins } from '@/plugins'

const app = createApp(App)

app.use(router);
app.use(store);

app.use(VueApexCharts);

registerPlugins(app)

app.mount('#app')
