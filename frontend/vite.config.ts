import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 5173,
      // Docker 部署时不需要代理（通过 Nginx 代理），开发环境需要代理
      proxy: env.VITE_DOCKER !== 'true' ? {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        },
        '/s': {
          target: 'http://localhost:8080',
          changeOrigin: true
        }
      } : undefined
    }
  }
})
