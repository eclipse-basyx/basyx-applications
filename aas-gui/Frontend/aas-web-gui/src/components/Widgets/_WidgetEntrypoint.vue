<template>
    <v-container fluid class="pa-0">
        <template v-if="widgetData && Object.keys(widgetData).length > 0">
            <!-- List of all available Widgets -->
            <v-card v-for="widget in widgetData" :key="widget.widget_id" class="mb-4">
                <!-- List of all available Widgets -->
                <!-- TODO: Element Details don't get updated when a widget changes the value(s) -> needs fix -->
                <v-card-title class="px-0">
                    <v-list-item>
                        <!-- Widget Title -->
                        <v-list-item-title class="text-primary text-subtitle-1">{{ widget.widgetSettings.widgetTitle }}</v-list-item-title>
                        <template v-slot:append>
                            <!-- Show Widget in Dashboard -->
                            <v-tooltip :open-delay="600" location="start">
                                <template v-slot:activator="{ props }">
                                    <v-btn :icon="widget.showInDash ? 'mdi-eye' : 'mdi-eye-off'" variant="plain" v-bind="props" size="small" @click="updateWidgetSettings(widget)"></v-btn>
                                </template>
                                <span>{{ 'Show/Hide Widget in Dashboard' }}</span>
                            </v-tooltip>
                            <!-- Remove Widget -->
                            <v-tooltip :open-delay="600" location="start">
                                <template v-slot:activator="{ props }">
                                    <v-btn icon="mdi-close" variant="plain" v-bind="props" size="small" @click="removeWidget(widget)"></v-btn>
                                </template>
                                <span>{{ 'Remove Widget' }}</span>
                            </v-tooltip>
                        </template>
                    </v-list-item>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                    <Button v-if="widget.widgetSettings.widgetType                  == 'inputElement'   && widget.widgetSettings.interactionElement == 'Button'" :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></Button>
                    <Switch v-else-if="widget.widgetSettings.widgetType             == 'inputElement'   && widget.widgetSettings.interactionElement == 'Switch'" :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></Switch>
                    <Lamp v-else-if="widget.widgetSettings.widgetType               == 'inputElement'   && widget.widgetSettings.interactionElement == 'Lamp'"   :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></Lamp>
                    <DisplayField v-else-if="widget.widgetSettings.widgetType       == 'outputElement'  && widget.widgetSettings.textElement == 'Display Field'" :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></DisplayField>
                    <InputField v-else-if="widget.widgetSettings.widgetType         == 'outputElement'  && widget.widgetSettings.textElement == 'Input Field'"   :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></InputField>
                    <SubmodelEntrypoint v-else-if="widget.widgetSettings.widgetType == 'plugin'"                                                                 :submodelElementData="submodelElementData" :selectedNode="selectedNode" :widgetSettings="widget.widgetSettings"></SubmodelEntrypoint>
                    <LineChart v-else-if="widget.widgetSettings.widgetType          == 'chart'          && widget.widgetSettings.chartType == 'Line Chart'"      :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></LineChart>
                    <AreaChart v-else-if="widget.widgetSettings.widgetType          == 'chart'          && widget.widgetSettings.chartType == 'Area Chart'"      :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></AreaChart>
                    <BarChart v-else-if="widget.widgetSettings.widgetType           == 'chart'          && widget.widgetSettings.chartType == 'Bar Chart'"       :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></BarChart>
                    <RadialChart v-else-if="widget.widgetSettings.widgetType        == 'chart'          && widget.widgetSettings.chartType == 'Radial Chart'"    :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></RadialChart>
                    <ColumnChart v-else-if="widget.widgetSettings.widgetType        == 'chart'          && widget.widgetSettings.chartType == 'Column Chart'"    :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></ColumnChart>
                    <DonutChart v-else-if="widget.widgetSettings.widgetType         == 'chart'          && widget.widgetSettings.chartType == 'Donut Chart'"     :submodelElementData="submodelElementData"                              :widgetSettings="widget.widgetSettings"></DonutChart>
                </v-card-text>
            </v-card>
        </template>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

import SubmodelEntrypoint from '../SubmodelPlugins/_SubmodelEntrypoint.vue';

