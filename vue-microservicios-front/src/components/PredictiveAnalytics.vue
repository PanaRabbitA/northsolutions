<template>
  <div class="panel mt-4 border-warning border-top border-3 d-print-none">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2><i class="bi bi-cpu text-warning"></i> Inteligencia Analítica y Pronósticos</h2>
      <span class="badge bg-warning text-dark px-3 py-2"><i class="bi bi-robot me-1"></i> Datos en Tiempo Real</span>
    </div>
    <p class="text-muted mb-4">Análisis predictivo basado en árbol de decisiones, clustering K-Means y regresión lineal sobre el histórico de operaciones.</p>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-warning" role="status"></div>
      <p class="mt-3 text-muted">Analizando datos históricos...</p>
    </div>

    <div v-else>
      <!-- 1. PRONÓSTICO DE VENTAS (Regresión Lineal) -->
      <div class="card shadow-sm mb-4 border-primary">
        <div class="card-body">
          <h4 class="card-title text-primary"><i class="bi bi-graph-up-arrow me-2"></i>Pronóstico de Ventas — Regresión Lineal</h4>
          <p class="text-muted small">Tendencia histórica de ingresos mensuales (línea sólida) y proyección a 3 meses (línea punteada). Modelo: <code>y = mx + b</code></p>
          <div style="position: relative; height: 370px;">
            <Line v-if="forecastChartData" :data="forecastChartData" :options="lineOptions" />
          </div>
          <div class="row mt-3">
            <div class="col-md-4 text-center">
              <div class="p-2 bg-light rounded border">
                <small class="text-muted">Pendiente (m)</small>
                <h5 class="fw-bold text-primary mb-0">{{ regressionSlope }}</h5>
                <small class="text-muted">Crecimiento mensual estimado</small>
              </div>
            </div>
            <div class="col-md-4 text-center">
              <div class="p-2 bg-light rounded border">
                <small class="text-muted">Pronóstico Mes +1</small>
                <h5 class="fw-bold text-success mb-0">${{ forecastValues[0] }}</h5>
              </div>
            </div>
            <div class="col-md-4 text-center">
              <div class="p-2 bg-light rounded border">
                <small class="text-muted">Pronóstico Mes +3</small>
                <h5 class="fw-bold text-info mb-0">${{ forecastValues[2] }}</h5>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 2. ÁRBOL DE DECISIONES -->
      <div class="card shadow-sm mb-4 border-success">
        <div class="card-body">
          <h4 class="card-title text-success"><i class="bi bi-diagram-3 me-2"></i>Árbol de Decisiones — Clasificación de Productos</h4>
          <p class="text-muted small">Cada producto se clasifica según su volumen de ventas frente a percentiles del catálogo. El árbol recomienda acciones operativas.</p>

          <!-- Visual Decision Tree -->
          <div class="decision-tree-container">
            <div class="tree-node tree-root" @click="toggleNode('root')">
              <i class="bi bi-database me-1"></i> {{ treeData.totalProducts }} Productos Analizados
            </div>
            <div class="tree-branches" v-if="expandedNodes.root">
              <div class="tree-branch">
                <div class="tree-connector"></div>
                <div class="tree-node tree-question">
                  ¿Ventas > Percentil 75 ({{ treeData.p75 }} unidades)?
                </div>
                <div class="tree-branches">
                  <!-- Alta Demanda -->
                  <div class="tree-branch">
                    <div class="tree-connector tree-yes"></div>
                    <div class="tree-node tree-leaf tree-leaf-success" @click="toggleNode('high')">
                      <i class="bi" :class="expandedNodes.high ? 'bi-chevron-down' : 'bi-chevron-right'"></i> 
                      <i class="bi bi-check-circle-fill mx-1"></i> SÍ → <strong>Alta Demanda</strong>
                      <span class="badge bg-success ms-2">{{ treeData.highDemand.length }} productos</span>
                    </div>
                    <div v-if="expandedNodes.high" class="tree-detail">
                      <div class="alert alert-success py-2 small mb-1">
                        <i class="bi bi-arrow-repeat me-1"></i> <strong>Acción:</strong> Reabastecer inventario urgentemente. Estos productos generan el mayor ingreso.
                      </div>
                      <ul class="list-group list-group-flush small">
                        <li class="list-group-item py-1 px-2" v-for="p in treeData.highDemand.slice(0, 5)" :key="p.name">
                          {{ p.name }} — <strong>{{ p.totalSold }} unidades</strong>
                        </li>
                        <li v-if="treeData.highDemand.length > 5" class="list-group-item py-1 px-2 text-muted">... y {{ treeData.highDemand.length - 5 }} más</li>
                      </ul>
                    </div>
                  </div>
                  <!-- Pregunta intermedia -->
                  <div class="tree-branch">
                    <div class="tree-connector tree-no"></div>
                    <div class="tree-node tree-question">
                      ¿Ventas > Percentil 25 ({{ treeData.p25 }} unidades)?
                    </div>
                    <div class="tree-branches">
                      <!-- Demanda Estable -->
                      <div class="tree-branch">
                        <div class="tree-connector tree-yes"></div>
                        <div class="tree-node tree-leaf tree-leaf-warning" @click="toggleNode('stable')">
                          <i class="bi" :class="expandedNodes.stable ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
                          <i class="bi bi-dash-circle-fill mx-1"></i> SÍ → <strong>Demanda Estable</strong>
                          <span class="badge bg-warning text-dark ms-2">{{ treeData.stableDemand.length }} productos</span>
                        </div>
                        <div v-if="expandedNodes.stable" class="tree-detail">
                          <div class="alert alert-warning py-2 small mb-1">
                            <i class="bi bi-shield-check me-1"></i> <strong>Acción:</strong> Mantener nivel actual de inventario. Monitorear tendencia.
                          </div>
                          <ul class="list-group list-group-flush small">
                        <li class="list-group-item py-1 px-2" v-for="p in treeData.stableDemand.slice(0, 5)" :key="p.name">
                              {{ p.name }} — <strong>{{ p.totalSold }} unidades</strong>
                            </li>
                            <li v-if="treeData.stableDemand.length > 5" class="list-group-item py-1 px-2 text-muted">... y {{ treeData.stableDemand.length - 5 }} más</li>
                          </ul>
                        </div>
                      </div>
                      <!-- Baja Demanda -->
                      <div class="tree-branch">
                        <div class="tree-connector tree-no"></div>
                        <div class="tree-node tree-leaf tree-leaf-danger" @click="toggleNode('low')">
                          <i class="bi" :class="expandedNodes.low ? 'bi-chevron-down' : 'bi-chevron-right'"></i>
                          <i class="bi bi-x-circle-fill mx-1"></i> NO → <strong>Baja Demanda</strong>
                          <span class="badge bg-danger ms-2">{{ treeData.lowDemand.length }} productos</span>
                        </div>
                        <div v-if="expandedNodes.low" class="tree-detail">
                          <div class="alert alert-danger py-2 small mb-1">
                            <i class="bi bi-megaphone me-1"></i> <strong>Acción:</strong> Lanzar promociones o evaluar descontinuar. Bajo retorno de inversión.
                          </div>
                          <ul class="list-group list-group-flush small">
                            <li class="list-group-item py-1 px-2" v-for="p in treeData.lowDemand.slice(0, 5)" :key="p.name">
                              {{ p.name }} — <strong>{{ p.totalSold }} unidades</strong>
                            </li>
                            <li v-if="treeData.lowDemand.length > 5" class="list-group-item py-1 px-2 text-muted">... y {{ treeData.lowDemand.length - 5 }} más</li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. CLUSTERING K-MEANS -->
      <div class="card shadow-sm mb-4 border-info">
        <div class="card-body">
          <h4 class="card-title text-info"><i class="bi bi-bullseye me-2"></i>Clustering K-Means — Segmentación de Productos</h4>
          <p class="text-muted small">Agrupación automática de productos en 3 segmentos según <strong>precio unitario</strong> (eje X) vs <strong>volumen vendido</strong> (eje Y).</p>
          <div style="position: relative; height: 400px;">
            <Scatter v-if="clusterChartData" :data="clusterChartData" :options="scatterOptions" />
          </div>
          <div class="row mt-3">
            <div class="col-md-4">
              <div class="p-2 bg-light rounded border text-center cluster-card" :class="{ 'opacity-50': activeCluster !== null && activeCluster !== 0 }" @click="toggleCluster(0)">
                <span class="badge bg-danger mb-1">Estrella</span>
                <p class="small mb-0 text-muted">Alto precio + Alto volumen</p>
                <strong>{{ clusterCounts.star }} productos</strong>
                <div class="small text-primary mt-1" v-if="activeCluster === 0">Mostrando solo este segmento</div>
                <div class="small text-muted mt-1" v-else>Clic para filtrar</div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="p-2 bg-light rounded border text-center cluster-card" :class="{ 'opacity-50': activeCluster !== null && activeCluster !== 1 }" @click="toggleCluster(1)">
                <span class="badge bg-primary mb-1">Vaca Lechera</span>
                <p class="small mb-0 text-muted">Bajo precio + Alto volumen</p>
                <strong>{{ clusterCounts.cow }} productos</strong>
                <div class="small text-primary mt-1" v-if="activeCluster === 1">Mostrando solo este segmento</div>
                <div class="small text-muted mt-1" v-else>Clic para filtrar</div>
              </div>
            </div>
            <div class="col-md-4">
              <div class="p-2 bg-light rounded border text-center cluster-card" :class="{ 'opacity-50': activeCluster !== null && activeCluster !== 2 }" @click="toggleCluster(2)">
                <span class="badge bg-secondary mb-1">Interrogante</span>
                <p class="small mb-0 text-muted">Bajo volumen de venta</p>
                <strong>{{ clusterCounts.question }} productos</strong>
                <div class="small text-primary mt-1" v-if="activeCluster === 2">Mostrando solo este segmento</div>
                <div class="small text-muted mt-1" v-else>Clic para filtrar</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. RIESGO DE DESABASTO -->
      <div class="card shadow-sm mb-4 border-danger">
        <div class="card-body">
          <h4 class="card-title text-danger"><i class="bi bi-exclamation-triangle me-2"></i>Proyección de Desabasto — Próximos 30 Días</h4>
          <p class="text-muted small">Productos con riesgo de quiebre de stock según su velocidad de consumo promedio diario.</p>
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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import salesService from '../services/salesService'
import warehouseService from '../services/warehouseService'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
  Filler
} from 'chart.js'
import { Line, Scatter } from 'vue-chartjs'

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale, Filler)

