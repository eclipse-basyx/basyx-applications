import { createRouter, createWebHistory } from 'vue-router';
import MainWindow                         from './components/MainWindow.vue';
import AASList                            from './components/AppNavigation/AASList.vue';
import AASTreeview                        from './components/AASTreeview.vue';
import SubmodelElementView                from './components/SubmodelElementView.vue';
import ComponentVisualization             from './components/ComponentVisualization.vue';

const routes = [
    { path: '/',                        name: 'MainWindow',             component: MainWindow },
    { path: '/aaslist',                 name: 'AASList',                component: AASList },
    { path: '/aastreeview',             name: 'AASTreeview',            component: AASTreeview },
    { path: '/submodelelementview',     name: 'SubmodelElementView',    component: SubmodelElementView },
    { path: '/componentvisualization',  name: 'ComponentVisualization', component: ComponentVisualization },
    { path: '/:pathMatch(.*)*',         name: 'NotFound',               component: MainWindow }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_BASE_PATH ? `${import.meta.env.VITE_BASE_PATH}/` : '/'),
  routes
});

export default router;