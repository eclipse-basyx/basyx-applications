import Vue  from 'vue';
import Vuex from 'vuex';

import aasData from './modules/aasData';
import navigation from './modules/navigation';

Vue.use(Vuex);

export default new Vuex.Store({
    modules: {
        aasData,
        navigation,
    },
});