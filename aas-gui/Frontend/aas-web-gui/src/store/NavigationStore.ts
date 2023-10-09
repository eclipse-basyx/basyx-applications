export interface SnackbarType {
    status: boolean;
    timeout?: number;
    color?: string;
    btnColor?: string;
    text?: string;
    baseError?: string;
    extendedError?: string;
}

export interface AutoSyncType {
    state: boolean;
    interval: number;
}

export interface PlatformType {
    android: boolean;
    chrome: boolean;
    cordova: boolean;
    edge: boolean;
    electron: boolean;
    firefox: boolean;
    ios: boolean;
    linux: boolean;
    mac: boolean;
    opera: boolean;
    ssr: boolean;
    touch: boolean;
    win: boolean;
}

export interface PluginType {
    name: string;
    SemanticID: string;
}

import { defineStore } from 'pinia';
import { useAASStore } from './AASDataStore';

export const useNavigationStore = defineStore({
    id: 'navigation',

    state: () => ({
        drawerState: false as boolean,
        registryURL: '' as string,
        AASRepoURL: '' as string,
        SubmodelRepoURL: '' as string,
        ConceptDescriptionRepoURL: '' as string,
        widgetApiURL: '' as string,
        Snackbar: {} as SnackbarType,
        AutoSync: {} as AutoSyncType,
        StatusCheck: false as boolean,
        isMobile: false as boolean,
        platform: {} as PlatformType,
        plugins: [] as PluginType[],
        triggerAASListReload: false as boolean,
    }),

    getters: {
        getDrawerState: (state) => state.drawerState,
        getRegistryURL: (state) => state.registryURL,
        getAASRepoURL: (state) => state.AASRepoURL,
        getSubmodelRepoURL: (state) => state.SubmodelRepoURL,
        getConceptDescriptionRepoURL: (state) => state.ConceptDescriptionRepoURL,
        getWidgetApiURL: (state) => state.widgetApiURL,
        getSnackbar: (state) => state.Snackbar,
        getAutoSync: (state) => state.AutoSync,
        getStatusCheck: (state) => state.StatusCheck,
        getIsMobile: (state) => state.isMobile,
        getPlatform: (state) => state.platform,
        getPlugins: (state) => state.plugins,
        getTriggerAASListReload: (state) => state.triggerAASListReload,
    },

    actions: {
        dispatchDrawerState(drawerState: boolean) {
            this.drawerState = drawerState;
        },
        dispatchRegistryURL(url: string) {
            this.registryURL = url;
            useAASStore().dispatchSelectedNode({});
        },
        dispatchRepoURL(type: string, url: string) {
            switch (type) {
                case 'AAS':
                    this.AASRepoURL = url;
                    break;
                case 'Submodel':
                    this.SubmodelRepoURL = url;
                    break;
                case 'ConceptDescription':
                    this.ConceptDescriptionRepoURL = url;
                    break;
            }
        },
        dispatchWidgetApiURL(url: string) {
            this.widgetApiURL = url;
        },
        dispatchSnackbar(snackbarObj: SnackbarType) {
            this.Snackbar = snackbarObj;
        },
        dispatchUpdateAutoSync(autoSync: AutoSyncType) {
            this.AutoSync = autoSync;
        },
        dispatchUpdateStatusCheck(statusCheck: boolean) {
            this.StatusCheck = statusCheck;
        },
        dispatchIsMobile(isMobile: boolean) {
            this.isMobile = isMobile;
        },
        dispatchPlatform(platform: PlatformType) {
            this.platform = platform;
        },
        dispatchPlugins(plugins: Array<PluginType>) {
            this.plugins = plugins;
        },
        dispatchTriggerAASListReload(triggerAASListReload: boolean) {
            this.triggerAASListReload = triggerAASListReload;
        },
    },
});
