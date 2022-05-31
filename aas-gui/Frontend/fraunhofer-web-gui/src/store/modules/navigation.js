import Vue from 'vue';
import VueResource from 'vue-resource'

Vue.use(VueResource, {})

const state = {
    registryServer: null,
    Snackbar: {},
};

const getters = {
    getRegistryServer: state => {
        return state.registryServer;
    },
    snackbar: state => {
        return state.Snackbar
    },
};

const actions = {
    dispatchRegistryServer({ commit }, url) {
        // console.log(url);
        commit('setRegistryServer', url);
    },
    getSnackbar({ commit }, snackbarObj) {
        commit('setSnackbar', snackbarObj)
    },
};

const mutations = {
    setRegistryServer: (state, url) => {
        // console.log(url);
        state.registryServer = url;
    },
    setSnackbar: (state, snackbarObj) => {
        state.Snackbar = snackbarObj
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};