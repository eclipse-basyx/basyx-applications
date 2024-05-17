<template>
    <v-container class="pt-12">
        <div>
            <v-list nav slim density="compact" :lines="false" class="bg-background pb-0">
                <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase">Components</v-list-subheader>
            </v-list>
            <v-treeview :items="items" item-value="id" activatable open-on-click open-all :opened="openItems" density="compact" v-model:activated="active" class="bg-background" rounded expand-icon="mdi-chevron-down" collapse-icon="mdi-chevron-up" :lines="false" slim variant="plain">
                <!-- Icon for each item type -->
                <template v-slot:prepend="{ item }">
                    <v-icon v-if="!item.type">mdi-cube-outline</v-icon>
                    <v-icon v-else-if="item.type === 'config'" size="small">mdi-file-cog-outline</v-icon>
                    <v-icon v-else-if="item.type === 'db'" size="small">mdi-database</v-icon>
                    <v-icon v-else-if="item.type === 'communication'" size="small">mdi-comment-arrow-right-outline</v-icon>
                    <v-icon v-else-if="item.type === 'design'" size="small">mdi-palette</v-icon>
                    <v-icon v-else size="small">mdi-file-document</v-icon>
                </template>
                <!-- Title for each item -->
                <template v-slot:title="{ item }">
                    <v-list-item-title v-if="!item.type" v-text="item.title" class="text-header"></v-list-item-title>
                    <v-list-item-title v-else v-text="item.title" class="text-subheader"></v-list-item-title>
                </template>
            </v-treeview>
        </div>
        <!-- Preview of selected output file -->
        <V-dialog width="1000px" v-model="outputDialog">
            <v-card>
                <v-card-title>
                    <v-row align="center">
                        <v-col>
                            <span>{{ topLevelParent.title }}</span>
                        </v-col>
                        <v-spacer></v-spacer>
                        <v-col cols="auto">
                            <v-btn icon="mdi-close" variant="plain" @click="outputDialog = false"></v-btn>
                        </v-col>
                    </v-row>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text style="height: 600px; overflow-y: auto" class="px-4">
                    <v-textarea v-if="selectedItemConfig && selectedItemConfig.value" style="font-family: monospace;" spellcheck="false" variant="outlined" :label="selectedItemConfig.name" :model-value="selectedItemConfig.value" readonly bg-color="background" auto-grow></v-textarea>
                    <v-textarea v-if="selectedServiceConfig && selectedServiceConfig.value" style="font-family: monospace;" spellcheck="false" variant="outlined" :label="selectedServiceConfig.name + ' (excerpt)'" :model-value="selectedServiceConfig.value" readonly bg-color="background" auto-grow></v-textarea>
                </v-card-text>
            </v-card>
        </V-dialog>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app'

export default defineComponent({
    name: 'OutputView',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            items: [] as Array<any>,
            openItems: [],
            outputDialog: false,
            active: undefined as any,
            selectedItem: undefined as any,
            topLevelParent: undefined as any,
        };
    },

    watch: {
        basyxConfig: {
            immediate: true,
            handler() {
                this.items = this.basyxConfig;
                this.openItems = this.basyxConfig.map((item: any) => item.id);
            },
        },

        active: {
            immediate: true,
            handler() {
                // console.log('active: ', this.active);
                if (this.active && this.active.length > 0) {
                    this.openOutputDialog(this.active[0]);
                }
            },
        },
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },

        // get the config of the selected item from the store
        selectedItemConfig(): any {
            return this.appStore.getSelectedOutputConfig;
        },

        // get the docker compose service configuration for the selected component from the store
        selectedServiceConfig(): any {
            const output = this.topLevelParent;
            let serviceConfig = undefined as String | undefined;
            switch (output.id) {
                case '5289baf4-21a8-4c22-8361-8a4b8bfb5716':
                    serviceConfig = this.appStore.getDockerComposeConfigService('aas-env');
                    break;
                case '59cc5124-3a8d-46ae-a622-fe044606d096':
                    serviceConfig = this.appStore.getDockerComposeConfigService('aas-registry');
                    break;
                case 'dd16fbe2-bb12-4a0a-9d60-630e02607c66':
                    serviceConfig = this.appStore.getDockerComposeConfigService('sm-registry');
                    break;
                case 'da6a3af1-f998-487a-8092-2847b0ec74e0':
                    serviceConfig = this.appStore.getDockerComposeConfigService('aas-discovery');
                    break;
                case 'e007e23b-1588-4b64-b17f-69f475684842':
                    serviceConfig = this.appStore.getDockerComposeConfigService('mongo');
                    break;
                case '9e8d5083-9dae-4be1-bafc-bcdb03e9dbdc':
                    serviceConfig = this.appStore.getDockerComposeConfigService('mosquitto');
                    break;
                case '562336fb-852c-4041-b1ee-fc88886a4c51':
                    serviceConfig = this.appStore.getDockerComposeConfigService('aas-web-ui');
                    break;
                case 'ebee24da-6e60-45c6-8bc1-e5dfa7c052bc':
                    serviceConfig = this.appStore.getDockerComposeConfigService('dashboard-api');
                    break;
                case 'cbd3567b-8ccc-4798-a501-1f97535912f7':
                    serviceConfig = this.appStore.getDockerComposeConfigService('influxdb');
                    break;
                case '0d9187db-6b75-4431-a589-d12238233f96':
                    serviceConfig = this.appStore.getDockerComposeConfigService('telegraf');
                    break;
                default:
                    serviceConfig = undefined;
            }
            return serviceConfig;
        },
    },

    methods: {
        openOutputDialog(id: any) {
            // console.log('openOutputDialog: ', id);
            // find the selected item by it's id in the nested items (recursively)
            const result = this.findItem(this.items, id);
            this.selectedItem = result.item;
            this.topLevelParent = result.parent;
            // console.log('selectedItem: ', this.selectedItem);
            // console.log('topLevelParent: ', this.topLevelParent);
            this.appStore.selectOutput(this.topLevelParent);
            this.outputDialog = true;
        },

        findItem(items: any[], id: any, parent: any = null): any {
            for (let item of items) {
                if (item.id === id) {
                    return { item, parent };
                } else if (item.children && item.children.length) {
                    let found = this.findItem(item.children, id, parent || item);
                    if (found) return found;
                }
            }
            return null;
        },
    },
});
</script>