import Button from './Button.vue';
import Switch from './Switch.vue';
import Lamp from './Lamp.vue';
import DisplayField from './DisplayField.vue';
import InputField from './InputField.vue';
import AreaChart from './AreaChart.vue';
import LineChart from './LineChart.vue';
import BarChart from './BarChart.vue';
import RadialChart from './RadialChart.vue';
import ColumnChart from './ColumnChart.vue'
import DonutChart from './DonutChart.vue';

export default defineComponent({
    name: 'WidgetEntrypoint',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        SubmodelElementHandling, // Mixin to handle the SubmodelElements

        SubmodelEntrypoint, // Submodel Entrypoint

        // Widgets
        Button,
        Switch,
        Lamp,
        DisplayField,
        InputField,
        AreaChart,
        LineChart,
        BarChart,
        RadialChart,
        ColumnChart,
        DonutChart,
    },
    mixins: [RequestHandling, SubmodelElementHandling],

    props: ['submodelElementData', 'selectedNode'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            addWidget: false,
            widgetData: [] as any, // Object containing the Widget Settings for the current SubmodelElement
        }
    },

    mounted() {
        this.widgetData = []; // Reset the Widget Settings
        this.getSubmodelElementWidget() // Get the Widget Information for the SubmodelElement
    },

    watch: {
        // Watch for changes in the submodelElementDataID and (re-)initialize the Component
        submodelElementPath() {
            this.widgetData = []; // Reset the Widget Settings
            this.getSubmodelElementWidget() // Get the Widget Information for the SubmodelElement
        },

        // Watch for changes in the UpdateWidget and (re-)initialize the Component
        UpdateWidget() {
            this.getSubmodelElementWidget() // Get the Widget Information for the SubmodelElement
            this.store.dispatch('dispatchUpdateWidget', false);
        },
    },

    computed: {
        // Get the prompt to get the Widget Information for the SubmodelElement
        UpdateWidget() {
            return this.store.getters.getUpdateWidget;
        },

        // Get the ID of the submodelElementData
        submodelElementPath() {
            return this.submodelElementData.path;
        },

        // get Widget Api URL from Store
        widgetApiURL() {
            return this.store.getters.getWidgetApiURL;
        },
    },

    methods: {
        // Function to get the Widget Information for a SubmodelElement
        getSubmodelElementWidget() {
            // console.log('getSubmodelElementWidget', this.selectedNode, this.submodelElementData)
            this.widgetData = []; // Reset the Widget Settings
            if (!this.selectedNode || !(Object.keys(this.selectedNode).length > 0) || !this.selectedNode.path) return;
            let path = this.widgetApiURL + '/api/getwidget/' + this.generateUUIDFromString(this.selectedNode.path);
            let context = 'getting SubmodelElement Widget';
            let disableMessage = false;
            // Send Request to get the Widget Information for the SubmodelElement
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success && response.data && response.data.length > 0) {
                    let widgetData = response.data;
                    widgetData.forEach((element: any) => {
                        if (element.widgetSettings) {
                            element.widgetSettings = JSON.parse(element.widgetSettings);
                        }
                    });
                    this.widgetData = widgetData;
                    // console.log(this.widgetSettings);
                } else {
                    this.widgetData = [];
                }
            });
        },

        // Function to remove a Widget
        removeWidget(widget: any) {
            if (confirm('Are you sure you want to remove the Widget?')) {
                // console.log('removeWidget', widget);
                let path = this.widgetApiURL + '/api/deletewidget/' + widget.widgetId;
                let context = 'removing SubmodelElement Widget';
                let disableMessage = false;
                // Send Request to remove the Widget Information for the SubmodelElement
                this.deleteRequest(path, context, disableMessage).then((response: any) => {
                    if (response.success) {
                        this.widgetData = this.widgetData.filter((element: any) => element.widgetId != widget.widgetId);
                    }
                });
            }
        },

        // Function to update a Widget
        updateWidgetSettings(widget : any) {
            if(widget.showInDash) {
                widget.showInDash = false;
            } else {
                widget.showInDash = true;
            }
            // console.log('updateWidgetSettings', widget);
            // stringify the widgetSettings
            let localWidget = { ...widget };
            localWidget.widgetSettings = JSON.stringify(localWidget.widgetSettings);
            let path = this.widgetApiURL + '/api/updatewidget/' + localWidget.widgetId;
            let content = JSON.stringify(localWidget);
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating SubmodelElement Widget';
            let disableMessage = false;
            // Send Request to update the value of the SubmodelElement
            this.putRequest(path, content, headers, context, disableMessage);
        },
    },
});
</script>