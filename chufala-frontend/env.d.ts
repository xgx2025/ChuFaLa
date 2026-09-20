/// <reference types="vite/client" />

/**
 * 这里**故意没有** `declare module '*.vue'` 通配声明
 * ------------------------------------------------------------------
 * 网上常见的写法是补一条：
 *   declare module '*.vue' {
 *     const c: DefineComponent<...>; export default c
 *   }
 * 用来消除「import .vue 报 TS7016」。但本项目**不要这么做**，原因是：
 *
 *   1. 本项目绝大多数 SFC 用的是不带 lang="ts" 的 <script setup>（纯 JS），
 *      这条通配声明会**遮住它们的真实导出**。
 *      最典型的是 src/components/Icon.vue —— 它是 30 个具名导出的图标库
 *      （`export const Plane = () => h('svg', ...)`，没有 default 导出），
 *      加上通配声明后 `import { Plane } from '@/components/Icon.vue'`
 *      会立刻报 TS2614: Module '"*.vue"' has no exported member 'Plane'。
 *
 *   2. 正确的解法是 tsconfig.app.json 里的 `"allowJs": true`：
 *      让 TS 直接读 JS 写的 SFC 并推断类型，既消除 TS7016，
 *      又完整保留具名导出与推断出来的 props 类型。
 *
 * 如果哪天又看到 TS7016，请先确认 `allowJs` 是否被改动，
 * 而不是往这个文件里加通配声明。
 *
 * 另：诊断这类问题时有个快捷线索 —— 全站只有 Flight.vue 与 my/Subscription.vue
 * 写了 lang="ts"，所以只有这两个文件不会报 TS7016。凡是「报错只对部分页面出现」
 * 的情况，先查 script 的 lang 差异。
 */
