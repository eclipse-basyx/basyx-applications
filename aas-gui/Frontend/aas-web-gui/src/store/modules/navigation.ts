interface state {
    drawerState: boolean,
    registryURL: string,
    aasServerURL: string,
    widgetApiURL: string,
    Snackbar: object,
    AutoSync: object,
    StatusCheck: boolean,
    isMobile: boolean,
    platform: object,
    plugins: Array<object>,
    triggerAASListReload: boolean,
};

const state: state = {
    drawerState: false,
    registryURL: '',
    aasServerURL: '',
    widgetApiURL: '',
    Snackbar: {},
    AutoSync: {},
    StatusCheck: false,
    isMobile: false,
    platform: {},
    plugins: [],
    triggerAASListReload: false,
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
    getStatusCheck(state: state) {
        return state.StatusCheck;
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
    getTriggerAASListReload(state: state) {
        return state.triggerAASListReload;
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
    dispatchUpdateStatusCheck({ commit }: { commit: Function }, statusCheck: boolean) {
        // console.log('dispatchUpdateStatusCheck: ', statusCheck);
        commit('updateStatusCheck', statusCheck);
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
    dispatchTriggerAASListReload({ commit }: { commit: Function }, triggerAASListReload: boolean) {
        // console.log('dispatchTriggerAASListReload: ', triggerAASListReload);
        commit('setTriggerAASListReload', triggerAASListReload);
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
    updateStatusCheck(state: state, statusCheck: boolean) {
        state.StatusCheck = statusCheck;
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
    setTriggerAASListReload(state: state, triggerAASListReload: boolean) {
        state.triggerAASListReload = triggerAASListReload;
    },
};

export default {
    state,
    getters,
    actions,
    mutations,
};
