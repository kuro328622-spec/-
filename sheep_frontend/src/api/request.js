import axios from "axios";
import { ElMessage } from "element-plus";

// 统一标记位，防止跳转死循环
let isRedirecting = false;

const request = axios.create({
  // 向 utils 看齐：保持基础路径，由 API 文件决定具体路由
  baseURL: "http://localhost:8080",
  timeout: 5000,
});

// 1. 请求拦截器：向 utils 看齐，统一处理 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error("请求发起失败:", error);
    return Promise.reject(error);
  }
);

// 2. 响应拦截器：向 utils 看齐，统一处理跳转逻辑
request.interceptors.response.use(
  (response) => {
    // 自动解构，直接返回后端的数据对象
    return response.data;
  },
  (error) => {
    const { response, config } = error;
    const status = response ? response.status : null;
    const url = config ? config.url : "未知接口";

    // 调试日志：向标准看齐，方便排查路径问题
    console.error(`[网络异常] URL: ${url} | Status: ${status}`);

    // 【关键跳转逻辑】
    if (status === 401) {
      if (!isRedirecting) {
        isRedirecting = true;

        // 清理用户信息
        localStorage.removeItem("token");
        localStorage.removeItem("userInfo");
        sessionStorage.clear();

        ElMessage.error("登录状态已过期，请重新登录");

        // 强力重定向到登录页
        setTimeout(() => {
          isRedirecting = false;
          window.location.href = "/login";
        }, 1200);
      }
      return new Promise(() => {}); // 彻底切断后面的业务逻辑执行
    }

    // 404 处理：通常是因为 API 文件里的路径没写对（比如缺了 /api）
    if (status === 404) {
      ElMessage.warning(`接口不存在: ${url}`);
    } else {
      // 其他错误（500, 403, 400等）
      const message = response?.data?.message || error.message || "服务器响应错误";
      ElMessage.error(message);
    }

    return Promise.reject(error);
  }
);

export default request;
