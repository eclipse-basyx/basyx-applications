import { createRouter, createWebHistory } from 'vue-router';
import MainWindow                         from './components/MainWindow.vue';
import AASList                            from './components/AppNavigation/AASList.vue';
import AASTreeview                        from './components/AASTreeview.vue';
import SubmodelElementView                from './components/SubmodelElementView.vue';
import ComponentVisualization             from './components/ComponentVisualization.vue';
import Dashboard                          from './components/UIComponents/Dashboard.vue';

const routes = [
    { path: '/',                        name: 'MainWindow',             component: MainWindow },
    { path: '/dashboard',               name: 'Dashboard',              component: Dashboard },
    { path: '/aaslist',                 name: 'AASList',                component: AASList },
    { path: '/aastreeview',             name: 'AASTreeview',            component: AASTreeview },
    { path: '/submodelelementview',     name: 'SubmodelElementView',    component: SubmodelElementView },
    { path: '/componentvisualization',  name: 'ComponentVisualization', component: ComponentVisualization },
    { path: '/:pathMatch(.*)*',         name: 'NotFound',               component: MainWindow }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;