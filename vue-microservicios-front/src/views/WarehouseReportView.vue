<template>
  <div class="report-page bg-white min-vh-100 p-0">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-3">Cargando datos del reporte...</p>
    </div>
    
    <div v-else class="container-fluid bg-white px-5 py-4 report-container">
      <div class="text-center mb-4 border-bottom pb-4">
        <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        <h2 class="mb-0 mt-3 text-primary fw-bold">Reporte Analítico de Almacén e Inventario</h2>
        <p class="text-muted">Indicadores de Existencias y Movimientos</p>
      </div>
      
      <p><strong>Fecha de emisión:</strong> {{ new Date().toLocaleString() }}</p>
      
      <!-- Toolbar de Filtros (Solo Pantalla) -->
      <div class="d-print-none mb-4 p-3 bg-light rounded border d-flex gap-4 align-items-center justify-content-center">
        <strong class="text-secondary"><i class="bi bi-funnel-fill me-1"></i> Filtros de Vista:</strong>
        <label class="form-check-label ms-3">
          <input type="checkbox" v-model="showKpis" class="form-check-input me-1"> Mostrar KPIs
        </label>
        <label class="form-check-label ms-3">
          <input type="checkbox" v-model="showCharts" class="form-check-input me-1"> Mostrar Gráficas
        </label>
        <label class="form-check-label ms-3">
          <input type="checkbox" v-model="showForecast" class="form-check-input me-1"> Mostrar Prospecto
        </label>
      </div>
      
      <!-- Indicadores de Desempeño (KPIs) -->
      <div v-if="showKpis" class="row text-center mb-5 mt-4">
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
      <div v-if="showCharts" class="row mt-4">
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

      <!-- Prospecto de Inventario -->
      <div v-if="showForecast" class="mt-5 pt-4 border-top">
        <h3 class="text-center mb-2 text-primary"><i class="bi bi-cpu me-2"></i>Prospecto de Inventario — Análisis Predictivo</h3>
        <p class="text-center text-muted small mb-4">Proyección de desabasto basada en la velocidad de consumo promedio diario de los últimos 90 días.</p>

        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-danger">
              <tr>
                <th>Producto</th>
                <th>Stock Actual</th>
                <th>Consumo Diario Prom.</th>
                <th>Días Restantes (est.)</th>
                <th>Riesgo</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in stockRiskItems" :key="item.name">
                <td><strong>{{ item.name }}</strong></td>
                <td>{{ item.stock }}</td>
                <td>{{ item.dailyConsumption.toFixed(2) }}</td>
                <td>
                  <span :class="item.daysLeft <= 7 ? 'text-danger fw-bold' : item.daysLeft <= 15 ? 'text-warning fw-bold' : ''">
                    {{ item.daysLeft }} días
                  </span>
                </td>
                <td>
                  <span class="badge" :class="item.daysLeft <= 7 ? 'bg-danger' : item.daysLeft <= 15 ? 'bg-warning text-dark' : 'bg-info'">
                    {{ item.daysLeft <= 7 ? 'CRÍTICO' : item.daysLeft <= 15 ? 'ALERTA' : 'MODERADO' }}
                  </span>
                </td>
              </tr>
              <tr v-if="stockRiskItems.length === 0">
                <td colspan="5" class="text-center text-muted py-3">No se detectan riesgos de desabasto en los próximos 30 días.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Clasificación por rotación -->
        <h4 class="mt-4 mb-3 text-secondary text-center"><i class="bi bi-diagram-3 me-2"></i>Clasificación de Productos por Rotación</h4>
        <div class="row text-center mb-3">
          <div class="col-md-4">
            <div class="p-3 border rounded bg-light shadow-sm">
              <span class="badge bg-success mb-1">Alta Rotación</span>
              <h4 class="fw-bold text-success">{{ rotationCounts.high }}</h4>
              <small class="text-muted">Productos con movimiento frecuente</small>
            </div>
          </div>
          <div class="col-md-4">
            <div class="p-3 border rounded bg-light shadow-sm">
              <span class="badge bg-warning text-dark mb-1">Rotación Media</span>
              <h4 class="fw-bold text-warning">{{ rotationCounts.medium }}</h4>
              <small class="text-muted">Movimiento estándar</small>
            </div>
          </div>
          <div class="col-md-4">
            <div class="p-3 border rounded bg-light shadow-sm">
              <span class="badge bg-secondary mb-1">Baja Rotación</span>
              <h4 class="fw-bold text-secondary">{{ rotationCounts.low }}</h4>
              <small class="text-muted">Sin movimiento significativo</small>
            </div>
          </div>
        </div>
      </div>
      
      <div class="text-center mt-5 pt-4 border-top d-print-none">
        <div class="d-flex justify-content-center gap-3">
          <button class="btn btn-outline-secondary btn-lg px-5 shadow-sm" @click="closeWindow">
            <i class="bi bi-x-circle me-2"></i> Cerrar
          </button>
          <button class="btn btn-primary btn-lg px-5 shadow-sm" @click="printReport">
            <i class="bi bi-printer-fill me-2"></i> Imprimir Reporte
          </button>
        </div>
        <p class="text-muted mt-3 small">Al imprimir, las gráficas se adaptarán al formato A4 automáticamente.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import warehouseService from '../services/warehouseService'
import salesService from '../services/salesService'
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

const showKpis = ref(true)
const showCharts = ref(true)
const showForecast = ref(true)

const stockRiskItems = ref([])
const rotationCounts = reactive({ high: 0, medium: 0, low: 0 })

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
    await buildWarehouseForecast()
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

const closeWindow = () => {
  window.close()
}

const buildWarehouseForecast = async () => {
  try {
    const salesData = await salesService.getSales().catch(() => [])
    const daysWindow = 90
    const cutoff = new Date(Date.now() - daysWindow * 24 * 60 * 60 * 1000)
    const recentSales = salesData.filter(s => new Date(s.saleDate) >= cutoff)

    const consumption = {}
    recentSales.forEach(s => {
      if (s.details) s.details.forEach(d => {
        consumption[d.productId] = (consumption[d.productId] || 0) + d.quantity
      })
    })

    const riskItems = []
    products.value.forEach(prod => {
      const total = consumption[prod.id] || 0
      const daily = total / daysWindow
      if (daily > 0) {
        const daysLeft = Math.floor(prod.stock / daily)
        if (daysLeft <= 30) {
          riskItems.push({ name: prod.name, stock: prod.stock, dailyConsumption: daily, daysLeft })
        }
      }
    })
    stockRiskItems.value = riskItems.sort((a, b) => a.daysLeft - b.daysLeft)

    // Rotation classification
    let high = 0, medium = 0, low = 0
    products.value.forEach(prod => {
      const total = consumption[prod.id] || 0
      if (total > 20) high++
      else if (total > 5) medium++
      else low++
    })
    rotationCounts.high = high
    rotationCounts.medium = medium
    rotationCounts.low = low
  } catch (err) {
    console.error('Error building warehouse forecast:', err)
  }
}
</script>

<style scoped>
.report-page {
  background-color: #f8f9fa;
}

.report-container {
  width: 100%;
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
