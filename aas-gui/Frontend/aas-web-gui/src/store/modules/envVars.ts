interface state {
    registryPath: string,
    aasServerPath: string,
    primaryColor: string,
};

const state: state = {
    registryPath: import.meta.env.VITE_REGISTRY_PATH,
    aasServerPath: import.meta.env.VITE_AAS_SERVER_PATH,
    primaryColor: import.meta.env.VITE_PRIMARY_COLOR,
};

const getters = {
    getEnvRegistryPath(state: state) {
        return state.registryPath;
    },
    getEnvAASServerPath(state: state) {
        return state.aasServerPath;
    },
    getEnvPrimaryColor(state: state) {
        return state.primaryColor;
    },
};

const actions = {
};

const mutations = {
};

export default {
    state,
    getters,
    actions,
    mutations,
};