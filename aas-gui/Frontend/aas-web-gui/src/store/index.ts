import { createStore } from 'vuex';

import navigation from './modules/navigation';
import aasData from './modules/aasData';

const store = createStore({
    modules: {
        navigation,
        aasData,
    },
});

export default store;