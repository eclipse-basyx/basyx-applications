<template>
  <v-list-item>
    <v-list-item>
      <v-list-item-title class="text-subtitle-2">
        User
      </v-list-item-title>
      <v-label>{{ email }}</v-label>
    </v-list-item>
    <v-btn @click="logOut()"  color="primary" variant="outlined">Log out</v-btn>
  </v-list-item>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {MetadataService, UserManager} from 'oidc-client-ts';
import {OIDC_CONFIG} from '@/constants/oidc-config';

export default defineComponent({
  name: 'LdLogout',

  setup() {
    return {}
  },

  data() {
    return {
      email: ''
    }
  },

  mounted() {
    const userManager = new UserManager(OIDC_CONFIG);
    userManager.getUser()
      .then((user) => {
        this.email = user?.profile.email ?? '';
      });
  },

  computed: {},

  methods: {
    logOut() {
      const userManager = new UserManager(OIDC_CONFIG);
      userManager.metadataService.getMetadata().then((metadata) => {
        //@ts-ignore
        metadata.end_session_endpoint = metadata.logout_endpoint;
        userManager.signoutRedirect();
      })
    }
  },
});
</script>
