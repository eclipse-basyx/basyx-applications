<template>
    <v-container fluid class="pa-0">
        <!-- Header -->
        <v-card class="mb-4">
            <v-card-title>
                <div class="text-subtitle-1">{{ "Time Series Data:" }}</div>
            </v-card-title>
        </v-card>
        <!-- Data Preview Config -->
        <v-card class="mb-4">
            <!-- Title -->
            <v-list nav class="py-0">
                <v-list-item class="pb-0">
                    <template v-slot:title>
                        <div class="text-subtitle-2">{{ "Preview Configuration: " }}</div>
                    </template>
                </v-list-item>
            </v-list>
            <!-- Preview Config -->
            <v-card-text class="pt-1">
                <!-- Segment Selection -->
                <v-select variant="outlined" density="compact" clearable label="Segment" :items="segments" item-title="idShort" item-value="idShort" return-object v-model="selectedSegment"></v-select>
                <!-- Record Selection -->
                <v-row>
                    <v-col cols="12" md="6">
                        <v-select variant="outlined" density="compact" clearable label="time-value" :items="records" item-title="idShort" item-value="idShort" return-object v-model="timeVariable"></v-select>
                    </v-col>
                    <v-col cols="12" md="6">
                        <v-select variant="outlined" density="compact" clearable label="y-value(s)" :items="records" item-title="idShort" item-value="idShort" return-object multiple v-model="yVariables"></v-select>
                    </v-col>
                </v-row>
                <!-- API Token -->
                <v-text-field v-if="segmentType == 'LinkedSegment'" variant="outlined" density="compact" clearable label="API Token" hide-details v-model="apiToken"></v-text-field>
            </v-card-text>
            <v-divider></v-divider>
            <v-list nav class="pr-2 pt-0">
                <v-list-item>
                    <template v-slot:append>
                        <v-btn v-if="segmentType == 'LinkedSegment'" @click="fetchLinkedData()" size="small" class="text-buttonText" color="primary">Fetch Data</v-btn>
                        <v-btn v-if="segmentType == 'InternalSegment'" @click="fetchInternalData()" size="small" class="text-buttonText" color="primary">Fetch Data</v-btn>
                        <v-btn v-if="segmentType == 'ExternalSegment'" @click="fetchExternalData()" size="small" class="text-buttonText" color="primary">Fetch Data</v-btn>
                    </template>
                </v-list-item>
            </v-list>
        </v-card>
        <!-- Data Preview Chart -->
        <v-card>
            <!-- Title -->
            <v-list nav class="py-0">
                <v-list-item class="pb-0">
                    <template v-slot:title>
                        <div class="text-subtitle-2">{{ "Preview Chart: " }}</div>
                    </template>
                </v-list-item>
            </v-list>
            <v-card-text class="pt-1">
                <!-- Chart Type Selection -->
                <v-select variant="outlined" density="compact" clearable label="Chart Type" :items="chartTypes" item-title="name" item-value="name" return-object v-model="selectedChartType"></v-select>
                <!-- Chart Preview -->
                <LineChart v-if="selectedChartType && selectedChartType.id == 1" :chartData="timeSeriesValues" :timeVariable="timeVariable" :yVariables="yVariables"></LineChart>
                <AreaChart v-if="selectedChartType && selectedChartType.id == 2" :chartData="timeSeriesValues" :timeVariable="timeVariable" :yVariables="yVariables"></AreaChart>
                <ScatterChart v-if="selectedChartType && selectedChartType.id == 3" :chartData="timeSeriesValues" :timeVariable="timeVariable" :yVariables="yVariables"></ScatterChart>
                <Histogram v-if="selectedChartType && selectedChartType.id == 4" :chartData="timeSeriesValues" :timeVariable="timeVariable" :yVariables="yVariables"></Histogram>
                <Gauge v-if="selectedChartType && selectedChartType.id == 5" :chartData="timeSeriesValues" :timeVariable="timeVariable" :yVariables="yVariables"></Gauge>
            </v-card-text>
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../mixins/RequestHandling';
import SubmodelElementHandling from '../../mixins/SubmodelElementHandling';

