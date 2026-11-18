/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// The Spring Boot backend runs on port 1027 (application.properties: server.port).
// In development the browser talks only to Vite (port 5173), and Vite forwards API calls to Spring Boot,
// so cookies and CSRF work exactly as they will when Spring Boot serves the built app itself.
const backend = process.env.KAAJ_BACKEND ?? 'http://localhost:1027'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
      '/swagger-ui': backend,
      '/v3/api-docs': backend,
    },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
  },
})
