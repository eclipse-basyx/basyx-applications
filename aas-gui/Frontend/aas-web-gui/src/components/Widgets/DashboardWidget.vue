<template>
    <v-container fluid class="pa-0 fill-height">
        <template v-if="selectedWidget && Object.keys(selectedWidget).length > 0">
            <!-- Representation of a single widget -->
            <v-card class="mb-4 fill-height">
                <v-card-title class="px-0">
                    <v-list-item>
                        <!-- Widget Title -->
                        <v-list-item-title class="text-primary text-subtitle-1">{{ selectedWidget.widgetSettings.widgetTitle }}</v-list-item-title>
                        <template v-slot:append>
                            <!-- Show Widget in Dashboard -->
                            <v-tooltip :open-delay="600" location="start">
                                <template v-slot:activator="{ props }">
                                    <v-btn :icon="selectedWidget.showInDash ? 'mdi-eye' : 'mdi-eye-off'" variant="plain" v-bind="props" size="small" @click="updateWidgetSettings(selectedWidget)"></v-btn>
                                </template>
                                <span>{{ 'Show/Hide Widget in Dashboard' }}</span>
                            </v-tooltip>
                        </template>
                    </v-list-item>
                </v-card-title>
                <v-divider></v-divider>
                <!-- List of all available Widgets -->
                <v-card-text>
                    <Button             v-if="selectedWidget.widgetSettings.widgetType      == 'inputElement'   && selectedWidget.widgetSettings.interactionElement == 'Button'"    :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></Button>
                    <Switch             v-else-if="selectedWidget.widgetSettings.widgetType == 'inputElement'   && selectedWidget.widgetSettings.interactionElement == 'Switch'"    :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></Switch>
                    <Lamp               v-else-if="selectedWidget.widgetSettings.widgetType == 'inputElement'   && selectedWidget.widgetSettings.interactionElement == 'Lamp'"      :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></Lamp>
                    <DisplayField       v-else-if="selectedWidget.widgetSettings.widgetType == 'outputElement'  && selectedWidget.widgetSettings.textElement == 'Display Field'"    :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></DisplayField>
                    <InputField         v-else-if="selectedWidget.widgetSettings.widgetType == 'outputElement'  && selectedWidget.widgetSettings.textElement == 'Input Field'"      :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></InputField>
                    <SubmodelEntrypoint v-else-if="selectedWidget.widgetSettings.widgetType == 'plugin'"                                                                            :submodelElementData="selectedWidget.submodelElementData" :selectedNode="selectedNode"  :widgetSettings="selectedWidget.widgetSettings"></SubmodelEntrypoint>
                    <LineChart          v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Line Chart'"         :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></LineChart>
                    <AreaChart          v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Area Chart'"         :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></AreaChart>
                    <BarChart           v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Bar Chart'"          :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></BarChart>
                    <RadialChart        v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Radial Chart'"       :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></RadialChart>
                    <ColumnChart        v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Column Chart'"       :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></ColumnChart>
                    <DonutChart         v-else-if="selectedWidget.widgetSettings.widgetType == 'chart'          && selectedWidget.widgetSettings.chartType == 'Donut Chart'"        :submodelElementData="selectedWidget.submodelElementData"                               :widgetSettings="selectedWidget.widgetSettings"></DonutChart>
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
import DonutChart from './DonutChart.vue'

export default defineComponent({
    name: 'DashboardWidget',
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

    props: ['selectedWidget', 'selectedNode'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            addWidget: false,
        }
    },

    mounted() {
    },

    watch: {
    },

    computed: {
        // get Widget Api URL from Store
        widgetApiURL() {
            return this.store.getters.getWidgetApiURL;
        },
    },

    methods: {
        // Function to show/hide a Widget in the Dashboard
        updateWidgetSettings(widget: any) {
            if (widget.showInDash) {
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
            this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    this.$emit('removeWidget', widget);
                }
            });
        },
    },
});
</script>