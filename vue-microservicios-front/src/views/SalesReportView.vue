<template>
  <div class="report-page bg-white min-vh-100 p-0">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-3">Cargando datos del reporte de ventas...</p>
    </div>
    
    <div v-else class="container-fluid bg-white px-5 py-4 report-container">
      <div class="text-center mb-4 border-bottom pb-4">
        <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        <h2 class="mb-0 mt-3 text-primary fw-bold">Reporte Analítico de Ventas</h2>
        <p class="text-muted">Desempeño Comercial y Movimientos</p>
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
      <div v-if="showCharts" class="row mt-4">
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

      <!-- Prospecto de Ventas (Regresión Lineal) -->
      <div v-if="showForecast" class="mt-5 pt-4 border-top">
        <h3 class="text-center mb-2 text-primary"><i class="bi bi-cpu me-2"></i>Prospecto de Ventas — Análisis Predictivo</h3>
        <p class="text-center text-muted small mb-4">Proyección a 3 meses basada en regresión lineal del histórico de ingresos mensuales.</p>
        <div class="row mb-4">
          <div class="col-md-4 text-center">
            <div class="p-3 border rounded bg-light shadow-sm">
              <h6 class="text-muted mb-1">Tendencia Mensual</h6>
              <h3 class="fw-bold" :class="forecastSlope >= 0 ? 'text-success' : 'text-danger'">{{ forecastSlope >= 0 ? '+' : '' }}${{ Math.abs(forecastSlope).toLocaleString() }}</h3>
              <small class="text-muted">Crecimiento/decrecimiento estimado por mes</small>
            </div>
          </div>
          <div class="col-md-4 text-center">
            <div class="p-3 border rounded bg-light shadow-sm">
              <h6 class="text-muted mb-1">Pronóstico Mes +1</h6>
              <h3 class="fw-bold text-primary">${{ forecastVals[0] }}</h3>
            </div>
          </div>
          <div class="col-md-4 text-center">
            <div class="p-3 border rounded bg-light shadow-sm">
              <h6 class="text-muted mb-1">Pronóstico Mes +3</h6>
              <h3 class="fw-bold text-info">${{ forecastVals[2] }}</h3>
            </div>
          </div>
        </div>
        <div style="height: 370px; width: 95%; margin: 0 auto; position: relative;">
          <Line v-if="forecastChartData" :data="forecastChartData" :options="lineOptions" />
        </div>

        <!-- Top Crecimiento -->
        <h4 class="mt-5 mb-3 text-secondary text-center"><i class="bi bi-arrow-up-right-circle me-2"></i>Productos con Mayor Crecimiento en Ventas</h4>
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead class="table-success">
              <tr>
                <th>Producto</th>
                <th>Unidades Vendidas</th>
                <th>Ingresos Generados</th>
                <th>Clasificación</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in topGrowthProducts" :key="p.name">
                <td><strong>{{ p.name }}</strong></td>
                <td>{{ p.totalSold }}</td>
                <td class="text-success fw-bold">${{ p.totalRevenue.toLocaleString() }}</td>
                <td><span class="badge" :class="p.totalSold > growthP75 ? 'bg-success' : p.totalSold > growthP25 ? 'bg-warning text-dark' : 'bg-secondary'">{{ p.totalSold > growthP75 ? 'Alta Demanda' : p.totalSold > growthP25 ? 'Estable' : 'Baja' }}</span></td>
              </tr>
            </tbody>
          </table>
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
  LineElement,
  PointElement,
  Filler
} from 'chart.js'
import { Bar, Line } from 'vue-chartjs'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, LineElement, PointElement, Filler)

const loading = ref(true)
const sales = ref([])
const products = ref([])
const employees = ref([])

const totalRevenue = ref(0)
const totalOperations = ref(0)
const averageTicket = ref(0)
const totalItemsSold = ref(0)

const showKpis = ref(true)
const showCharts = ref(true)
const showForecast = ref(true)

