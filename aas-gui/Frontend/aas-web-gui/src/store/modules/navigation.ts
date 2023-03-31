interface state {
    drawerState: boolean,
    registryURL: string,
    aasServerURL: string,
    widgetApiURL: string,
    Snackbar: object,
    AutoSync: object,
    isMobile: boolean,
    platform: object,
    plugins: Array<object>,
};

const state: state = {
    drawerState: false,
    registryURL: '',
    aasServerURL: '',
    widgetApiURL: '',
    Snackbar: {},
    AutoSync: {},
    isMobile: false,
    platform: {},
    plugins: [],
};

const getters = {
    getDrawerState(state: state) {
        return state.drawerState;
    },
    getRegistryURL(state: state) {
        return state.registryURL;
    },
    getAASServerURL(state: state) {
        return state.aasServerURL;
    },
    getWidgetApiURL(state: state) {
        return state.widgetApiURL;
    },
    snackbar(state: state) {
        return state.Snackbar;
    },
    getAutoSync(state: state) {
        return state.AutoSync;
    },
    getIsMobile(state: state) {
        return state.isMobile;
    },
    getPlatform(state: state) {
        return state.platform;
    },
    getPlugins(state: state) {
        return state.plugins;
    },
};

const actions = {
    dispatchDrawerState({ commit }: { commit: Function }, drawerState: boolean) {
        // console.log('dispatchDrawerState: ', drawerState);
        commit('setDrawerState', drawerState);
    },
    dispatchRegistryURL({ commit }: { commit: Function }, url: string) {
        // console.log('dispatchRegistryURL: ', url);
        commit('setRegistryURL', url);
        commit('setSelectedNode', {});
    },
    dispatchAASServerURL({ commit }: { commit: Function }, url: string) {
        // console.log('dispatchAASServerURL: ', url);
        commit('setAASServerURL', url);
    },
    dispatchWidgetApiURL({ commit }: { commit: Function }, url: string) {
        // console.log('dispatchWidgetApiURL: ', url);
        commit('setWidgetApiURL', url);
    },
    getSnackbar({ commit }: { commit: Function }, snackbarObj: object) {
        // console.log('getSnackbar: ', snackbarObj);
        commit('setSnackbar', snackbarObj);
    },
    dispatchUpdateAutoSync({ commit }: { commit: Function }, autoSync: object) {
        // console.log('dispatchUpdateAutoSync: ', autoSync);
        commit('updateAutoSync', autoSync);
    },
    dispatchIsMobile({ commit }: { commit: Function }, isMobile: boolean) {
        // console.log('dispatchIsMobile: ', isMobile);
        commit('setIsMobile', isMobile);
    },
    dispatchPlatform({ commit }: { commit: Function }, platform: object) {
        // console.log('dispatchPlatform: ', platform);
        commit('setPlatform', platform);
    },
    dispatchPlugins({ commit }: { commit: Function }, plugins: Array<object>) {
        // console.log('dispatchPlugins: ', plugins);
        commit('setPlugins', plugins);
    },
};

const mutations = {
    setDrawerState(state: state, drawerState: boolean) {
        state.drawerState = drawerState;
    },
    setRegistryURL(state: state, url: string ) {
        state.registryURL = url;
    },
    setAASServerURL(state: state, url: string ) {
        state.aasServerURL = url;
    },
    setWidgetApiURL(state: state, url: string ) {
        state.widgetApiURL = url;
    },
    setSnackbar(state: state, snackbarObj: object) {
        state.Snackbar = snackbarObj;
    },
    updateAutoSync(state: state, autoSync: object) {
        state.AutoSync = autoSync;
    },
    setIsMobile(state: state, isMobile: boolean) {
        state.isMobile = isMobile;
    },
    setPlatform(state: state, platform: object) {
        state.platform = platform;
    },
    setPlugins(state: state, plugins: Array<object>) {
        state.plugins = plugins;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};
