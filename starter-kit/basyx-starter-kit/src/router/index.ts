/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

// Composables
import { createRouter, createWebHashHistory } from 'vue-router/auto'
import { setupLayouts } from 'virtual:generated-layouts'
import { RouteLocationNormalized, NavigationGuardNext } from 'vue-router';

const router = createRouter({
  history: createWebHashHistory(),
  extendRoutes: setupLayouts,
})

let isFirstLoad = true;

router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  if (isFirstLoad && to.path !== '/') {
    isFirstLoad = false;
    next('/');
  } else {
    isFirstLoad = false;
    next();
  }
});

export default router