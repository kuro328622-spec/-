import { createRouter, createWebHistory } from "vue-router";
import { useUserStore } from "@/stores/userStore";

// --- 基础页面导入 ---
import LoginRegisterView from "../views/LoginRegisterView.vue";
import MainLayout from "../components/MainLayout.vue";
import HomeView from "../views/HomeView.vue";

// --- 业务页面导入 ---
// 注释掉未使用的导入，防止 ESLint 报错 (assigned a value but never used)
// const TestView = () => import("../views/TestView.vue");
const FeedView = () => import("../views/inventory/FeedView.vue");
const MedicineView = () => import("../views/inventory/MedicineView.vue");
const ManufacturerView = () => import("../views/inventory/ManufacturerView.vue");
const DrugVaccineStockInView = () => import("../views/stock/DrugVaccineStockInView.vue");
const FeedStockInView = () => import("../views/stock/FeedStockInView.vue");
const DrugVaccineStockOutView = () => import("../views/stock/DrugVaccineStockOutView.vue");
const FeedStockOutView = () => import("../views/stock/FeedStockOutView.vue");
const FinancialManagementView = () => import("../views/finance/FinancialManagementView.vue");
const FinancialSummaryView = () => import("../views/finance/FinancialSummaryView.vue");

// 下面这三个报表暂时不用，必须注释掉导入，否则 ESLint 会报错
// const MonthlyReportView = () => import("../views/report/MonthlyReportView.vue");
// const QuarterlyReportView = () => import("../views/report/QuarterlyReportView.vue");
// const BenefitAnalysisView = () => import("../views/report/BenefitAnalysisView.vue");

const ProfileView = () => import("../views/user/ProfileView.vue");

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: LoginRegisterView,
      meta: { requiresAuth: false },
    },
    {
      path: "/",
      name: "MainLayout",
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        // 1. 仪表盘
        { path: "", name: "home", component: HomeView, meta: { title: "仪表盘", icon: "House" } },

        // 2. 库存管理
        {
          path: "inventory",
          name: "InventoryManagement",
          meta: { title: "库存管理", icon: "Box", isParent: true },
          children: [
            {
              path: "medicine",
              name: "medicine",
              component: MedicineView,
              meta: { title: "药品疫苗库存" },
            },
            { path: "feed", name: "feed", component: FeedView, meta: { title: "饲料库存" } },
            {
              path: "Manufacturer",
              name: "Manufacturer",
              component: ManufacturerView,
              meta: { title: "供应商信息存储" },
            },
          ],
        },

        // 3. 出入库管理
        {
          path: "stock",
          name: "StockManagement",
          meta: { title: "出入库管理", icon: "Switch", isParent: true },
          children: [
            {
              path: "drug-vaccine",
              name: "DrugVaccineManagement",
              meta: { title: "药品/疫苗管理", isParent: true },
              children: [
                {
                  path: "in",
                  name: "DrugVaccineStockIn",
                  component: DrugVaccineStockInView,
                  meta: { title: "入库登记" },
                },
                {
                  path: "out",
                  name: "DrugVaccineStockOut",
                  component: DrugVaccineStockOutView,
                  meta: { title: "出库登记" },
                },
              ],
            },
            {
              path: "feed",
              name: "FeedManagement",
              meta: { title: "饲料管理", isParent: true },
              children: [
                {
                  path: "in",
                  name: "FeedStockIn",
                  component: FeedStockInView,
                  meta: { title: "入库登记" },
                },
                {
                  path: "out",
                  name: "FeedStockOut",
                  component: FeedStockOutView,
                  meta: { title: "出库登记" },
                },
              ],
            },
          ],
        },

        // 4. 财务管理
        {
          path: "finance",
          name: "FinanceManagement",
          meta: { title: "财务管理", icon: "Money", isParent: true },
          children: [
            {
              path: "records",
              name: "FinancialRecords",
              component: FinancialManagementView,
              meta: { title: "收支明细" },
            },
            {
              path: "summary",
              name: "FinancialSummary",
              component: FinancialSummaryView,
              meta: { title: "汇总看板" },
            },
          ],
        },

        // 5. 个人中心
        {
          path: "user/profile",
          name: "profile",
          component: ProfileView,
          meta: { title: "个人中心", icon: "User" },
        },
      ],
    },
  ],
});

// --- 路由守卫 ---
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const token = localStorage.getItem("token");
  const userInfoStr = localStorage.getItem("userInfo");

  if (token && userInfoStr && !userStore.token) {
    try {
      userStore.token = token;
      userStore.userInfo = JSON.parse(userInfoStr);
    } catch {
      localStorage.clear();
    }
  }

  const isAuthRequired = to.meta.requiresAuth;

  if (isAuthRequired && !token) {
    next({ name: "login" });
  } else if (to.name === "login" && token) {
    next({ name: "home" });
  } else {
    next();
  }
});

export default router;
