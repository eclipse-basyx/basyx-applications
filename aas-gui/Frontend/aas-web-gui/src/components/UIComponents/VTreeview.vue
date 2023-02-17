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
            <template v-slot:append>
                <!-- TODO: Change Text- and Border-Color to something more subtile (don't forget theme!) -->
                <input type="text" v-if="item.identification" :value="item.identification.id" readonly style="border: solid; border-radius: 3px; border-width: 1px; padding: 2px 6px; font-size: 10px" size="30" :style="{ 'color': isDark ? '#A5A5A5' : '#626262','border-color': isDark ? '#2F2F2F' : '#E0E0E0' }">
                <v-chip v-if="item.modelType && item.modelType.name != 'Submodel' && item.modelType.name != 'SubmodelDescriptor'"  color="primary" size="x-small">{{ item.modelType.name }}</v-chip>
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
        }
    },

    mounted() {
        // Check if the current items showChildren Property is true if it exists
        if('showChildren' in this.item && this.item.showChildren) {
            this.showChildren = true;
        }
    },

    computed: {
        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
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
                this.$router.push({ name: 'MainWindow', query: { ...this.$route.query, path: localItem.path } });
            } else {
                // remove the path query from the Route entirely
                let query = {...this.$route.query};
                delete query.path;
                this.$router.push({ name: 'MainWindow', query: query });
            }
            // dispatch the selected Node to the store
            this.store.dispatch('dispatchNode', localItem);
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
