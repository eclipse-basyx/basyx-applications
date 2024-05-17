<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Role Based Access Control</h1>
        <!-- Alert for BaSyx Version -->
        <v-alert color="alertCard">
            <v-row align="center">
                <v-col cols="auto" class="pr-0">
                    <v-icon color="subheader">mdi-alert-circle-outline</v-icon>
                </v-col>
                <v-col>
                    <div class="font-weight-medium text-header">Notice</div>
                </v-col>
            </v-row>
            <p class="text-subheader font-weight-medium" style="margin-left: 36px; margin-top: 8px">
                The Starter Kit does not currently support enabling this feature. There are BaSyx components that don't support RBAC yet. As soon as all main components include the RBAC feature, it will be added to the Starter Kit.
            </p>
        </v-alert>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            Role Based Access Control (RBAC) is a method of restricting network access based on the roles of individual users within an orgonisation or enterprise. RBAC lets users have access rights only to the information they need to. This is a security measure that is used to protect sensitive information from unauthorised users.
        </p>
        <p class="text-normalText mt-3 mb-8 text-subtitle-1">
            BaSyx leverages <a class="text-primary" style="text-decoration: none" href="https://www.keycloak.org/" target="_blank">Keycloak</a> for RBAC. Keycloak is an open source identity and access management solution that makes it easy to secure applications and services with little to no code. It is a single sign-on solution that allows you to manage users, roles, and permissions.
        </p>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/deployment/context-path">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-flag-checkered" to="/get-started/download">finalize</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'RBAC',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
        };
    },

    mounted() {
    },

    computed: {
        // get the BaSyx configuration from the store
        basyxConfig(): any {
            return this.appStore.getBasyxConfig;
        },
    },

    methods: {
    },
});
</script>