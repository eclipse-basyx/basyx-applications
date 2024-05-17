<template>
    <v-switch v-model="isDarkTheme" @update:modelValue="toggleTheme" hide-details density="compact" inset false-icon="mdi-weather-sunny" true-icon="mdi-weather-night" :color="isDarkTheme ? '#000' : '#fff'" style="margin-top: 3px" aria-label="theme switch"></v-switch>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify';
import { useAppStore } from '@/stores/app'

export default defineComponent({
    name: 'Theme',

    setup() {
        const theme = useTheme();
        const appStore = useAppStore();

        return {
            theme,
            appStore,
        };
    },

    data() {
        return {
            isDarkTheme: false,
        };
    },

    mounted() {
        // get theme from local storage
        const theme = localStorage.getItem('theme');
        if (theme) {
            this.isDarkTheme = theme === 'dark';
            this.theme.global.name.value = theme;
        } else {
            // get user prefered theme
            const userPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
            if (userPrefersDark) {
                this.isDarkTheme = userPrefersDark;
                this.theme.global.name.value = 'dark';
            } else {
                this.isDarkTheme = false;
                this.theme.global.name.value = 'light';
            }
        }
    },

    methods: {
        toggleTheme() {
            this.theme.global.name.value = this.isDarkTheme ? 'dark' : 'light';
            // save theme in local storage
            localStorage.setItem('theme', this.theme.global.name.value);
        },
    },
});
</script>
