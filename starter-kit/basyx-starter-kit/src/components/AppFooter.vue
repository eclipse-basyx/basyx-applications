<template>
  <v-footer height="40" app style="position: fixed" color="footer">
    <div style="position: absolute; left: 16px; display: flex; align-items: center;">
      <!-- BaSyx Old Website -->
      <a href="https://eclipse.dev/basyx/" target="_blank" title="BaSyx Website" class="mr-2" style="width: 24px; height: 24px;">
        <img src="@/assets/Icon_BaSyx.svg" width="24" height="24" alt="BaSyx Icon" />
      </a>

      <!-- Footer Links -->
      <v-btn icon="mdi-email" size="x-small" variant="plain" href="mailto:basyx-dev@eclipse.org" aria-label="Send email to the BaSyx developers"></v-btn>
      <v-btn icon="mdi-github" size="x-small" variant="plain" href="https://github.com/eclipse-basyx" target="_blank" aria-label="Open BaSyx GitHub page"></v-btn>
      <v-btn icon="mdi-docker" size="x-small" variant="plain" href="https://hub.docker.com/u/eclipsebasyx" target="_blank" aria-label="Open BaSyx DockerHub page"></v-btn>
      <v-btn icon="mdi-book-open-variant" size="x-small" variant="plain" href="https://wiki.basyx.org/" target="_blank" aria-label="Go to BaSyx documentation"></v-btn>
      <v-btn icon="mdi-cookie" size="x-small" variant="plain" @click="cookieDialog = true" aria-label="Open cookie policy page"></v-btn>
    </div>

    <div class="text-caption text-disabled text-footerText" style="position: absolute; left: 50%; transform: translateX(-50%);">
      &copy; {{ (new Date()).getFullYear() }} <span class="d-none d-sm-inline-block">Eclipse BaSyx™</span>
      —
      <a class="text-decoration-none on-surface" href="https://opensource.org/licenses/mit-license.php" rel="noopener noreferrer" target="_blank">
        MIT License
      </a>
    </div>
    <!-- HTW Logo -->
    <div style="position: absolute; right: 16px; display: flex; align-items: center;">
      <span class="mr-4 text-caption text-disabled text-footerText">Page developed by</span>
      <a href="https://www.htw-berlin.de/" target="_blank" title="HTW Berlin" style="display: flex; align-items: center;">
        <img v-if="isDarkTheme" src="@/assets/Logo_HTW_Dark.svg" width="130" height="16.36" alt="HTW Berlin Logo" />
        <img v-else src="@/assets/Logo_HTW_Light.svg" width="130" height="16.36" alt="HTW Berlin Logo" />
      </a>
    </div>
  </v-footer>
  <!-- Cookie Dialog -->
  <v-dialog v-model="cookieDialog" width="640px" height="auto">
    <v-card>
      <v-card-title>
        <v-row align="center">
          <v-col>
            <span class="text-header">Cookie Policy</span>
          </v-col>
          <v-spacer></v-spacer>
          <v-col cols="auto">
            <v-btn icon="mdi-close" variant="plain" @click="cookieDialog = false"></v-btn>
          </v-col>
        </v-row>
      </v-card-title>
      <v-divider></v-divider>
      <v-card-text>
        <div class="text-normalText">We collect anonymous analytics data to improve our service.</div>
        <div class="text-normalText">You can always change or withdraw your consent later.</div>
        <v-divider class="mt-3 mb-2"></v-divider>
        <v-row align="center" class="my-0">
          <v-col>
            <v-switch v-model="consent" hide-details class="mr-2" :label="'Cookies: ' + ( indeterminate ? 'not set' : (currentConsent ? 'accepted' : 'denied'))" :indeterminate="indeterminate"></v-switch>
          </v-col>
          <v-col>
            <v-btn @click="updateConsent()" variant="tonal">Update Consent</v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useTheme } from 'vuetify'

export default defineComponent({
  name: 'AppFooter',

  setup() {
    const theme = useTheme();

    return {
      theme,
    };
  },

  data() {
    return {
      cookieDialog: false,
      currentConsent: false,
      consent: true,
      indeterminate: false,
    };
  },

  mounted() {
    this.getCookieStatus();
  },

  watch: {
    cookieDialog() {
      if (this.cookieDialog) {
        this.getCookieStatus();
      }
    },
  },

  computed: {
    isDarkTheme() {
      return this.theme.global.current.value.dark;
    },
  },

  methods: {
    getCookieStatus() {
      // console.log('getCookieStatus: ', document.cookie);
      // check if consent cookie is set
      if (document.cookie.indexOf('consent=granted') !== -1) {
        // console.log('consent granted');
        this.consent = true;
        this.currentConsent = true;
        this.indeterminate = false;
      } else if (document.cookie.indexOf('consent=denied') !== -1) {
        // console.log('consent denied');
        this.consent = false;
        this.currentConsent = false;
        this.indeterminate = false;
      } else {
        // console.log('consent not set');
        this.indeterminate = true;
      }
    },

    updateConsent() {
      if (!this.consent) {
        window.gtag('consent', 'update', {
          'ad_storage': 'denied',
          'analytics_storage': 'denied',
        });
        document.cookie = "consent=denied; max-age=31536000"; // 1 year
        this.currentConsent = false;
      } else {
        window.gtag('consent', 'update', {
          'ad_storage': 'granted',
          'analytics_storage': 'granted',
        });
        document.cookie = "consent=granted; max-age=31536000"; // 1 year
        this.currentConsent = true;
      }
    },
  },
});
</script>

<style scoped lang="sass">
  .social-link :deep(.v-icon)
    color: rgba(var(--v-theme-on-background), var(--v-disabled-opacity))
    text-decoration: none
    transition: .2s ease-in-out

    &:hover
      color: rgba(25, 118, 210, 1)
</style>
