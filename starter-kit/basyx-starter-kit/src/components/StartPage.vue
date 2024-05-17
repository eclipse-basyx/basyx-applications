<template>
  <v-container class="fill-height">
    <v-responsive class="align-centerfill-height mx-auto" max-width="900">
      <v-img class="mb-4" height="150" src="@/assets/Icon_BaSyx.svg" alt="BaSyx Logo" />

      <div class="text-center">
        <div class="text-body-2 font-weight-light mb-n1 text-normalText">Welcome to</div>

        <h1 class="text-h2 font-weight-bold text-title">Eclipse BaSyx™</h1>
      </div>

      <div class="py-4" />

      <v-row>
        <v-col cols="12">
          <v-card class="py-4" color="surface-variant" prepend-icon="mdi-rocket-launch-outline" rounded="lg" variant="outlined" to="/get-started/introduction">
            <v-img src="@/assets/feature.svg" width="90px" height="90px" contain alt="Get Started abstract Image" loading="lazy" style="position: absolute; right: 20px; top: 12px" />

            <template #title>
              <h2 class="text-h5 font-weight-bold">Get started</h2>
            </template>

            <template #subtitle>
              <div class="text-subtitle-1">
                Start a new project with BaSyx - <v-kbd>Digital Twins</v-kbd> tailored to your needs.
              </div>
            </template>

            <v-overlay opacity=".12" scrim="primary" contained model-value persistent />
          </v-card>
        </v-col>

        <v-col cols="6">
          <v-card append-icon="mdi-open-in-new" class="py-4" color="surface-variant" href="https://wiki.basyx.org/" prepend-icon="mdi-text-box-outline" rel="noopener noreferrer" rounded="lg" subtitle="Learn about all things BaSyx in our documentation." target="_blank" title="Documentation" variant="text">
            <v-overlay opacity=".06" scrim="primary" contained model-value persistent />
          </v-card>
        </v-col>

        <v-col cols="6">
          <v-card append-icon="mdi-open-in-new" class="py-4" color="surface-variant" href="https://wiki.basyx.org/en/latest/content/user_documentation/basyx_components/index.html" prepend-icon="mdi-star-circle-outline" rel="noopener noreferrer" rounded="lg" subtitle="Explore available component and SDK features." target="_blank" title="Features" variant="text">
            <v-overlay opacity=".06" scrim="primary" contained model-value persistent />
          </v-card>
        </v-col>

        <v-col cols="6">
          <v-card append-icon="mdi-open-in-new" class="py-4" color="surface-variant" href="https://hub.docker.com/u/eclipsebasyx" prepend-icon="mdi-widgets-outline" rel="noopener noreferrer" rounded="lg" subtitle="Discover the BaSyx off-the-shelf components." target="_blank" title="Components" variant="text">
            <v-overlay opacity=".06" scrim="primary" contained model-value persistent />
          </v-card>
        </v-col>

        <v-col cols="6">
          <v-card append-icon="mdi-open-in-new" class="py-4" color="surface-variant" href="https://github.com/eclipse-basyx" prepend-icon="mdi-account-group-outline" rel="noopener noreferrer" rounded="lg" subtitle="Connect with BaSyx developers on GitHub." target="_blank" title="Community" variant="text">
            <v-overlay opacity=".06" scrim="primary" contained model-value persistent />
          </v-card>
        </v-col>
      </v-row>
    </v-responsive>
  </v-container>
</template>

<script setup lang="ts">
  //
</script>
