<template>
    <v-container fluid class="pa-0">
        <!-- Button/Switches/Lamps -->
        <v-row class="px-4 pt-4">
            <v-col class="px-2 pt-2" cols="12" xs="6" sm="6" md="3" lg="3" xl="2" v-for="widget in inputElementWidgets" :key="widget.widgetId">
                <DashboardWidget v-if="widget.submodelElementData && Object.keys(widget.submodelElementData).length > 0" :selectedWidget="widget" :selectedNode="SelectedNode(widget)" @removeWidget="removeWidget"></DashboardWidget>
            </v-col>
        </v-row>
        <!-- Display Fields -->
        <v-row class="px-4 pt-0">
            <v-col class="px-2 pt-0" cols="12" xs="12" sm="12" md="6" lg="4" xl="3" v-for="widget in displayFieldWidgets" :key="widget.widgetId">
                <DashboardWidget v-if="widget.submodelElementData && Object.keys(widget.submodelElementData).length > 0" :selectedWidget="widget" :selectedNode="SelectedNode(widget)" @removeWidget="removeWidget"></DashboardWidget>
            </v-col>
        </v-row>
        <!-- Charts and Diagrams -->
        <v-row class="px-4 py-0">
            <v-col class="px-2 py-0" cols="12" xs="12" sm="12" md="6" lg="6" xl="4" v-for="widget in chartWidgets" :key="widget.widgetId">
                <DashboardWidget v-if="widget.submodelElementData && Object.keys(widget.submodelElementData).length > 0" :selectedWidget="widget" :selectedNode="SelectedNode(widget)" @removeWidget="removeWidget"></DashboardWidget>
            </v-col>
        </v-row>
        <!-- Plugins -->
        <v-row class="px-4 pt-0 pb-4">
            <v-col class="px-2 pt-0" cols="12" xs="12" sm="12" md="6" lg="6" xl="4" v-for="widget in pluginWidgets" :key="widget.widgetId">
                <DashboardWidget v-if="widget.submodelElementData && Object.keys(widget.submodelElementData).length > 0" :selectedWidget="widget" :selectedNode="SelectedNode(widget)" @removeWidget="removeWidget"></DashboardWidget>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useNavigationStore } from '@/store/NavigationStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '@/mixins/SubmodelElementHandling';

import DashboardWidget from '@/components/Widgets/DashboardWidget.vue';

export default defineComponent({
    name: 'Dashboard',
    components: {
        RequestHandling, // Mixin to handle the requests to the Registry Server
        SubmodelElementHandling, // Mixin to handle the SubmodelElements

        DashboardWidget, // Visualization Widgets Dashboard Entrypoint Component
    },
    mixins: [RequestHandling, SubmodelElementHandling],

    setup() {
        const navigationStore = useNavigationStore()

        return {
            navigationStore, // NavigationStore Object
        }
    },

    data() {
        return {
            inputElementWidgets: [] as any, // Array containing all InputElement Widgets
            displayFieldWidgets: [] as any, // Array containing all DisplayField Widgets
            chartWidgets: [] as any, // Array containing all Chart Widgets
            pluginWidgets: [] as any, // Array containing all Plugin Widgets
            requestInterval: null as any, // interval to send requests to the AAS
        }
    },

    mounted() {
        this.getAllWidgets();
    },

    beforeUnmount() {
        clearInterval(this.requestInterval); // clear old interval
    },

    computed: {
        // get Widget Api URL from Store
        widgetApiURL() {
            return this.navigationStore.getWidgetApiURL;
        },
    },

    methods: {
        // Function to get all Widgets from the DB
        getAllWidgets() {
            let path = this.widgetApiURL + '/api/getallwidgets';
            let context = 'getting all Widgets';
            let disableMessage = false;
            // Send Request to get all Widgets
            this.getRequest(path, context, disableMessage).then((response: any) => {
                // console.log(response)
                if (response.success && response.data && response.data.length > 0) {
                    response.data.forEach((element: any) => {
                        element.widgetSettings = JSON.parse(element.widgetSettings);
                    });
                    // console.log('getAllWidgets: ', response.data);
                    // Filter Widgets by Type
                    this.inputElementWidgets = response.data.filter((element: any) => {
                        return element.widgetSettings.widgetType === 'inputElement';
                    });
                    this.displayFieldWidgets = response.data.filter((element: any) => {
                        return element.widgetSettings.widgetType === 'outputElement';
                    });
                    this.chartWidgets = response.data.filter((element: any) => {
                        return element.widgetSettings.widgetType === 'chart';
                    });
                    this.pluginWidgets = response.data.filter((element: any) => {
                        return element.widgetSettings.widgetType === 'plugin';
                    });
                    clearInterval(this.requestInterval); // clear old interval
                    // create new interval
                    this.requestInterval = setInterval(() => {
                        this.getSubmodelElementData(this.inputElementWidgets);
                        this.getSubmodelElementData(this.displayFieldWidgets);
                        this.getSubmodelElementData(this.chartWidgets);
                        this.getSubmodelElementData(this.pluginWidgets);
                    }, 1000);
                } else {
                    // console.log('No Widgets found');
                    this.inputElementWidgets = [];
                    this.displayFieldWidgets = [];
                    this.chartWidgets = [];
                    this.pluginWidgets = [];
                }
            });
        },

        // Function to get the value(s) of a SubmodelElement which is used in a Widget
        getSubmodelElementData(widgetArray : any) {
            // console.log('getSubmodelElementData:', widgetArray);
            widgetArray.forEach((element: any) => {
                let path = element.shellEndpoint + '/' + element.smSmePath;
                let context = 'getting submodelElementData for Widget';
                let disableMessage = false;
                // Send Request to get all Widgets
                this.getRequest(path, context, disableMessage).then((response: any) => {
                    // console.log(response)
                    if (response.success && response.data) {
                        // console.log('SubmodelElementData found');
                        response.data.timestamp = this.formatDate(new Date());
                        element.submodelElementData = response.data;
                    } else {
                        // console.log('No SubmodelElementData found');
                        element.submodelElementData = {};
                    }
                });
            });
        },

        // Function which returns the selected Node for a Widget
        SelectedNode(widget: any) {
            // console.log('SelectedNode: ', { path: widget.shellEndpoint + '/' + widget.smSmePath, idShort: widget.smSmeName });
            return {
                path: widget.smSmePath,
                pathFull: widget.shellEndpoint + '/' + widget.smSmePath,
                idShort: widget.smSmeName
            }
        },

        // Function to remove a Widget from the Dashboard
        removeWidget(widget: any) {
            // console.log('removeWidget: ', widget);
            let widgetType = widget.widgetSettings.widgetType;
            if (widgetType === 'inputElement') {
                this.inputElementWidgets = this.inputElementWidgets.filter((element: any) => {
                    return element.widgetId !== widget.widgetId;
                });
            } else if (widgetType === 'outputElement') {
                this.displayFieldWidgets = this.displayFieldWidgets.filter((element: any) => {
                    return element.widgetId !== widget.widgetId;
                });
            } else if (widgetType === 'chart') {
                this.chartWidgets = this.chartWidgets.filter((element: any) => {
                    return element.widgetId !== widget.widgetId;
                });
            } else if (widgetType === 'plugin') {
                this.pluginWidgets = this.pluginWidgets.filter((element: any) => {
                    return element.widgetId !== widget.widgetId;
                });
            }
        },
    },
});
</script>
