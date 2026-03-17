import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: "0.0.0.0",   // VERY IMPORTANT
    port: 5173,
    strictPort: true,
    allowedHosts: true,
    // VITE_BACKEND_TARGET: set to http://app:9090 in Docker, defaults to localhost for local dev
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_TARGET ?? 'http://localhost:9090',
        changeOrigin: true,
      }
    }
  }
})
