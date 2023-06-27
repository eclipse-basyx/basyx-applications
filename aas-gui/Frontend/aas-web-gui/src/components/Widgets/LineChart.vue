<template>
    <v-container fluid class="pa-0">
        <apexchart ref="linechart" height="350" :options="chartOptions" :series="chartSeries"></apexchart>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import { useTheme } from 'vuetify';

export default defineComponent({
    name: 'LineChart',
    props: ['submodelElementData', 'widgetSettings'],

    setup() {
        const store = useStore()
        const theme = useTheme()

        return {
            store, // Store Object
            theme, // Theme Object
        }
    },

    data() {
        return {
            chartSeries: [] as Array<any>,
            chartOptions: {
                chart: {
                    id: 'realtime',
                    background: '#ffffff00',
                    redrawOnParentResize: true,
                    redrawOnWindowResize: true,
                    animations: {
                        enabled: true,
                        easing: 'linear',
                        dynamicAnimation: {
                            speed: 1000,
                        },
                    },
                },
                xaxis: {
                    type: 'datetime',
                    range: 60000,
                    tickAmount: 10,
                    labels: {
                        datetimeFormatter: {
                            year: 'yyyy',
                            month: 'MMM \'yy',
                            day: 'dd MMM',
                            hour: 'HH:mm',
                        },
                    },
                    tickPlacement: 'on',
                },
                yaxis: {
                    decimalsInFloat: 2,
                    title: {
                        text: ''
                    },
                },
                stroke: {
                    curve: 'smooth',
                    width: 3,
                },
                grid: {
                    xaxis: {
                        lines: {
                            show: false
                        }
                    },
                },
                theme: {
                    mode: 'dark'
                },
            },
        }
    },

    mounted() {
        this.$nextTick(() => {
            const chart = (this.$refs.linechart as any).chart
            if (chart && this.submodelElementData && Object.keys(this.submodelElementData).length > 0) {
                // console.log('Chart has rendered')
                // apply the theme on component mount
                this.applyTheme();
                // append the series to the chart
                this.initializeSeries();
            }
        })
    },

    watch: {
        // appendData to the chart if the submodelElementData changed
        submodelElementData: {
            handler() {
                this.updateView();
            },
            deep: true,
        },

        // apply the chart-theme if the theme changed
        isDark() {
            this.applyTheme();
        },
    },

    computed: {
        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },
    },

    methods: {
        // Function to initialize the chart (by appending the series)
        initializeSeries() {
            // check if the submodelElementData is a collection or a property
            if (this.widgetSettings.idShorts && this.widgetSettings.idShorts.length > 0) { // Collection
                // Filter submodelElementData by idShorts from widgetSettings
                let collectionValues = [] as Array<any>;
                this.widgetSettings.idShorts.forEach((element: any) => {
                    this.submodelElementData.value.forEach((el: any) => {
                        if (element == el.idShort) {
                            collectionValues.push(el)
                        }
                    });
                });
                // console.log('collectionValues: ', collectionValues, 'widgetSettings: ', this.widgetSettings);
                collectionValues.forEach((element: any, index: number) => {
                    // seriesName is updated with set value for Displayed Name
                    // if value to display is a boolean, convert true to 1 and false to 0
                    if(typeof element.value === "boolean"){
                        element.value ? 1 : 0;
                    }
                    let seriesName = this.widgetSettings.chartNamesUnits[index].name + ' [' + this.widgetSettings.chartNamesUnits[index].unit + ']';
                    let chartValues = [{ x: this.submodelElementData.timestamp, y: element.value }];
                    let chartSeries = {
                        name: seriesName,
                        data: chartValues
                    } as any;
                    let chartOptions = {
                        xaxis: {
                            type: 'datetime',
                            range: this.widgetSettings.chartRange,
                            tickAmount: 10,
                            labels: {
                                datetimeFormatter: {
                                    year: 'yyyy',
                                    month: 'MMM \'yy',
                                    day: 'dd MMM',
                                    hour: 'HH:mm',
                                },
                            },
                            tickPlacement: 'on',
                        },
                    } as any;
                    // update the chartOptions
                    // console.log('chartOptions: ', chartOptions);
                    (this.$refs.linechart as any).updateOptions(chartOptions);
                    // append the series to the chart
                    (this.$refs.linechart as any).appendSeries(chartSeries);
                });
            } else { // Property
                // add seriesName as y-Axis title
                let chartOptions = {
                    xaxis: {
                        type: 'datetime',
                        range: this.widgetSettings.chartRange,
                        tickAmount: 10,
                        labels: {
                            datetimeFormatter: {
                                year: 'yyyy',
                                month: 'MMM \'yy',
                                day: 'dd MMM',
                                hour: 'HH:mm',
                            },
                        },
                        tickPlacement: 'on',
                    },
                    yaxis: {
                        decimalsInFloat: 2,
                        title: {
                            text: this.widgetSettings.chartNamesUnits[0].name + ' [' + this.widgetSettings.chartNamesUnits[0].unit + ']'
                        }
                    }
                } as any;
                // if value to display is a boolean, convert true to 1 and false to 0
                if(typeof this.submodelElementData.value === "boolean"){
                    chartOptions.xaxis.stroke = "straight";
                    this.submodelElementData.value ? 1 : 0;
                } else {
                    chartOptions.xaxis.stroke = "smooth"
                }
                // update the chartOptions
                // console.log('chartOptions: ', chartOptions);
                (this.$refs.linechart as any).updateOptions(chartOptions);
                // append the series to the chart
                (this.$refs.linechart as any).appendSeries({
                    name: this.widgetSettings.chartNamesUnits[0].name,
                    data: [{ x: this.submodelElementData.timestamp, y: this.submodelElementData.value }]
                });
            }
        },

        // Function to appendData to the chart
        updateView() {
            // console.log('updateView: ', this.submodelElementData);
            if(!this.submodelElementData || Object.keys(this.submodelElementData).length === 0) {
                return;
            }
            // check if the submodelElementData is a collection or a property
            if (this.widgetSettings.idShorts && this.widgetSettings.idShorts.length > 0) { // Collection
                // Filter submodelElementData by idShorts from widgetSettings
                let collectionValues = [] as Array<any>;
                this.widgetSettings.idShorts.forEach((element: any) => {
                    this.submodelElementData.value.forEach((el: any) => {
                        if (element == el.idShort) {
                            collectionValues.push(el)
                        }
                    });
                });
                // create a new array in the form [{ data: [{ x, y }] }, { data: [{ x, y }] }]
                let chartValues = [] as Array<any>;
                collectionValues.forEach((element: any) => {
                    // if value to display is a boolean, convert true to 1 and false to 0
                    if(typeof element.value === "boolean"){
                        element.value ? 1 : 0;
                    }
                    let chartValue = { x: this.submodelElementData.timestamp, y: element.value };
                    chartValues.push({ data: [chartValue] });
                });
                // append the new chartValues to the chart
                (this.$refs.linechart as any).appendData(chartValues);
            } else { // Property
                // if value to display is a boolean, convert true to 1 and false to 0
                if(typeof this.submodelElementData.value === "boolean"){
                    this.submodelElementData.value ? 1 : 0;
                }
                const x = this.submodelElementData.timestamp;
                const y = this.submodelElementData.value;
                (this.$refs.linechart as any).appendData([{ 
                    data: [{ x, y }] 
                }])
            }
        },

        // Function to apply the selected theme to the chart
        applyTheme() {
            if (this.isDark) {
                // apply the dark theme to the chart options
                (this.$refs.linechart as any).updateOptions({
                    theme: {
                        mode: 'dark'
                    }
                });
            } else {
                // apply the light theme to the chart options
                (this.$refs.linechart as any).updateOptions({
                    theme: {
                        mode: 'light'
                    }
                });
            }
        },
    },
})
</script>