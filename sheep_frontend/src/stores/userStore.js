// src/stores/userStore.js
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem("token") || null,
  }),
  actions: {
    /**
     * 登录时设置用户信息
     * @param {object} userData - 包含 token 和用户信息的对象
     */
    setUserInfo(userData) {
      this.userInfo = userData;
      this.token = userData.token;
      localStorage.setItem("token", userData.token);
      localStorage.setItem("userInfo", JSON.stringify(userData));
    },

    /**
     * 更新资料时同步更新 state 和 localStorage
     * @param {object} newProfile - 新的用户资料对象
     */
    updateProfile(newProfile) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...newProfile };
        localStorage.setItem("userInfo", JSON.stringify(this.userInfo));
      }
    },

    /**
     * 退出登录时清空 state 和 localStorage
     */
    logout() {
      this.userInfo = null;
      this.token = null;
      localStorage.removeItem("token");
      localStorage.removeItem("userInfo");
    },

    /**
     * 从 localStorage 恢复用户状态
     * 此方法需要在应用初始化时调用
     */
    initUserInfo() {
      try {
        const savedToken = localStorage.getItem("token");
        const savedUserInfoStr = localStorage.getItem("userInfo");

        if (savedToken && savedUserInfoStr) {
          const savedUserInfo = JSON.parse(savedUserInfoStr);
          if (savedUserInfo && typeof savedUserInfo === "object") {
            this.token = savedToken;
            this.userInfo = savedUserInfo;
            console.log("用户状态已从 localStorage 恢复。");
          }
        }
      } catch (err) {
        console.error("恢复用户信息失败，将清空本地存储：", err);
        this.logout(); // 如果解析失败，执行退出登录操作
      }
    },
  },
});
