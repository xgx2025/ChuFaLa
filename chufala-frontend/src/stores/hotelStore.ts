import { defineStore } from 'pinia'

// 定义酒店预订全局状态
export const useHotelStore = defineStore('hotel', {
  state: () => ({
    // 原window.IBU_HOTEL的核心数据（按需精简）
    translate: {
      common_sure: "确认",
      common_cancel: "取消",
      common_networkError: "网络连接失败，请检查网络设置。",
      book_continue_to_pay: "继续预订",
      book_contact_title: "联系人信息",
      // ... 其他翻译字段（保留原页面需要的）
    },
    cargo: {
      locale: "zh-CN",
      site: "CN",
      currency: "CNY"
    },
    localization: {
      currency: {
        CNY: { default: "CNY" },
        HKD: { default: "HK$" },
        USD: { default: "US$" }
        // ... 其他货币配置
      }
    },
    pageId: "102005",
    abTestingTracker: "M:73,250520_HTL_OLCTMerge:C"
  }),
  actions: {
    // 动态加载原页面的脚本（替代原loadScriptA函数）
    loadThirdScripts() {
      const scriptTags = [
        "https://ws-s.tripcdn.cn/ares/api/cc?f=locale%2Fv3%2F6001%2Fzh-CN.js%2C%2Flocale%2Fv3%2F6002%2Fzh-CN.js%2C%2Flocale%2Fv3%2F330153%2Fzh-CN.js&etagc=36de5b4389ca79a7152a8091739b6864",
        "https://ws-s.tripcdn.cn/modules/hotel/hotel-ctrip-online/smart/smart.b5e7009b179bc4ec9b96.js",
        "//webresource.c-ctrip.com/ares2/sysdev/ubt-sdk/*/default/ubt.minl.js?v=1757831787208"
      ];

      scriptTags.forEach(src => {
        if (src) {
          const script = document.createElement('script');
          script.type = 'text/javascript';
          script.src = src;
          script.crossOrigin = 'anonymous';
          script.async = false; // 保持原页面脚本加载顺序
          document.body.appendChild(script);
        }
      });
    }
  }
})