<template>
  <v-app>
    <!-- App Navigation and it's sub-Components (AASList, etc.) -->
    <AppNavigation />
    <v-main style="padding-top: 33px">
      <!-- App Content (eg. MainWindow, etc.) -->
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';
import AppNavigation from './components/AppNavigation/AppNavigation.vue';

export default defineComponent({
  name: 'App',
  components: {
    AppNavigation,
  },
});
</script>
