<template>
    <v-container fluid class="pa-0">
        <!-- Options -->
        <v-list nav class="pa-0" style="margin-left: -8px; margin-top: -14px">
            <v-list-item class="pb-0">
                <template v-slot:title>
                    <div class="text-subtitle-2">{{ "Options: " }}</div>
                </template>
            </v-list-item>
        </v-list>
        <v-row align="center">
            <v-col cols="auto">
                <v-text-field type="number" hide-details density="compact" v-model="numberOfCategories" @blur="initializeSeries()" @keydown.native.enter="initializeSeries()" label="Bins" variant="outlined"></v-text-field>
            </v-col>
            <v-col cols="auto">
                <v-switch hide-details label="stacked" v-model="stacked" density="compact" @change="changeVariant()"></v-switch>
            </v-col>
        </v-row>
        <apexchart ref="histogram" height="350" :options="chartOptions" :series="chartSeries"></apexchart>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import WidgetHandling from '@/mixins/WidgetHandling';

import { useNavigationStore } from '@/store/NavigationStore';

export default defineComponent({
    name: 'Histogram',
    props: ['chartData', 'timeVariable', 'yVariables'],
    mixins: [WidgetHandling],

    setup() {
        const theme = useTheme()
        const navigationStore = useNavigationStore()

        return {
            theme, // Theme Object
            navigationStore,
        }
    },

    data() {
        return {
            chartSeries: [] as Array<any>,
            chartOptions: {
                chart: {
                    id: 'histogram',
                    type: 'bar',
                    height: 350,
                    stacked: false,
                    background: '#ffffff00',
                },
                plotOptions: {
                    bar: {
                        borderRadius: 4,
                        horizontal: false,
                        dataLabels: {
                            position: 'top',
                        },
                    }
                },
                legend: {
                    show: false
                },
                dataLabels: {
                    enabled: true,
                    offsetX: -6,
                },
                xaxis: {
                    categories: [],
                },
                theme: {
                    mode: 'dark'
                },
            } as any,
            stacked: false,
            numberOfCategories: 20,
        }
    },

    mounted() {
        this.$nextTick(() => {
            const chart = (this.$refs.histogram as any).chart
            if (chart) {
                // console.log('Chart has rendered')
                // apply the theme on component mount
                this.applyTheme();
                // append the series to the chart
                this.initializeSeries();
            }
        })
    },

    watch: {
        chartData: {
            handler() {
                this.initializeSeries();
            },
            deep: true,
        },

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
            // console.log('initializeSeries: ', this.chartData);
            let { histograms, categories } = this.prepareHistogramData(this.chartData, this.numberOfCategories);
            // console.log('histograms: ', histograms, ' categories: ', categories);
            if (!histograms || !categories || histograms.length === 0 || categories.length === 0) {
                return;
            }
            let newSeries = histograms.map((histogram: any, index: number) => {
                return {
                    name: 'Number of values in bin',
                    data: histogram,
                }
            });
            let chartOptions = {
                xaxis: {
                    categories: categories
                },
            } as any;
            // Update the chartOptions
            (this.$refs.histogram as any).updateOptions(chartOptions);
            // update the series
            // console.log('chartSeries: ', newSeries);
            (this.$refs.histogram as any).updateSeries(newSeries);
        },

        changeVariant() {
            (this.$refs.histogram as any).updateOptions({
                chart: {
                    stacked: this.stacked
                }
            });
        },

        // Function to apply the selected theme to the chart
        applyTheme() {
            if (this.isDark) {
                // apply the dark theme to the chart options
                (this.$refs.histogram as any).updateOptions({
                    theme: {
                        mode: 'dark'
                    }
                });
            } else {
                // apply the light theme to the chart options
                (this.$refs.histogram as any).updateOptions({
                    theme: {
                        mode: 'light'
                    }
                });
            }
        },
    },
})
</script>