// src/utils/amap-loader.js

/**
 * 异步加载高德地图JS API
 * @param {string} key - 你在高德开放平台申请的Web端(JS API)密钥
 * @param {string[]} plugins - 需要加载的插件列表，例如 ['AMap.ToolBar', 'AMap.Scale']
 * @returns {Promise} 返回一个Promise，resolve时为AMap对象
 */
export function loadAMap(key, plugins = []) {
  // 配置安全密钥
  const securityCode = import.meta.env.VITE_AMAP_SECURITY_CODE;
  if (securityCode) {
    window._AMapSecurityConfig = {
      securityJsCode: securityCode,
    };
  }

  // 检查是否已经加载过
  if (window.AMap) {
    return Promise.resolve(window.AMap);
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.async = true;
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}&plugin=${plugins.join(',')}`;

    script.onerror = (err) => {
      reject(new Error('高德地图JS API加载失败'));
    };

    script.onload = () => {
      // 脚本加载完成后，等待一小段时间确保AMap对象已完全初始化
      setTimeout(() => {
        if (window.AMap) {
          resolve(window.AMap);
        } else {
          reject(new Error('高德地图JS API初始化失败'));
        }
      }, 100);
    };

    document.head.appendChild(script);
  });
}
