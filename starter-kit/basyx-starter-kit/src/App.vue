<template>
  <v-app>
    <v-main>
      <router-view />
    </v-main>
    <!-- Cookies Panel -->
    <v-snackbar v-model="showConsentSnackbar" :timeout="-1" theme="dark">
      <div>We collect anonymous analytics data to improve our service.</div>
      <div>By clicking "Accept", you agree to our use of cookies.</div>
      <template v-slot:actions>
        <v-btn @click="acceptConsent(false)" variant="outlined" color="grey-darken-3" class="mr-2">Decline</v-btn>
        <v-btn @click="acceptConsent(true)" variant="elevated" color="grey-darken-3">Accept</v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

declare global {
  interface Window {
    gtag: any;
  }
}

export default defineComponent({
  name: 'App',

  data() {
    return {
      showConsentSnackbar: document.cookie.indexOf('consent=granted') === -1 && document.cookie.indexOf('consent=denied') === -1,
    };
  },

  mounted() {
  },

  methods: {
    acceptConsent(consent: boolean) {
      this.showConsentSnackbar = false;
      if (!consent) {
        window.gtag('consent', 'update', {
          'ad_storage': 'denied',
          'analytics_storage': 'denied',
        });
        document.cookie = "consent=denied; max-age=31536000"; // 1 year
      } else {
        window.gtag('consent', 'update', {
          'ad_storage': 'granted',
          'analytics_storage': 'granted',
        });
        document.cookie = "consent=granted; max-age=31536000"; // 1 year
      }
    },
  },
});
</script>

<style>
div.v-switch__track {
  background-color: #424242 !important;
}
</style>
