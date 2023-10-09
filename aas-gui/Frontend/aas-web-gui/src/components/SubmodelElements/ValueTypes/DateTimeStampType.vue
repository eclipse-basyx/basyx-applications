<template>
    <v-list-item class="pt-0">
        <v-list-item-title>
            <v-text-field type="text" variant="outlined" density="compact" clearable @keydown.native.enter="updateValue()" v-model="newDateTimeStampValue" :color="dateTimeStampValue.value == newDateTimeStampValue ? '' : 'warning'" persistent-hint :hint="dateTimeStampValue.value == newDateTimeStampValue ? '' : 'Current Value not yet saved.'">
                <!-- Update Value Button -->
                <template v-slot:append-inner>
                    <span class="text-subtitleText">{{ unitSuffix(dateTimeStampValue) }}</span>
                    <v-btn size="small" variant="elevated" color="primary" class="text-buttonText" style="right: -4px" @click.stop="updateValue()">
                        <v-icon>mdi-upload</v-icon>
                    </v-btn>
                </template>
            </v-text-field>
        </v-list-item-title>
        <v-row class="mt-0">
            <!-- Date Picker -->
            <v-col cols="auto">
                <v-date-picker color="primary" @update:modelValue="applyDate" @click:cancel="revertDate()" elevation="1"></v-date-picker>
            </v-col>
            <!-- Time Picker -->
            <v-col cols="auto">
                <v-text-field type="time" variant="solo" v-model="time" @change="applyTime"></v-text-field>
            </v-col>
            <!-- Timezone Picker -->
        </v-row>
    </v-list-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useAASStore } from '@/store/AASDataStore';
import RequestHandling from '../../../mixins/RequestHandling';

import { VDatePicker } from 'vuetify/labs/VDatePicker';
import { useDate } from 'vuetify/labs/date';