const forecastChartData = ref(null)
const forecastSlope = ref(0)
const forecastVals = ref(['0', '0', '0'])
const topGrowthProducts = ref([])
const growthP75 = ref(0)
const growthP25 = ref(0)

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' }, tooltip: { callbacks: { label: (ctx) => `${ctx.dataset.label}: $${Number(ctx.parsed.y).toLocaleString()}` } } },
  scales: { y: { beginAtZero: true, ticks: { callback: (v) => v >= 1e6 ? '$'+(v/1e6).toFixed(1)+'M' : v >= 1000 ? '$'+(v/1000).toFixed(0)+'k' : '$'+v } } }
}

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
    buildForecast()
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

const closeWindow = () => {
  window.close()
}

// ── Regresión Lineal ──
const linearRegression = (data) => {
  const n = data.length
  if (n < 2) return { slope: 0, intercept: 0 }
  let sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0
  data.forEach((y, i) => { sumX += i; sumY += y; sumXY += i * y; sumX2 += i * i })
  const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
  const intercept = (sumY - slope * sumX) / n
  return { slope: isNaN(slope) ? 0 : slope, intercept: isNaN(intercept) ? 0 : intercept }
}

const buildForecast = () => {
  const monthlyRevenue = {}
  sales.value.forEach(s => {
    const d = new Date(s.saleDate)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    monthlyRevenue[key] = (monthlyRevenue[key] || 0) + (s.total || 0)
  })
  const sortedMonths = Object.keys(monthlyRevenue).sort()
  const revenueValues = sortedMonths.map(k => monthlyRevenue[k])
  const { slope, intercept } = linearRegression(revenueValues)
  forecastSlope.value = Math.round(slope)

  const forecastLabels = [...sortedMonths]
  const historicData = [...revenueValues]
  const projectedData = new Array(revenueValues.length).fill(null)
  const fV = []
  for (let i = 1; i <= 3; i++) {
    const val = Math.max(0, slope * (revenueValues.length + i - 1) + intercept)
    fV.push(Math.round(val).toLocaleString())
    const last = new Date(sortedMonths[sortedMonths.length - 1] + '-01')
    last.setMonth(last.getMonth() + i)
    forecastLabels.push(`${last.getFullYear()}-${String(last.getMonth()+1).padStart(2,'0')}`)
    historicData.push(null)
    projectedData.push(val)
  }
  if (revenueValues.length > 0) projectedData[revenueValues.length - 1] = revenueValues[revenueValues.length - 1]
  forecastVals.value = fV

  forecastChartData.value = {
    labels: forecastLabels,
    datasets: [
      { label: 'Ingresos Históricos', data: historicData, borderColor: '#0d6efd', backgroundColor: 'rgba(13,110,253,0.1)', fill: true, tension: 0.3, pointRadius: 5, pointBackgroundColor: '#0d6efd' },
      { label: 'Pronóstico', data: projectedData, borderColor: '#dc3545', borderDash: [8, 4], pointRadius: 6, pointBackgroundColor: '#dc3545', pointStyle: 'triangle', tension: 0.3, fill: false }
    ]
  }

  // Top growth products
  const productMap = {}
  sales.value.forEach(s => {
    if (s.details) s.details.forEach(d => {
      if (!productMap[d.productId]) productMap[d.productId] = { totalSold: 0, totalRevenue: 0 }
      productMap[d.productId].totalSold += d.quantity
      productMap[d.productId].totalRevenue += d.subtotal || (d.quantity * d.unitPrice)
    })
  })
  const enriched = Object.entries(productMap).map(([id, v]) => {
    const prod = products.value.find(p => p.id === parseInt(id))
    return { name: prod ? prod.name : `Producto #${id}`, ...v }
  }).sort((a, b) => b.totalSold - a.totalSold)

  const sorted = [...enriched].sort((a, b) => a.totalSold - b.totalSold)
  growthP25.value = sorted[Math.floor(sorted.length * 0.25)]?.totalSold || 0
  growthP75.value = sorted[Math.floor(sorted.length * 0.75)]?.totalSold || 0
  topGrowthProducts.value = enriched.slice(0, 10)
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
