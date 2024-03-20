import { createRouter, createWebHistory } from 'vue-router';
import MainWindow from './components/MainWindow.vue';
import AASList from './components/AppNavigation/AASList.vue';
import SubmodelList from './components/SubmodelList.vue';
import ComponentVisualization from './components/ComponentVisualization.vue';
import AASViewer from './components/AASViewer.vue';
import Impressum from './components/Impressum.vue';

const routes = [
  { path: '/', name: 'MainWindow', component: MainWindow },
  { path: '/aaslist', name: 'AASList', component: AASList },
  { path: '/submodellist', name: 'SubmodelList', component: SubmodelList },
  { path: '/componentvisualization', name: 'ComponentVisualization', component: ComponentVisualization },
  { path: '/aasviewer', name: 'AASViewer', component: AASViewer },
  { path: '/impressum', name: 'Impressum', component: Impressum },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: MainWindow }
];

export async function createAppRouter() {

  const configResponse = await fetch('config.json');
  const config = await configResponse.json();
  const base = config.basePath + '/';

  const router = createRouter({
    history: createWebHistory(base || '/'),
    routes
  });

  return router;
}