export default defineComponent({
    name: 'DateTimeStampType',
    components: {
        RequestHandling, // Mixin to handle the requests to the AAS

        VDatePicker,
    },
    mixins: [RequestHandling],
    props: ['dateTimeStampValue'],

    setup() {
        const aasStore = useAASStore()
        const date = useDate()

        return {
            aasStore, // AASStore Object
            date,
        }
    },

    data() {
        return {
            newDateTimeStampValue: '',          // new value of the property
            time: '',                           // string to store the time
        }
    },

    mounted() {
        if (!this.dateTimeStampValue.value || this.dateTimeStampValue.value == '') {
            this.newDateTimeStampValue = this.createXSDDateString();
        } else {
            this.newDateTimeStampValue = this.dateTimeStampValue.value;
        }
        let date = this.createDateObject(this.newDateTimeStampValue); // create a new Date Object from the given string
        this.time = this.getTimeFromDate(date); // get the time from the date
    },

    watch: {
        // Watch for changes in the selected Node and reset input
        SelectedNode: {
            deep: true,
            handler() {
                this.newDateTimeStampValue = '';
            }
        },

        // Watch for changes in the dateTimeStampValue and update the newDateTimeStampValue if the input field is not focused
        dateTimeStampValue: {
            deep: true,
            handler() {
                if (!this.dateTimeStampValue.value || this.dateTimeStampValue.value == '') {
                    this.newDateTimeStampValue = this.createXSDDateString();
                } else {
                    this.newDateTimeStampValue = this.dateTimeStampValue.value;
                }
            }
        },
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
    },

    methods: {
        // Function to update the value of the property
        updateValue() {
            // console.log("Update Value: " + this.newPropertyValue);
            let path = this.dateTimeStampValue.path + '/$value';
            let content = JSON.stringify(this.newDateTimeStampValue);
            let headers = { 'Content-Type': 'application/json' };
            let context = 'updating ' + this.dateTimeStampValue.modelType + ' "' + this.dateTimeStampValue.idShort + '"';
            let disableMessage = false;
            // Send Request to update the value of the property
            this.patchRequest(path, content, headers, context, disableMessage).then((response: any) => {
                if (response.success) {
                    // this.newPropertyValue = ''; // reset input
                    let updatedDateTimeStampValue = { ...this.dateTimeStampValue }; // copy the stringValue
                    updatedDateTimeStampValue.value = content.toString().replace(/'/g, ''); // update the value of the stringValue
                    this.$emit('updateValue', updatedDateTimeStampValue); // emit event to update the value in the parent component
                }
            });
        },

        // Get the Unit from the EmbeddedDataSpecification of the Property (if available)
        unitSuffix(prop: any) {
            if (prop.embeddedDataSpecifications && prop.embeddedDataSpecifications.length > 0 && prop.embeddedDataSpecifications[0].dataSpecificationContent && prop.embeddedDataSpecifications[0].dataSpecificationContent.unit) {
                return prop.embeddedDataSpecifications[0].dataSpecificationContent.unit;
            } else {
                return '';
            }
        },

        // create XSD Date String for the current date
        createXSDDateString() {
            const date = new Date();

            // Generate the date and time part in the format 'yyyy-mm-ddThh:mm:ss.sss'
            let dateTime = date.toISOString();

            // Get the timezone offset in minutes and convert it to the format '+hh:mm'
            let timezoneOffset = -date.getTimezoneOffset();
            const timezoneSign = timezoneOffset >= 0 ? '+' : '-';
            timezoneOffset = Math.abs(timezoneOffset);
            const timezoneHours = String(Math.floor(timezoneOffset / 60)).padStart(2, '0');
            const timezoneMinutes = String(timezoneOffset % 60).padStart(2, '0');
            const timezone = timezoneSign + timezoneHours + ':' + timezoneMinutes;

            // Add pseudo microseconds and replace the timezone part
            const pseudoMicroseconds = '000';
            dateTime = dateTime.replace('Z', pseudoMicroseconds + timezone);

            // console.log(dateTime);
            return dateTime;
        },

        // Function to create a new Date Object from the given string
        createDateObject(dateString: string) {
            let cleanedTimestamp = dateString.split('[')[0];
            return new Date(cleanedTimestamp);
        },

        // Function to get the time from the given Date Object
        getTimeFromDate(date: Date) {
            let hours = date.getHours().toString().padStart(2, '0');
            let minutes = date.getMinutes().toString().padStart(2, '0');
            let seconds = date.getSeconds().toString().padStart(2, '0');
            return hours + ':' + minutes + ':' + seconds;
        },

        // Function to apply the selected date to the newDateTimeStampValue
        applyDate(date: any) {
            if(!date) return;
            let singleDate = date[0];
            // convert date to string (format: YYYY-MM-DD)
            let year = singleDate.getFullYear();
            let month = (1 + singleDate.getMonth()).toString().padStart(2, '0'); // Months are zero indexed, hence the +1. padStart will add a 0 in front if it's a single digit
            let day = singleDate.getDate().toString().padStart(2, '0'); // padStart will add a 0 in front if it's a single digit
            let dateString = year + '-' + month + '-' + day;
            // console.log('dateString: ', dateString);
            // replace the date in the newDateTimeStampValue
            let tempDateTimeStampValue = this.newDateTimeStampValue.split('T')[1];
            this.newDateTimeStampValue = dateString + 'T' + tempDateTimeStampValue;
        },

        // Function to revert the dateObject to the previous value
        revertDate() {
            // console.log('revertDate: ', this.dateTimeStampValue.value);
            this.newDateTimeStampValue = this.dateTimeStampValue.value;
        },

        // Function to apply the selected time to the newDateTimeStampValue
        applyTime() {
            // console.log('applyTime: ', this.time);
            // replace the time in the newDateTimeStampValue
            let tempDateTimeStampValue = this.newDateTimeStampValue.split('T')[0];
            let tempStampEnd = this.newDateTimeStampValue.split('.')[1];
            this.newDateTimeStampValue = tempDateTimeStampValue + 'T' + this.time + '.' + tempStampEnd;
        },
    },
});
</script>

<style scoped>
:deep()div.v-messages__message {
    color: #FB8C00;
}
</style>
