import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      // 针对特定API路径进行代理，而不是所有请求
      '/auth': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/device': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/user': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/owner': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/rental': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/file': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/avatar': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/message': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      }
    }
  }
})