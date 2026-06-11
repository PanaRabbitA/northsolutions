<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Módulo Comercial</span>
        <h1>Ventas y Facturación</h1>
        <p>Registro de ventas y consulta de historial (conecta con Almacenes para validar stock).</p>
      </div>
      <div class="d-flex gap-2 align-items-center">
        <select v-model="reportPeriod" class="form-select d-print-none" style="width: auto;">
          <option value="dia">Día</option>
          <option value="semanal">Semanal</option>
          <option value="mensual">Mensual</option>
          <option value="todos">Histórico (Todos)</option>
        </select>
        <button class="btn btn-outline-secondary d-print-none" @click="printReport">
          <i class="bi bi-file-earmark-bar-graph"></i> Generar Reporte
        </button>
        <button class="btn btn-primary d-print-none" @click="showSaleModal = true">
          <i class="bi bi-cart-plus"></i> Nueva Venta
        </button>
      </div>
    </div>

    <!-- Printable Report logic migrated to new tab -->

    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <div class="panel">
      <h2>Historial de Ventas</h2>
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>ID Venta</th>
              <th>Fecha</th>
              <th>Total</th>
              <th>Detalles</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredSales.length === 0">
              <td colspan="5" class="text-center text-muted py-4">No hay ventas registradas en este periodo.</td>
            </tr>
            <tr v-for="sale in filteredSales" :key="sale.id">
              <td><strong>#{{ sale.id }}</strong></td>
              <td>{{ new Date(sale.saleDate).toLocaleString() }}</td>
              <td><strong class="text-success">${{ sale.total.toFixed(2) }}</strong></td>
              <td>
                <span class="badge bg-secondary">{{ sale.details.length }} artículos</span>
              </td>
              <td>
                <span v-if="!sale.requiresApproval" class="badge bg-success">
                  <i class="bi bi-check-circle"></i> Completada
                </span>
                <span v-else-if="workflows[sale.id] === 'APROBADO'" class="badge bg-success">
                  <i class="bi bi-check-circle"></i> Autorizada
                </span>
                <span v-else-if="workflows[sale.id] === 'RECHAZADO'" class="badge bg-danger">
                  <i class="bi bi-x-circle"></i> Rechazada
                </span>
                <span v-else-if="workflows[sale.id] === 'PENDIENTE'" class="badge bg-warning text-dark">
                  <i class="bi bi-hourglass-split"></i> Pendiente
                </span>
                <button v-else class="btn btn-sm btn-outline-warning" @click="requestApproval(sale.id)" title="Re-solicitar Aprobación (Venta Atascada)">
                  <i class="bi bi-shield-exclamation"></i> Solicitar Aprobación
                </button>
                <button class="btn btn-sm btn-dark ms-2 shadow-sm" @click="printInvoice(sale)" title="Imprimir Factura Fiscal">
                  <i class="bi bi-receipt"></i> Generar Factura
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Nueva Venta -->
    <div v-if="showSaleModal" class="modal-backdrop">
      <div class="panel modal-content">
        <h2>Punto de Venta</h2>
        <form @submit.prevent="submitSale" class="form-stack">
          
          <div class="product-selector mb-3 p-3 bg-light rounded border">
            <label class="form-label">Agregar Producto al Carrito</label>
            <div class="row g-3 align-items-end">
              <div class="col-md-4">
                <label class="form-label small text-muted mb-1">Categoría</label>
                <select v-model="selectedCategoryFilter" class="form-select">
                  <option value="">Todas las Categorías</option>
                  <option value="Redes">Redes</option>
                  <option value="Telecomunicaciones">Telecomunicaciones</option>
                  <option value="Combos">Combos</option>
                  <option value="Software">Software</option>
                  <option value="Servicios">Servicios</option>
                  <option value="Sin Categoría">Sin Categoría</option>
                </select>
              </div>
              <div class="col-md-8">
                <label class="form-label small text-muted mb-1">Producto</label>
                <select v-model="selectedProductId" class="form-select">
                  <option value="">Seleccione un producto...</option>
                  <option v-for="p in filteredAvailableProducts" :key="p.id" :value="p.id">
                    {{ p.name }} (Stock: {{ p.stock }})
                  </option>
                </select>
              </div>
              
              <div class="col-md-5">
                <label class="form-label small text-muted mb-1">Precio Unitario Modificable</label>
                <div class="input-group">
                  <span class="input-group-text"><i class="bi bi-currency-dollar"></i></span>
                  <input v-model.number="selectedCustomPrice" type="number" step="0.01" class="form-control" placeholder="Precio">
                </div>
              </div>
              <div class="col-md-4">
                <label class="form-label small text-muted mb-1">Cantidad</label>
                <input v-model.number="selectedQuantity" type="number" class="form-control" min="1" placeholder="Cant">
              </div>
              <div class="col-md-3">
                <button type="button" class="btn btn-primary w-100" @click="addToCart" :disabled="!selectedProductId || selectedQuantity < 1">
                  <i class="bi bi-plus-circle me-1"></i> Agregar
                </button>
              </div>
            </div>
          </div>

          <!-- Carrito de compras -->
          <div v-if="cart.length > 0" class="mb-3">
            <h5>Carrito</h5>
            <ul class="list-group mb-2">
              <li v-for="(item, index) in cart" :key="index" class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                  {{ item.name }} x {{ item.quantity }}
                </div>
                <div class="d-flex gap-3 align-items-center">
                  <span>${{ (item.price * item.quantity).toFixed(2) }}</span>
                  <button type="button" class="btn btn-sm btn-link text-danger p-0" @click="removeFromCart(index)">
                    <i class="bi bi-trash"></i>
                  </button>
                </div>
              </li>
            </ul>
            <div class="text-end">
              <strong>Total Estimado: <span class="text-success">${{ cartTotal.toFixed(2) }}</span></strong>
            </div>
          </div>
          <div v-else class="text-center text-muted mb-3">
            El carrito está vacío.
          </div>

          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-success w-50" :disabled="cart.length === 0 || isProcessing">
              {{ isProcessing ? 'Procesando...' : 'Confirmar Venta' }}
            </button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="closeSaleModal" :disabled="isProcessing">Cancelar</button>
          </div>
        </form>
      </div>
    </div>

    <!-- La plantilla de factura se ha movido a su propia vista para impresión limpia -->

  </section>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import salesService from '../services/salesService'
