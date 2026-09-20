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

/**
 * 修复 /package 路由被 package.json 抢占的问题
 * ------------------------------------------------------------------
 * Vite 开发服务器的静态中间件在解析请求时会做「补扩展名」尝试：
 * 请求 `/package` → 命中根目录的 `package.json` → 当成 JSON 模块转成 ESM 返回。
 * 结果是「旅游套餐」页在**硬刷新或直接输入网址**时，浏览器里显示的是
 * `export const name = "chufala";` 这样的 package.json 源码。
 *
 * 平时点导航栏不会发现，因为那是 Vue Router 的客户端跳转，根本不经过服务器；
 * 只有直接访问 URL / 刷新才会暴露。生产环境取决于部署服务器的 SPA 回退配置。
 *
 * 这里把精确的 `/package`（含带 query 的形式）改写成 `/`，走正常的 SPA 入口。
 * 注意用精确匹配，不要误伤 `/package/xxx` 或未来可能新增的 `/packages`。
 */
const spaFallbackForPackageRoute = () => ({
  name: 'spa-fallback-for-package-route',
  configureServer(server: { middlewares: { use: Function } }) {
    server.middlewares.use((req: { url?: string }, _res: unknown, next: () => void) => {
      if (req.url === '/package' || req.url?.startsWith('/package?')) {
        req.url = '/'
      }
      next()
    })
  },
})

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    spaFallbackForPackageRoute(),
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
