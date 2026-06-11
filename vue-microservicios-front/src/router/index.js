import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import WorkflowView from '../views/WorkflowView.vue'
import UserAuthView from '../views/UserAuthView.vue'
import WarehouseView from '../views/WarehouseView.vue'
import SalesView from '../views/SalesView.vue'
import HrView from '../views/HrView.vue'
import HrReportView from '../views/HrReportView.vue'
import WarehouseReportView from '../views/WarehouseReportView.vue'
import SalesReportView from '../views/SalesReportView.vue'
import LoginView from '../views/LoginView.vue'
import InvoiceView from '../views/InvoiceView.vue'
import SupportView from '../views/SupportView.vue'

const routes = [
  { path: '/', name: 'dashboard', component: DashboardView },
  { path: '/workflow', name: 'workflow', component: WorkflowView },
  { path: '/user-auth', name: 'userAuth', component: UserAuthView },
  { path: '/warehouse', name: 'warehouse', component: WarehouseView },
  { path: '/sales', name: 'sales', component: SalesView },
  { path: '/hr', name: 'hr', component: HrView },
  { path: '/hr-report', name: 'hrReport', component: HrReportView },
  { path: '/warehouse-report', name: 'warehouseReport', component: WarehouseReportView },
  { path: '/sales-report', name: 'salesReport', component: SalesReportView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/invoice', name: 'invoice', component: InvoiceView },
  { path: '/support', name: 'support', component: SupportView }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const role = sessionStorage.getItem('role')

  if (to.path !== '/login' && to.path !== '/invoice' && to.path !== '/hr-report' && to.path !== '/warehouse-report' && to.path !== '/sales-report' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    // Si el usuario navega de regreso al login (ej. botón Atrás), destruimos su sesión por seguridad
    sessionStorage.clear()
    next()
  } else if (to.path === '/' && token && role !== 'ADMIN') {
    if (role === 'ROLE_SALES' || role === 'VENTAS') {
      next('/sales')
    } else if (role === 'ROLE_WAREHOUSE' || role === 'ALMACEN') {
      next('/warehouse')
    } else if (role === 'ROLE_HR' || role === 'RRHH') {
      next('/hr')
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
