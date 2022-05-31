<template>
  <v-app>
    <AppNavigation/>
    <v-main>
      <MainWindow/>
    </v-main>
  </v-app>
</template>

<script>
import AppNavigation from './components/AppNavigation.vue';
import MainWindow from './components/MainWindow.vue';

export default {
  name: 'VWS-GUI-Web-App',

  components: {
    AppNavigation,
    MainWindow,
  },

  data: () => ({
    //
  }),
};
</script>

<style>
html { overflow-y: auto } /* hide main scrollbar when not needed */
.custom-icon {
  fill: currentColor;
}
</style>