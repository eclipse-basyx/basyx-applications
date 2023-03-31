<template>
    <v-container fluid class="pa-0">
        <v-row v-for="element in outputElements" :key="element.idShort" align="center" justify="space-between">
            <v-col cols="auto">
                <div>{{ element.name }}</div>
            </v-col>
            <v-col cols="auto" class="text-right py-1">
                <v-text-field v-if="element.name && element.unit" :value="element.value" readonly variant="outlined" density="compact" hide-details style="width: 140px">
                    <template v-slot:append-inner>
                        <span class="text-subtitleText">{{ element.unit }}</span>
                    </template>
                </v-text-field>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';

export default defineComponent({
    name: 'DisplayField',
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server
    },
    mixins: [RequestHandling],

    props: ['submodelElementData', 'widgetSettings'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            outputElements: [] as Array<any>,
        }
    },

    mounted() {
        this.initializeView(); // initialize list
    },

    watch: {
        // Watch for changes in the submodelElementData and (re-)initialize the Component
        submodelElementData: {
            deep: true,
            handler() {
                this.initializeView(); // initialize switch
            }
        },
    },

    computed: {
    },

    methods: {
        // Initialize the Component
        initializeView() {
            // console.log('Switch Data: ', this.submodelElementData);
            if (!this.widgetSettings || !this.submodelElementData || Object.keys(this.submodelElementData).length == 0) return;
            let localSubmodelElementData = { ...this.submodelElementData };
            // check if the submodelELelement is a Property or a SubmodelElementCollection
            if (this.widgetSettings.idShorts && this.widgetSettings.idShorts.length > 0 && localSubmodelElementData && Object.keys(localSubmodelElementData).length > 0) { // Collection
                // Filter submodelElementData by idShorts from widgetSettings
                let collectionValues = [] as Array<any>;
                this.widgetSettings.idShorts.forEach((element: any, i: number) => {
                    localSubmodelElementData.value.forEach((el: any) => {
                        if (element == el.idShort) {
                            let elementToPush = { ...el };
                            // console.log('WidgetSettings: ', this.widgetSettings.chartNamesUnits[i]);
                            elementToPush.name = this.widgetSettings.chartNamesUnits[i].name;
                            elementToPush.unit = this.widgetSettings.chartNamesUnits[i].unit;
                            collectionValues.push(elementToPush)
                        }
                    });
                });
                // console.log('collectionValues: ', collectionValues);
                this.outputElements = collectionValues;
            } else { // Property
                localSubmodelElementData.name = this.widgetSettings.chartNamesUnits[0].name;
                localSubmodelElementData.unit = this.widgetSettings.chartNamesUnits[0].unit;
                this.outputElements = [localSubmodelElementData];
            }
        },
    },
});
</script>
