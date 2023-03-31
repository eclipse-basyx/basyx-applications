<template>
    <v-container fluid class="pa-0">
        <v-dialog v-model="configuratorDialog" width="600px">
            <!-- Button to open the Dialog -->
            <template v-slot:activator="{ props }">
                <v-btn v-bind="props" append-icon="mdi-widgets">Add Widget</v-btn>
            </template>
            <v-card>
                <!-- Configuration Dialog -->
                <v-card-title>
                    <v-row justify="space-between" align="center">
                        <!-- Title -->
                        <v-col cols="auto" class="pl-3">
                            <span class="text-subtile-1 text-primary">Add Widget</span>
                        </v-col>
                        <!-- Widget Weight -->
                        <v-col cols="auto" class="pr-2">
                            <v-text-field v-model="widgetWeight" variant="outlined" density="compact" hide-details style="width: 250px" type="number">
                                <template v-slot:prepend>
                                    <div>Widget Weight</div>
                                    <!-- Info Icon -->
                                    <v-tooltip location="bottom">
                                        <template v-slot:activator="{ props }">
                                            <v-icon v-bind="props" class="ml-1" size="x-small" style="margin-top: 3px">mdi-information-outline</v-icon>
                                        </template>
                                        <span>Set the Weight of the Widget. The lower the Weight, the higher it will be displayed in the Dashboard.</span>
                                    </v-tooltip>
                                </template>
                            </v-text-field>
                        </v-col>
                    </v-row>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text class="px-3 py-3">
                    <v-form v-model="form">
                        <!-- Set Widget Title -->
                        <v-text-field label="Widget Title" density="compact" variant="outlined" v-model="widgetTitle" class="mb-5" :rules="titlerules" hide-details="auto"></v-text-field>
                        <!-- Select Type of Widget -->
                        <v-select label="Select Widget Type" :items="widgetTypes" density="compact" variant="outlined" v-model="widgetType" hide-details return-object item-title="name" item-value="value"></v-select>
                        <v-divider v-if="widgetType.value && widgetType.value != 'plugin'" class="my-5"></v-divider>
                        <!-- Select Input Element if InputElement is selected -->
                        <v-select v-if="widgetType.value == 'inputElement'" label="Select Element" :items="interactionElements" density="compact" variant="outlined" v-model="interactionElement" hide-details></v-select>
                        <!-- Select Type of Chart if Charts and Diagrams is selected -->
                        <v-select v-if="widgetType.value == 'chart'" label="Select Type of Chart" :items="chartTypes" v-model="chartType" variant="outlined" density="compact"  class="mt-5" hide-details></v-select>
                        <!-- Select Textfield Element if Textfield is selected -->
                        <v-select v-if="widgetType.value == 'outputElement'" label="Select Element" :items="textElements" density="compact" variant="outlined" v-model="textElement" hide-details></v-select>
                        <!-- Select multiple Data for Collection -->
                        <v-select v-if="submodelElementData.modelType.name == 'SubmodelElementCollection' && ((chartType != 'Radial Chart' && chartType != 'Donut Chart' && chartType != '') || widgetType.value == 'outputElement' || widgetType.value == 'inputElement')" label="Select Data" :items="submodelElementData.value" item-title="idShort" item-value="idShort" chips multiple density="compact" variant="outlined" v-model="chartCollectionValues" return-object class="mt-5" hide-details></v-select>
                        <!-- Textfield to set range of Area- and Line-Chart -->
                        <v-text-field v-if="widgetType.value == 'chart' && (chartType == 'Area Chart' || chartType == 'Line Chart')" label="Set Range" v-model="chartRange" variant="outlined" density="compact" class="mt-5" hide-details type="number" suffix="ms"></v-text-field>
                        <!-- Select special Data for Radial Chart -->
                        <v-select v-if="widgetType.value == 'chart' && submodelElementData.modelType.name == 'SubmodelElementCollection' && (chartType == 'Radial Chart' || chartType == 'Donut Chart')" label="Select Target Value" :items="submodelElementData.value" item-title="idShort" item-value="idShort" density="compact" variant="outlined" v-model="radialChartTarget" return-object class="mt-5" hide-details></v-select>
                        <v-select v-if="widgetType.value == 'chart' && submodelElementData.modelType.name == 'SubmodelElementCollection' && (chartType == 'Radial Chart' || chartType == 'Donut Chart')" label="Select Actual Value" :items="submodelElementData.value" item-title="idShort" item-value="idShort" density="compact" variant="outlined" v-model="radialChartCurrent" return-object class="mt-5" hide-details></v-select>
                        <v-select v-if="widgetType.value == 'chart' && submodelElementData.modelType.name == 'SubmodelElementCollection' && chartType == 'Donut Chart'" label="Select Maximal Value" :items="submodelElementData.value" item-title="idShort" item-value="idShort" density="compact" variant="outlined" v-model="donutChartMax" return-object class="mt-5" hide-details></v-select>
                        
                        <v-divider v-if="chartCollectionValues.length > 0 || (widgetType.value == 'chart' && submodelElementData.modelType.name == 'Property' && chartType != '') || (radialChartTarget != '' && radialChartCurrent != '') || (widgetType.value == 'inputElement' && submodelElementData.modelType.name == 'Property')" class="my-5"></v-divider>

                        <!-- Option to Change Names of Data to be displayed for Collection -->
                        <template v-if="chartCollectionValues.length > 0">
                            <v-row v-for="entry in chartNamesUnits" :key="entry.id">
                                <!-- DisplayName -->
                                <v-col cols="12" :md="widgetType.value == 'inputElement' ? 12 : 6" :class="widgetType.value == 'inputElement' ? '' : 'pr-0'">
                                    <v-text-field v-model="entry.name" label="Displayed Name" variant="outlined" density="compact" :class="widgetType.value == 'inputElement' ? '' : 'mr-2'" hide-details clearable></v-text-field>
                                </v-col>
                                <!-- Unit -->
                                <v-col cols="12" md="6" class="pl-0" v-if="widgetType.value != 'inputElement'">
                                    <v-text-field v-model="entry.unit" label="Unit" variant="outlined" density="compact" class="ml-2" hide-details clearable></v-text-field>
                                </v-col>
                            </v-row>
                        </template>
                        <!-- Option to Change Names of Data to be displayed for Property -->
                        <v-row v-else-if="submodelElementData.modelType.name == 'Property' && (chartType != '' || widgetType.value == 'outputElement' || widgetType.value == 'inputElement')">
                            <!-- DisplayName -->
                            <v-col cols="12" :md="widgetType.value == 'inputElement' ? 12 : 6" :class="widgetType.value == 'inputElement' ? '' : 'pr-0'">
                                <v-text-field label="Displayed Name" v-model="chartNamesUnits[0].name" variant="outlined" density="compact" :class="widgetType.value == 'inputElement' ? '' : 'mr-2'" hide-details clearable></v-text-field>
                            </v-col>
                            <!-- Unit -->
                            <v-col cols="12" md="6" class="pl-0" v-if="widgetType.value != 'inputElement'">
                                <v-text-field label="Unit" v-model="chartNamesUnits[0].unit" variant="outlined" density="compact" class="ml-2" :disabled="setUnit" hide-details clearable></v-text-field>
                            </v-col>
                        </v-row>
                        <!-- Option to Change Names of Data to be displayed for Radial Chart -->
                        <v-row v-else-if="((chartType == 'Radial Chart' || chartType == 'Donut Chart') && radialChartTarget != '' && radialChartCurrent != '')">
                            <!-- DisplayName -->
                            <v-col cols="12" md="6" class="pr-0">
                                <v-text-field label="Displayed Name" v-model="chartNamesUnits[0].name" variant="outlined" density="compact" class="mr-2" hide-details clearable></v-text-field>
                            </v-col>
                            <!-- Unit -->
                            <v-col cols="12" md="6" class="pl-0">
                                <v-text-field label="Unit" v-model="chartNamesUnits[0].unit" variant="outlined" density="compact" class="ml-2" :disabled="setUnit" hide-details clearable></v-text-field>
                            </v-col>
                        </v-row>
                    </v-form>
                </v-card-text>
                <v-divider></v-divider>
                <v-card-actions>
                    <!-- Cancel Button -->
                    <v-btn variant="tonal" @click="configuratorDialog = false">Cancel</v-btn>
                    <v-spacer></v-spacer>
                    <!-- Add Widget -->
                    <v-btn @click="addWidget()" color="primary" variant="elevated" :disabled="!form">Add Widget</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useStore } from 'vuex';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

