<template>
    <v-container fluid class="pa-0">
      <!-- Header -->
      <v-card class="mb-4">
        <v-card-title>
          <div class="text-subtitle-1">{{ "Asset Interfaces Description:" }}</div>
        </v-card-title>
      </v-card>
      <v-expansion-panels>
        <!-- Iterate through each SubmodelElementCollection -->
        <v-expansion-panel v-for="collection in submodelElementCollections" :key="collection.idShort">
          <v-expansion-panel-title>
            <div class="text-subtitle-1">{{ collection.idShort }}</div>
          </v-expansion-panel-title>
          <v-expansion-panel-text>
            <!-- Dynamically display each property's metadata and endpoint -->
            <v-card v-for="property in collection.properties" :key="property.idShort" class="mb-4 py-8">
              <v-row flex align="center" justify="center">
                <v-col cols="12" md="11">
                  <div class="text-subtitle-1">
                    {{ property.title || "No title available" }}
                  </div>
                  <div style="font-size: 14px">
                    {{ property.unit ? "Unit: " + property.unit : "" }}
                  </div>
                  <div style="font-size: 14px">
                    Endpoint: {{ property.endpoint }}
                  </div>
                </v-col>
              </v-row>
            </v-card>
          </v-expansion-panel-text>
        </v-expansion-panel>
      </v-expansion-panels>
  
      <!-- Handle case when no properties are available -->
      <v-card v-if="!submodelElementCollections.length" class="mb-4 py-8">
        <v-row flex align="center" justify="center">
          <div>No data available</div>
        </v-row>
      </v-card>
    </v-container>
  </template>
  
  <script lang="ts">
  import { defineComponent } from "vue";
  import { useTheme } from "vuetify";
  import { useNavigationStore } from "@/store/NavigationStore";
  import { useAASStore } from "@/store/AASDataStore";
  import RequestHandling from "../../mixins/RequestHandling";
  import SubmodelElementHandling from "../../mixins/SubmodelElementHandling";
  
  interface Property {
    idShort: string;
    title: string;
    unit: string;
    endpoint: string;
    semanticId?: string;
  }
  
  interface SubmodelElementCollection {
    idShort: string;
    properties: Property[];
  }
  
  export default defineComponent({
    name: "AssetInterfacesDescription",
    components: {
      RequestHandling, // Mixin to handle the requests to the AAS
    },
    mixins: [RequestHandling, SubmodelElementHandling],
    props: {
      submodelElementData: {
        type: Object,
        required: true,
      },
    },
    setup() {
      const theme = useTheme();
      const navigationStore = useNavigationStore();
      const aasStore = useAASStore();
  
      return {
        theme, // Theme Object
        navigationStore, // NavigationStore Object
        aasStore, // AASStore Object
      };
    },
    data() {
      return {
        submodelElementCollections: [] as Array<SubmodelElementCollection>,
      };
    },
    mounted() {
      this.fetchData();
    },
    methods: {
      fetchData() {
        if (!this.submodelElementData?.submodelElements) {
          console.warn("SubmodelElementData or submodelElements is not defined");
          return;
        }
        // console.log('Component Mounted. SubmodelElementData:', this.submodelElementData);
        // Find all SubmodelElementCollection
        const submodelElementCollections = this.submodelElementData.submodelElements.filter(
          (element: any) => element.modelType === "SubmodelElementCollection"
        );
  
        // Fetch details for each collection
        submodelElementCollections.forEach((collection: any) => {
          const propertiesCollection = this.findNestedElement(collection.value, "properties");
          if (propertiesCollection) {
            const propertyDefinitions = propertiesCollection.value;
            const baseEndpoint = this.getEndpointBase(collection);
  
            const properties = propertyDefinitions.map((propertyDefinition: any) => {
              return {
                idShort: propertyDefinition.idShort,
                title: this.getPropertyValue(propertyDefinition, "title"),
                unit: this.getPropertyValue(propertyDefinition, "unit"),
                endpoint: `${baseEndpoint}${this.getPropertyValue(propertyDefinition, "href")}`,
                semanticId: propertyDefinition.semanticId?.value || '',
              } as Property;
            });
  
            this.submodelElementCollections.push({
              idShort: collection.idShort,
              properties,
            });
          }
        });
      },
      getEndpointBase(submodelElementCollection: any): string {
        const endpointMetadata = submodelElementCollection.value.find(
          (property: any) => property.idShort === "EndpointMetadata"
        );
        if (!endpointMetadata) {
          console.warn("EndpointMetadata not found in SubmodelElementCollection");
          return "";
        }
  
        const baseProperty = endpointMetadata.value.find(
          (property: any) => property.idShort === "base"
        );
        return baseProperty?.value || "";
      },
      getPropertyValue(propertyCollection: any, idShort: string): string {
        const directProperty = propertyCollection.value?.find(
          (prop: any) => prop.idShort === idShort
        );
        if (directProperty) {
          return directProperty.value || "";
        }
  
        const propertyWithForms = propertyCollection.value?.find(
          (prop: any) => prop.idShort === "forms"
        );
        if (propertyWithForms) {
          const formProperty = propertyWithForms.value.find(
            (form: any) => form.idShort === idShort
          );
          return formProperty?.value || "";
        }
        return "";
      },
      findNestedElement(elements: any[], idShort: string): any {
        for (const element of elements) {
          if (element.idShort === idShort) {
            return element;
          }
          if (Array.isArray(element.value)) {
            const nestedElement = this.findNestedElement(element.value, idShort);
            if (nestedElement) {
              return nestedElement;
            }
          }
        }
        return null;
      },
    },
  });
  </script>