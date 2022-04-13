import Vue                from 'vue';
import App                from './App.vue';
import vuetify            from './plugins/vuetify';
import VueResource        from 'vue-resource';
import store              from './store';

Vue.config.productionTip = false;
Vue.use(VueResource, {});

document.title = 'VWS GUI Fraunhofer';



new Vue({
  vuetify,
  store,
  render: h => h(App)
}).$mount('#app')