export default defineComponent({
    name: 'WidgetConfigurator',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS
        SubmodelElementHandling, // Mixin to handle the SubmodelElements
    },
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['submodelElementData'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
            configuratorDialog: false, // State Variable for the Configurator Dialog
            buildDialog: false, // State Variable for the Preview Dialog

            widgetTitle: "" as string, // Title of the Widget

            widgetType: "" as any, // Type of the Widget (Chart/Diagram or Button/Switch/Lamp)

            chartType: "" as string, // Type of the Chart (Line Chart, Bar Chart, Radial Chart, etc.)

            interactionElements: ["Button", "Switch", "Lamp"] as string[], // List of Interaction Elements when widgetType is Interaction Button/Switch/Lamp
            interactionElement: "" as string, // Selected Interaction Element

            textElements: ["Display Field", "Input Field"] as string[], // List of Text Elements when widgetType is Text Field
            textElement: "" as string, // Selected Text Element

            includePlugin: false as boolean, // State Variable for Plugin Widgets (Include Plugin as Widget)

            chartCollectionValues:  [] as any, // Selected Values when using a SubmodelElementCollection as Data Source for a Chart
            radialChartTarget:      "" as any, // Selected Value when using a SubmodelElementCollection as Data Source for a Radial Chart (Target Value)
            radialChartCurrent:     "" as any, // Selected Value when using a SubmodelElementCollection as Data Source for a Radial Chart (Current Value)

            donutChartMax: "" as any,

            chartRange: 60000 as number, // Range of the Chart (in ms)

            chartNamesUnits: [] as any, // List of Names and Units for the Data to be displayed in the chart

            widgetWeight: 0 as number, // Weight of the Widget (sorting in the Dashboard)

            displayName: "" as any,
            unit: "" as any,

            setUnit: false,

            titlerules: [ // Rules for the Title Validation
                (value: any) => {
                    if (value?.length >= 3) return true;
                    return 'Title must be at leat 3 characters long';
                },
            ],

            form: false, // State Variable for the Form Validation
            
            PluginSemanticIDs: [ // List of SemanticId's which represent Plugins usable as Widgets
                'http://htw-berlin.de/smc_statemachine'
            ],
        }
    },

    mounted() {
    },

    watch: {
        // Watch for changes in the configuratorDialog and clear the old widget Settings if the dialog is closed
        configuratorDialog() {
            if (!this.configuratorDialog) {
                this.chartCollectionValues  = [];
                this.widgetTitle            = "";
                this.widgetWeight           = 0;
                this.chartRange             = 60000;
                this.widgetType             = "";
                this.chartType              = "";
                this.interactionElement     = "";
                this.radialChartTarget      = "";
                this.radialChartCurrent     = "";
                this.donutChartMax          = "";
                this.widgetType             = "";
                this.chartNamesUnits        = [];
            }
        },

        chartCollectionValues(values) {
            this.chartNamesUnits = [];
            // iterate over values and prefill the displayNames and units
            values.forEach((value: any, index: number) => {
                this.chartNamesUnits.push({
                    id: index,
                    name: value.idShort,
                    unit: '',
                });
            });
            // console.log('ChartNames and ChartUnits: ', this.chartNamesUnits);
        },

        widgetType() {
            if(this.widgetType.value == 'outputElement' || this.widgetType.value == 'inputElement') { // If the Widget Type is an Output Element
                let entry = {
                    id: 0,
                    name: this.submodelElementData.idShort,
                    unit: '',
                }
                this.chartNamesUnits[0] = entry
            }
        },

        chartType() {
            if(this.submodelElementData.modelType.name == 'Property') { // If the SubmodelElement is a Property
                let entry = {
                    id: 0,
                    name: this.submodelElementData.idShort,
                    unit: '',
                }
                this.chartNamesUnits[0] = entry
            } else if(this.chartType == 'Radial Chart' || this.chartType == 'Donut Chart') { // If the Chart Type is a Radial Chart
                this.chartNamesUnits = [];
                let entry = {
                    id: 0,
                    name: this.radialChartCurrent,
                    unit: '',
                };
                this.chartNamesUnits[0] = entry;
            }
        },
    },

    computed: {
        // get Registry Server URL from Store
        registryServerURL() {
            return this.store.getters.getRegistryURL;
        },

        // get Widget Api URL from Store
        widgetApiURL() {
            return this.store.getters.getWidgetApiURL;
        },

        // get selected AAS from Store
        SelectedAAS() {
            return this.store.getters.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.store.getters.getSelectedNode;
        },

        // returs the available widgetTypes
        widgetTypes() {
            let widgetTypes = [
                { id: 1, name: "Button/Switches/Lamps", value: "inputElement" },
                { id: 2, name: "Text Fields",           value: "outputElement" },
                { id: 3, name: "Charts and Diagrams",   value: "chart" },
            ] as Array<any>
            if (this.hasPlugin()) widgetTypes.push({ id: 4, name: "Plugin", value: "plugin" });
            return widgetTypes;
        },

        chartTypes(){
            let chartTypes = [
                "Area Chart", "Bar Chart", "Line Chart", "Column Chart"
            ] as Array<any>
            if (this.submodelElementData.modelType.name == 'SubmodelElementCollection') chartTypes.push("Radial Chart", "Donut Chart");
            return chartTypes;
        },
    },

    methods: {
        // Function to add a new Widget to the SubmodelElement
        addWidget() {
            let widgetObject = {
                widgetTitle: this.widgetTitle,
                widgetType: this.widgetType.value,
                interactionElement: this.interactionElement,
                textElement: this.textElement,
                chartNamesUnits: this.chartNamesUnits,
            } as any;
            if(this.chartCollectionValues.length > 0){
                let idShorts = [] as any;
                this.chartCollectionValues.forEach((element: any) => {
                    idShorts.push(element.idShort);
                });
                widgetObject['idShorts'] = idShorts;
            }
            if(this.widgetType.value == "chart" && (this.chartType == "Area Chart" || this.chartType == "Line Chart")) {
                let chartRange = this.chartRange as any;
                if(chartRange <= 0) chartRange = undefined;
                widgetObject['chartRange'] = chartRange;
            }
            if(this.radialChartTarget != ""){
                widgetObject['targetValue'] = this.radialChartTarget.idShort;
            }
            if(this.radialChartCurrent != ""){
                widgetObject['actualValue'] = this.radialChartCurrent.idShort;
            }
            if(this.donutChartMax != ''){
                widgetObject['maxValue'] = this.donutChartMax.idShort;
            }
            if(this.interactionElement.length > 0){
                widgetObject['interactionElement'] = this.interactionElement
            } else if(this.chartType.length > 0){
                widgetObject['chartType'] = this.chartType;
            }
            let widgetObjectJSON = JSON.stringify(widgetObject);
            let path = this.widgetApiURL + '/api/addwidget';
            // console.log(this.SelectedAAS, this.SelectedNode);
            let content = JSON.stringify({
                widgetId: this.UUID(),
                smSmeID: this.generateUUIDFromString(this.SelectedNode.path),
                smSmePath: this.SelectedNode.path,
                smSmeName: this.SelectedNode.idShort,
                shellEndpoint: this.SelectedAAS.endpoints[0].address,
                showInDash: true,
                widgetWeight: this.widgetWeight,
                widgetSettings: widgetObjectJSON,
            });
            let headers = { 'Content-Type': 'application/json' };
            let context = 'putting SubmodelElement Widget';
            let disableMessage = false;
            // Send Request to update the value of the SubmodelElement
            this.putRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if(response.success) {
                    // trigger request in _WidgetEntrypoint.vue to update the widget
                    this.store.dispatch('dispatchUpdateWidget', true);
                    // close the configurator dialog
                    this.configuratorDialog = false;
                }
            });
        },

        // Function to check if a Plugin exists for the selected SubmodelElement
        hasPlugin() {
            let hasPlugin = false;
            this.PluginSemanticIDs.forEach((semanticId: any) => {
                // check if the submodelElementData contains a semanticId
                if(Object.keys(this.submodelElementData).length > 0 && this.submodelElementData.semanticId && this.submodelElementData.semanticId.keys && this.submodelElementData.semanticId.keys.length > 0) {
                    // check if the semanticId matches any of the PluginSemanticID's
                    this.submodelElementData.semanticId.keys.forEach((key: any) => {
                        if (key.value === semanticId) {
                            hasPlugin = true;
                        }
                    });
                }
            });
            return hasPlugin;
        },
    },
});
</script>