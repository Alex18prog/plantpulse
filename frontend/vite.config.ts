import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  // Only the GitHub Pages deploy job sets this, to "/plantpulse-starter_1/"
  // (the repo is served at <user>.github.io/<repo>/, not the domain root).
  // Local dev and the Docker/nginx image both serve from "/", so they're
  // unaffected by leaving this unset.
  base: process.env.VITE_BASE_PATH ?? '/',
  // sockjs-client expects the Node.js `global` to exist; Vite doesn't
  // polyfill it like webpack did, so without this the app throws on load.
  define: {
    global: 'globalThis',
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
      },
    },
  },
})
