<template>
    <div class="VTreeview">
        <!-- List Item with a Submodel / SubmodelelementCollection / submodelElement (like Property) -->
        <!-- TODO: Fix weird Ripple effect on isActive change to false -->
        <v-list-item @click="toggleNode()" :style="{ 'padding-left': depth * 22 + 'px' }" density="compact" class="py-0" nav active-color="primary" :active="item.isActive">
            <v-list-item-title>{{ item.idShort ? item.idShort : '' }}</v-list-item-title>
            <template v-slot:prepend>
                <!-- Button to show/hide children -->
                <v-btn v-if="item.children" size="small" variant="plain" @click.stop="toggleChildren()" :icon="showChildren ? 'mdi-menu-down' : 'mdi-menu-right'" :ripple="false"></v-btn>
                <div v-else style="width: 40px; height: 40px"></div>
                <!-- Empty Submodel Icon -->
                <v-icon v-if="item.modelType.name === 'Submodel' && !item.children" color="primary">mdi-folder-alert</v-icon>
                <!-- Icon for Submodel with children (open/closed) -->
                <v-icon v-else-if="item.modelType.name === 'Submodel' && item.children" color="primary">{{ showChildren ? 'mdi-folder-open' : 'mdi-folder' }}</v-icon>
                <!-- Icon for SubmodelelementCollection -->
                <v-icon v-else-if="item.modelType.name === 'SubmodelElementCollection' && !item.children" color="primary">mdi-file-alert</v-icon>
                <!-- Icon for SubmodelelementCollection -->
                <v-icon v-else-if="item.modelType.name === 'SubmodelElementCollection'" color="primary">mdi-file-multiple</v-icon>
                <!-- Icon for every other SubmodelElement (like Property) -->
                <v-icon v-else color="primary">mdi-file-code</v-icon>
            </template>
            <template v-slot:append="{isActive}">
                <v-chip v-if="item.modelType && item.modelType.name"  color="primary" size="x-small" :style="{ 'margin-right': isActive ? '-14px' : '' }">{{ item.modelType.name }}</v-chip>
                <!-- Button to Copy the Path to the Clipboard -->
                <v-tooltip v-if="isActive" text="Copy Path to Clipboard" :open-delay="600" location="bottom">
                    <template v-slot:activator="{ props }">
                        <v-icon color="subtitleText" v-bind="props" @click.stop.prevent="copyPathToClipboard(item.path)">{{ copyIcon }}</v-icon>
                    </template>
                </v-tooltip>
            </template>
        </v-list-item>
        <!-- Recursive Treeview -->
        <template v-if="showChildren">
            <vTreeview v-for="innerItem in item.children" :key="innerItem.id" :item="innerItem" :depth="depth + 1"></vTreeview>
        </template>
    </div>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import { useTheme } from 'vuetify';

export default defineComponent({
    name: 'VTreeview',
    props: ['item', 'depth'],

    setup() {
        const store = useStore()
        const theme = useTheme()

        return {
            store, // Store Object
            theme, // Theme Object
        };
    },

    data() {
        return {
            showChildren: false,
            copyIcon: 'mdi-clipboard-file-outline',
        }
    },

    mounted() {
        // Check if the current items showChildren Property is true if it exists
        if('showChildren' in this.item && this.item.showChildren) {
            this.showChildren = true;
        }
    },

    computed: {
        // Check if the current Platform is Mobile
        isMobile() {
            return this.platform.android || this.platform.ios ? true : false;
        },

        // get Platform from store
        platform() {
            return this.store.getters.getPlatform;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },
    },

    methods: {
        // Function to show/hide Children
        toggleChildren() {
            this.showChildren = !this.showChildren;
        },

        // Function to toggle a Node
        toggleNode() {
            // console.log('Selected Prop: ', this.item);
            // dublicate the selected Node Object
            let localItem = {...this.item};
            // invert the isActive Property
            localItem.isActive = !localItem.isActive;
            // Add path of the selected Node to the URL as Router Query
            if(localItem.isActive) {
                if (this.isMobile) {
                    // Change to SubmodelElementView on Mobile and add the path to the URL
                    this.$router.push({ path: '/submodelelementview', query: { aas: this.SelectedAAS.endpoints[0].address, path: localItem.path } });
                } else {
                    // just add the path to the URL
                    this.$router.push({ query: { aas: this.SelectedAAS.endpoints[0].address, path: localItem.path } });
                }
            } else {
                // remove the path query from the Route entirely
                let query = {...this.$route.query};
                delete query.path;
                this.$router.push({ query: query });
            }
            // dispatch the selected Node to the store
            this.store.dispatch('dispatchNode', localItem);
        },

        // Function to copy the path of the current Node to the Clipboard
        copyPathToClipboard(path: string) {
            // console.log('Copy Path to Clipboard: ', this.SelectedAAS.endpoints[0].address + '/' +  path);
            // set the icon to checkmark
            this.copyIcon = 'mdi-check';
            // copy the path to the clipboard
            navigator.clipboard.writeText(this.SelectedAAS.endpoints[0].address + '/' +  path);
            // set the clipboard tooltip to false after 1.5 seconds
            setTimeout(() => {
                this.copyIcon = 'mdi-clipboard-file-outline';
            }, 2000);
            // open Snackbar to inform the user that the path was copied to the clipboard
            this.store.dispatch('getSnackbar', { status: true, timeout: 2000, color: 'success', btnColor: 'buttonText', text: 'Path copied to Clipboard.' });
        },
    },
});
</script>

<style scoped>
/* move the Treeview Text a few Pixels to the left for a better look */
::v-deep(.v-list-item__prepend) {
    margin-right: -16px !important;
}
</style>
