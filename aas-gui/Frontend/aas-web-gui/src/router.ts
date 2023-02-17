import { createRouter, createWebHistory } from 'vue-router';
import MainWindow from './components/MainWindow.vue';

const routes = [
    {
    path: '/',
    name: 'MainWindow',
    component: MainWindow
    },
    {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: MainWindow
    }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;