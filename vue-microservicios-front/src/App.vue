<template>
  <div :class="{'app-shell': !($route.path.includes('/invoice') || $route.path.includes('-report'))}">
    <aside class="sidebar d-print-none" v-if="!($route.path.includes('/invoice') || $route.path.includes('-report'))">
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
        <RouterLink class="nav-link" :class="{'nav-pulse': pendingApprovalsCount > 0 && !approvalsSeen}" to="/workflow" v-if="role === 'ADMIN'" @click="approvalsSeen = true">
          <i class="bi bi-ui-checks"></i> Aprobaciones
          <span v-if="pendingApprovalsCount > 0" class="badge bg-danger ms-2">{{ pendingApprovalsCount }}</span>
        </RouterLink>
        <RouterLink class="nav-link" :class="{'nav-pulse': hasPendingSupport && !supportSeen}" to="/support" v-if="token" @click="supportSeen = true">
          <i class="bi bi-headset"></i> Soporte y Chat
          <span v-if="hasPendingSupport && !supportSeen" class="badge bg-warning text-dark ms-2">!</span>
        </RouterLink>
        <button class="btn btn-outline-danger mt-4" @click="() => logout()"><i class="bi bi-box-arrow-left"></i> Cerrar Sesión</button>
      </nav>
    </aside>

    <main :class="{'content': !($route.path.includes('/invoice') || $route.path.includes('-report'))}">
      <RouterView />
      
      <!-- Global Footer -->
      <div class="text-center text-muted mt-5 pt-3 border-top d-print-none" style="font-size: 0.85rem;">
        &copy; 2026 North Solutions S.A. de C.V. Todos los derechos reservados.
      </div>
    </main>
  </div>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router'
import { ref, onMounted, computed, watch, watchEffect } from 'vue'
import { authService } from './services/authService'
import hrService from './services/hrService'
import { workflowService } from './services/workflowService'
import { ticketService } from './services/ticketService'

const token = ref(sessionStorage.getItem('token'))
const role = ref(sessionStorage.getItem('role'))
const username = ref(sessionStorage.getItem('username'))
const employeeName = ref('')
const pendingApprovalsCount = ref(0)
const hasPendingSupport = ref(false)
const approvalsSeen = ref(false)
const supportSeen = ref(false)
const route = useRoute()
let notificationInterval = null
let sessionCheckInterval = null
let supportInterval = null

// When navigating away and back, re-check if seen
watch(() => route.path, (newPath) => {
  if (newPath === '/workflow') {
    approvalsSeen.value = true
  }
  if (newPath === '/support') {
    supportSeen.value = true
  }
})

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

watchEffect(() => {
  if (displayRole.value && displayRole.value !== 'Invitado') {
    document.title = `${displayRole.value} North Solutions`
  } else {
    document.title = 'North Solutions'
  }
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

  // Poll for pending support tickets
  if (token.value) {
    checkPendingSupport()
    supportInterval = setInterval(checkPendingSupport, 15000)
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
      // New approvals arrived — re-trigger pulse even if user had seen before
      approvalsSeen.value = false
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

const checkPendingSupport = async () => {
  try {
    const isAdmin = role.value === 'ADMIN'
    let tickets
    if (isAdmin) {
      tickets = await ticketService.getAllTickets()
    } else {
      tickets = await ticketService.getUserTickets(username.value)
    }
    const openTickets = tickets.filter(t => t.status !== 'RESUELTO' && t.status !== 'CERRADO')
    const hadPending = hasPendingSupport.value
    hasPendingSupport.value = openTickets.length > 0
    // If new tickets appeared, reset seen so it blinks again
    if (!hadPending && hasPendingSupport.value) {
      supportSeen.value = false
    }
  } catch (err) {
    console.error('Error checking support tickets:', err)
  }
}

const logout = async (force = false) => {
  if (notificationInterval) {
    clearInterval(notificationInterval)
  }
  if (sessionCheckInterval) {
    clearInterval(sessionCheckInterval)
  }
  if (supportInterval) {
    clearInterval(supportInterval)
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
