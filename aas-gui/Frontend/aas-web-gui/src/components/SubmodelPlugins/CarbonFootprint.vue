<template>
    <v-container fluid class="pa-0">
        <!-- Header -->
        <v-card class="mb-4">
            <v-card-title>
                <div class="text-subtitle-1">{{ "Carbon Footprint:" }}</div>
            </v-card-title>
        </v-card>
        <!-- Product and Transport CarbonFootprint -->
        <v-card class="mb-4 py-8">
            <v-row flex align="center" justify="center">
                <v-col cols="12" md="2" style="min-width: fit-content; padding-left: 20px; font-size: 20px;">
                    <div v-if="productCarbonFootprintData">
                        <p style="margin: 40px 0">
                            <span
                                style="display: inline-block; width: 14px; height: 14px; margin-right: 6px; border-radius: 3px"
                                class="bg-pcf"></span>
                            <span class="text-subtitle-1 subtitleText">Product:</span>
                        <div class="text-h6">{{ productIsoValue + ' CO₂eq' }}</div>
                        </p>
                    </div>
                    <div
                        v-if="transportCarbonFootprintData && Number(transportENValue) !== 0 && transportENValue !== null">
                        <p>
                            <span
                                style="display: inline-block; width: 14px; height: 14px; margin-right: 6px; border-radius: 3px"
                                class="bg-tcf"></span>
                            <span class="text-subtitle-1 subtitleText">Transport:</span>
                        <div class="text-h6">{{ transportENValue + ' CO₂eq' }}</div>
                        </p>
                    </div>
                </v-col>
                <v-col cols="12" md="6">
                    <svg :fill="fingersColor" maxWidth="400px" height="100%" style="margin-left: -50px" version="1.1"
                        id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                        viewBox="0 0 489.978 489.978" xml:space="preserve">
                        <defs>
                            <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
                                <stop :offset="pcfPercentage + '%'" :stop-color="pcfValueColor" />
                                <stop :offset="pcfPercentage + '%'" :stop-color="tcfValueColor" />
                                <stop offset="100%" :stop-color="tcfValueColor" />
                            </linearGradient>
                        </defs>
                        <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                        <g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g>
                        <g id="SVGRepo_iconCarrier">
                            <g>
                                <g>
                                    <g>
                                        <path :style="{ fill: 'url(#gradient)' }"
                                            d="M184.231,239.9c43.9,23.3,64.5,75.4,49.8,123.3c-2.7,8.6-5.1,17.5-7,26.4c-7.8,40,2.7,90.2,46.7,99.1 c22.2,4.7,44.3-3.9,59.9-20.2c46.5-50.5,32.3-202.6,31.9-240.7c0-16.7-0.4-33.8-7-49.4c-14.8-35-56.4-49-93.3-58.7 c-60.6-22.1-98.8,5.8-111.6,26.4S131.531,212.5,184.231,239.9z">
                                        </path>
                                        <ellipse cx="141.831" cy="44.3" rx="31.5" ry="44.3"></ellipse>
                                        <ellipse cx="214.631" cy="51.7" rx="18.7" ry="26.4"></ellipse>
                                        <ellipse cx="272.931" cy="68" rx="16.7" ry="23.3"></ellipse>
                                        <ellipse cx="326.131" cy="81.7" rx="14.8" ry="20.6"></ellipse>
                                        <ellipse transform="matrix(-0.2014 0.9795 -0.9795 -0.2014 546.9331 -227.4809)"
                                            cx="366.2" cy="109.218" rx="18.3" ry="13.2"></ellipse>
                                    </g>
                                </g>
                            </g>
                        </g>
                    </svg>
                </v-col>
            </v-row>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useNavigationStore } from '@/store/NavigationStore';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

const PRODUCT_CARBON_FOOTPRINT = 'ProductCarbonFootprint';
const TRANSPORT_CARBON_FOOTPRINT = 'TransportCarbonFootprint';
const CO2_EQ = 'CO2eq';

export default defineComponent({
    name: 'CarbonFootprint',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['submodelElementData'],
    setup() {
        const theme = useTheme();
        const navigationStore = useNavigationStore();
        const aasStore = useAASStore();

        return {
            theme, // Theme Object
            navigationStore, // NavigationStore Object
            aasStore, // AASStore Object
        };
    },
    data() {
        return {
            productCarbonFootprintData: {} as Record<string, any>,
            transportCarbonFootprintData: {} as Record<string, any>,
            productIsoValue: '' as string,
            transportENValue: '' as string,
        };
    },
    mounted() {
        // console.log('Component Mounted. SubmodelElementData:', this.submodelElementData);
        this.initializeProductCarbonFootprint(); // initialize Product Carbon Footprint Plugin
        this.initializeTransportCarbonFootprint(); // initialize Transport Carbon Footprint Plugin
    },
    computed: {
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },
        pcfPercentage() {
            return this.calculatePercentage(this.productIsoValue, this.transportENValue);
        },
        tcfPercentage() {
            return this.calculatePercentage(this.transportENValue, this.productIsoValue);
        },
        pcfValueColor() {
            return this.getThemeColor('pcf');
        },
        tcfValueColor() {
            return this.getThemeColor('tcf');
        },
        fingersColor() {
            return this.getThemeColor('fingers');
        }
    },
    methods: {
        calculatePercentage(value1: string, value2: string): number {
            const v1 = parseFloat(value1) || 0;
            const v2 = parseFloat(value2) || 0;
            const total = v1 + v2;
            return total ? (v1 / total) * 100 : 0;
        },
        getThemeColor(colorKey: string): string {
            const isDark = this.$vuetify.theme.global.current.dark;
            const themeColors = isDark ? this.$vuetify.theme.themes.dark.colors : this.$vuetify.theme.themes.light.colors;
            return themeColors[colorKey];
        },
        // Utility function to initialize Carbon Footprint
        initializeCarbonFootprint(type: string, dataKey: 'productCarbonFootprintData' | 'transportCarbonFootprintData', valueKey: 'productIsoValue' | 'transportENValue') {
            this[dataKey] = {};
            this[valueKey] = "";

            if (Object.keys(this.submodelElementData).length === 0) {
                return;
            }

            try {
                const carbonFootprintData = { ...this.submodelElementData };
                const entries = carbonFootprintData.submodelElements.filter((elem: any) => elem.idShort && elem.idShort.includes(type));
                const totalValue = entries.reduce((accumulator: number, entry: any) => {
                    const co2eqValue = entry.value ? parseFloat(entry.value.find((val: any) => val?.idShort?.includes(CO2_EQ))?.value) ?? 0 : 0;
                    return accumulator + co2eqValue;
                }, 0);

                this[dataKey] = entries; // Set the entries for display or further processing
                this[valueKey] = totalValue.toFixed(2);
            } catch (error) {
                console.error(`Error initializing ${type}:`, error);
            }
        },

        // Function to initialize the Product Carbon Footprint Plugin
        initializeProductCarbonFootprint() {
            this.initializeCarbonFootprint(PRODUCT_CARBON_FOOTPRINT, 'productCarbonFootprintData', 'productIsoValue');
        },

        // Function to initialize the Transport Carbon Footprint Plugin
        initializeTransportCarbonFootprint() {
            this.initializeCarbonFootprint(TRANSPORT_CARBON_FOOTPRINT, 'transportCarbonFootprintData', 'transportENValue');
        }
    }
});
</script>
