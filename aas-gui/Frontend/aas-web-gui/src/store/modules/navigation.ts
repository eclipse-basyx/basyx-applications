interface state {
    registryURL: string,
    Snackbar: object,
    AutoSync: object,
};

const state: state = {
    registryURL: '',
    Snackbar: {},
    AutoSync: {},
};

const getters = {
    getRegistryURL(state: state) {
        return state.registryURL;
    },
    snackbar(state: state) {
        return state.Snackbar;
    },
    getAutoSync(state: state) {
        return state.AutoSync;
    },
};

const actions = {
    dispatchRegistryURL({ commit }: { commit: Function }, url: string) {
        // console.log('dispatchRegistryURL: ', url);
        commit('setRegistryURL', url);
        commit('setSelectedNode', {});
    },
    getSnackbar({ commit }: { commit: Function }, snackbarObj: object) {
        // console.log('getSnackbar: ', snackbarObj);
        commit('setSnackbar', snackbarObj);
    },
    dispatchUpdateAutoSync({ commit }: { commit: Function }, autoSync: object) {
        // console.log('dispatchUpdateAutoSync: ', autoSync);
        commit('updateAutoSync', autoSync);
    },
};

const mutations = {
    setRegistryURL(state: state, url: string ) {
        state.registryURL = url;
    },
    setSnackbar(state: state, snackbarObj: object) {
        state.Snackbar = snackbarObj;
    },
    updateAutoSync(state: state, autoSync: object) {
        state.AutoSync = autoSync;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};
