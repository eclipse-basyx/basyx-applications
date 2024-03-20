import { defineStore } from 'pinia';

export const useEnvStore = defineStore({
    id: 'envStore',
    state: () => ({
        logoPath: "",
        basePath: "",
        aasDiscoveryPath: "",
        aasRegistryPath: "",
        submodelRegistryPath: "",
        aasRepoPath: "",
        submodelRepoPath: "",
        conceptDescriptionRepoPath: "",
        primaryColor: "",
    }),
    getters: {
        getEnvLogoPath: (state) => state.logoPath,
        getEnvBasePath: (state) => state.basePath,
        getEnvAASDiscoveryPath: (state) => state.aasDiscoveryPath,
        getEnvAASRegistryPath: (state) => state.aasRegistryPath,
        getEnvSubmodelRegistryPath: (state) => state.submodelRegistryPath,
        getEnvAASRepoPath: (state) => state.aasRepoPath,
        getEnvSubmodelRepoPath: (state) => state.submodelRepoPath,
        getEnvConceptDescriptionRepoPath: (state) => state.conceptDescriptionRepoPath,
        getEnvPrimaryColor: (state) => state.primaryColor,
    },
    actions: {
        async fetchConfig() {
            try {
                const configResponse = await fetch('config.json');
                const config = await configResponse.json();
                this.logoPath = config.logoPath;
                this.basePath = config.basePath;
                this.aasDiscoveryPath = config.aasDiscoveryPath;
                this.aasRegistryPath = config.aasRegistryPath;
                this.submodelRegistryPath = config.submodelRegistryPath;
                this.aasRepoPath = config.aasRepoPath;
                this.submodelRepoPath = config.submodelRepoPath;
                this.conceptDescriptionRepoPath = config.cdRepoPath;
                this.primaryColor = config.primaryColor;
            } catch (error) {
                console.error('Error fetching config.json: ', error);
            }
        },
    },
});
