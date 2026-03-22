// src/directives/index.js

import dayjs from "dayjs";
import "dayjs/locale/zh-cn";

// 加载中文语言包
dayjs.locale("zh-cn");

/**
 * 日期格式化指令 (已增强兼容性)
 * 能够处理多种日期格式：
 * 1. 标准的 ISO 字符串 (e.g., "2023-10-25T12:00:00.000Z")
 * 2. 后端返回的 Date 对象字符串 (e.g., "Thu Jan 05 2026 15:00:00 GMT+0800 (China Standard Time)")
 * 3. 自定义的 yyyy-MM-dd 字符串 (e.g., "2023-10-25")
 * 使用方法: v-format-date="YYYY年MM月DD日"
 */
const formatDate = {
  mounted(el, binding) {
    const formatString = binding.value || "YYYY-MM-DD";
    let originalDateStr = el.innerText.trim();

    if (originalDateStr) {
      try {
        // 关键修改：先使用 new Date() 将字符串转换为标准的 Date 对象
        // 然后再用 dayjs 进行格式化
        const dateObject = new Date(originalDateStr);

        // 检查 dateObject 是否有效
        if (dateObject.toString() === "Invalid Date") {
          console.warn(`无法解析的日期字符串: "${originalDateStr}"`);
          return; // 如果无效，则不进行格式化
        }

        const formattedDate = dayjs(dateObject).format(formatString);
        el.innerText = formattedDate;
      } catch (e) {
        console.error("日期格式化失败:", e);
      }
    }
  },
};

// 导出一个函数，用于在 main.js 中注册所有指令
export default (app) => {
  app.directive("format-date", formatDate);
};