const loading = ref(true)
const forecastChartData = ref(null)
const clusterChartData = ref(null)
const regressionSlope = ref('0')
const forecastValues = ref(['0', '0', '0'])
const clusterCounts = reactive({ star: 0, cow: 0, question: 0 })
const stockRiskItems = ref([])

const treeData = reactive({
  totalProducts: 0,
  p75: 0,
  p25: 0,
  highDemand: [],
  stableDemand: [],
  lowDemand: []
})

const expandedNodes = reactive({ root: true, high: false, stable: false, low: false })

const toggleNode = (key) => {
  expandedNodes[key] = !expandedNodes[key]
}

const activeCluster = ref(null)
const clusterDatasets = ref([])

const toggleCluster = (clusterId) => {
  if (activeCluster.value === clusterId) {
    activeCluster.value = null // reset
    clusterChartData.value = { datasets: clusterDatasets.value }
  } else {
    activeCluster.value = clusterId
    const filtered = clusterDatasets.value.map((ds, idx) => ({
      ...ds,
      backgroundColor: idx === clusterId ? ds.backgroundColor : 'rgba(200, 200, 200, 0.2)',
      pointRadius: idx === clusterId ? 7 : 3,
    }))
    clusterChartData.value = { datasets: filtered }
  }
}

// ── Chart Options ──
const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.dataset.label}: $${Number(ctx.parsed.y).toLocaleString()}`
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (v) => v >= 1000000 ? '$' + (v / 1e6).toFixed(1) + 'M' : v >= 1000 ? '$' + (v / 1000).toFixed(0) + 'k' : '$' + v
      }
    }
  }
}

const scatterOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.raw.label || ''} | Precio: $${ctx.parsed.x.toFixed(0)} | Vendidos: ${ctx.parsed.y}`
      }
    }
  },
  scales: {
    x: {
      title: { display: true, text: 'Precio Unitario ($)' },
      beginAtZero: true
    },
    y: {
      title: { display: true, text: 'Unidades Vendidas' },
      beginAtZero: true
    }
  }
}

