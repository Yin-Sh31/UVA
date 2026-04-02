import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/images': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/avatar': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/tencent': {
        target: 'https://apis.map.qq.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/tencent/, '')
      },
      '/qweather': {
        target: 'https://mb33jqaeff.re.qweatherapi.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/qweather/, ''),
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('代理转发请求头:', proxyReq.getHeaders());
          });
        }
      }
    }
  }
})