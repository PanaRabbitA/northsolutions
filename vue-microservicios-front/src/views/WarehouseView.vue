<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Módulo Logístico</span>
        <h1>Gestión de Almacenes</h1>
        <p>Control de inventario, registro de productos y movimientos de entrada/salida.</p>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <button class="btn btn-outline-secondary d-print-none" @click="printReport">
          <i class="bi bi-file-earmark-bar-graph"></i> Generar Reporte
        </button>
        <button class="btn btn-primary d-print-none" @click="showAddModal = true">
          <i class="bi bi-plus-lg"></i> Nuevo Producto
        </button>
      </div>
    </div>

    <!-- Printable Report logic migrated to new tab -->

    <!-- Error/Success Alerts -->
    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <div class="panel">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="m-0">Catálogo de Productos</h2>
        <div class="d-flex gap-2 w-50 justify-content-end">
          <input v-model="searchName" type="text" class="form-control form-control-sm w-50" placeholder="Buscar por nombre o SKU...">
          <select v-model="selectedCategoryFilter" class="form-select form-select-sm w-auto">
            <option value="">Todas las Categorías</option>
            <option value="Redes">Redes</option>
            <option value="Telecomunicaciones">Telecomunicaciones</option>
            <option value="Combos">Combos</option>
            <option value="Software">Software</option>
            <option value="Servicios">Servicios</option>
            <option value="Sin Categoría">Sin Categoría</option>
          </select>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>SKU</th>
              <th>Nombre</th>
              <th>Categoría</th>
              <th>Precio</th>
              <th>Stock</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredProducts.length === 0">
              <td colspan="6" class="text-center text-muted py-4">No hay productos registrados en esta categoría.</td>
            </tr>
            <tr v-for="product in filteredProducts" :key="product.id">
              <td><code>{{ product.sku }}</code></td>
              <td>
                <strong>{{ product.name }}</strong>
                <br><small class="text-muted">{{ product.description }}</small>
              </td>
              <td><span class="badge bg-secondary">{{ product.category || 'Sin Categoría' }}</span></td>
              <td>${{ product.price.toFixed(2) }}</td>
              <td>
                <span class="badge" :class="product.stock > 10 ? 'bg-success' : 'bg-danger'">
                  {{ product.stock }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-2">
                  <button class="btn btn-sm btn-outline-success" @click="openTransactionModal(product, 'IN')">
                    <i class="bi bi-box-arrow-in-down"></i> Entrada
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="openTransactionModal(product, 'OUT')">
                    <i class="bi bi-box-arrow-up"></i> Salida
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Nuevo Producto -->
    <Teleport to="body">
      <div v-if="showAddModal" class="custom-modal-backdrop">
      <div class="panel custom-modal-content">
        <h2>Nuevo Producto</h2>
        <form @submit.prevent="saveProduct" class="form-stack">
          <div class="mb-2">
            <label class="d-block text-muted small">SKU Asignado</label>
            <div class="p-2 bg-light border rounded">
              <strong>{{ newProduct.sku || 'Se generará al seleccionar categoría' }}</strong>
            </div>
          </div>
          <div>
            <label>Nombre</label>
            <input v-model="newProduct.name" type="text" class="form-control" required>
          </div>
          <div>
            <label>Descripción</label>
            <input v-model="newProduct.description" type="text" class="form-control">
          </div>
          <div>
            <label>Categoría</label>
            <select v-model="newProduct.category" class="form-select" @change="generateSKU" required>
              <option value="" disabled>Seleccione una categoría</option>
              <option value="Redes">Redes</option>
              <option value="Telecomunicaciones">Telecomunicaciones</option>
              <option value="Combos">Combos</option>
              <option value="Software">Software</option>
              <option value="Servicios">Servicios</option>
            </select>
          </div>
          <div class="d-flex gap-3">
            <div class="w-50">
              <label>Precio</label>
              <input v-model.number="newProduct.price" type="number" step="0.01" class="form-control" required>
            </div>
            <div class="w-50">
              <label>Stock Inicial</label>
              <input v-model.number="newProduct.stock" type="number" class="form-control" required>
            </div>
          </div>
          <div class="d-flex gap-2 mt-2">
            <button type="submit" class="btn btn-primary w-50">Guardar</button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="showAddModal = false">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
    </Teleport>

    <!-- Modal Transacción (Entrada/Salida) -->
    <Teleport to="body">
      <div v-if="showTxModal" class="custom-modal-backdrop">
      <div class="panel custom-modal-content">
        <h2>{{ txType === 'IN' ? 'Entrada de Mercancía' : 'Salida de Mercancía' }}</h2>
        <p class="text-muted">Producto: <strong>{{ selectedProduct?.name }}</strong> (Actual: {{ selectedProduct?.stock }})</p>
        <form @submit.prevent="submitTransaction" class="form-stack">
          <div>
            <label>Cantidad</label>
            <input v-model.number="txQuantity" type="number" class="form-control" min="1" required>
          </div>
          <div>
            <label>Motivo</label>
            <input v-model="txReason" type="text" class="form-control" placeholder="Ej: Compra a proveedor" required>
          </div>
          <div class="d-flex gap-2 mt-2">
            <button type="submit" class="btn" :class="txType === 'IN' ? 'btn-success' : 'btn-danger'">
              Confirmar {{ txType === 'IN' ? 'Entrada' : 'Salida' }}
            </button>
            <button type="button" class="btn btn-outline-secondary" @click="showTxModal = false">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
    </Teleport>

  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import warehouseService from '../services/warehouseService'
import hrService from '../services/hrService'

const products = ref([])
const error = ref('')
const success = ref('')
const currentUserName = ref(sessionStorage.getItem('username') || 'Usuario')

// Modal state
const showAddModal = ref(false)
const showTxModal = ref(false)

// Forms state
const newProduct = ref({ sku: '', name: '', description: '', price: 0, stock: 0, category: '' })
const selectedProduct = ref(null)
const txType = ref('IN')
const txQuantity = ref(1)
const txReason = ref('')

const selectedCategoryFilter = ref('')
const searchName = ref('')
const employeesMap = ref({})

const filteredProducts = computed(() => {
  let result = products.value
  if (selectedCategoryFilter.value) {
    result = result.filter(p => (p.category || 'Sin Categoría') === selectedCategoryFilter.value)
  }
  if (searchName.value) {
    const q = searchName.value.toLowerCase()
    result = result.filter(p => p.name.toLowerCase().includes(q) || (p.sku && p.sku.toLowerCase().includes(q)))
  }
  return result
})

const loadProducts = async () => {
  try {
    products.value = await warehouseService.getProducts()
  } catch (err) {
    error.value = 'Error al cargar productos: ' + (err.response?.data?.message || err.message)
  }
}

const loadEmployees = async () => {
  try {
    const emps = await hrService.getEmployees()
    const map = {}
    emps.forEach(emp => {
      if (emp.username) {
        map[emp.username] = `${emp.firstName} ${emp.lastName} (${emp.position || 'Sin área'})`
      }
    })
    employeesMap.value = map
  } catch (err) {
    console.error('Error al cargar empleados para nombres:', err.message)
  }
}

const getEmployeeName = (username) => {
  if (!username) return ''
  return employeesMap.value[username] || username
}

const generateSKU = () => {
  if (!newProduct.value.category) return;
  const prefixMap = {
    'Redes': 'RED',
    'Telecomunicaciones': 'TEL',
    'Combos': 'CMB',
    'Software': 'SFT',
    'Servicios': 'SRV'
  };
  const prefix = prefixMap[newProduct.value.category] || 'GEN';
  const catProducts = products.value.filter(p => p.category === newProduct.value.category);
  const nextNum = catProducts.length + 1;
  newProduct.value.sku = `${prefix}-${nextNum.toString().padStart(3, '0')}`;
}

const saveProduct = async () => {
  try {
    error.value = ''
    await warehouseService.createProduct(newProduct.value)
    success.value = 'Producto creado exitosamente.'
    showAddModal.value = false
    newProduct.value = { sku: '', name: '', description: '', price: 0, stock: 0 }
    loadProducts()
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    error.value = 'Error al crear producto.'
  }
}

const openTransactionModal = (product, type) => {
  selectedProduct.value = product
  txType.value = type
  txQuantity.value = 1
  txReason.value = type === 'IN' ? 'Entrada manual' : 'Salida manual'
  showTxModal.value = true
}

const submitTransaction = async () => {
  try {
    error.value = ''
    const username = sessionStorage.getItem('username') || 'Usuario Desconocido'
    if (txType.value === 'IN') {
      await warehouseService.registerEntry(selectedProduct.value.id, txQuantity.value, txReason.value, username)
    } else {
      if (selectedProduct.value.stock < txQuantity.value) {
        error.value = 'Stock insuficiente para realizar esta salida.'
        showTxModal.value = false
        return
      }
      await warehouseService.registerExit(selectedProduct.value.id, txQuantity.value, txReason.value, username)
    }
    success.value = `Transacción (${txType.value}) registrada con éxito.`
    showTxModal.value = false
    loadProducts()
    setTimeout(() => success.value = '', 3000)
  } catch (err) {
    error.value = 'Error en la transacción: ' + (err.response?.data?.message || err.message)
  }
}

const printReport = () => {
  window.open('/warehouse-report', '_blank')
}

onMounted(() => {
  loadProducts()
  loadEmployees()
})
</script>

<style scoped>
@media print {
  .page-header, .panel, .modal-backdrop, .alert {
    display: none !important;
  }
  .d-print-block {
    display: block !important;
  }
  body {
    background: white;
  }
}

.custom-modal-backdrop {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.custom-modal-content {
  width: 100%;
  max-width: 500px;
}
.badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: 700;
  color: white;
}
.bg-success { background: #10b981; }
.bg-danger { background: #ef4444; }
.btn-outline-success { border: 1px solid #10b981; color: #10b981; background: transparent; }
.btn-outline-danger { border: 1px solid #ef4444; color: #ef4444; background: transparent; }
.btn-outline-success:hover { background: #10b981; color: white; }
.btn-outline-danger:hover { background: #ef4444; color: white; }
.btn-success { background: #10b981; color: white; }
.btn-danger { background: #ef4444; color: white; }
</style>
