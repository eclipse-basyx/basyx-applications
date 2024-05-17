<template>
    <v-app-bar :elevation="0" class="bg-background" app :height="54" style="border-width: 0px 0px 1px 0px; position: fixed;">
        <v-container class="py-0 pl-7 pr-0" fluid style="max-width: 1300px; display: flex">
            <!-- BaSyx Logo -->
            <v-btn variant="plain" to="/" style="margin-top: 6px">
                <v-img src="@/assets/Logo_BaSyx_wide.svg" contain :width="120" alt="BaSyx Logo"></v-img>
            </v-btn>
            <v-spacer></v-spacer>
            <v-divider vertical style="height: 24px; margin-top: 11px" class="mr-2"></v-divider>
            <Theme class="mr-3 ml-2"></Theme>
            <v-btn icon="mdi-github" variant="plain" href="https://github.com/eclipse-basyx" target="_blank" aria-label="Open BaSyx GitHub page"></v-btn>
            <v-btn icon="mdi-docker" variant="plain" href="https://hub.docker.com/u/eclipsebasyx" target="_blank" aria-label="Open BaSyx DockerHub page"></v-btn>
            <v-btn icon="mdi-email" variant="plain" href="mailto:basyx-dev@eclipse.org" aria-label="Send email to the BaSyx developers"></v-btn>
        </v-container>
    </v-app-bar>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'AppBar',

    data() {
        return {
        };
    },

    mounted() {
    },

    methods: {
    },
});
</script>