import warehouseService from '../services/warehouseService'
import hrService from '../services/hrService'
import { workflowService } from '../services/workflowService'

const sales = ref([])
const availableProducts = ref([])
const employeesMap = ref({})
const workflows = ref({})
const error = ref('')
const success = ref('')
const currentUserName = ref(sessionStorage.getItem('username') || 'Usuario')
const reportPeriod = ref('mensual')

const selectedInvoice = ref(null)

const showSaleModal = ref(false)
const isProcessing = ref(false)

const filteredSales = computed(() => {
  if (reportPeriod.value === 'todos') return sales.value;
  const now = new Date();
  let limitDate = new Date(0);
  
  if (reportPeriod.value === 'dia') {
    limitDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  } else if (reportPeriod.value === 'semanal') {
    limitDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
  } else if (reportPeriod.value === 'mensual') {
    limitDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
  }
  
  return sales.value.filter(s => new Date(s.saleDate) >= limitDate);
})

const requestApproval = async (saleId) => {
  try {
    const token = sessionStorage.getItem('token')
    await workflowService.solicitarAprobacion(saleId, 'VENTAS', token)
    success.value = `¡Solicitud de aprobación para la venta #${saleId} enviada con éxito a Gerencia!`
    await loadWorkflows()
    setTimeout(() => success.value = '', 5000)
  } catch (err) {
    error.value = 'Error al solicitar aprobación: ' + (err.response?.data?.error || err.message)
    setTimeout(() => error.value = '', 5000)
  }
}

// Shopping Cart
const selectedCategoryFilter = ref('')
const selectedProductId = ref('')
const selectedQuantity = ref(1)
const selectedCustomPrice = ref(0)
const cart = ref([])

watch(selectedProductId, (newId) => {
  if (newId) {
    const prod = availableProducts.value.find(p => p.id === newId)
    if (prod) {
      selectedCustomPrice.value = prod.price
    }
  } else {
    selectedCustomPrice.value = 0
  }
})

const filteredAvailableProducts = computed(() => {
  if (!selectedCategoryFilter.value) return availableProducts.value
  return availableProducts.value.filter(p => {
    const cat = p.category || 'Sin Categoría'
    return cat === selectedCategoryFilter.value
  })
})

