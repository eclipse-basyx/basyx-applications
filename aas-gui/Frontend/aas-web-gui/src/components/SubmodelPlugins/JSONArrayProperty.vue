<template>
    <v-container fluid class="pa-0">
        <v-card class="pa-3">
            <apexchart ref="areachart" type="area" height="350" :options="chartOptions" :series="chartData"></apexchart>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';
import { useTheme } from 'vuetify';
import { useStore } from 'vuex';

export default defineComponent({
    name: 'JSONArrayProperty',

    setup() {
        const theme = useTheme()
        const store = useStore()

        const chartData = ref([
            {
                name: 'series1',
                data: []
            }
        ])

        const chartOptions = ref({
            chart: {
                id: 'chartMain',
                type: 'area',
                height: 350,
                background: '#ffffff00',
            },
            // colors: ['#1e8567'],
            dataLabels: {
                enabled: false
            },
            stroke: {
                curve: 'smooth'
            },
            markers: {
                size: 0
            },
            xaxis: {
                type: 'numeric',
                decimalsInFloat: 0
            },
            yaxis: {
                decimalsInFloat: 2
            },
            theme: {
                mode: 'dark'
            },
        })

        return {
            theme, // Theme Object
            store, // Store Object
            chartData,
            chartOptions,
        }
    },

    data() {
        return {
            submodelElementData: {} as any, // SubmodelElement Data
        }
    },

    mounted() {
        // wait for the chart to be initialized
        setTimeout(() => {
            this.initChart();
            // apply the theme on component mount
            this.applyTheme();
        }, 1000);
    },

    watch: {
        // Watch for changes in the RealTimeDataObject and (re-)initialize the Component
        RealTimeObject: {
            deep: true,
            handler() {
                // clear old submodelElementData
                this.submodelElementData = {};
                this.initChart(); // initialize list
            }
        },
        // watch for changes in the vuetify theme and update the chart options
        isDark() {
            this.applyTheme();
        },
    },

    computed: {
        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.store.getters.getSelectedNode;
        },

        // Get the real-time object from the store
        RealTimeObject() {
            return this.store.getters.getRealTimeObject;
        },

        // RealTimeDataObject value
        RealTimeObjectValue() {
            return this.RealTimeObject.value;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },
    },

    methods: {
        initChart() {
            // Check if a Node is selected
            if (Object.keys(this.RealTimeObject).length == 0) {
                this.submodelElementData = {}; // Reset the SubmodelElement Data when no Node is selected
                return;
            }
            this.submodelElementData = { ...this.RealTimeObject }; // create local copy of the SubmodelElement Object
            let chartData = JSON.parse(this.submodelElementData.value); // parse the value of the SubmodelElement
            let seriesName = this.submodelElementData.idShort; // get the idShort of the SubmodelElement
            // check if the value is an array or an object
            if (Array.isArray(chartData)) { // array in form of [y1, y2, y3, ..., yn ]
                // set/update the chart data
                (this.$refs.areachart as any).updateSeries([{
                    name: seriesName,
                    data: chartData
                }]);
            } else if (typeof chartData === 'object') { // object in form of { title1: [y11, y12, y13, ..., y1n], title2: [y21, y22, y23, ..., y2n] }
                // set/update the chart data
                (this.$refs.areachart as any).updateSeries(Object.keys(chartData).map((key) => {
                    return {
                        name: key,
                        data: chartData[key]
                    }
                }));
            }
        },

        // Function to apply the selected theme to the chart
        applyTheme() {
            if (this.isDark) {
                (this.$refs.areachart as any).updateOptions({
                    theme: {
                        mode: 'dark'
                    }
                });
            } else {
                (this.$refs.areachart as any).updateOptions({
                    theme: {
                        mode: 'light'
                    }
                });
            }
        },
    },
});
</script>