// Widget imports
import LineChart from '../Widgets/LineChart.vue';
import AreaChart from '../Widgets/AreaChart.vue';
import ScatterChart from '../Widgets/ScatterChart.vue';
import Histogram from '../Widgets/Histogram.vue';
import Gauge from '../Widgets/Gauge.vue';

export default defineComponent({
    name: 'TimeSeriesData',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        // Widgets
        LineChart,
        AreaChart,
        ScatterChart,
        Histogram,
        Gauge,
    },
    mixins: [RequestHandling, SubmodelElementHandling],
    props: ['submodelElementData'],

    setup() {
        const theme = useTheme()
        const aasStore = useAASStore()

        return {
            theme, // Theme Object
            aasStore, // AASStore Object
        }
    },

    data() {
        return {
            timeSeriesData: {} as any, // Object to store the data of the time series smt
            segments: [] as Array<any>, // Array to store the segments of the time series smt
            selectedSegment: null as any, // Object to store the selected segment of the time series smt
            records: [] as Array<any>, // Array to store the records of the time series smt
            timeVariable: null as any, // Object to store the selected time variable of the time series smt
            yVariables: [] as Array<any>, // Array to store the selected y variables of the time series smt
            apiToken: '', // API Token for the Time Series Database
            timeSeriesValues: [] as Array<any>, // Array to store the values of the time series smt
            chartTypes: [
                { id: 1, name: 'Line Chart' },
                { id: 2, name: 'Area Chart' },
                { id: 3, name: 'Scatter Chart' },
                { id: 4, name: 'Histogram' },
                { id: 5, name: 'Gauge' },
            ] as Array<any>, // Array to store the chart types
            selectedChartType: null as any, // Object to store the selected chart type
        }
    },

    mounted() {
        this.initializeTimeSeriesData(); // initialize TimeSeriesData Plugin
    },

    computed: {
        // get selected AAS from Store
        SelectedAAS() {
            return this.aasStore.getSelectedAAS;
        },

        // Get the selected Treeview Node (SubmodelElement) from the store
        SelectedNode() {
            return this.aasStore.getSelectedNode;
        },

        // Check if the current Theme is dark
        isDark() {
            return this.theme.global.current.value.dark
        },

        // Determine Segment Type of the selected Segment
        segmentType() {
            if (!this.selectedSegment) {
                return null;
            }
            // create an array of semanticIds from the selected Segment (this.selectedSegment.semanticId.keys)
            let semanticIds = this.selectedSegment.semanticId.keys.map((semanticId: any) => semanticId.value);
            // check if the semanticIds contain the semanticId for InternalSegment
            if (semanticIds.includes('https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1')) {
                return 'InternalSegment';
            }
            // check if the semanticIds contain the semanticId for LinkedSegment
            if (semanticIds.includes('https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1')) {
                return 'LinkedSegment';
            }
            // check if the semanticIds contain the semanticId for ExternalSegment
            if (semanticIds.includes('https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1')) {
                return 'ExternalSegment';
            }
            // return null if no Segment Type was found
            return null;
        },
    },

    methods: {
        // Function to initialize the TimeSeriesData Plugin
        initializeTimeSeriesData() {
            // Check if a Node is selected
            if (Object.keys(this.submodelElementData).length == 0) {
                this.timeSeriesData = {}; // Reset the TimeSeriesData when no Node is selected
                return;
            }
            let timeSeriesData = { ...this.submodelElementData };; // create local copy of the TimeSeriesData
            this.timeSeriesData = timeSeriesData; // set the local copy to the data object
            // get the collection for segments
            const segmentsSMC = timeSeriesData.submodelElements.find((smc: any) => smc.idShort === 'Segments');
            // create an array of segments
            this.segments = segmentsSMC.value;
            // console.log('Segments: ', this.segments)
            // get the collection for metadata
            const metadataSMC = timeSeriesData.submodelElements.find((smc: any) => smc.idShort === 'Metadata');
            // get the collection for records
            const recordsSMC = metadataSMC.value.find((smc: any) => smc.idShort === 'Record');
            // create an array of records
            let records = recordsSMC.value;
            // request the concept descriptions for the records (if they have semanticIds)
            // Create a list of promises
            let promises = records.map((record: any) => {
                return this.getConceptDescription(record).then((response: any) => {
                    // console.log('Response: ', response, ' Record: ', record)
                    // check if the response is not an empty object and if it contains embeddedDataSpecifications
                    if (response && Object.keys(response).length !== 0 && response.embeddedDataSpecifications) {
                        // create new property embeddedDataSpecifications in the record
                        record.embeddedDataSpecifications = response.embeddedDataSpecifications;
                    }
                    return record;
                });
            });

            // Wait for all promises to resolve and then update this.records
            Promise.all(promises).then((updatedRecords) => {
                // console.log('Updated Records: ', updatedRecords)
                this.records = updatedRecords;
            });
        },

        fetchInternalData() {
            if (!this.selectedSegment) {
                return;
            }
            if (!this.timeVariable) {
                return;
            }
            if (this.yVariables.length == 0) {
                return;
            }
            this.getRecordValues();
        },

        // Function to get the record values of an InternalSegment
        getRecordValues() {
            // console.log('Selected Segment: ', this.selectedSegment);
            // get the records submodel element collection
            const recordsSMC = this.selectedSegment.value.find((smc: any) => smc.idShort === 'Records');
            // save the records in an array
            const records = recordsSMC.value;
            // console.log('Records: ', records, ' Time Variable: ', this.timeVariable, ' Y Variables: ', this.yVariables);
            let transformedArray = this.yVariables.filter(yVar =>
                // Check if yVarEntry exists in all records
                records.every((item: any) => item.value.some((entry: any) => entry.idShort === yVar.idShort))
                // display an alert if the yVariable is not available in the records (specify the yVariable name)
                || this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'warning', btnColor: 'buttonText', text: 'y-value ' + yVar.idShort + ' not available in InternalSegment Records!' })
            ).map(yVar => {
                // For each yVariable, go through each item in the original array
                return records.map((item: any) => {
                    // Extract the time value
                    const timeEntry = item.value.find((entry: any) => entry.idShort === this.timeVariable.idShort);
                    // display an alert if the timeVariable is not available the Records
                    if (!timeEntry) {
                        this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'warning', btnColor: 'buttonText', text: 'time-value ' + this.timeVariable.idShort + ' not available in InternalSegment Records!' });
                    }
                    const time = timeEntry ? timeEntry.value : null;

                    // Extract the yVariable value
                    const yVarEntry = item.value.find((entry: any) => entry.idShort === yVar.idShort);
                    const yVarValue = yVarEntry ? yVarEntry.value : null;

                    // Return an object with time and the yVariable value
                    return { time, value: yVarValue };
                });
            });
            // console.log('Transformed Array: ', transformedArray);
            this.timeSeriesValues = transformedArray;
        },

        // Function to fetch the data from the API of the Time Series Database
        fetchLinkedData() {
            // check if a segment is selected
            if (!this.selectedSegment || Object.keys(this.selectedSegment).length == 0) {
                console.warn('No Segment selected');
                return;
            }
            // get the Endpoint from the selected Segment
            const endpoint = this.selectedSegment.value.find((smc: any) => smc.idShort === 'Endpoint').value;
            // get the query from the selected Segment
            const query = this.selectedSegment.value.find((smc: any) => smc.idShort === 'Query').value;
            // console.log('Endpoint: ', endpoint, ' Query: ', query);
            // construct the headers for the request
            let requestHeaders = new Headers();
            requestHeaders.append("Authorization", "Token " + this.apiToken);
            requestHeaders.append("Accept", "application/csv");
            requestHeaders.append("Content-Type", "application/vnd.flux");
            // construct the request
            let path = endpoint;
            let content = query;
            let headers = requestHeaders;
            let context = 'fetching data from Time Series Database';
            let disableMessage = false;
            // send the request
            this.postRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    this.navigationStore.dispatchSnackbar({ status: true, timeout: 2000, color: 'success', btnColor: 'buttonText', text: 'Succesfully retrieved data!' });
                    this.convertInfluxCSVtoArray(response.data);

                }
            });
        },

        convertInfluxCSVtoArray(csvData: any) {
            const lines = csvData.trim().split('\n');
            const datasets = [];
            let currentDataset = [] as Array<any>;
            let currentTable = null as any;

            lines.forEach((line: any, index: number) => {
                const columns = line.split(',');

                if (columns[1] === 'result') {
                    // get the next line by index
                    const nextLine = lines[index + 1];
                    const table = nextLine.split(',')[2];
                    if (currentTable === null) {
                        currentTable = table;
                        // push the header line to the dataset
                        currentDataset.push(line);
                    } else if (table !== currentTable) {
                        // New dataset detected, process the current dataset
                        datasets.push(this.processDataset(currentDataset));
                        currentDataset = [];
                        currentTable = table;
                        // push the header line to the dataset
                        currentDataset.push(line);
                    }
                }

                if (columns[1] !== '_result') {
                    // Skip lines that don't contain data
                    return;
                }

                currentDataset.push(line);
            });

            // Process the last dataset
            if (currentDataset.length > 0) {
                datasets.push(this.processDataset(currentDataset));
            }

            // console.log('Datasets: ', datasets);
            this.timeSeriesValues = datasets;
        },

        processDataset(datasetLines: any) {
            // console.log('Dataset Lines: ', datasetLines)
            const headers = datasetLines[0].split(',');
            const valueIndex = headers.indexOf('_value');
            const timeIndex = headers.indexOf('_time');

            return datasetLines.slice(1).map((line: any) => {
                const columns = line.split(',');
                return {
                    time: columns[timeIndex],
                    value: parseFloat(columns[valueIndex])
                };
            });
        },

        fetchExternalData() {
            if (!this.selectedSegment) {
                return;
            }
            if (!this.timeVariable) {
                return;
            }
            if (this.yVariables.length == 0) {
                return;
            }
            this.getFileData();
        },

        // Function to get the file contents of an ExternalSegments File
        getFileData() {
            // console.log('Selected Segment: ', this.selectedSegment);
            // get the Data File/Blob submodel element
            const dataFile = this.selectedSegment.value.find((smc: any) => smc.idShort === 'Data');
            // determine the path to the file
            let path = dataFile.value
            if (path.startsWith('/')) {
                path = this.submodelElementData.path + '/submodel-elements/Segments.' + this.selectedSegment.idShort + '.Data/attachment';
            }
            // console.log('Path: ', path);
            // get the file contents
            let context = 'retrieving File Contents';
            let disableMessage = true;
            this.getRequest(path, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // console.log('File Contents: ', response.data);
                    this.convertPlainCSVtoArray(response.data);
                }
            });
        },

        convertPlainCSVtoArray(csvData: any) {
            const { headers, data } = this.parseCSV(csvData);
            const timeIndex = headers.indexOf(this.timeVariable.idShort);
            // handle the case where timeIndex is -1
            if (timeIndex === -1) {
                this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'warning', btnColor: 'buttonText', text: 'time-value ' + this.timeVariable.idShort + ' not available in ExternalSegment Data!' });
                return;
            }
            let yIndexes = this.yVariables.map(yVar => headers.indexOf(yVar.idShort));
            // display an alert if the yVariable is not available in the records (specify the yVariable name)
            yIndexes.forEach((yIndex, index) => {
                if (yIndex === -1) {
                    this.navigationStore.dispatchSnackbar({ status: true, timeout: 4000, color: 'warning', btnColor: 'buttonText', text: 'y-value ' + this.yVariables[index].idShort + ' not available in ExternalSegment Data!' });
                }
            });
            // handle the case where yIndexes contains -1 (remove only the -1 values)
            yIndexes = yIndexes.filter(index => index !== -1);
            const datasets = yIndexes.map(yIndex =>
                data.map(row => ({
                    time: row[timeIndex],
                    value: Number(row[yIndex])
                }))
            );
            // console.log('Datasets: ', datasets);
            this.timeSeriesValues = datasets;
        },

        parseCSV(csvString: string) {
            // Splitting by a regular expression to handle both \n and \r\n
            const lines = csvString.split(/\r?\n/);
            const headers = lines[0].split(',').map(header => header.trim()); // Trimming to remove any trailing \r
            // Filter out empty lines and then split each line into columns
            const data = lines.slice(1).filter(line => line).map(line => line.split(','));
            // console.log('Headers: ', headers, ' Data: ', data);
            return { headers, data };
        },
    },
});
</script>