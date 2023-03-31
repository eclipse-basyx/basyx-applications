<template>
    <v-container v-if="showExtended" class="pa-0" fluid>
        <!-- AAS Details Card (only visible if the Information Button is pressed on an AAS) -->
        <v-expand-transition>
            <div v-if="showDetailsCard" class="transition-fast-in-fast-out" :class="isMobile ? 'v-card--reveal-mobile' : 'v-card--reveal-desktop'" style="z-index: 9000">
                <v-divider></v-divider>
                <v-card-title class="bg-detailsHeader pl-3">
                    <v-row align="center" class="pl-4">
                        <!-- AAS Status -->
                        <div class="text-caption">{{ 'Status: ' }}</div>
                        <div class="text-caption ml-1" :class="detailsObject.status == 'online'? 'text-success' : 'text-error'">{{ detailsObject.status }}</div>
                        <v-spacer></v-spacer>
                        <!-- Close Button -->
                        <v-btn @click="closeDetails()" icon="mdi-close-circle-outline" size="small" variant="plain" style="z-index: 2000; margin-right: -8px"></v-btn>
                    </v-row>
                </v-card-title>
                <v-divider></v-divider>
                <!-- AAS Details -->
                <v-list v-if="detailsObject" lines="one" nav class="bg-detailsCard">
                    <!-- AAS Identification -->
                    <IdentificationElement class="mb-2" :identificationObject="detailsObject" :modelType="'AAS'"></IdentificationElement>
                    <v-divider v-if="detailsObject.description && detailsObject.description.length > 0"></v-divider>
                    <!-- AAS Description -->
                    <DescriptionElement v-if="detailsObject.description && detailsObject.description.length > 0" :descriptionObject="detailsObject.description" :descriptionTitle="'Description'" :small="false"></DescriptionElement>
                </v-list>
                <v-divider v-if="detailsObject && detailsObject.asset"></v-divider>
                <!-- Asset Details -->
                <v-list v-if="detailsObject && detailsObject.asset" lines="one" nav class="bg-detailsCard">
                    <!-- Asset Identification -->
                    <IdentificationElement class="mb-2" :identificationObject="detailsObject.asset" :modelType="'Asset'"></IdentificationElement>
                    <v-divider v-if="detailsObject.asset.description && detailsObject.asset.description.length > 0"></v-divider>
                    <!-- Asset Description -->
                    <DescriptionElement v-if="detailsObject.asset.description && detailsObject.asset.description.length > 0" :descriptionObject="detailsObject.asset.description" :descriptionTitle="'Description'" :small="false"></DescriptionElement>
                </v-list>
            </div>
        </v-expand-transition>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';
import IdentificationElement from '../UIComponents/IdentificationElement.vue';
import DescriptionElement from '../UIComponents/DescriptionElement.vue';

export default defineComponent({
    name: 'AASListDetails',
    components: {
        IdentificationElement,
        DescriptionElement,
    },
    props: ['detailsObject', 'showDetailsCard', 'showExtended'], // Props from the parent component with the AAS Details Object and the boolean to show the AAS Details Card

    setup() {
        const store = useStore()

        return {
            store, // Store Object
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
    },

    methods: {
        // Function to close the AAS Details Card and emit the event to the parent component
        closeDetails() {
            this.$emit('close-details');
        }
    }
});
</script>

<style>
.v-card--reveal-mobile {
    bottom: 0px;
    opacity: .96;
    position: absolute;
    width: 100%;
}
.v-card--reveal-desktop {
    bottom: 48px;
    opacity: .96;
    position: absolute;
    width: 100%;
}
</style>
