<template>
    <v-container fluid class="pa-0">
        <v-card color="elevatedCard" v-if="multiLanguagePropertyObject" class="mt-4">
            <!-- Value(s) of the MultiLanguageProperty -->
            <v-list nav class="bg-elevatedCard pt-0" v-if="multiLanguagePropertyObject.value && multiLanguagePropertyObject.value.length > 0">
                <v-list-item v-for="(value, i) in multiLanguagePropertyObject.value" :key="i">
                    <v-list-item-title class="pt-2">
                        <!-- Input Field containing the Variable Value -->
                        <v-text-field :value="value.text" variant="outlined" density="compact" hide-details clearable readonly>
                            <template v-slot:prepend-inner>
                                <!-- language -->
                                <v-chip label size="x-small" border style="margin-top: 2px">{{ value.language ? value.language : 'no-lang' }}</v-chip>
                            </template>
                        </v-text-field>
                    </v-list-item-title>
                </v-list-item>
            </v-list>
            <!-- Warning when MultiLanguageProperty has no value(s) -->
            <v-list nav class="bg-elevatedCard pt-0" v-else>
                <v-list-item>
                    <v-list-item-title class="pt-2">
                        <v-alert text="MultiLanguageProperty doesn't contain a Value!" density="compact" type="warning" variant="outlined"></v-alert>
                    </v-list-item-title>
                </v-list-item>
            </v-list>
            <!-- TODO: Add Option to edit the MultiLanguageProperty -->
        </v-card>
    </v-container>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import { useStore } from 'vuex';

export default defineComponent({
    name: 'MultiLanguageProperty',
    props: ['multiLanguagePropertyObject'],

    setup() {
        const store = useStore()

        return {
            store, // Store Object
        }
    },

    data() {
        return {
        }
    },

    mounted() {
    },

    watch: {
    },

    computed: {
    },

    methods: {
    },
});
</script>