// ── ALGORITMO 1: Regresión Lineal ──
const linearRegression = (data) => {
  const n = data.length
  if (n < 2) return { slope: 0, intercept: 0 }
  let sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0
  data.forEach((y, i) => {
    const x = i
    sumX += x
    sumY += y
    sumXY += x * y
    sumX2 += x * x
  })
  const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
  const intercept = (sumY - slope * sumX) / n
  return { slope: isNaN(slope) ? 0 : slope, intercept: isNaN(intercept) ? 0 : intercept }
}

// ── ALGORITMO 2: K-Means Simplificado (3 clusters) ──
const kMeans = (points, k = 3, maxIterations = 20) => {
  if (points.length === 0) return []

  // Initialize centroids spread across data
  const sorted = [...points].sort((a, b) => (a.x + a.y) - (b.x + b.y))
  let centroids = []
  for (let i = 0; i < k; i++) {
    const idx = Math.floor((i / k) * sorted.length)
    centroids.push({ x: sorted[idx].x, y: sorted[idx].y })
  }

  let assignments = new Array(points.length).fill(0)

  for (let iter = 0; iter < maxIterations; iter++) {
    // Assign each point to nearest centroid
    let changed = false
    points.forEach((p, i) => {
      let minDist = Infinity
      let nearest = 0
      centroids.forEach((c, j) => {
        const dist = Math.sqrt((p.x - c.x) ** 2 + (p.y - c.y) ** 2)
        if (dist < minDist) { minDist = dist; nearest = j }
      })
      if (assignments[i] !== nearest) changed = true
      assignments[i] = nearest
    })

    if (!changed) break

    // Recalculate centroids
    for (let j = 0; j < k; j++) {
      const members = points.filter((_, i) => assignments[i] === j)
      if (members.length > 0) {
        centroids[j] = {
          x: members.reduce((s, p) => s + p.x, 0) / members.length,
          y: members.reduce((s, p) => s + p.y, 0) / members.length
        }
      }
    }
  }

  return assignments
}

