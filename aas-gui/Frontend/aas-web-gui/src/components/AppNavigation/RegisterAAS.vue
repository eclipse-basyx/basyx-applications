<template>
    <v-btn variant="plain">
        <v-icon size="x-large">mdi-plus</v-icon>
        <v-tooltip activator="parent" open-delay="600" location="bottom">Add existing AAS to Registry</v-tooltip>
        <v-dialog activator="parent" v-model="RegisterAASDialog" width="600">
            <v-card>
                <v-card-title>
                    <span class="text-subtile-1">Add existing AAS to Registry</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                    <v-text-field variant="outlined" density="compact" label="AAS Endpoint" hint="E.g. http://localhost:4000/aas" clearable v-model="aasEndpoint"></v-text-field>
                </v-card-text>
                <v-divider></v-divider>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn size="small" variant="outlined" color="primary" @click="RegisterAASDialog = false">Cancel</v-btn>
                    <v-btn size="small" class="text-buttonText" variant="elevated" color="primary" @click="addAAS()" :loading="registrationLoading">Add</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-btn>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'RegisterAAS',
    mixins: [RequestHandling],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            RegisterAASDialog: false, // Dialog State Handler
            registrationLoading: false, // Loading State Handler
            aasEndpoint: '', // AAS Endpoint
        }
    },

    computed: {
        // get Registry URL from Store
        registryURL() {
            return this.store.getters.getRegistryURL;
        },
    },

    methods: {
        // Function to add a new AAS to the Registry
        addAAS() {
            this.registrationLoading = true;
            let path = this.aasEndpoint;
            let context = 'retrieving to be registered AAS';
            let disableMessage = false;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) { // execute if the AAS exists
                    let aas = response.data;
                    let identificationId = aas.identification.id;
                    // console.log('Add AAS to Registry', aas, identificationId);
                    aas.endpoints[0].address = this.aasEndpoint;
                    aas.endpoints[0].type = this.aasEndpoint.split(':')[0];
                    let path = this.registryURL + '/api/v1/registry/' + identificationId;
                    let content = JSON.stringify(aas);
                    let headers = { 'Content-Type': 'application/json' };
                    let context = 'registering AAS';
                    let disableMessage = false;
                    this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                        if (response.success) { // execute if the AAS is successfully registered
                            this.store.dispatch('dispatchTriggerAASListReload', true); // trigger AAS List reload
                            this.RegisterAASDialog = false; // close dialog
                        }
                    });
                }
                this.registrationLoading = false;
            });
        },
    },
});
</script>
