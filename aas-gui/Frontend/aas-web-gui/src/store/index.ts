import { createStore } from 'vuex';

import navigation from './modules/navigation';
import aasData from './modules/aasData';
import widgets from './modules/widgets';
import envVars from './modules/envVars';

const store = createStore({
    modules: {
        navigation,
        aasData,
        widgets,
        envVars,
    },
});

export default store;