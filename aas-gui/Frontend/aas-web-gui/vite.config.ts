// Plugins
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'

// Utilities
import { defineConfig } from 'vite'
import * as path from 'path'
import { readFileSync } from 'fs'
import { resolve } from 'path'

// Read and parse the config.json file
const config = JSON.parse(readFileSync(resolve(__dirname, './public/config.json'), 'utf-8'));

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vite-plugin
    vuetify({
      autoImport: true,
    }),
  ],
  // define: { 'process.env': {} },
  base: `${config.basePath || ''}`,
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    extensions: [
      '.js',
      '.json',
      '.jsx',
      '.mjs',
      '.ts',
      '.tsx',
      '.vue',
    ],
  },
  server: {
    port: 3000,
    hmr: true, // enable hot module replacement
  },
})
