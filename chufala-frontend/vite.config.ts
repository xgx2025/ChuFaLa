import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// 读取证书文件（路径相对于 vite.config.js）
const httpsOptions = {
  key: fs.readFileSync(path.resolve(__dirname, 'key.pem')),
  cert: fs.readFileSync(path.resolve(__dirname, 'cert.pem'))
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
      port: 8881,
      host: '0.0.0.0', 
      allowedHosts: [
      '5279774f.r3.cpolar.top', // 允许该主机访问
      '192.168.1.103',
      '10.251.29.4'
    ],
    // cors: true,
    proxy: {
      // 匹配所有以 '/api' 开头的请求
      '/api': {
        target: 'http://localhost:9010', 
        changeOrigin: true, // 允许跨域（修改请求头中的 Origin）
        rewrite: (path) => path.replace(/^\/api/, '') // 去除请求路径中的 '/api' 前缀
      }
    },
    // https: httpsOptions
  }
})
