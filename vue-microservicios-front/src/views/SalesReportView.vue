<template>
  <div class="report-page bg-light min-vh-100 py-5">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-3">Cargando datos del reporte de ventas...</p>
    </div>
    
    <div v-else class="container bg-white p-5 rounded shadow report-container">
      <div class="text-center mb-4 border-bottom pb-4">
        <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        <h2 class="mb-0 mt-3 text-primary fw-bold">Reporte Analítico de Ventas</h2>
        <p class="text-muted">Desempeño Comercial y Movimientos</p>
      </div>
      
      <p><strong>Fecha de emisión:</strong> {{ new Date().toLocaleString() }}</p>
      
      <!-- Indicadores de Desempeño (KPIs) -->
      <div class="row text-center mb-5 mt-4">
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Ingresos Totales</h5>
            <h2 class="fw-bold text-success">${{ totalRevenue.toFixed(2) }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Operaciones</h5>
            <h2 class="fw-bold text-primary">{{ totalOperations }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Ticket Promedio</h5>
            <h2 class="fw-bold text-info">${{ averageTicket.toFixed(2) }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Artículos Vendidos</h5>
            <h2 class="fw-bold text-secondary">{{ totalItemsSold }}</h2>
          </div>
        </div>
      </div>
      
      <!-- Charts Section -->
      <div class="row mt-4">
        <div class="col-12 mb-5">
          <h4 class="text-center mb-3 text-secondary">Desempeño por Vendedor (Ingresos)</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="sellersChartData" :data="sellersChartData" :options="barOptions" />
          </div>
        </div>
        <div class="col-12 mt-4">
          <h4 class="text-center mb-3 text-secondary">Top 5 Productos Más Vendidos</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="productsChartData" :data="productsChartData" :options="barOptions" />
          </div>
        </div>
      </div>
      
      <div class="text-center mt-5 pt-4 border-top d-print-none">
        <button class="btn btn-primary btn-lg px-5 shadow-sm" @click="printReport">
          <i class="bi bi-printer-fill me-2"></i> Imprimir Reporte
        </button>
        <p class="text-muted mt-3 small">Al imprimir, las gráficas se adaptarán al formato A4 automáticamente.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import salesService from '../services/salesService'
import warehouseService from '../services/warehouseService'
import hrService from '../services/hrService'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from 'chart.js'
import { Bar } from 'vue-chartjs'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const loading = ref(true)
const sales = ref([])
const products = ref([])
const employees = ref([])

const totalRevenue = ref(0)
const totalOperations = ref(0)
const averageTicket = ref(0)
const totalItemsSold = ref(0)

const sellersChartData = ref(null)
const productsChartData = ref(null)

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  animation: false, // Vital para impresión instantánea
  plugins: {
    legend: {
      position: 'bottom'
    }
  }
}

onMounted(async () => {
  try {
    const [salesData, prodsData, empsData] = await Promise.all([
      salesService.getSales().catch(() => []),
      warehouseService.getProducts().catch(() => []),
      hrService.getEmployees().catch(() => [])
    ])
    
    // Only count approved/completed sales
    sales.value = salesData.filter(s => !s.requiresApproval || s.workflowStatus === 'APPROVED' || s.workflowStatus === 'COMPLETED')
    products.value = prodsData
    employees.value = empsData
    
    calculateMetrics()
  } catch (error) {
    console.error('Error cargando datos del reporte de ventas', error)
  } finally {
    loading.value = false
  }
})

const getEmployeeName = (username) => {
  const emp = employees.value.find(e => e.username === username)
  return emp ? `${emp.firstName} ${emp.lastName}` : username
}

const getProductName = (id) => {
  const prod = products.value.find(p => p.id === id)
  return prod ? prod.name : `Producto #${id}`
}

const calculateMetrics = () => {
  let revenue = 0
  let items = 0
  
  const sellerTotals = {}
  const productTotals = {}
  
  sales.value.forEach(sale => {
    revenue += sale.total
    
    // Calcular por vendedor
    const seller = getEmployeeName(sale.createdBy) || 'Sistema'
    sellerTotals[seller] = (sellerTotals[seller] || 0) + sale.total
    
    // Calcular por producto
    sale.details.forEach(detail => {
      items += detail.quantity
      const pName = getProductName(detail.productId)
      productTotals[pName] = (productTotals[pName] || 0) + detail.quantity
    })
  })
  
  totalRevenue.value = revenue
  totalOperations.value = sales.value.length
  averageTicket.value = totalOperations.value > 0 ? (revenue / totalOperations.value) : 0
  totalItemsSold.value = items
  
  // Chart: Vendedores
  const sortedSellers = Object.entries(sellerTotals).sort((a, b) => b[1] - a[1])
  sellersChartData.value = {
    labels: sortedSellers.map(s => s[0]),
    datasets: [
      {
        label: 'Ventas Totales ($)',
        backgroundColor: '#198754',
        data: sortedSellers.map(s => s[1])
      }
    ]
  }
  
  // Chart: Productos
  const sortedProducts = Object.entries(productTotals).sort((a, b) => b[1] - a[1]).slice(0, 5)
  productsChartData.value = {
    labels: sortedProducts.map(p => p[0]),
    datasets: [
      {
        label: 'Unidades Vendidas',
        backgroundColor: '#0dcaf0',
        data: sortedProducts.map(p => p[1])
      }
    ]
  }
}

const printReport = () => {
  window.print()
}
</script>

<style scoped>
.report-page {
  background-color: #f8f9fa;
}

.report-container {
  max-width: 900px;
  margin: 0 auto;
}

@media print {
  @page {
    margin: 20mm;
    size: portrait;
  }
  
  body, .report-page, .report-container {
    background-color: white !important;
    min-height: auto !important;
    padding: 0 !important;
    box-shadow: none !important;
    margin: 0 !important;
  }
  
  .shadow, .shadow-sm {
    box-shadow: none !important;
    border: 1px solid #ddd !important;
  }
  
  .d-print-none {
    display: none !important;
  }
}
</style>
