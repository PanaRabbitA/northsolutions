<template>
  <div class="report-page bg-light min-vh-100 py-5">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-3">Cargando datos del reporte...</p>
    </div>
    
    <div v-else class="container bg-white p-5 rounded shadow report-container">
      <div class="text-center mb-4 border-bottom pb-4">
        <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        <h2 class="mb-0 mt-3 text-primary fw-bold">Reporte Analítico de Almacén e Inventario</h2>
        <p class="text-muted">Indicadores de Existencias y Movimientos</p>
      </div>
      
      <p><strong>Fecha de emisión:</strong> {{ new Date().toLocaleString() }}</p>
      
      <!-- Indicadores de Desempeño (KPIs) -->
      <div class="row text-center mb-5 mt-4">
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Total en Catálogo</h5>
            <h2 class="fw-bold text-primary">{{ totalProducts }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Valor del Inventario</h5>
            <h2 class="fw-bold text-success">${{ totalInventoryValue.toFixed(2) }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Productos Bajo Stock</h5>
            <h2 class="fw-bold text-warning">{{ lowStockCount }}</h2>
          </div>
        </div>
        <div class="col-3">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Total Movimientos</h5>
            <h2 class="fw-bold text-info">{{ transactionsCount }}</h2>
          </div>
        </div>
      </div>
      
      <!-- Charts Section -->
      <div class="row mt-4">
        <div class="col-12 mb-5">
          <h4 class="text-center mb-3 text-secondary">Comparativa General de Movimientos (Entradas vs Salidas)</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="movementsChartData" :data="movementsChartData" :options="barOptions" />
          </div>
        </div>
        <div class="col-12 mt-4">
          <h4 class="text-center mb-3 text-secondary">Top 5 Productos con Menor Stock</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="lowStockChartData" :data="lowStockChartData" :options="barOptions" />
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
import warehouseService from '../services/warehouseService'
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
const products = ref([])
const transactions = ref([])

const totalProducts = ref(0)
const totalInventoryValue = ref(0)
const lowStockCount = ref(0)
const transactionsCount = ref(0)

const movementsChartData = ref(null)
const lowStockChartData = ref(null)

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
    const [prodsData, txData] = await Promise.all([
      warehouseService.getProducts(),
      warehouseService.getAllTransactions().catch(() => [])
    ])
    
    products.value = prodsData
    transactions.value = txData
    
    calculateMetrics()
  } catch (error) {
    console.error('Error cargando datos del reporte', error)
  } finally {
    loading.value = false
  }
})

const calculateMetrics = () => {
  totalProducts.value = products.value.length
  
  let val = 0
  let low = 0
  products.value.forEach(p => {
    val += (p.price || 0) * (p.stock || 0)
    if (p.stock < 10) low++
  })
  totalInventoryValue.value = val
  lowStockCount.value = low
  transactionsCount.value = transactions.value.length
  
  // Calcular entradas vs salidas
  let entradas = 0
  let salidas = 0
  transactions.value.forEach(tx => {
    if (tx.type === 'IN') entradas++
    else if (tx.type === 'OUT') salidas++
  })
  
  movementsChartData.value = {
    labels: ['Movimientos Registrados'],
    datasets: [
      {
        label: 'Entradas de Inventario',
        backgroundColor: '#0d6efd',
        data: [entradas]
      },
      {
        label: 'Salidas de Inventario',
        backgroundColor: '#dc3545',
        data: [salidas]
      }
    ]
  }
  
  // Calcular top 5 menor stock
  const sortedProds = [...products.value].sort((a, b) => a.stock - b.stock).slice(0, 5)
  lowStockChartData.value = {
    labels: sortedProds.map(p => p.name),
    datasets: [
      {
        label: 'Cantidad en Stock',
        backgroundColor: '#ffc107',
        data: sortedProds.map(p => p.stock)
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
