/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css';
import 'vuetify/styles';

// Composables
import { createVuetify } from 'vuetify';

// get the primary color from the environment variables
let primaryColor = import.meta.env.VITE_PRIMARY_COLOR;
// check if primary color is set
if (!primaryColor) {
  primaryColor = '#1e8567';
}

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export default createVuetify({
  // display: {
  //   mobileBreakpoint: 'md',
  //   thresholds: {
  //     xs: 600,
  //     sm: 960,
  //     md: 1370,
  //     lg: 1904,
  //     xl: 2500,
  //   },
  // },
  theme: {
    themes: {
      light: {
        colors: {
          primary: primaryColor,
          appBar: '#f5f5f5',
          appNavigation: '#ffffff',
          card: '#FFFFFF',
          cardDialog: '#F0F0F0',
          cardHeader: '#FFFFFF',
          elevatedCard: '#F1F1F1',
          detailsCard: '#FBFBFB',
          detailsHeader: '#F5F5F5',
          listItem: '#F5F5F5',
          hover: '#242424',
          buttonText: '#ffffff',
          divider: '#E0E0E0',
          subtitleText: '#626262',
          lamp: '#7A7A7A',
        },
      },
      dark: {
        colors: {
          primary: primaryColor,
          appBar: '#1E1E1E',
          appNavigation: '#121212',
          card: '#121212',
          cardDialog: '#202020',
          cardHeader: '#282828',
          elevatedCard: '#343434',
          detailsCard: '#181818',
          detailsHeader: '#151515',
          listItem: '#1e1e1e',
          hover: '#E2E2E2',
          buttonText: '#272727',
          divider: '#2F2F2F',
          subtitleText: '#A5A5A5',
          lamp: '#959595',
        },
      },
    },
  },
})
