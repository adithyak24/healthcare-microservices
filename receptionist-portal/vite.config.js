import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { visualizer } from 'rollup-plugin-visualizer';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    visualizer({
      open: !process.env.CI && process.env.NODE_ENV !== 'production', // Don't auto-open in CI/production
      filename: 'stats.html', // Output file name
    }),
  ],
  server: {
    port: 3001, // Ensure dev server runs on 3001
    proxy: {
      // Proxy /api requests to your API Gateway
      '/api': {
        target: 'http://localhost:4004', // Your API Gateway URL
        changeOrigin: true, // Recommended for most cases
        // rewrite: (path) => path.replace(/^\/api/, '') // Only if API Gateway doesn't expect /api prefix
      }
    }
  }
}) 