// ── ALGORITMO 3: Árbol de Decisiones (por percentiles) ──
const buildDecisionTree = (productSales) => {
  const sorted = [...productSales].sort((a, b) => a.totalSold - b.totalSold)
  const n = sorted.length
  if (n === 0) return

  const p25 = sorted[Math.floor(n * 0.25)]?.totalSold || 0
  const p75 = sorted[Math.floor(n * 0.75)]?.totalSold || 0

  treeData.totalProducts = n
  treeData.p25 = p25
  treeData.p75 = p75
  treeData.highDemand = productSales.filter(p => p.totalSold > p75).sort((a, b) => b.totalSold - a.totalSold)
  treeData.stableDemand = productSales.filter(p => p.totalSold > p25 && p.totalSold <= p75).sort((a, b) => b.totalSold - a.totalSold)
  treeData.lowDemand = productSales.filter(p => p.totalSold <= p25).sort((a, b) => b.totalSold - a.totalSold)
}

// ── Main Analysis ──
const runAnalysis = async () => {
  try {
    const [salesData, productsData, transData] = await Promise.all([
      salesService.getSales().catch(() => []),
      warehouseService.getProducts().catch(() => []),
      warehouseService.getAllTransactions().catch(() => [])
    ])

    // ── 1. Pronóstico con Regresión Lineal ──
    const monthlyRevenue = {}
    salesData.forEach(s => {
      const d = new Date(s.saleDate)
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
      monthlyRevenue[key] = (monthlyRevenue[key] || 0) + (s.total || 0)
    })

    const sortedMonths = Object.keys(monthlyRevenue).sort()
    const revenueValues = sortedMonths.map(k => monthlyRevenue[k])

    const { slope, intercept } = linearRegression(revenueValues)
    regressionSlope.value = slope >= 0 ? '+$' + slope.toFixed(0) : '-$' + Math.abs(slope).toFixed(0)

    // Forecast 3 months ahead
    const forecastLabels = [...sortedMonths]
    const forecastData = [...revenueValues]
    const projectedData = new Array(revenueValues.length).fill(null)
    const fVals = []

    for (let i = 1; i <= 3; i++) {
      const nextIdx = revenueValues.length + i - 1
      const val = Math.max(0, slope * nextIdx + intercept)
      fVals.push(val.toFixed(0))

      // Generate next month label
      const lastMonth = new Date(sortedMonths[sortedMonths.length - 1] + '-01')
      lastMonth.setMonth(lastMonth.getMonth() + i)
      const label = `${lastMonth.getFullYear()}-${String(lastMonth.getMonth() + 1).padStart(2, '0')}`
      forecastLabels.push(label)
      forecastData.push(null)
      projectedData.push(val)
    }
    // Connect projection line to last historical point
    if (projectedData.length > 0 && revenueValues.length > 0) {
      projectedData[revenueValues.length - 1] = revenueValues[revenueValues.length - 1]
    }

    forecastValues.value = fVals.map(v => Number(v).toLocaleString())

    forecastChartData.value = {
      labels: forecastLabels,
      datasets: [
        {
          label: 'Ingresos Históricos',
          data: forecastData,
          borderColor: '#0d6efd',
          backgroundColor: 'rgba(13, 110, 253, 0.1)',
          fill: true,
          tension: 0.3,
          pointRadius: 5,
          pointBackgroundColor: '#0d6efd'
        },
        {
          label: 'Pronóstico (Regresión Lineal)',
          data: projectedData,
          borderColor: '#dc3545',
          borderDash: [8, 4],
          pointRadius: 6,
          pointBackgroundColor: '#dc3545',
          pointStyle: 'triangle',
          tension: 0.3,
          fill: false
        }
      ]
    }

    // ── 2. Árbol de Decisiones ──
    const productSalesMap = {}
    salesData.forEach(s => {
      if (s.details) {
        s.details.forEach(d => {
          const pid = d.productId
          if (!productSalesMap[pid]) productSalesMap[pid] = { id: pid, totalSold: 0, totalRevenue: 0 }
          productSalesMap[pid].totalSold += d.quantity
          productSalesMap[pid].totalRevenue += d.subtotal || (d.quantity * d.unitPrice)
        })
      }
    })

    // Enrich with product names and prices
    const productSales = Object.values(productSalesMap).map(ps => {
      const prod = productsData.find(p => p.id === ps.id)
      return {
        ...ps,
        name: prod ? prod.name : `Producto #${ps.id}`,
        price: prod ? prod.price : 0,
        stock: prod ? prod.stock : 0
      }
    })

    buildDecisionTree(productSales)

    // ── 3. Clustering K-Means ──
    const clusterPoints = productSales.map(p => ({
      x: p.price,
      y: p.totalSold,
      label: p.name
    }))

    if (clusterPoints.length >= 3) {
      const assignments = kMeans(clusterPoints)
      
      // Determine which cluster is which based on centroid characteristics
      const clusterStats = [0, 1, 2].map(c => {
        const members = clusterPoints.filter((_, i) => assignments[i] === c)
        return {
          id: c,
          avgX: members.reduce((s, p) => s + p.x, 0) / (members.length || 1),
          avgY: members.reduce((s, p) => s + p.y, 0) / (members.length || 1),
          count: members.length
        }
      })
      
      // Sort: Star = highest combined score, Cow = high Y low X, Question = lowest
      clusterStats.sort((a, b) => (b.avgX + b.avgY) - (a.avgX + a.avgY))
      const clusterLabels = {}
      clusterLabels[clusterStats[0].id] = { name: 'Estrella', color: 'rgba(220, 53, 69, 0.7)' }
      clusterLabels[clusterStats[1].id] = { name: 'Vaca Lechera', color: 'rgba(13, 110, 253, 0.7)' }
      clusterLabels[clusterStats[2].id] = { name: 'Interrogante', color: 'rgba(108, 117, 125, 0.5)' }

      clusterCounts.star = clusterStats[0].count
      clusterCounts.cow = clusterStats[1].count
      clusterCounts.question = clusterStats[2].count

      // Build datasets per cluster
      const datasets = Object.entries(clusterLabels).map(([cId, info]) => ({
        label: info.name,
        data: clusterPoints
          .filter((_, i) => assignments[i] === parseInt(cId))
          .map(p => ({ x: p.x, y: p.y, label: p.label })),
        backgroundColor: info.color,
        pointRadius: 7,
        pointHoverRadius: 10
      }))

      clusterDatasets.value = datasets
      
      // Apply filter if one is active
      if (activeCluster.value !== null) {
        toggleCluster(activeCluster.value) // re-trigger to apply styling
        activeCluster.value = null
        toggleCluster(activeCluster.value)
      } else {
        clusterChartData.value = { datasets }
      }
    }

    // ── 4. Riesgo de Desabasto ──
    // Calculate daily consumption from sales in the last 90 days
    const now = new Date()
    const daysWindow = 90
    const cutoff = new Date(now.getTime() - daysWindow * 24 * 60 * 60 * 1000)

    const recentSales = salesData.filter(s => new Date(s.saleDate) >= cutoff)
    const productConsumption = {}

    recentSales.forEach(s => {
      if (s.details) {
        s.details.forEach(d => {
          productConsumption[d.productId] = (productConsumption[d.productId] || 0) + d.quantity
        })
      }
    })

    const riskItems = []
    productsData.forEach(prod => {
      const totalConsumed = productConsumption[prod.id] || 0
      const dailyRate = totalConsumed / daysWindow
      if (dailyRate > 0) {
        const daysLeft = Math.floor(prod.stock / dailyRate)
        if (daysLeft <= 30) {
          riskItems.push({
            name: prod.name,
            stock: prod.stock,
            dailyConsumption: dailyRate,
            daysLeft
          })
        }
      }
    })

    stockRiskItems.value = riskItems.sort((a, b) => a.daysLeft - b.daysLeft)

  } catch (error) {
    console.error('Error en análisis predictivo:', error)
  } finally {
    loading.value = false
  }
}

