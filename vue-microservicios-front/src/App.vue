<template>
  <div :class="{'app-shell': !$route.path.includes('/invoice')}">
    <aside class="sidebar d-print-none" v-if="!$route.path.includes('/invoice')">
      <div class="brand">
        <img src="./assets/images/logo.png" alt="North Solutions Logo" class="brand-logo" />
      </div>

      <div class="user-profile" v-if="token">
        <div class="user-avatar">
          <i class="bi bi-person-circle"></i>
        </div>
        <div class="user-info">
          <span class="user-name">{{ employeeName || username || 'Usuario' }}</span>
          <span class="user-role badge" :class="roleClass">{{ displayRole }}</span>
        </div>
      </div>

      <nav class="d-flex flex-column gap-2" v-if="token">
        <RouterLink class="nav-link" to="/" v-if="role === 'ADMIN'"><i class="bi bi-grid-1x2"></i> Sistema Central</RouterLink>
        <RouterLink class="nav-link" to="/warehouse" v-if="role === 'ADMIN' || role === 'ROLE_WAREHOUSE' || role === 'ALMACEN' || role === 'ROLE_SALES' || role === 'VENTAS'"><i class="bi bi-box-seam"></i> Gestión Almacenes</RouterLink>
        <RouterLink class="nav-link" to="/sales" v-if="role === 'ADMIN' || role === 'ROLE_SALES' || role === 'VENTAS'"><i class="bi bi-cart"></i> Ventas y Facturación</RouterLink>
        <RouterLink class="nav-link" to="/hr" v-if="role === 'ADMIN' || role === 'ROLE_HR' || role === 'RRHH'"><i class="bi bi-people"></i> Recursos Humanos</RouterLink>
        <RouterLink class="nav-link" to="/workflow" v-if="role === 'ADMIN'">
          <i class="bi bi-ui-checks"></i> Aprobaciones
          <span v-if="pendingApprovalsCount > 0" class="badge bg-danger ms-2">{{ pendingApprovalsCount }}</span>
        </RouterLink>
        <RouterLink class="nav-link" to="/support" v-if="token">
          <i class="bi bi-headset"></i> Soporte y Chat 
        </RouterLink>
        <button class="btn btn-outline-danger mt-4" @click="() => logout()"><i class="bi bi-box-arrow-left"></i> Cerrar Sesión</button>
      </nav>
    </aside>

    <main :class="{'content': !$route.path.includes('/invoice')}">
      <RouterView />
      
      <!-- Global Footer -->
      <div class="text-center text-muted mt-5 pt-3 border-top d-print-none" style="font-size: 0.85rem;">
        &copy; 2026 North Solutions S.A. de C.V. Todos los derechos reservados.
      </div>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { authService } from './services/authService'
import hrService from './services/hrService'
import { workflowService } from './services/workflowService'

const token = ref(sessionStorage.getItem('token'))
const role = ref(sessionStorage.getItem('role'))
const username = ref(sessionStorage.getItem('username'))
const employeeName = ref('')
const pendingApprovalsCount = ref(0)
let notificationInterval = null
let sessionCheckInterval = null

const displayRole = computed(() => {
  if (role.value === 'ADMIN') return 'Administrador'
  if (role.value === 'ROLE_SALES' || role.value === 'VENTAS') return 'Ventas'
  if (role.value === 'ROLE_WAREHOUSE' || role.value === 'ALMACEN') return 'Almacén'
  if (role.value === 'ROLE_HR' || role.value === 'RRHH') return 'Recursos Humanos'
  return role.value || 'Invitado'
})

const roleClass = computed(() => {
  if (role.value === 'ADMIN') return 'bg-danger'
  if (role.value === 'ROLE_SALES' || role.value === 'VENTAS') return 'bg-success'
  if (role.value === 'ROLE_WAREHOUSE' || role.value === 'ALMACEN') return 'bg-warning text-dark'
  if (role.value === 'ROLE_HR' || role.value === 'RRHH') return 'bg-info text-dark'
  return 'bg-secondary'
})

onMounted(async () => {
  if (username.value) {
    try {
      const emps = await hrService.getEmployees()
      const myEmp = emps.find(e => e.username === username.value)
      if (myEmp) {
        employeeName.value = `${myEmp.firstName} ${myEmp.lastName}`
      }
    } catch (e) {
      console.error('Failed to load employee info in sidebar:', e.message)
    }
  }

  // Request Notification Permissions
  if ('Notification' in window) {
    if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
      Notification.requestPermission()
    }
  }

  // Poll for pending approvals
  if (role.value === 'ADMIN' && token.value) {
    checkPendingApprovals()
    notificationInterval = setInterval(checkPendingApprovals, 15000)
    
    // Listen for instant updates from local actions
    window.addEventListener('workflow-updated', checkPendingApprovals)
  }

  // Real-time session validation (forces logout if kicked by admin)
  if (token.value) {
    sessionCheckInterval = setInterval(async () => {
      try {
        const res = await authService.validarToken(token.value)
        if (!res.data || !res.data.valid) {
          throw { response: { status: 401 } }
        }
      } catch (err) {
        // Only force logout if the server explicitly rejected the token
        if (err.response && (err.response.status === 401 || err.response.status === 403)) {
          alert('Tu sesión ha sido cerrada por un administrador o ha expirado.')
          logout(true)
        }
      }
    }, 5000)
  }
})

const checkPendingApprovals = async () => {
  try {
    const res = await workflowService.getPendientes(token.value)
    const newCount = res.data ? res.data.length : 0
    if (newCount > pendingApprovalsCount.value && newCount > 0) {
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification('Nuevas Aprobaciones Pendientes', {
          body: `Tienes ${newCount} documento(s) pendiente(s) de aprobación.`,
          icon: '/favicon.ico'
        })
      }
    }
    pendingApprovalsCount.value = newCount
  } catch (err) {
    console.error('Error checking pending approvals:', err)
  }
}

const logout = async (force = false) => {
  if (notificationInterval) {
    clearInterval(notificationInterval)
  }
  if (sessionCheckInterval) {
    clearInterval(sessionCheckInterval)
  }
  
  window.removeEventListener('workflow-updated', checkPendingApprovals)
  
  if (token.value && !force) {
    try {
      await authService.logout(token.value)
    } catch (e) {
      console.error('Logout failed:', e)
    }
  }
  sessionStorage.clear()
  window.location.href = '/login'
}
</script>