const cartTotal = computed(() => {
  return cart.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

const loadSales = async () => {
  try {
    sales.value = await salesService.getSales()
  } catch (err) {
    error.value = 'Error al cargar ventas: ' + err.message
  }
}

const loadWorkflows = async () => {
  try {
    const token = sessionStorage.getItem('token')
    if (token) {
      const res = await workflowService.getPorModulo('VENTAS', token)
      const wfs = res.data || []
      const map = {}
      wfs.forEach(w => {
        map[w.documentId] = w.estado
      })
      workflows.value = map
    }
  } catch (err) {
    console.error('Error al cargar workflows:', err)
  }
}

const loadProducts = async () => {
  try {
    availableProducts.value = await warehouseService.getProducts()
  } catch (err) {
    error.value = 'Error al cargar inventario: ' + err.message
  }
}

const loadEmployees = async () => {
  try {
    const emps = await hrService.getEmployees()
    const map = {}
    emps.forEach(emp => {
      if (emp.username) {
        map[emp.username] = `${emp.firstName} ${emp.lastName}`
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

const getProductName = (productId) => {
  const prod = availableProducts.value.find(p => p.id === productId)
  return prod ? prod.name : `Producto ${productId}`
}

const printInvoice = (sale) => {
  const invoiceData = {
    id: sale.id,
    saleDate: sale.saleDate,
    createdBy: getEmployeeName(sale.createdBy) || sale.createdBy,
    total: sale.total,
    details: sale.details.map(d => ({
      quantity: d.quantity,
      productName: getProductName(d.productId),
      price: d.unitPrice || d.price || 0
    }))
  };
  localStorage.setItem('print_invoice_data', JSON.stringify(invoiceData));
  window.open('/invoice', '_blank');
}

const addToCart = () => {
  const prod = availableProducts.value.find(p => p.id === selectedProductId.value)
  if (prod) {
    if (prod.stock < selectedQuantity.value) {
      alert(`No hay stock suficiente. Stock actual: ${prod.stock}`)
      return
    }
    
    // Check if already in cart
    const existingItem = cart.value.find(item => item.productId === prod.id && item.price === selectedCustomPrice.value)
    if (existingItem) {
      if (existingItem.quantity + selectedQuantity.value > prod.stock) {
         alert(`No puedes agregar más, excede el stock.`)
         return
      }
      existingItem.quantity += selectedQuantity.value
    } else {
      cart.value.push({
        productId: prod.id,
        name: prod.name,
        price: selectedCustomPrice.value,
        quantity: selectedQuantity.value
      })
    }
    selectedProductId.value = ''
    selectedQuantity.value = 1
    selectedCustomPrice.value = 0
  }
}

const removeFromCart = (index) => {
  cart.value.splice(index, 1)
}

const closeSaleModal = () => {
  showSaleModal.value = false
  cart.value = []
  selectedProductId.value = ''
  selectedQuantity.value = 1
}

const submitSale = async () => {
  if (cart.value.length === 0) return
  
  try {
    isProcessing.value = true
    error.value = ''
    
    const username = sessionStorage.getItem('username') || 'Usuario Desconocido'
    
    const requestPayload = {
      items: cart.value.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.price
      })),
      createdBy: username
    }
    
    const sale = await salesService.createSale(requestPayload)

    if (sale.requiresApproval) {
      const token = sessionStorage.getItem('token')
      await workflowService.solicitarAprobacion(sale.id, 'VENTAS', token)
      success.value = '¡Venta registrada! Al modificar el precio, se ha enviado automáticamente a Gerencia para su aprobación.'
    } else {
      success.value = '¡Venta registrada con éxito!'
    }

    closeSaleModal()
    loadSales()
    loadProducts() // refresh stock
    loadWorkflows()
    
    setTimeout(() => success.value = '', 5000)
  } catch (err) {
    error.value = 'Error al procesar la venta: ' + (err.response?.data?.message || err.message)
  } finally {
    isProcessing.value = false
  }
}

const printReport = () => {
  window.open('/sales-report', '_blank')
}

onMounted(() => {
  loadSales()
  loadProducts()
  loadEmployees()
  loadWorkflows()
})
</script>

<style scoped>
@media print {
  .page-header, .panel, .modal-backdrop, .alert {
    display: none !important;
  }
  .app-shell {
    display: block !important;
    grid-template-columns: 1fr !important;
  }
  .content {
    padding: 0 !important;
    margin: 0 !important;
  }
  .d-print-block {
    display: block !important;
  }
  body {
    background: white;
  }
}

.invoice-container {
  background: white;
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
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
  max-width: 600px;
}
</style>
