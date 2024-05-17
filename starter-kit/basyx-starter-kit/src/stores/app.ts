// Utilities
import { defineStore } from 'pinia'
import * as yaml from 'js-yaml';

export const useAppStore = defineStore('app', {
  state: () => ({
    aasDiscovery: false,
    mongoDB: false,
    mqtt: false,
    mqttBroker: false,
    timeSeriesData: false,
    userInterface: false,
    dashboard: false,
    customColor: '',
    appIcon: undefined as File[] | undefined,
    logo: undefined as File[] | undefined,
    telegrafConf: undefined as File[] | undefined,
    aasFiles: undefined as File[] | undefined,
    basyxConfig: [
      {
        id: '5289baf4-21a8-4c22-8361-8a4b8bfb5716',
        title: 'AAS Environment',
        children: [
          { id: '88fed67d-3494-4e49-930e-bb13eb759f72', title: 'Config: default', type: 'config' },
        ],
      },
      {
        id: '59cc5124-3a8d-46ae-a622-fe044606d096',
        title: 'AAS Registry',
        children: [
          { id: '6993af16-6100-4b6c-9163-3719b4278e0b', title: 'Config: default', type: 'config' },
        ],
      },
      {
        id: 'dd16fbe2-bb12-4a0a-9d60-630e02607c66',
        title: 'Submodel Registry',
        children: [
          { id: '0de572cb-9b0b-4a41-bbc5-efa112b3a6c3', title: 'Config: default', type: 'config' },
        ],
      },
    ],
    selectedOutput: undefined as any | undefined,
    selectedOutputConfig: undefined as any | undefined,
    // config files
    dockerComposeConfig: undefined as any | undefined,
    aasEnvConfig: undefined as any | undefined,
    aasRegistryConfig: undefined as any | undefined,
    submodelRegistryConfig: undefined as any | undefined,
    aasDiscoveryConfig: undefined as any | undefined,
    mosquittoConfig: undefined as any | undefined,
    dashboardConfig: undefined as any | undefined,
  }),
  getters: {
    getAasDiscovery: (state) => state.aasDiscovery,
    getMongoDB: (state) => state.mongoDB,
    getMQTT: (state) => state.mqtt,
    getMQTTBroker: (state) => state.mqttBroker,
    getTimeSeriesData: (state) => state.timeSeriesData,
    getUserInterface: (state) => state.userInterface,
    getDashboard: (state) => state.dashboard,
    getPrimaryColor: (state) => state.customColor,
    getAppIcon: (state) => state.appIcon,
    getLogo: (state) => state.logo,
    getTelegrafConf: (state) => state.telegrafConf,
    getAasFiles: (state) => state.aasFiles,
    getBasyxConfig: (state) => state.basyxConfig,
    getSelectedOutput: (state) => state.selectedOutput,
    getSelectedOutputConfig: (state) => state.selectedOutputConfig,
    // get the config files as string
    getDockerComposeConfigAsString: (state) => {
      let dockerComposeConfigObject = { ...state.dockerComposeConfig };
      let yamlDockerComposeString = yaml.dump(dockerComposeConfigObject.value);
      dockerComposeConfigObject.value = yamlDockerComposeString;
      return dockerComposeConfigObject;
    },
    getDockerComposeConfigService: (state) => (serviceName: string) => {
      let dockerComposeConfigObject = { ...state.dockerComposeConfig };
      let services = dockerComposeConfigObject.value.services;
      let service = services[serviceName];
      let servicesString = yaml.dump({ services: { [serviceName]: service } });
      dockerComposeConfigObject.value = servicesString;
      return dockerComposeConfigObject;
    },
    getAasEnvConfigAsString: (state) => {
      let aasEnvConfigObject = { ...state.aasEnvConfig };
      let aasEnvString = '';
      for (const key in aasEnvConfigObject.value) {
        aasEnvString += `${key}=${aasEnvConfigObject.value[key]}\n`;
      }
      aasEnvConfigObject.value = aasEnvString;
      return aasEnvConfigObject;
    },
    getAasRegistryConfigAsString: (state) => {
      let aasRegistryConfigObject = { ...state.aasRegistryConfig };
      let yamlAasRegistryString = yaml.dump(aasRegistryConfigObject.value);
      aasRegistryConfigObject.value = yamlAasRegistryString;
      return aasRegistryConfigObject;
    },
    getSubmodelRegistryConfigAsString: (state) => {
      let submodelRegistryConfigObject = { ...state.submodelRegistryConfig };
      let yamlSubmodelRegistryString = yaml.dump(submodelRegistryConfigObject.value);
      submodelRegistryConfigObject.value = yamlSubmodelRegistryString;
      return submodelRegistryConfigObject;
    },
    getAasDiscoveryConfigAsString: (state) => {
      let aasDiscoveryConfigObject = { ...state.aasDiscoveryConfig };
      let aasDiscoveryString = '';
      for (const key in aasDiscoveryConfigObject.value) {
        aasDiscoveryString += `${key}=${aasDiscoveryConfigObject.value[key]}\n`;
      }
      aasDiscoveryConfigObject.value = aasDiscoveryString;
      return aasDiscoveryConfigObject;
    },
    getDashboardConfigAsString: (state) => {
      let dashboardConfigObject = { ...state.dashboardConfig };
      let yamlDashboardString = yaml.dump(dashboardConfigObject.value);
      dashboardConfigObject.value = yamlDashboardString;
      return dashboardConfigObject;
    },
    // get the config files as object
    getDockerComposeConfig: (state) => state.dockerComposeConfig,
    getAasEnvConfig: (state) => state.aasEnvConfig,
    getAasRegistryConfig: (state) => state.aasRegistryConfig,
    getSubmodelRegistryConfig: (state) => state.submodelRegistryConfig,
    getAasDiscoveryConfig: (state) => state.aasDiscoveryConfig,
    getMosquittoConfig: (state) => state.mosquittoConfig,
    getDashboardConfig: (state) => state.dashboardConfig,
  },
  actions: {
    updateAasDiscovery(value: any) {
      this.aasDiscovery = value;
    },
    updateMongoDB(value: any) {
      this.mongoDB = value;
    },
    updateMQTT(value: any) {
      this.mqtt = value;
    },
    updateMQTTBroker(value: any) {
      this.mqttBroker = value;
    },
    updateTimeSeriesData(value: any) {
      this.timeSeriesData = value;
    },
    updateUserInterface(value: any) {
      this.userInterface = value;
    },
    updateDashboard(value: any) {
      this.dashboard = value;
    },
    setPrimaryColor(color: string) {
      this.customColor = color;
    },
    setAppIcon(icon: File[] | undefined) {
      if (icon && icon.length > 0) {
        const oldFile = icon[0];
        const newFile = new File([oldFile], "favicon.ico", { type: oldFile.type });
        this.appIcon = [newFile];
      } else {
        this.appIcon = icon;
      }
    },
    setLogo(logo: File[] | undefined) {
      this.logo = logo;
    },
    setTelegrafConf(conf: File[] | undefined) {
      this.telegrafConf = conf;
    },
    setAasFiles(files: File[] | undefined) {
      this.aasFiles = files;
    },
    updateBasyxConfig(config: any) {
      this.basyxConfig = config;
    },
    // sets the output item and the respective config
    selectOutput(output: any) {
      this.selectedOutput = output;
      switch (output.id) {
        case '5289baf4-21a8-4c22-8361-8a4b8bfb5716':
          this.selectedOutputConfig = this.getAasEnvConfigAsString;
          break;
        case '59cc5124-3a8d-46ae-a622-fe044606d096':
          this.selectedOutputConfig = this.getAasRegistryConfigAsString;
          break;
        case 'dd16fbe2-bb12-4a0a-9d60-630e02607c66':
          this.selectedOutputConfig = this.getSubmodelRegistryConfigAsString;
          break;
        case 'da6a3af1-f998-487a-8092-2847b0ec74e0':
          this.selectedOutputConfig = this.getAasDiscoveryConfigAsString;
          break;
        case 'ebee24da-6e60-45c6-8bc1-e5dfa7c052bc':
          this.selectedOutputConfig = this.getDashboardConfigAsString;
          break;
        case '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc':
          this.selectedOutputConfig = this.getMosquittoConfig;
          break;
        default:
          this.selectedOutputConfig = undefined;
      }
    },
    // add the config files
    setDockerComposeConfig(config: any) {
      this.dockerComposeConfig = config;
    },
    setAasEnvConfig(config: any) {
      this.aasEnvConfig = config;
    },
    setAasRegistryConfig(config: any) {
      this.aasRegistryConfig = config;
    },
    setSubmodelRegistryConfig(config: any) {
      this.submodelRegistryConfig = config;
    },
    setAasDiscoveryConfig(config: any) {
      this.aasDiscoveryConfig = config;
    },
    setMosquittoConfig(config: any) {
      this.mosquittoConfig = config;
    },
    setDashboardConfig(config: any) {
      this.dashboardConfig = config;
    },
  },
})
