<template>
  <section>
    <div class="page-header d-print-none">
      <div>
        <span class="eyebrow">Panel General (Vista Administrador)</span>
        <h1>Sistema Central</h1>
        <p>Integración de Microservicios: Workflow, Auth, User, Warehouse, Sales y HR.</p>
      </div>
    </div>

    <!-- KPIs Section -->
    <div class="panel mt-4 bg-primary text-white d-print-none" v-if="kpis">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="text-white mb-0"><i class="bi bi-bar-chart-fill"></i> Indicadores del Negocio en Tiempo Real</h2>
        <div style="width: 200px;">
          <select v-model="timeFilter" @change="applyTimeFilter" class="form-select form-select-sm bg-transparent text-white border-light fw-bold shadow-none" style="cursor: pointer;">
            <option value="all" class="text-dark">Histórico Total</option>
            <option value="day" class="text-dark">Hoy (Día)</option>
            <option value="week" class="text-dark">Últimos 7 días (Semana)</option>
            <option value="month" class="text-dark">Último Mes</option>
            <option value="6months" class="text-dark">Últimos 6 Meses</option>
            <option value="year" class="text-dark">Último Año</option>
          </select>
        </div>
      </div>
      <div class="row text-center g-4">
        <div class="col-md-3">
          <div class="display-5 fw-bold">{{ kpis.totalRevenue ? '$' + kpis.totalRevenue.toFixed(2) : '$0.00' }}</div>
          <div class="text-white-50">Ingresos Totales ({{ kpis.totalSales || 0 }} ventas)</div>
        </div>
        <div class="col-md-3">
          <div class="display-5 fw-bold">{{ kpis.totalProducts || 0 }}</div>
          <div class="text-white-50">Productos en Catálogo</div>
        </div>
        <div class="col-md-3">
          <div class="display-5 fw-bold text-warning">{{ kpis.lowStockProducts || 0 }}</div>
          <div class="text-white-50">Productos con Bajo Stock (< 10)</div>
        </div>
        <div class="col-md-3">
          <div class="display-5 fw-bold">{{ kpis.totalEmployees || 0 }}</div>
          <div class="text-white-50">Empleados Activos</div>
        </div>
      </div>
      <div v-if="kpis.error" class="text-warning mt-3 small"><i class="bi bi-exclamation-triangle"></i> {{ kpis.error }}</div>
    </div>

    <!-- Active Sessions Section -->
    <div class="panel mt-4 d-print-none">
      <h2><i class="bi bi-person-lines-fill text-primary"></i> Usuarios Conectados en Tiempo Real</h2>
      <p class="text-muted mb-4">Monitor de sesiones activas en el sistema.</p>
      
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Rol</th>
              <th>Hora de Conexión</th>
              <th>Estado</th>
              <th class="text-end">Acción</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(session, index) in activeSessions" :key="index">
              <td><strong><i class="bi bi-person-circle text-secondary me-2"></i> {{ session.username }}</strong></td>
              <td><span class="badge bg-info text-dark">{{ session.role }}</span></td>
              <td>{{ new Date(session.connectedAt).toLocaleString() }}</td>
              <td><span class="badge bg-success">En línea</span></td>
              <td class="text-end">
                <button class="btn btn-sm btn-outline-danger" @click="forceLogout(session.username)" :disabled="session.username === currentUserName">
                  <i class="bi bi-box-arrow-right"></i> Expulsar
                </button>
              </td>
            </tr>
            <tr v-if="activeSessions.length === 0">
              <td colspan="5" class="text-center text-muted py-3">No hay usuarios conectados en este momento.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="panel mt-4 d-print-none">
      <h2>Gestión de Servicios (Admin)</h2>
      <p class="text-muted mb-4">Administra el ciclo de vida de los microservicios en el sistema operativo local.</p>
      
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>Microservicio</th>
              <th>Puerto</th>
              <th>Descripción</th>
              <th class="text-end">Acciones de Energía</th>
            </tr>
          </thead>
          <tbody>
            <!-- Warehouse -->
            <tr>
              <td><strong><i class="bi bi-box-seam text-primary me-2"></i> ms-warehouse</strong></td>
              <td>8084</td>
              <td>Gestión de productos y control de stock.</td>
              <td class="text-end">
                <button v-if="statusMap['ms-warehouse'] === 'RUNNING'" class="btn btn-sm btn-danger" @click="toggleService('ms-warehouse', 'stop')" :disabled="loadingSvc['ms-warehouse']">
                  <i class="bi bi-power"></i> Apagar
                </button>
                <button v-else class="btn btn-sm btn-success" @click="toggleService('ms-warehouse', 'start')" :disabled="loadingSvc['ms-warehouse']">
                  <i class="bi bi-play-circle"></i> Encender
                </button>
              </td>
            </tr>
            <!-- Sales -->
            <tr>
              <td><strong><i class="bi bi-cart text-primary me-2"></i> ms-sales</strong></td>
              <td>8085</td>
              <td>Punto de venta y transacciones.</td>
              <td class="text-end">
                <button v-if="statusMap['ms-sales'] === 'RUNNING'" class="btn btn-sm btn-danger" @click="toggleService('ms-sales', 'stop')" :disabled="loadingSvc['ms-sales']">
                  <i class="bi bi-power"></i> Apagar
                </button>
                <button v-else class="btn btn-sm btn-success" @click="toggleService('ms-sales', 'start')" :disabled="loadingSvc['ms-sales']">
                  <i class="bi bi-play-circle"></i> Encender
                </button>
              </td>
            </tr>
            <!-- HR -->
            <tr>
              <td><strong><i class="bi bi-people text-primary me-2"></i> ms-hr</strong></td>
              <td>8086</td>
              <td>Gestión de personal y nómina.</td>
              <td class="text-end">
                <button v-if="statusMap['ms-hr'] === 'RUNNING'" class="btn btn-sm btn-danger" @click="toggleService('ms-hr', 'stop')" :disabled="loadingSvc['ms-hr']">
                  <i class="bi bi-power"></i> Apagar
                </button>
                <button v-else class="btn btn-sm btn-success" @click="toggleService('ms-hr', 'start')" :disabled="loadingSvc['ms-hr']">
                  <i class="bi bi-play-circle"></i> Encender
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Advanced KPI Reports -->
    <KpiReports />

    <!-- Predictive Analytics -->
    <PredictiveAnalytics />

    <!-- Pending Approvals Section -->
    <div class="panel mt-4 border-info border-top border-3 d-print-none">
      <h2><i class="bi bi-ui-checks text-info"></i> Aprobaciones Pendientes</h2>
      <p class="text-muted mb-4">Gestión de solicitudes de cambios de precio u otros flujos operativos.</p>
      
      <div v-if="approvalsError" class="alert alert-danger">{{ approvalsError }}</div>
      <div v-if="approvalsSuccess" class="alert alert-success">{{ approvalsSuccess }}</div>

      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>ID Solicitud</th>
              <th>Módulo</th>
              <th>Documento Relacionado</th>
              <th>Solicitante</th>
              <th>Descripción</th>
              <th class="text-end">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="req in pendingApprovals" :key="req.id">
              <td><strong>#{{ req.id }}</strong></td>
              <td><span class="badge bg-info text-dark">{{ req.modulo }}</span></td>
              <td>#{{ req.documentId }}</td>
              <td>{{ req.responsable }}</td>
              <td>{{ req.descripcion }}</td>
              <td class="text-end">
                <button class="btn btn-sm btn-success me-2" @click="handleApprove(req.documentId)" :disabled="isProcessingApproval">
                  <i class="bi bi-check-lg"></i> Autorizar
                </button>
                <button class="btn btn-sm btn-danger" @click="handleReject(req.documentId)" :disabled="isProcessingApproval">
                  <i class="bi bi-x-lg"></i> Rechazar
                </button>
              </td>
            </tr>
            <tr v-if="pendingApprovals.length === 0">
              <td colspan="6" class="text-center text-muted py-4">No hay solicitudes pendientes en este momento.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- System Users Accounts -->
    <div class="panel mt-4 border-warning border-top border-3 d-print-none">
      <h2><i class="bi bi-shield-lock-fill text-warning"></i> Cuentas de Acceso al Sistema</h2>
      <p class="text-muted mb-4">Consulta segura de credenciales. Por seguridad, debes ingresar tu contraseña de administrador para ver los datos de un usuario.</p>
      
      <div class="row mb-4">
        <div class="col-md-5">
          <label class="form-label">Buscar Usuario (Nombre o ID)</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" class="form-control" v-model="searchUserQuery" placeholder="Ej: ventas, 2, admin...">
          </div>
        </div>
        <div class="col-md-5">
          <label class="form-label">Tu Contraseña de Administrador</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-key-fill text-warning"></i></span>
            <input type="password" class="form-control" v-model="adminPasswordInput" placeholder="Confirma tu identidad..." @keyup.enter="handleSearchUser">
          </div>
        </div>
        <div class="col-md-2 d-flex align-items-end">
          <button class="btn btn-warning w-100 fw-bold" @click="handleSearchUser" :disabled="isSearchingUser">
            <span v-if="isSearchingUser" class="spinner-border spinner-border-sm me-2"></span>
            Buscar
          </button>
        </div>
      </div>
      
      <div v-if="searchUserError" class="alert alert-danger">
        <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ searchUserError }}
      </div>

      <div class="table-responsive" v-if="searchUserResult">
        <table class="table align-middle table-hover">
          <thead class="table-light">
            <tr>
              <th>ID</th>
              <th>Usuario</th>
              <th>Contraseña</th>
              <th>Rol</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in searchUserResult" :key="user.id">
              <td>{{ user.id }}</td>
              <td><strong>{{ user.username }}</strong></td>
              <td><code class="text-danger fs-6">{{ user.password }}</code></td>
              <td><span class="badge bg-secondary">{{ user.role }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { authService } from '../services/authService'
import { userService } from '../services/userService'
import { workflowService } from '../services/workflowService'
import KpiReports from '../components/KpiReports.vue'
import PredictiveAnalytics from '../components/PredictiveAnalytics.vue'

const kpis = ref(null)
const activeSessions = ref([])
const allUsers = ref([])
const dashboardUrl = import.meta.env.VITE_DASHBOARD_API_URL || '/api/dashboard'
const salesUrl = import.meta.env.VITE_SALES_API_URL || '/api/sales'
const authUrl = import.meta.env.VITE_AUTH_API_URL || '/api/auth'
const currentUserName = ref(sessionStorage.getItem('username'))

const timeFilter = ref('all')
const allSalesData = ref([])

// Security search states
const searchUserQuery = ref('')
const adminPasswordInput = ref('')
const searchUserResult = ref(null)
const searchUserError = ref('')
const isSearchingUser = ref(false)

// Approvals state
const pendingApprovals = ref([])
const isProcessingApproval = ref(false)
const approvalsError = ref('')
const approvalsSuccess = ref('')

// Status management
const statusMap = ref({
  'ms-warehouse': 'STOPPED',
  'ms-sales': 'STOPPED',
  'ms-hr': 'STOPPED'
})
const loadingSvc = ref({})

const handleSearchUser = async () => {
  searchUserError.value = ''
  searchUserResult.value = null
  
  if (!searchUserQuery.value || !adminPasswordInput.value) {
    searchUserError.value = 'Debes ingresar el usuario/ID a buscar y tu contraseña de administrador.'
    return
  }
  
  isSearchingUser.value = true
  
  try {
    const adminUsername = sessionStorage.getItem('username') || 'admin'
    // Verify admin credentials
    await axios.post(`${authUrl}/verify-password`, {
      username: adminUsername,
      password: adminPasswordInput.value
    })
    
    // Perform search
    const query = searchUserQuery.value.toString().toLowerCase()
    const found = allUsers.value.find(u => 
      u.username.toLowerCase() === query || 
      u.id.toString() === query
    )
    
    if (found) {
      searchUserResult.value = [found]
    } else {
      searchUserError.value = 'Usuario no encontrado en el sistema.'
    }
  } catch (err) {
    if (err.response && err.response.status === 401) {
      searchUserError.value = 'Contraseña de administrador incorrecta. Acceso denegado.'
    } else {
      searchUserError.value = 'Error de conexión con el servicio de autenticación.'
    }
  } finally {
    isSearchingUser.value = false
    adminPasswordInput.value = '' // Clear password field
  }
}

const applyTimeFilter = () => {
  if (!kpis.value) return
  if (timeFilter.value === 'all') {
     let totalRev = 0
     allSalesData.value.forEach(s => totalRev += s.total)
     kpis.value.totalSales = allSalesData.value.length
     kpis.value.totalRevenue = totalRev
     return
  }
  
  const now = new Date()
  let cutoff = new Date()
  if (timeFilter.value === 'day') {
    cutoff.setHours(0,0,0,0) // Inicio de hoy
  } else if (timeFilter.value === 'week') {
    cutoff.setDate(now.getDate() - 7)
  } else if (timeFilter.value === 'month') {
    cutoff.setMonth(now.getMonth() - 1)
  } else if (timeFilter.value === '6months') {
    cutoff.setMonth(now.getMonth() - 6)
  } else if (timeFilter.value === 'year') {
    cutoff.setFullYear(now.getFullYear() - 1)
  }
  
  const filtered = allSalesData.value.filter(s => new Date(s.saleDate) >= cutoff)
  
  let totalRev = 0
  filtered.forEach(s => totalRev += s.total)
  
  kpis.value.totalSales = filtered.length
  kpis.value.totalRevenue = totalRev
}

const loadKpis = async () => {
  try {
    const res = await axios.get(dashboardUrl + '/kpi')
    kpis.value = res.data
    
    // Cargar ventas en crudo para aplicar filtro de tiempo en el frontend
    try {
      const salesRes = await axios.get(salesUrl)
      allSalesData.value = salesRes.data
      applyTimeFilter()
    } catch(err) {
      console.warn("No se pudieron cargar las ventas para el filtro", err)
    }
    
  } catch (err) {
    console.error("Error cargando KPIs", err)
  }
}

const loadActiveSessions = async () => {
  try {
    const res = await authService.obtenerUsuariosConectados()
    activeSessions.value = res.data
  } catch (err) {
    console.error("Error cargando sesiones", err)
  }
}

const forceLogout = async (username) => {
  if (confirm(`¿Estás seguro de que deseas cerrar la sesión de ${username}?\nEl usuario será desconectado inmediatamente.`)) {
    try {
      await authService.forzarCierreSesion(username)
      await loadActiveSessions() // Refresh table
    } catch (err) {
      alert(`Error al cerrar sesión: ${err.response?.data?.error || err.message}`)
    }
  }
}

const loadAllUsers = async () => {
  try {
    allUsers.value = await userService.getUsers()
  } catch (err) {
    console.error("Error cargando usuarios", err)
  }
}

const loadPendingApprovals = async () => {
  try {
    const token = sessionStorage.getItem('token')
    if (token) {
      const res = await workflowService.getPendientes(token)
      pendingApprovals.value = res.data || []
    }
  } catch (err) {
    console.error("Error cargando aprobaciones pendientes", err)
  }
}

const handleApprove = async (documentId) => {
  isProcessingApproval.value = true
  approvalsError.value = ''
  approvalsSuccess.value = ''
  try {
    const token = sessionStorage.getItem('token')
    await workflowService.aprobarDocumento(documentId, token)
    approvalsSuccess.value = `Documento #${documentId} aprobado correctamente.`
    window.dispatchEvent(new Event('workflow-updated'))
    await loadPendingApprovals()
    setTimeout(() => approvalsSuccess.value = '', 4000)
  } catch (err) {
    approvalsError.value = `Error al aprobar: ${err.response?.data?.error || err.message}`
  } finally {
    isProcessingApproval.value = false
  }
}

const handleReject = async (documentId) => {
  if (!confirm(`¿Estás seguro de que deseas rechazar el documento #${documentId}?`)) return
  
  isProcessingApproval.value = true
  approvalsError.value = ''
  approvalsSuccess.value = ''
  try {
    const token = sessionStorage.getItem('token')
    await workflowService.rechazarDocumento(documentId, token)
    approvalsSuccess.value = `Documento #${documentId} rechazado.`
    window.dispatchEvent(new Event('workflow-updated'))
    await loadPendingApprovals()
    setTimeout(() => approvalsSuccess.value = '', 4000)
  } catch (err) {
    approvalsError.value = `Error al rechazar: ${err.response?.data?.error || err.message}`
  } finally {
    isProcessingApproval.value = false
  }
}

const checkServiceStatus = async (serviceName) => {
  try {
    const res = await axios.get(`${dashboardUrl}/admin/${serviceName}/status`)
    statusMap.value[serviceName] = res.data.status
  } catch (err) {
    statusMap.value[serviceName] = 'STOPPED'
  }
}

const toggleService = async (serviceName, action) => {
  loadingSvc.value[serviceName] = true
  try {
    const res = await axios.post(`${dashboardUrl}/admin/${serviceName}/${action}`)
    statusMap.value[serviceName] = res.data.status
    if (action === 'start') {
      alert(`Servicio ${serviceName} iniciando. Espera unos segundos.`)
    }
  } catch (err) {
    alert(`Error: ${err.message}`)
  } finally {
    loadingSvc.value[serviceName] = false
    checkServiceStatus(serviceName)
  }
}

let intervalId = null

onMounted(() => {
  loadKpis()
  loadActiveSessions()
  loadAllUsers()
  loadPendingApprovals()
  // Initial check
  checkServiceStatus('ms-warehouse')
  checkServiceStatus('ms-sales')
  checkServiceStatus('ms-hr')
  
  // Refresh kpis and statuses periodically
  intervalId = setInterval(() => {
    loadKpis()
    loadActiveSessions()
    loadPendingApprovals()
    checkServiceStatus('ms-warehouse')
    checkServiceStatus('ms-sales')
    checkServiceStatus('ms-hr')
  }, 10000)
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>
