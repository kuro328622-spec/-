// src/main.js

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
// 这行代码会引入并执行 ./stores/index.js 文件，从而得到 pinia 实例
import pinia from "./stores";

// 引入Element Plus
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

// 1. 引入我们将要创建的全局指令文件
import directives from "./directives";

// [修改] 1. 引入你的 userStore
import { useUserStore } from "./stores/userStore";

const app = createApp(App);

// 按推荐的顺序使用插件
app.use(pinia);
app.use(router);
app.use(ElementPlus);

// 2. 调用 directives 函数来注册所有的全局指令
directives(app);

// [修改] 2. 在挂载 App 之前，获取 userStore 实例并调用初始化方法
// 因为 app.use(pinia) 已经执行，所以现在可以安全地创建 store 实例
const userStore = useUserStore();
userStore.initUserInfo();

app.mount("#app");
