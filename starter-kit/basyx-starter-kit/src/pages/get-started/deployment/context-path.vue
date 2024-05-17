<template>
    <v-container class="py-0 px-12" fluid>
        <h1 class="mb-8 text-header">Context Path</h1>
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
                The Starter Kit does not currently support changing the context path. This feature will be added in the future.
            </p>
        </v-alert>
        <p class="text-normalText mt-8 mb-5 text-subtitle-1">
            The context path is the part of the URL that comes after the domain name. For example, in the URL <code class="text-subheader">http://localhost:8080/aas-env</code>, the context path is <code class="text-subheader">/aas-env</code>. This way you are able to configure your BaSyx components to run under a specific path on the same domain.
        </p>
        <!-- Navigation to previous and next page -->
        <v-card-actions class="px-0 mb-8">
            <v-btn variant="tonal" prepend-icon="mdi-arrow-left" to="/get-started/deployment/integration">Back</v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="tonal" color="primary" append-icon="mdi-arrow-right" to="/get-started/deployment/rbac">Next</v-btn>
        </v-card-actions>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAppStore } from '@/stores/app';

export default defineComponent({
    name: 'ContextPath',

    setup() {
        const appStore = useAppStore();

        return {
            appStore,
        };
    },

    data() {
        return {
            selection: 'noUI',
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