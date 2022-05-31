import Vue          from 'vue';
import VueResource  from 'vue-resource'

Vue.use(VueResource, {})

const state = {
    aasObject: null,
    loadingState: false,
    selectedProp: null,
    realtimeProp: null,
};

const getters = {
    selectedAAS: state => {
        return state.aasObject;
    },
    getLoadingState: state => {
        return state.loadingState;
    },
    getSelectedProp: state => {
        return state.selectedProp;
    },
    getRealtimeProp: state => {
        return state.realtimeProp;
    },
};

const actions = {
    dispatchAAS({ commit }, aasData) {
        // console.log(aasData);
        commit('setAAS', aasData);
    },
    dispatchLoading({ commit }, loadingState) {
        commit('setLoadingState', loadingState);
    },
    dispatchSelectedProp({ commit }, selectedProp) {
        // console.log(selectedProp);
        commit('setSelectedProp', selectedProp);
    },
    dispatchRealtimeProp({ commit }, realtimeProp) {
        // console.log(realtimeProp);
        commit('setRealtimeProp', realtimeProp);
    },
};

const mutations = {
    setAAS: (state, aasData) => {
        // console.log(aasData);
        state.aasObject = aasData;
    },
    setLoadingState: (state, loadingState) => {
        state.loadingState = loadingState;
    },
    setSelectedProp: (state, selectedProp) => {
        // console.log(selectedProp);
        state.selectedProp = selectedProp;
    },
    setRealtimeProp: (state, realtimeProp) => {
        // console.log(realtimeProp);
        state.realtimeProp = realtimeProp;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};