<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Corporate Design</h1>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The AAS Web UI can be configured to include your corporate identity. Add your logo, icons and color scheme to ensure a consistent brand experience throughout the interface.
        </p>
        <v-alert color="alertCard">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">Prerequisite</div>
                </v-col>
            </v-row>
            <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                This configuration only applies if the AAS Web UI has been added.
            </p>
        </v-alert>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- Primary Color Picker -->
        <h2 class="text-header">Application Primary Color</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Choose a primary color for the AAS Web UI to match your corporate identity. Keep in mind that some colors may not be good options for dark and light application themes.
        </p>
        <v-color-picker mode="hex" class="mt-8 mb-5" v-model="primaryColor" @update:modelValue="applyColor"></v-color-picker>
        <v-divider class="mt-12 mb-8"></v-divider>
        <h2 class="text-header">Application Logo</h2>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Add you companies logo and icon to the AAS Web UI. The logo will be displayed in the top left corner of the interface. The icon is used as website icon and in the browser tab.
        </p>
        <!-- Logo File Upload -->
        <v-file-input class="mt-8" variant="solo-filled" prepend-inner-icon="$file" prepend-icon="" label="Application Icon (favicon.ico)" density="compact" v-model="iconFile" @update:modelValue="addIcon()" accept="image/x-icon"></v-file-input>
        <!-- Icon File Upload -->
        <v-file-input variant="solo-filled" prepend-inner-icon="$file" prepend-icon="" label="Company Logo" density="compact" v-model="logoFile" @update:modelValue="addLogo()" accept="image/*, "></v-file-input>
        <v-alert color="alertCard">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">Keep in mind:</div>
                </v-col>
            </v-row>
            <ul style="margin-left: 54px; margin-top: 8px">
                <li class="font-weight-medium text-subheader">While the logo can have any image format and file name, the icon must be called favicon and should use the .ico file format.</li>
                <li class="font-weight-medium text-subheader">For the logo, it's best to use a vector image format such as .svg to ensure the best image quality at different screen sizes.</li>
            </ul>
        </v-alert>
        <v-divider class="mt-12 mb-8"></v-divider>
        <!-- Apply the design -->
        <h2 class="text-header">Apply the Design</h2>
        <v-btn class="mt-8" block variant="tonal" @click="updateConfig()">Apply Corporate Design</v-btn>
        <v-btn color="error" append-icon="mdi-delete" block variant="tonal" class="mt-3 mb-8" @click="updateConfig(true)">Clear custom design</v-btn>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/visualization/ui">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/visualization/dashboard">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'CorporateDesign',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            primaryColor: '',
            iconFile: undefined as File[] | undefined,
            logoFile: undefined as File[] | undefined,
        };
    },

    mounted() {
        if (this.primaryColorStore && this.primaryColorStore !== '') {
            this.primaryColor = this.primaryColorStore;
        }
        if (this.logoStore) {
            this.logoFile = this.logoStore;
        }
        if (this.iconStore) {
            this.iconFile = this.iconStore;
        }
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // check if the User Interface is enabled
        isUIEnabled(): boolean {
            return this.appStore.getUserInterface;
        },

        // get primary color from the store
        primaryColorStore(): string {
            return this.appStore.getPrimaryColor;
        },

        // get the logo from the store
        logoStore(): File[] | undefined{
            return this.appStore.getLogo;
        },

        // get the icon from the store
        iconStore(): File[] | undefined{
            return this.appStore.getAppIcon;
        },

        // get the docker-compose.yml config object from the store
        dockerComposeConfigObject() {
            return this.appStore.getDockerComposeConfig;
        },
    },

    methods: {
        applyColor() {
            this.appStore.setPrimaryColor(this.primaryColor);
        },

        addPrimaryColorToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and return if not
                if (!Object.keys(dockerComposeConfig.services).includes('aas-web-ui')) {
                    return;
                }
                // add PRIMARY_COLOR to the AAS Web UI service environment variables
                if (dockerComposeConfig.services['aas-web-ui']) {
                    dockerComposeConfig.services['aas-web-ui'].environment.PRIMARY_COLOR = this.primaryColor;
                }
            }
        },

        removePrimaryColorFromDockerCompose() {
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value
            // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and return if not
            if (!Object.keys(dockerComposeConfig.services).includes('aas-web-ui')) {
                return;
            }
            // remove PRIMARY_COLOR from the AAS Web UI service environment variables
            if (dockerComposeConfig.services['aas-web-ui']) {
                delete dockerComposeConfig.services['aas-web-ui'].environment.PRIMARY_COLOR;
            }
        },

        addLogoConfigToDockerCompose() {
            // check if the docker-compose.yml file already exists
            if (this.dockerComposeConfigObject) {
                let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
                let dockerComposeConfig = dockerComposeConfigObject.value
                // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and return if not
                if (!Object.keys(dockerComposeConfig.services).includes('aas-web-ui')) {
                    return;
                }
                // add LOGO_PATH to the AAS Web UI service environment variables
                if (dockerComposeConfig.services['aas-web-ui'] && this.logoStore) {
                    dockerComposeConfig.services['aas-web-ui'].environment.LOGO_PATH = "Logo/" + this.logoStore[0].name;
                }
                // add volume to the AAS Web UI service volumes
                if (dockerComposeConfig.services['aas-web-ui'] && this.logoStore) {
                    dockerComposeConfig.services["aas-web-ui"].volumes = ["./logo:/usr/src/app/dist/Logo"];
                }
                dockerComposeConfigObject.value = dockerComposeConfig;
                this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
            }
        },

        removeLogoConfigFromDockerCompose() {
            let dockerComposeConfigObject = { ...this.dockerComposeConfigObject };
            let dockerComposeConfig = dockerComposeConfigObject.value
            // check if the dockerComposeConfigObject already includes the AAS Web UI configuration and return if not
            if (!Object.keys(dockerComposeConfig.services).includes('aas-web-ui')) {
                return;
            }
            // remove LOGO_PATH from the AAS Web UI service environment variables
            if (dockerComposeConfig.services['aas-web-ui']) {
                delete dockerComposeConfig.services['aas-web-ui'].environment.LOGO_PATH;
            }
            // remove volume from the AAS Web UI service volumes
            if (dockerComposeConfig.services['aas-web-ui']) {
                delete dockerComposeConfig.services['aas-web-ui'].volumes;
            }
            dockerComposeConfigObject.value = dockerComposeConfig;
            this.appStore.setDockerComposeConfig(dockerComposeConfigObject);
        },

        addIcon() {
            // add icon to the store
            this.appStore.setAppIcon(this.iconFile);
        },

        addLogo() {
            // add logo to the store
            this.appStore.setLogo(this.logoFile);
        },

        updateConfig(clear: boolean = false) {
            if (!this.isUIEnabled) {
                return;
            }
            let basyxConfig = [...this.basyxConfig];
            // update AAS Web UI -> add primary color
            if (clear) {
                // remove primary color from the Docker Compose configuration
                this.removePrimaryColorFromDockerCompose();
                // remove logo  volume from the Docker Compose configuration
                this.removeLogoConfigFromDockerCompose();
                // remove corporate design config
                basyxConfig.forEach((item: any) => {
                    if (item.id === '562336fb-852c-4041-b1ee-fc88886a4c51') {
                        item.children = item.children.filter((child: any) => child.id !== '00248694-1e0b-4f1b-8f41-809bbac8fac6');
                    }
                });
                this.primaryColor = '';
                this.iconFile = undefined;
                this.logoFile = undefined;
                this.appStore.setPrimaryColor('');
                this.appStore.setAppIcon(undefined);
                this.appStore.setLogo(undefined);
            } else {
                // add primary color to the Docker Compose configuration if it exists
                if (this.primaryColor && this.primaryColor !== '') {
                    this.addPrimaryColorToDockerCompose();
                }
                // add volume to the Docker Compose configuration if a logo has been uploaded
                if (this.logoFile && this.logoFile.length > 0) {
                    this.addLogoConfigToDockerCompose();
                }
                // add corporate design config
                basyxConfig.forEach((item: any) => {
                    if (item.id === '562336fb-852c-4041-b1ee-fc88886a4c51') {
                        // check if config already exists
                        if (!item.children.some((child: any) => child.id === '00248694-1e0b-4f1b-8f41-809bbac8fac6')) {
                            item.children.push({ id: '00248694-1e0b-4f1b-8f41-809bbac8fac6', title: 'Corporate Design', type: 'design' });
                        }
                    }
                });
            }
        },
    },
});
</script>