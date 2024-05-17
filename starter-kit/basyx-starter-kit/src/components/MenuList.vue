<template>
    <v-container class="pt-8">
        <v-list nav slim width="200px" rounded color="primary" :lines="false" density="compact" class="bg-background">
            <!-- Getting Started -->
            <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase">Getting Started</v-list-subheader>
            <v-list-item title="Introduction" to="/get-started/introduction" :class="isActive('/get-started/introduction') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="Application" to="/get-started/application" :class="isActive('/get-started/application') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <!-- Behaviour -->
            <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase mt-5">Behaviour</v-list-subheader>
            <v-list-item title="Persistence Backend" to="/get-started/behaviour/persistence" :class="isActive('/get-started/behaviour/persistence') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="MQTT Eventing" to="/get-started/behaviour/mqtt" :class="isActive('/get-started/behaviour/mqtt') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="Time Series Data" to="/get-started/behaviour/time-series" :class="isActive('/get-started/behaviour/time-series') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <!-- Visualization -->
            <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase mt-5">Visualisation</v-list-subheader>
            <v-list-item title="AAS User Interface" to="/get-started/visualization/ui" :class="isActive('/get-started/visualization/ui') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="Corporate Design" to="/get-started/visualization/corporate-design" :class="isActive('/get-started/visualization/corporate-design') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="AAS Dashboard" to="/get-started/visualization/dashboard" :class="isActive('/get-started/visualization/dashboard') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <!-- Deployment -->
            <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase mt-5">Deployment</v-list-subheader>
            <v-list-item title="AAS Integration" to="/get-started/deployment/integration" :class="isActive('/get-started/deployment/integration') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="Context Path" to="/get-started/deployment/context-path" :class="isActive('/get-started/deployment/context-path') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <v-list-item title="Access Control" to="/get-started/deployment/rbac" :class="isActive('/get-started/deployment/rbac') ? 'text-primary' : 'text-subheader'"></v-list-item>
            <!-- Download -->
            <v-list-subheader class="text-high-emphasis font-weight-black text-uppercase mt-5">Download</v-list-subheader>
            <v-list-item title="Download BaSyx" to="/get-started/download" :class="isActive('/get-started/download') ? 'text-primary' : 'text-subheader'"></v-list-item>
        </v-list>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'MenuList',

    methods: {
        isActive(path: string): boolean {
            // console.log('Path: ', this.$route.path);
            return this.$route.path === path;
        },
    },
});
</script>