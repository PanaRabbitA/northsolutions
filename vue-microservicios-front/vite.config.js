import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: true,
    port: 5174,
    proxy: {
      '/api/auth': 'http://localhost:8081',
      '/api/usuarios': 'http://localhost:8082',
      '/api/procesos': 'http://localhost:8083',
      '/api/warehouse': 'http://localhost:8084',
      '/api/sales': 'http://localhost:8085',
      '/api/hr': 'http://localhost:8086',
      '/api/dashboard': 'http://localhost:8087',
      '/tickets': 'http://localhost:8083',
      '/chat': 'http://localhost:8083'
    }
  }
})