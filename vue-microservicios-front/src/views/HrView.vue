<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Gestión de Personal</span>
        <h1>Recursos Humanos</h1>
        <p>Administración de empleados, asistencia y control de personal.</p>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <div class="me-3">
          <select v-model="selectedArea" class="form-select form-select-sm">
            <option value="">Todas las Áreas</option>
            <option v-for="area in availableAreas" :key="area" :value="area">{{ area }}</option>
          </select>
        </div>
          <div class="form-check form-switch me-3 d-print-none">
            <input class="form-check-input" type="checkbox" id="showBajas" v-model="showBajas">
            <label class="form-check-label" for="showBajas">Mostrar Bajas</label>
          </div>
          <button class="btn btn-outline-secondary d-print-none" @click="generateReport">
            <i class="bi bi-file-earmark-bar-graph"></i> Generar Reporte
          </button>
          <button class="btn btn-primary d-print-none" @click="showAddModal = true">
          <i class="bi bi-person-plus"></i> Nuevo Empleado
        </button>
      </div>
    </div>

    <!-- El Reporte Analítico ahora se abre en una nueva pestaña -->

    <!-- Printable Contract Section -->
    <div v-if="printMode === 'contract' && contractEmployee" class="d-none d-print-block contract-print-area">
      <div class="contract-header d-flex justify-content-between align-items-center mb-5 border-bottom pb-4">
        <div class="contract-company-info">
          <h2 class="mb-1 text-primary" style="font-weight: 800; font-size: 2rem;">NORTH SOLUTIONS</h2>
          <p class="mb-0 text-muted">S.A. DE C.V.</p>
        </div>
        <div class="text-end text-muted small">
          <p class="mb-0">San Felipe del Progreso</p>
          <p class="mb-0">Estado de México, C.P. 50640</p>
          <p class="mb-0">contacto@northsolutions.mx | Tel: 555-123-4567</p>
        </div>
      </div>
      
      <h3 class="text-center mb-5" style="text-decoration: underline; font-weight: bold;">
        CONTRATO INDIVIDUAL DE TRABAJO POR TIEMPO INDEFINIDO
      </h3>
      
      <div class="contract-body" style="line-height: 1.8; text-align: justify; font-size: 1.1rem;">
        <p>
          En San Felipe del Progreso, Estado de México, a <strong>{{ new Date().toLocaleDateString('es-MX', { year: 'numeric', month: 'long', day: 'numeric' }) }}</strong>, los que suscriben:
        </p>
        <p>
          Por una parte, la empresa <strong>NORTH SOLUTIONS S.A. DE C.V.</strong>, legalmente constituida conforme a las leyes de la República Mexicana, que en lo sucesivo se denominará <strong>"EL PATRÓN"</strong>.
        </p>
        <p>
          Y por la otra parte, <strong>{{ contractEmployee.firstName }} {{ contractEmployee.lastName }}</strong>, que en lo sucesivo se denominará <strong>"EL TRABAJADOR"</strong>, celebran el presente CONTRATO INDIVIDUAL DE TRABAJO POR TIEMPO INDEFINIDO, sujeto a las siguientes cláusulas:
        </p>
        
        <h4 class="mt-4 mb-3 font-weight-bold">CLÁUSULAS</h4>
        
        <p><strong>PRIMERA.</strong> EL PATRÓN contrata a EL TRABAJADOR por tiempo indefinido para prestar sus servicios personales y subordinados bajo su dirección y dependencia.</p>
        
        <p><strong>SEGUNDA.</strong> EL TRABAJADOR se obliga a prestar sus servicios como <strong>{{ contractEmployee.position }}</strong>, realizando todas las actividades inherentes a dicho puesto y cualquier otra que sea compatible con sus fuerzas, aptitudes, estado o condición, y que sea del mismo género de las que forman el objeto de la empresa.</p>
        
        <p><strong>TERCERA.</strong> El salario mensual que percibirá EL TRABAJADOR por la prestación de sus servicios será la cantidad neta de <strong>${{ contractEmployee.salary.toFixed(2) }} MXN</strong> (Moneda Nacional), mismos que serán pagados de manera quincenal en el lugar de trabajo o mediante transferencia electrónica.</p>
        
        <p><strong>CUARTA.</strong> La duración de la jornada de trabajo será la establecida por la Ley Federal del Trabajo, concediendo a EL TRABAJADOR el descanso semanal y los días de descanso obligatorio correspondientes, con goce de salario íntegro.</p>

        <p><strong>QUINTA.</strong> EL TRABAJADOR reconoce que toda la información, procesos, metodologías y datos a los que tenga acceso durante su relación laboral con EL PATRÓN son de carácter estrictamente confidencial.</p>
        
        <p class="mt-5">
          Leído que fue el presente contrato por las partes y enteradas de su contenido y alcance legal, lo firman por duplicado en el lugar y fecha arriba indicados, quedando un ejemplar en poder de cada una de las partes.
        </p>
        
        <div class="row mt-5 pt-5 text-center">
          <div class="col-6">
            <div style="border-top: 1px solid #000; width: 80%; margin: 0 auto; padding-top: 10px;">
              <strong>EL PATRÓN</strong><br>
              Representante Legal<br>
              North Solutions S.A. de C.V.
            </div>
          </div>
          <div class="col-6">
            <div style="border-top: 1px solid #000; width: 80%; margin: 0 auto; padding-top: 10px;">
              <strong>EL TRABAJADOR</strong><br>
              {{ contractEmployee.firstName }} {{ contractEmployee.lastName }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <div class="panel">
      <h2>Plantilla de Empleados</h2>
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre Completo</th>
              <th>Puesto</th>
              <th>Salario</th>
              <th>Asistencia Hoy</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredEmployees.length === 0">
              <td colspan="6" class="text-center text-muted py-4">No hay empleados para mostrar.</td>
            </tr>
            <tr v-for="emp in filteredEmployees" :key="emp.id" :class="{'opacity-50 bg-light': emp.status === 'SUSPENDIDO', 'text-decoration-line-through text-muted': emp.status === 'BAJA'}">
              <td>
                <strong>{{ emp.id }}</strong>
                <span v-if="emp.status === 'SUSPENDIDO'" class="badge bg-warning text-dark ms-1">Suspendido</span>
                <span v-if="emp.status === 'BAJA'" class="badge bg-danger ms-1">Baja</span>
              </td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <div class="avatar bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 32px; height: 32px;">
                    {{ emp.firstName.charAt(0) }}{{ emp.lastName.charAt(0) }}
                  </div>
                  {{ emp.firstName }} {{ emp.lastName }}
                </div>
              </td>
              <td>
                <div>{{ emp.position }}</div>
                <div class="small text-muted" style="font-size: 0.8em;">({{ emp.roleName || 'Sin Área Asignada' }})</div>
              </td>
              <td>${{ emp.salary.toFixed(2) }}</td>
              <td>
                <div class="d-flex gap-2">
                  <button class="btn btn-sm btn-outline-success" 
                          :disabled="emp.hasAttendanceToday"
                          :class="{'active': emp.attendanceStatus === 'PRESENT'}"
                          @click="markAttendance(emp.id, 'PRESENT')">
                    <i class="bi bi-check-circle"></i> Presente
                  </button>
                  <button class="btn btn-sm btn-outline-warning" 
                          :disabled="emp.hasAttendanceToday"
                          :class="{'active': emp.attendanceStatus === 'LATE'}"
                          @click="markAttendance(emp.id, 'LATE')">
                    <i class="bi bi-clock"></i> Retardo
                  </button>
                  <button class="btn btn-sm btn-outline-danger" 
                          :disabled="emp.hasAttendanceToday"
                          :class="{'active': emp.attendanceStatus === 'ABSENT'}"
                          @click="markAttendance(emp.id, 'ABSENT')">
                    <i class="bi bi-x-circle"></i> Falta
                  </button>
                </div>
              </td>
              <td>
                <div class="d-flex gap-1 flex-wrap">
                  <button class="btn btn-sm btn-outline-info" @click="openDocModal(emp)" title="Expediente de Documentos">
                    <i class="bi bi-folder2-open"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-secondary" @click="downloadContract(emp)" title="Descargar Contrato">
                    <i class="bi bi-file-earmark-pdf"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-primary" @click="openEditModal(emp)" title="Editar">
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button v-if="!emp.status || emp.status === 'ACTIVO'" class="btn btn-sm btn-outline-warning" @click="promptStatusChange(emp, 'SUSPENDIDO')" title="Suspender">
                    <i class="bi bi-pause-circle"></i>
                  </button>
                  <button v-if="emp.status === 'SUSPENDIDO' || emp.status === 'BAJA'" class="btn btn-sm btn-outline-success" @click="promptStatusChange(emp, 'ACTIVO')" title="Reactivar">
                    <i class="bi bi-play-circle"></i>
                  </button>
                  <button v-if="emp.status !== 'BAJA'" class="btn btn-sm btn-outline-danger" @click="promptStatusChange(emp, 'BAJA')" title="Eliminar (Baja)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Nuevo Empleado -->
    <div v-if="showAddModal" class="modal-backdrop">
      <div class="panel modal-content">
        <h2>Alta de Empleado</h2>
        <form @submit.prevent="saveEmployee" class="form-stack">
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Nombre</label>
              <input v-model="newEmp.firstName" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Apellidos</label>
              <input v-model="newEmp.lastName" type="text" class="form-control" required>
            </div>
          </div>
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Puesto</label>
              <input v-model="newEmp.position" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Salario Mensual</label>
              <input v-model.number="newEmp.salary" type="number" step="0.01" class="form-control" required>
            </div>
          </div>
          <hr>
          <h5>Credenciales de Acceso</h5>
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Usuario</label>
              <input v-model="newUser.username" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Contraseña</label>
              <input v-model="newUser.password" type="password" class="form-control" required>
            </div>
          </div>
          <div>
            <label>Rol de Sistema</label>
            <select v-model="newUser.role" class="form-select" required>
              <option value="ROLE_SALES">Ventas (Comercial)</option>
              <option value="ROLE_WAREHOUSE">Almacén (Logística)</option>
              <option value="ROLE_HR">RRHH (Recursos Humanos)</option>
              <option value="ADMIN">Administrador General</option>
            </select>
          </div>
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary w-50" :disabled="isSaving">
              {{ isSaving ? 'Guardando...' : 'Registrar Empleado' }}
            </button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="showAddModal = false" :disabled="isSaving">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Editar Empleado -->
    <div v-if="showEditModal" class="modal-backdrop">
      <div class="panel modal-content">
        <h2>Editar Empleado</h2>
        <form @submit.prevent="updateEmployee" class="form-stack">
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Nombre</label>
              <input v-model="editEmp.firstName" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Apellidos</label>
              <input v-model="editEmp.lastName" type="text" class="form-control" required>
            </div>
          </div>
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Puesto</label>
              <input v-model="editEmp.position" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Salario Mensual</label>
              <input v-model.number="editEmp.salary" type="number" step="0.01" class="form-control" required>
            </div>
          </div>
          <hr>
          <h5>Credenciales de Acceso</h5>
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Usuario</label>
              <input v-model="editUser.username" type="text" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Contraseña</label>
              <input v-model="editUser.password" type="text" class="form-control" required>
            </div>
          </div>
          <div>
            <label>Rol de Sistema</label>
            <select v-model="editUser.role" class="form-select" required>
              <option value="ROLE_SALES">Ventas (Comercial)</option>
              <option value="ROLE_WAREHOUSE">Almacén (Logística)</option>
              <option value="ROLE_HR">RRHH (Recursos Humanos)</option>
              <option value="ADMIN">Administrador General</option>
            </select>
          </div>
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary w-50" :disabled="isSaving">
              {{ isSaving ? 'Guardando...' : 'Actualizar Datos' }}
            </button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="showEditModal = false" :disabled="isSaving">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Autorización de RH -->
    <div v-if="showAuthModal" class="modal-backdrop" style="z-index: 1050;">
      <div class="panel modal-content" style="max-width: 400px;">
        <h3 class="text-danger"><i class="bi bi-shield-lock"></i> Autorización de RH</h3>
        <p>Introduce tu contraseña de Recursos Humanos para autorizar este cambio.</p>
        <form @submit.prevent="executeStatusChange" class="form-stack">
          <div>
            <label>Contraseña:</label>
            <input v-model="hrAuthPassword" type="password" class="form-control" required autofocus>
          </div>
          <div v-if="authError" class="text-danger small mt-1">{{ authError }}</div>
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-danger w-50" :disabled="isAuthorizing">
              {{ isAuthorizing ? 'Validando...' : 'Autorizar' }}
            </button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="showAuthModal = false" :disabled="isAuthorizing">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Expediente de Documentos -->
    <div v-if="showDocModal" class="modal-backdrop">
      <div class="panel modal-content" style="max-width: 600px;">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h2><i class="bi bi-folder2-open text-primary"></i> Expediente de {{ docEmployee?.firstName }}</h2>
          <button class="btn-close" @click="showDocModal = false"></button>
        </div>
        
        <p class="text-muted mb-4">Sube o reemplaza los documentos oficiales del empleado. Solo se admiten archivos PDF o imágenes.</p>
        
        <div class="list-group mb-4">
          <div v-for="docType in docTypes" :key="docType.id" class="list-group-item d-flex justify-content-between align-items-center">
            <div>
              <strong>{{ docType.label }}</strong><br>
              <span v-if="hasDocument(docType.id)" class="badge bg-success"><i class="bi bi-check-circle"></i> Subido</span>
              <span v-else class="badge bg-secondary"><i class="bi bi-exclamation-circle"></i> Falta</span>
            </div>
            <div class="d-flex gap-2 align-items-center">
              <a v-if="hasDocument(docType.id)" :href="getDocumentUrl(docType.id)" target="_blank" class="btn btn-sm btn-outline-primary" title="Ver Documento">
                <i class="bi bi-eye"></i> Ver
              </a>
              <label class="btn btn-sm btn-outline-secondary mb-0" style="cursor:pointer;" title="Subir o Reemplazar">
                <i class="bi bi-upload"></i> Subir
                <input type="file" style="display:none;" accept=".pdf,image/*" @change="e => uploadDocument(e, docType.id)">
              </label>
            </div>
          </div>
        </div>
        
        <div class="text-end">
          <button class="btn btn-primary" @click="showDocModal = false">Cerrar Expediente</button>
        </div>
      </div>
    </div>

  </section>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import hrService from '../services/hrService'
import userService from '../services/userService'
import { authService } from '../services/authService'

const employees = ref([])
const error = ref('')
const success = ref('')

const showAddModal = ref(false)
const showEditModal = ref(false)
const isSaving = ref(false)
const newEmp = ref({ firstName: '', lastName: '', position: '', salary: 0 })
const editEmp = ref({ id: null, firstName: '', lastName: '', position: '', salary: 0, username: '' })
const newUser = ref({ username: '', password: '', role: 'ROLE_SALES' })
const editUser = ref({ id: null, username: '', password: '', role: 'ROLE_SALES', originalUsername: '' })

const showBajas = ref(false)
const selectedArea = ref('')
const showAuthModal = ref(false)
const hrAuthPassword = ref('')
const authError = ref('')
const isAuthorizing = ref(false)
const pendingStatusChange = ref(null)

const printMode = ref('report')
const contractEmployee = ref(null)

// Docs State
const showDocModal = ref(false)
const docEmployee = ref(null)
const uploadedDocs = ref([])
const docTypes = [
  { id: 'INE', label: 'Identificación Oficial (INE/Pasaporte)' },
  { id: 'CURP', label: 'Comprobante de CURP' },
  { id: 'DOMICILIO', label: 'Comprobante de Domicilio' },
  { id: 'CONTRATO', label: 'Contrato Firmado' }
]

const availableAreas = computed(() => {
  let activeEmps = employees.value
  if (!showBajas.value) {
    activeEmps = employees.value.filter(e => e.status !== 'BAJA')
  }
  const areas = activeEmps.map(e => e.position).filter(Boolean)
  return [...new Set(areas)].sort()
})

const filteredEmployees = computed(() => {
  let result = employees.value
  
  if (!showBajas.value) {
    result = result.filter(e => e.status !== 'BAJA')
  }
  
  if (selectedArea.value) {
    result = result.filter(e => e.position === selectedArea.value)
  }
  
  return result
})

const roleMap = {
  'ROLE_SALES': 'Ventas',
  'ROLE_WAREHOUSE': 'Almacén',
  'ROLE_HR': 'RH',
  'ADMIN': 'Dirección/General'
}

const loadEmployees = async () => {
  try {
    const [empsData, usersData, attendanceData] = await Promise.all([
      hrService.getEmployees(),
      userService.getUsers().catch(() => []), // No fallar si users está caído
      hrService.getAllAttendance().catch(() => []) // No fallar si attendance falla
    ])
    
    // Obtener la fecha local de hoy en formato YYYY-MM-DD
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    const todayStr = `${year}-${month}-${day}`
    
    // Filtrar registros de asistencia de hoy
    const todayAttendances = attendanceData.filter(a => a.date === todayStr)
    
    employees.value = empsData.map(emp => {
      const user = usersData.find(u => u.username === emp.username)
      // Buscar si el empleado tiene un registro hoy (ordenado para tomar el último si hay múltiples)
      const empAttendances = todayAttendances.filter(a => a.employee.id === emp.id)
      const empAttendance = empAttendances.length > 0 ? empAttendances[empAttendances.length - 1] : null
      
      return {
        ...emp,
        roleName: user && roleMap[user.role] ? roleMap[user.role] : (user?.role || 'Sin área'),
        hasAttendanceToday: !!empAttendance,
        attendanceStatus: empAttendance ? empAttendance.status : null
      }
    })
  } catch (err) {
    error.value = 'Error al cargar empleados: ' + err.message
  }
}

const saveEmployee = async () => {
  try {
    error.value = ''
    isSaving.value = true
    
    // 1. Create user account
    await userService.createUser(newUser.value)
    
    // 2. Create employee record
    newEmp.value.username = newUser.value.username
    await hrService.addEmployee(newEmp.value)
    
    success.value = 'Empleado y usuario registrados con éxito.'
    showAddModal.value = false
    newEmp.value = { firstName: '', lastName: '', position: '', salary: 0 }
    newUser.value = { username: '', password: '', role: 'ROLE_SALES' }
    loadEmployees()
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    error.value = 'Error al registrar: ' + (err.response?.data?.message || err.response?.data?.error || err.message || err)
  } finally {
    isSaving.value = false
  }
}

const openEditModal = async (emp) => {
  editEmp.value = { ...emp }
  editUser.value = { id: null, username: emp.username, password: '', role: 'ROLE_SALES', originalUsername: emp.username }
  
  try {
    // Try to load user data to populate role and password
    const user = await userService.obtenerUsuarioPorUsername(emp.username)
    if (user) {
      editUser.value.id = user.id
      editUser.value.password = user.password
      editUser.value.role = user.role
    }
  } catch (err) {
    console.warn("No se pudo cargar el usuario de este empleado", err)
  }
  
  showEditModal.value = true
}

const updateEmployee = async () => {
  try {
    error.value = ''
    isSaving.value = true
    
    // 1. Update or Create user
    if (editUser.value.id) {
      // User exists, update it
      await userService.updateUser(editUser.value.originalUsername, {
        username: editUser.value.username,
        password: editUser.value.password,
        role: editUser.value.role
      })
    } else if (editUser.value.username) {
      // User doesn't exist in ms-user, create it
      await userService.createUser({
        username: editUser.value.username,
        password: editUser.value.password,
        role: editUser.value.role
      })
    }
    
    // 2. Update employee
    editEmp.value.username = editUser.value.username
    await hrService.updateEmployee(editEmp.value.id, editEmp.value)
    
    success.value = 'Datos del empleado y usuario actualizados con éxito.'
    showEditModal.value = false
    await loadEmployees() // Await to ensure UI is updated
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    let msg = err.response?.data?.message || err.response?.data?.error || err.message
    error.value = 'Error al actualizar: ' + msg
    alert('Error al actualizar: ' + msg) // Force user to see the error
  } finally {
    isSaving.value = false
  }
}

const markAttendance = async (employeeId, status) => {
  try {
    error.value = ''
    const username = sessionStorage.getItem('username') || 'Usuario Desconocido'
    await hrService.registerAttendance(employeeId, status, username)
    
    // Deshabilitar los botones inmediatamente sin recargar todo
    const emp = employees.value.find(e => e.id === employeeId)
    if (emp) {
      emp.hasAttendanceToday = true
      emp.attendanceStatus = status
    }
    
    let statusText = status === 'PRESENT' ? 'Asistencia' : status === 'LATE' ? 'Retardo' : 'Falta'
    success.value = `${statusText} registrada con éxito.`
    
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    let msg = err.response?.data?.message || err.response?.data || err.message
    if (typeof msg === 'object') msg = 'Ya existe un registro para hoy o ocurrió un error.'
    if (err.response?.status === 500 && typeof err.response?.data === 'string' && err.response.data.includes('Ya se registró')) {
       msg = 'Ya se registró una asistencia, retardo o falta para este empleado el día de hoy.'
    }
    error.value = 'Error al registrar asistencia: ' + msg
  }
}

  const generateReport = () => {
    window.open('/hr-report', '_blank')
  }

const downloadContract = async (emp) => {
  // Ensure we have the absolute latest data before printing
  try {
    const latestEmp = await hrService.getEmployees().then(emps => emps.find(e => e.id === emp.id))
    contractEmployee.value = latestEmp || emp
  } catch (e) {
    contractEmployee.value = emp
  }
  printMode.value = 'contract'
  setTimeout(() => {
    window.print()
  }, 300)
}

const promptStatusChange = (emp, newStatus) => {
  pendingStatusChange.value = { id: emp.id, status: newStatus }
  hrAuthPassword.value = ''
  authError.value = ''
  showAuthModal.value = true
}

const executeStatusChange = async () => {
  isAuthorizing.value = true
  authError.value = ''
  try {
    const hrUsername = sessionStorage.getItem('username')
    // Validar contraseña llamando al endpoint especial para no crear conflicto de sesión
    const authRes = await authService.verifyPassword(hrUsername, hrAuthPassword.value)
    
    if (authRes && authRes.data && authRes.data.valid) {
      // Contraseña correcta, procedemos a cambiar el status
      await hrService.changeStatus(pendingStatusChange.value.id, pendingStatusChange.value.status)
      success.value = 'Estado actualizado correctamente.'
      showAuthModal.value = false
      loadEmployees()
      setTimeout(() => success.value = '', 3000)
    } else {
      authError.value = 'Contraseña incorrecta.'
    }
  } catch (err) {
    authError.value = 'Credenciales inválidas o no tienes permisos.'
  } finally {
    isAuthorizing.value = false
  }
}

const openDocModal = async (emp) => {
  docEmployee.value = emp
  showDocModal.value = true
  await loadEmployeeDocs(emp.id)
}

const loadEmployeeDocs = async (empId) => {
  try {
    uploadedDocs.value = await hrService.getEmployeeDocuments(empId)
  } catch (err) {
    console.warn("No se pudieron cargar los documentos", err)
    uploadedDocs.value = []
  }
}

const hasDocument = (type) => {
  return uploadedDocs.value.some(d => d.type === type)
}

const getDocumentUrl = (type) => {
  if (!docEmployee.value) return '#'
  return hrService.getDocumentUrl(docEmployee.value.id, type)
}

const uploadDocument = async (event, type) => {
  const file = event.target.files[0]
  if (!file) return
  
  if (file.size > 5 * 1024 * 1024) { // 5MB limit
    alert('El archivo es demasiado grande. El máximo permitido es 5MB.')
    return
  }

  try {
    success.value = `Subiendo documento ${type}...`
    await hrService.uploadDocument(docEmployee.value.id, file, type)
    success.value = 'Documento guardado exitosamente.'
    await loadEmployeeDocs(docEmployee.value.id)
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    alert('Error al subir el documento: ' + (err.response?.data?.message || err.message))
    success.value = ''
  }
  // Reset file input
  event.target.value = ''
}

onMounted(() => {
  loadEmployees()
})
</script>

<style scoped>
@media print {
  @page {
    margin: 0; /* Remueve el encabezado y pie de página predeterminado del navegador */
  }
  .page-header, .panel, .modal-backdrop, .alert, .d-print-none {
    display: none !important;
  }
  .d-print-block {
    display: block !important;
  }
  body {
    background: white;
    margin: 0;
    padding: 0;
  }
  .contract-print-area {
    padding: 2.5cm 2.5cm !important; /* Añade margen interno para compensar el margin 0 de la página */
    box-sizing: border-box;
    width: 100%;
  }
}

.modal-backdrop {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  width: 100%;
  max-width: 500px;
}
</style>
