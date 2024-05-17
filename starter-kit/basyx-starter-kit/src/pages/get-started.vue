<template>
    <v-container style="max-width: 1300px; display: flex;" class="py-0">
        <!-- Left side Menu -->
        <MenuList style="width: 250px; height: calc(100svh - 94px); position: fixed; overflow-y: auto;"></MenuList>
        <v-divider vertical style="margin-left: 250px; height: calc(100svh - 94px); position: fixed;"></v-divider>
        <!-- current comnponent based on the nested route -->
        <router-view style="margin-left: 250px; width: 800px;" class="mt-9" />
        <!-- Treview with the BaSyx components and configs -->
        <OutputView style="margin-left: 1050px; width: 320px; height: calc(100svh - 94px); position: fixed; overflow-y: auto;"></OutputView>
        <!-- Disclaimer Dialog window -->
        <v-dialog width="600px" v-model="showDisclaimer" persistent>
            <v-card color="alertCard">
                <v-card-title class="py-5">
                    <v-row align="center">
                        <v-col cols="auto">
                            <v-icon color="primary" size="small">mdi-alert-circle</v-icon>
                        </v-col>
                        <v-col>
                            <span class="text-primary">Disclaimer</span>
                        </v-col>
                    </v-row>
                </v-card-title>
                <v-divider></v-divider>
                <v-window v-model="window" style="height: 240px">
                    <!-- First Page (general notice) -->
                    <v-window-item>
                        <v-card-item v-if="!disclaimerAccepted" class="px-0 pt-0">
                            <v-card-text style="height: 240px; overflow-y: auto;" class="px-8">
                                <span style="white-space: pre-line; font-family: monospace;" class="text-normalText">
                                    The information provided by this application is for general informational purposes only. While we endeavor to keep the information up-to-date and correct, we make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability, or availability with respect to the application or the information, products, services, or related graphics contained on the application for any purpose. Any reliance you place on such information is therefore strictly at your own risk.
                                </span>
                                <br><br>
                                <span style="white-space: pre-line; font-family: monospace;" class="text-normalText">
                                    We expressly disclaim any obligation to update or correct information contained in the application and explicitly disclaim any duty to do so. We assume no liability or responsibility for any errors or omissions in the content of this application.
                                </span>
                                <br><br>
                                <span style="white-space: pre-line; font-family: monospace;" class="text-normalText">
                                    Please be advised that the application may be subject to updates and changes without notice.
                                </span>
                            </v-card-text>
                        </v-card-item>
                    </v-window-item>
                    <!-- Second Page (data storage notice) -->
                    <v-window-item>
                        <v-card-item v-if="disclaimerAccepted" class="px-0 pt-0">
                            <v-card-text style="height: 240px; overflow-y: auto;" class="px-8">
                                <span style="white-space: pre-line;" class="text-normalText">
                                    This application does not store any data on the server. Refreshing the page will reset the application to its initial state.
                                </span>
                            </v-card-text>
                        </v-card-item>
                    </v-window-item>
                </v-window>
                <v-divider></v-divider>
                <!-- Card actions to acknowledge the disclaimer -->
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn v-if="!disclaimerAccepted" append-icon="mdi-arrow-right" variant="tonal" @click="acceptDisclaimer()">Accept</v-btn>
                    <v-btn v-if="disclaimerAccepted" append-icon="mdi-check" color="primary" variant="tonal" @click="showDisclaimer = false">Ok</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
    name: 'GetStarted',

    data() {
        return {
            showDisclaimer: false,
            disclaimerAccepted: false,
            window: 0,
        };
    },

    mounted() {
        this.showDisclaimer = true;
    },

    methods: {
        acceptDisclaimer() {
            this.disclaimerAccepted = true;
            this.window = 1;
        },
    },
});
</script>
