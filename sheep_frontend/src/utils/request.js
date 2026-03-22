import axios from "axios";
import { ElMessage } from "element-plus";

// 使用全局变量或闭包标记，确保即便多个请求并发报错，也只执行一次跳转逻辑
let isRedirecting = false;

const service = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 5000,
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 调试日志：如果跳转失败，请在控制台查看这个状态码
    const status = error.response ? error.response.status : null;
    console.log("Axios拦截器捕获状态:", status);

    // 只要是 401（未授权/过期），就执行强制跳转逻辑
    if (status === 401 || status === 403) {
      if (!isRedirecting) {
        isRedirecting = true;

        // 1. 立即清空缓存（这一步必须在跳转前做，否则路由守卫会拦截跳转）
        localStorage.clear();
        sessionStorage.clear();

        // 2. 提示用户（使用 alert 作为最高优先级的强制提醒，防止 ElMessage 被覆盖）
        // 如果你希望用户体验更柔和，可以换回 ElMessage.error
        ElMessage.error("登录已失效，正在跳转至登录页面...");

        // 3. 强制跳转 (使用 window.location.href 重新加载环境，这是最稳妥的跳转方式)
        setTimeout(() => {
          isRedirecting = false;
          window.location.href = "/login";
        }, 1000);
      }

      // 【核心点】返回一个永远处于 pending 状态的 Promise
      // 这样组件里的 .catch() 就不会执行，控制台里那些“获取列表失败”的弹窗就会消失
      return new Promise(() => {});
    }

    // 处理其他非 401 的报错
    const errorMsg = error.response?.data?.message || error.message || "请求失败";
    ElMessage.error(errorMsg);

    return Promise.reject(error);
  }
);

export default service;
