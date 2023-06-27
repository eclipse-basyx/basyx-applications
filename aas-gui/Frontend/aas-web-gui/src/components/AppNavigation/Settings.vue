<template>
    <v-menu :close-on-content-click="false" location="bottom">
        <template v-slot:activator="{ props }">
            <v-btn v-bind="props" icon="mdi-cog" class="ml-3"></v-btn>
        </template>
        <v-card min-width="300px">
            <v-list nav class="pt-0 pb-2 bg-card">
                <!-- Switch to change the app theme -->
                <ThemeSwitch></ThemeSwitch>
            </v-list>
        </v-card>
    </v-menu>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import ThemeSwitch from './Settings/ThemeSwitch.vue';

export default defineComponent({
    name: 'Settings',
    components: {
        ThemeSwitch,
    },
});
</script>