let refreshInterval = null

onMounted(() => {
  runAnalysis()
  refreshInterval = setInterval(runAnalysis, 30000) // Refresh every 30s
})

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval)
})
</script>

<style scoped>
/* ── Decision Tree Styles ── */
.decision-tree-container {
  padding: 20px;
  overflow-x: auto;
}

.tree-node {
  padding: 10px 18px;
  border-radius: 10px;
  display: inline-block;
  font-size: 0.92rem;
  margin: 6px 0;
  transition: all 0.2s ease;
}

.tree-root {
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  color: white;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(30, 58, 138, 0.3);
}

.tree-root:hover {
  transform: scale(1.02);
}

.tree-question {
  background: #f8f9fa;
  border: 2px solid #6c757d;
  color: #333;
  font-weight: 600;
}

.tree-leaf {
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
  user-select: none;
}

.tree-leaf:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

.tree-leaf-success { background: #d1e7dd; border: 2px solid #198754; color: #0f5132; }
.tree-leaf-warning { background: #fff3cd; border: 2px solid #ffc107; color: #664d03; }
.tree-leaf-danger  { background: #f8d7da; border: 2px solid #dc3545; color: #842029; }

.tree-branches {
  margin-left: 32px;
  border-left: 3px solid #dee2e6;
  padding-left: 20px;
}

.tree-branch {
  position: relative;
  margin-bottom: 8px;
}

.tree-connector {
  width: 20px;
  height: 2px;
  background: #dee2e6;
  position: absolute;
  left: -20px;
  top: 18px;
}

.tree-connector.tree-yes::after {
  content: 'SÍ';
  position: absolute;
  left: -20px;
  top: -10px;
  font-size: 0.7rem;
  font-weight: 700;
  color: #198754;
}

.tree-connector.tree-no::after {
  content: 'NO';
  position: absolute;
  left: -20px;
  top: -10px;
  font-size: 0.7rem;
  font-weight: 700;
  color: #dc3545;
}

.tree-detail {
  margin-left: 16px;
  margin-top: 4px;
  max-width: 400px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-5px); }
  to { opacity: 1; transform: translateY(0); }
}

.cluster-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.cluster-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1) !important;
}

.cluster-card.opacity-50 {
  opacity: 0.5;
}
</style>
