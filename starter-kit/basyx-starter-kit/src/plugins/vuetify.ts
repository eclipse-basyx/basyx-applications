/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'
import { VTreeview } from 'vuetify/labs/VTreeview'

// Composables
import { createVuetify } from 'vuetify'

// https://vuetifyjs.com/en/introduction/why-vuetify/#feature-guides
export default createVuetify({
  components: {
    VTreeview,
  },
  theme: {
    themes: {
      light: {
        dark: false,
        colors: {
          primary: '#009ee2',
          title: '#009ee2',
          header: '#303030',
          subheader: '#707070',
          normalText: '#303030',
          buttonText: '#E1E1E1',
          tableOdd: '#E1E1E1',
          tableEven: '#F5F5F5',
          background: '#FFFFFF',
          alertCard: '#F9F9F9',
          footer: '#F2F2F2',
          footerText: '#303030',
        },
      },
      dark: {
        dark: true,
        colors: {
          primary: '#009ee2',
          title: '#FFFFFF',
          header: '#E1E1E1',
          subheader: '#9B9B9B',
          normalText: '#E1E1E1',
          buttonText: '#212121',
          tableOdd: '#272727',
          tableEven: '#212121',
          background: '#1A1A1A',
          alertCard: '#242424',
          footer: '#212121',
          footerText: '#E1E1E1',
        },
      },
    },
  },
})
