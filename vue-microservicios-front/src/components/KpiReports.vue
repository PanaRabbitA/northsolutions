<template>
  <div class="panel mt-4 border-primary border-top border-3 print-section">
    <div class="d-flex justify-content-between align-items-center mb-4 no-print">
      <h2><i class="bi bi-graph-up-arrow text-primary"></i> Reportes Avanzados KPI</h2>
      <button class="btn btn-primary" @click="downloadReport">
        <i class="bi bi-printer"></i> Imprimir / Descargar PDF
      </button>
    </div>

    <!-- Filtros (no visibles al imprimir) -->
    <div class="row mb-4 no-print g-3 bg-light p-3 rounded">
      <div class="col-md-3">
        <label class="form-label fw-bold"><i class="bi bi-calendar-event"></i> Periodo desde:</label>
        <input type="date" class="form-control" v-model="filterStartDate" @change="applyFilters">
      </div>
      <div class="col-md-3">
        <label class="form-label fw-bold"><i class="bi bi-calendar-event"></i> Periodo hasta:</label>
        <input type="date" class="form-control" v-model="filterEndDate" @change="applyFilters">
      </div>
      <div class="col-md-3">
        <label class="form-label fw-bold"><i class="bi bi-bar-chart"></i> Agrupar Ventas por:</label>
        <select class="form-select" v-model="salesTimeGroup" @change="applyFilters">
          <option value="DAY">Día</option>
          <option value="WEEK">Semana</option>
          <option value="MONTH">Mes</option>
          <option value="SIX_MONTHS">Seis Meses</option>
          <option value="YEAR">Anual</option>
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label fw-bold"><i class="bi bi-arrow-left-right"></i> Comparativa Otros KPIs:</label>
        <select class="form-select" v-model="comparePeriod" @change="applyFilters">
          <option value="NONE">Sin Comparar</option>
          <option value="PREV_DAY">vs Día Anterior</option>
          <option value="PREV_WEEK">vs Semana Anterior</option>
          <option value="PREV_MONTH">vs Mes Anterior</option>
        </select>
      </div>
      <div class="col-md-12 mt-3">
        <label class="form-label fw-bold me-2">Área del Reporte Detallado:</label>
        <div class="btn-group" role="group">
          <input type="radio" class="btn-check" name="area" id="areaAll" value="ALL" v-model="selectedArea" @change="applyFilters">
          <label class="btn btn-outline-primary" for="areaAll">Resumen General</label>

          <input type="radio" class="btn-check" name="area" id="areaSales" value="SALES" v-model="selectedArea" @change="applyFilters">
          <label class="btn btn-outline-primary" for="areaSales">Ventas</label>

          <input type="radio" class="btn-check" name="area" id="areaWarehouse" value="WAREHOUSE" v-model="selectedArea" @change="applyFilters">
          <label class="btn btn-outline-primary" for="areaWarehouse">Almacén</label>

          <input type="radio" class="btn-check" name="area" id="areaHr" value="HR" v-model="selectedArea" @change="applyFilters">
          <label class="btn btn-outline-primary" for="areaHr">Recursos Humanos</label>
        </div>
      </div>
    </div>

    <!-- Contenido del Reporte -->
    <div class="report-content" id="report-content">
      <div class="d-flex justify-content-between align-items-start mb-4 only-print border-bottom pb-3">
        <div class="d-flex align-items-center">
          <img src="../assets/images/logo.png" alt="North Solutions Logo" style="height: 80px; margin-right: 20px;" />
          <div>
            <h2 class="m-0 text-primary fw-bold" style="font-size: 1.5rem;">North Solutions S.A. de C.V.</h2>
            <p class="text-muted m-0 small">Av. Principal 123, Ciudad de México, C.P. 12345</p>
            <p class="text-muted m-0 small">Tel: (555) 123-4567 | contacto@northsolutions.com</p>
          </div>
        </div>
        <div class="text-end">
          <h1 class="m-0 text-primary" style="font-size: 1.8rem;">Reporte de KPIs Ejecutivo</h1>
          <p class="text-muted m-0">Generado el: {{ new Date().toLocaleString() }} | Confidencial</p>
          <p class="text-muted m-0">Generado por: <strong>{{ currentUserName }}</strong></p>
          <p class="fw-bold mt-1 mb-0" v-if="filterStartDate || filterEndDate">
            Periodo del reporte: {{ filterStartDate || 'Inicio' }} al {{ filterEndDate || 'Fin' }}
          </p>
          <p class="fw-bold mt-1 mb-0 text-success" v-if="comparePeriod !== 'NONE'">
            Comparativa activa: {{ getCompareLabel(comparePeriod) }}
          </p>
        </div>
      </div>

      <!-- RESUMEN GENERAL -->
      <div v-if="selectedArea === 'ALL'" class="row g-4">
        <!-- 1. Ventas (Más Importante) -->
        <div class="col-md-12">
          <div class="card shadow-sm border-primary">
            <div class="card-body">
              <h4 class="card-title text-primary"><i class="bi bi-cash-coin"></i> 1. Desempeño Comercial (Ingresos)</h4>
              <p class="text-muted small">Agrupado por: <strong>{{ getGroupLabel(salesTimeGroup) }}</strong>. Suma total de ingresos en el periodo filtrado.</p>
              <div style="position: relative; height: 300px;">
                <Bar v-if="salesChartData" :data="salesChartData" :options="barChartOptions" />
                <div v-else class="text-center text-muted py-5">Cargando...</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. Almacén e Inventario -->
        <div class="col-md-6">
          <div class="card h-100 shadow-sm border-success">
            <div class="card-body">
              <h5 class="card-title text-success"><i class="bi bi-box-seam"></i> 2. Salud del Inventario</h5>
              <p class="text-muted small">Productos con abastecimiento adecuado frente a bajo stock (< 10).</p>
              
              <div v-if="compareStats" class="mb-3 alert alert-warning py-2 small">
                <strong>Comparativa:</strong> Prod. bajo stock actual: {{ compareStats.whCurrentLow }} | Anterior: {{ compareStats.whPrevLow }} 
                <span :class="compareStats.whDiff <= 0 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                  ({{ compareStats.whDiff > 0 ? '+' : '' }}{{ compareStats.whDiff }}% bajo stock)
                </span>
              </div>

              <div style="position: relative; height: 250px;">
                <Pie v-if="warehouseChartData" :data="warehouseChartData" :options="pieChartOptions" />
                <div v-else class="text-center text-muted py-5">Cargando...</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 3. Recursos Humanos -->
        <div class="col-md-6">
          <div class="card h-100 shadow-sm border-info">
            <div class="card-body">
              <h5 class="card-title text-info"><i class="bi bi-people"></i> 3. Tasa de Asistencia Operativa</h5>
              <p class="text-muted small">Contrasta las asistencias regulares contra faltas o retardos.</p>
              
              <div v-if="compareStats" class="mb-3 alert alert-info py-2 small">
                <strong>Comparativa:</strong> Asistencias actuales: {{ compareStats.hrCurrent }} | Anteriores: {{ compareStats.hrPrev }} 
                <span :class="compareStats.hrDiff >= 0 ? 'text-success fw-bold' : 'text-danger fw-bold'">
                  ({{ compareStats.hrDiff > 0 ? '+' : '' }}{{ compareStats.hrDiff }}%)
                </span>
              </div>

              <div style="position: relative; height: 250px;">
                <Pie v-if="hrChartData" :data="hrChartData" :options="pieChartOptions" />
                <div v-else class="text-center text-muted py-5">Cargando...</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- REPORTES ESPECÍFICOS -->
      <div v-if="selectedArea === 'SALES'">
        <h4 class="mb-3 text-primary"><i class="bi bi-cash-coin"></i> Reporte Detallado de Ventas</h4>
        <div class="card shadow-sm mb-4 border-primary">
          <div class="card-body">
            <div style="position: relative; height: 350px;">
              <Bar v-if="salesChartData" :data="salesChartData" :options="barChartOptions" />
            </div>
          </div>
        </div>
        <table class="table table-bordered table-striped mt-3 align-middle">
          <thead class="table-dark">
            <tr>
              <th>ID Venta</th>
              <th>Fecha</th>
              <th>Total</th>
              <th>Usuario</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sale in filteredSales" :key="sale.id">
              <td><strong>#{{ sale.id }}</strong></td>
              <td>{{ new Date(sale.saleDate).toLocaleString() }}</td>
              <td class="text-success fw-bold">${{ sale.total?.toFixed(2) }}</td>
              <td>{{ sale.createdBy }}</td>
            </tr>
            <tr v-if="filteredSales.length === 0">
              <td colspan="4" class="text-center text-muted">No hay ventas en el periodo seleccionado.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="selectedArea === 'WAREHOUSE'">
        <h4 class="mb-3 text-success"><i class="bi bi-box-seam"></i> Reporte de Existencias</h4>
        <div v-if="compareStats" class="mb-3 alert alert-warning py-2">
          <strong>Comparativa:</strong> Prod. bajo stock actual: {{ compareStats.whCurrentLow }} | Anterior: {{ compareStats.whPrevLow }} 
          <span :class="compareStats.whDiff <= 0 ? 'text-success fw-bold' : 'text-danger fw-bold'">
            ({{ compareStats.whDiff > 0 ? '+' : '' }}{{ compareStats.whDiff }}% bajo stock)
          </span>
        </div>
        <div class="card shadow-sm mb-4 border-success">
          <div class="card-body">
            <div style="position: relative; height: 350px;">
              <Pie v-if="warehouseChartData" :data="warehouseChartData" :options="pieChartOptions" />
            </div>
          </div>
        </div>
        <table class="table table-bordered table-striped mt-3 align-middle">
          <thead class="table-dark">
            <tr>
              <th>Producto</th>
              <th>Stock Actual</th>
              <th>Precio Venta</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="prod in rawProducts" :key="prod.id">
              <td>{{ prod.name }}</td>
              <td :class="{'text-danger fw-bold': prod.stock < 10}">{{ prod.stock }}</td>
              <td>${{ prod.price?.toFixed(2) }}</td>
              <td>
                <span class="badge" :class="prod.stock < 10 ? 'bg-danger' : 'bg-success'">
                  {{ prod.stock < 10 ? 'Bajo Stock' : 'Adecuado' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="selectedArea === 'HR'">
        <h4 class="mb-3 text-info"><i class="bi bi-people"></i> Reporte de Ausentismo y Asistencias</h4>
        <div v-if="compareStats" class="mb-3 alert alert-info py-2">
          <strong>Comparativa:</strong> Asistencias actuales: {{ compareStats.hrCurrent }} | Anteriores: {{ compareStats.hrPrev }} 
          <span :class="compareStats.hrDiff >= 0 ? 'text-success fw-bold' : 'text-danger fw-bold'">
            ({{ compareStats.hrDiff > 0 ? '+' : '' }}{{ compareStats.hrDiff }}%)
          </span>
        </div>
        <div class="card shadow-sm mb-4 border-info">
          <div class="card-body">
            <div style="position: relative; height: 350px;">
              <Pie v-if="hrChartData" :data="hrChartData" :options="pieChartOptions" />
            </div>
          </div>
        </div>
        <table class="table table-bordered table-striped mt-3 align-middle">
          <thead class="table-dark">
            <tr>
              <th>ID Empleado</th>
              <th>Fecha</th>
              <th>Estatus</th>
              <th>Registrado Por</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="att in filteredAttendance" :key="att.id">
              <td>EMP-{{ att.employee?.id }} ({{ att.employee?.firstName }})</td>
              <td>{{ new Date(att.date).toLocaleDateString() }}</td>
              <td>
                <span class="badge" :class="att.status === 'PRESENTE' ? 'bg-success' : 'bg-danger'">
                  {{ att.status }}
                </span>
              </td>
              <td>{{ att.registeredBy }}</td>
            </tr>
            <tr v-if="filteredAttendance.length === 0">
              <td colspan="4" class="text-center text-muted">No hay registros de asistencia en el periodo.</td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement
} from 'chart.js'
import { Bar, Pie } from 'vue-chartjs'
import salesService from '../services/salesService'
import warehouseService from '../services/warehouseService'
import hrService from '../services/hrService'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const currentUserName = ref(sessionStorage.getItem('username') || 'Usuario')

// Filters State
const selectedArea = ref('ALL')
const filterStartDate = ref('')
const filterEndDate = ref('')
const salesTimeGroup = ref('MONTH')
const comparePeriod = ref('NONE')

// Chart Data
const salesChartData = ref(null)
const warehouseChartData = ref(null)
const hrChartData = ref(null)

// Raw Data
const rawSales = ref([])
const rawProducts = ref([])
const rawAttendance = ref([])
const rawTransactions = ref([])

// Filtered Data
const filteredSales = ref([])
const filteredAttendance = ref([])

// Comparative Stats
const compareStats = ref(null)

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function(value) {
          if (value >= 1000000) return '$' + (value / 1000000).toFixed(1) + 'M'
          if (value >= 1000) return '$' + (value / 1000).toFixed(1) + 'k'
          return '$' + value
        }
      }
    }
  }
}

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false
}

// Helper Functions
const getStartOfDay = (d) => { const dt = new Date(d); dt.setHours(0,0,0,0); return dt; }
const getStartOfWeek = (d) => {
  const dt = new Date(d);
  const day = dt.getDay();
  const diff = dt.getDate() - day + (day === 0 ? -6 : 1);
  dt.setDate(diff);
  dt.setHours(0,0,0,0);
  return dt;
}
const getStartOfMonth = (d) => { const dt = new Date(d); dt.setDate(1); dt.setHours(0,0,0,0); return dt; }
const getStartOfSemester = (d) => {
  const dt = new Date(d);
  dt.setMonth(dt.getMonth() < 6 ? 0 : 6);
  dt.setDate(1);
  dt.setHours(0,0,0,0);
  return dt;
}

const formatGroupKey = (dateStr, groupType) => {
  if(!dateStr) return 'Desc';
  const d = new Date(dateStr);
  if(isNaN(d.getTime())) return 'Desc';

  if (groupType === 'DAY') return getStartOfDay(d).toISOString().split('T')[0];
  if (groupType === 'WEEK') {
    const sw = getStartOfWeek(d);
    return `Semana ${sw.toLocaleDateString()}`;
  }
  if (groupType === 'MONTH') {
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}`;
  }
  if (groupType === 'SIX_MONTHS') {
    return `${d.getFullYear()}-${d.getMonth() < 6 ? 'S1' : 'S2'}`;
  }
  if (groupType === 'YEAR') {
    return `${d.getFullYear()}`;
  }
  return 'Desc';
}

const getGroupLabel = (val) => {
  const labels = { DAY: 'Día', WEEK: 'Semana', MONTH: 'Mes', SIX_MONTHS: 'Seis Meses', YEAR: 'Anual' }
  return labels[val] || val
}

const getCompareLabel = (val) => {
  const labels = { PREV_DAY: 'vs Día Anterior', PREV_WEEK: 'vs Semana Anterior', PREV_MONTH: 'vs Mes Anterior' }
  return labels[val] || val
}

const getPeriodBounds = (period) => {
  const now = new Date();
  const currentStart = new Date(now);
  const currentEnd = new Date(now);
  const previousStart = new Date(now);
  const previousEnd = new Date(now);

  if (period === 'PREV_DAY') {
    currentStart.setHours(0,0,0,0);
    currentEnd.setHours(23,59,59,999);
    previousStart.setDate(now.getDate() - 1);
    previousStart.setHours(0,0,0,0);
    previousEnd.setDate(now.getDate() - 1);
    previousEnd.setHours(23,59,59,999);
  } else if (period === 'PREV_WEEK') {
    const sw = getStartOfWeek(now);
    currentStart.setTime(sw.getTime());
    currentEnd.setHours(23,59,59,999);
    previousStart.setTime(sw.getTime());
    previousStart.setDate(previousStart.getDate() - 7);
    previousEnd.setTime(sw.getTime());
    previousEnd.setMilliseconds(-1);
  } else if (period === 'PREV_MONTH') {
    const sm = getStartOfMonth(now);
    currentStart.setTime(sm.getTime());
    currentEnd.setHours(23,59,59,999);
    previousStart.setTime(sm.getTime());
    previousStart.setMonth(previousStart.getMonth() - 1);
    previousEnd.setTime(sm.getTime());
    previousEnd.setMilliseconds(-1);
  } else {
    return null;
  }
  return { currentStart, currentEnd, previousStart, previousEnd };
}

const calculateComparisons = () => {
  const bounds = getPeriodBounds(comparePeriod.value)
  if (!bounds) {
    compareStats.value = null
    return
  }

  // HR Comparison
  let currentHR = 0, prevHR = 0
  rawAttendance.value.forEach(a => {
    const d = new Date(a.date)
    if (d >= bounds.currentStart && d <= bounds.currentEnd && a.status === 'PRESENTE') currentHR++
    if (d >= bounds.previousStart && d <= bounds.previousEnd && a.status === 'PRESENTE') prevHR++
  })

  // Warehouse Comparison
  let currentLowStock = 0
  rawProducts.value.forEach(p => { if(p.stock < 10) currentLowStock++ })

  const stockAtPrevEnd = {}
  rawProducts.value.forEach(p => stockAtPrevEnd[p.id] = p.stock)

  rawTransactions.value.forEach(t => {
    const d = new Date(t.transactionDate)
    if (d > bounds.previousEnd) {
      if (t.type === 'IN') stockAtPrevEnd[t.product?.id || t.productId] -= t.quantity
      else if (t.type === 'OUT') stockAtPrevEnd[t.product?.id || t.productId] += t.quantity
    }
  })

  let prevLowStock = 0
  Object.values(stockAtPrevEnd).forEach(s => {
    if (s < 10) prevLowStock++
  })

  compareStats.value = {
    hrCurrent: currentHR,
    hrPrev: prevHR,
    hrDiff: prevHR > 0 ? (((currentHR - prevHR) / prevHR) * 100).toFixed(1) : (currentHR > 0 ? 100 : 0),
    whCurrentLow: currentLowStock,
    whPrevLow: prevLowStock,
    whDiff: prevLowStock > 0 ? (((currentLowStock - prevLowStock) / prevLowStock) * 100).toFixed(1) : (currentLowStock > 0 ? 100 : 0)
  }
}

const applyFilters = () => {
  // 1. Base date filtering for standard display
  let sales = [...rawSales.value]
  let attendance = [...rawAttendance.value]
  
  const start = filterStartDate.value ? new Date(filterStartDate.value + 'T00:00:00') : null
  const end = filterEndDate.value ? new Date(filterEndDate.value + 'T23:59:59') : null

  if (start) {
    sales = sales.filter(s => new Date(s.saleDate) >= start)
    attendance = attendance.filter(a => new Date(a.date) >= start)
  }
  if (end) {
    sales = sales.filter(s => new Date(s.saleDate) <= end)
    attendance = attendance.filter(a => new Date(a.date) <= end)
  }

  filteredSales.value = sales
  filteredAttendance.value = attendance

  // 2. Group Sales for Chart
  const salesGrouped = sales.reduce((acc, sale) => {
    const key = formatGroupKey(sale.saleDate, salesTimeGroup.value)
    acc[key] = (acc[key] || 0) + (sale.total || 0)
    return acc
  }, {})

  const sortedKeys = Object.keys(salesGrouped).sort()
  salesChartData.value = {
    labels: sortedKeys.length ? sortedKeys : ['Sin Datos'],
    datasets: [{
      label: 'Ingresos ($)',
      backgroundColor: '#0d6efd',
      data: sortedKeys.length ? sortedKeys.map(k => salesGrouped[k]) : [0]
    }]
  }

  // 3. Process HR standard
  let present = 0, absent = 0
  attendance.forEach(a => {
    if (a.status === 'PRESENTE') present++
    else absent++
  })
  hrChartData.value = {
    labels: ['Asistencias', 'Faltas / Retardos'],
    datasets: [{
      backgroundColor: ['#0dcaf0', '#ffc107'],
      data: [present, absent]
    }]
  }

  // 4. Process Warehouse standard
  let lowStock = 0, normalStock = 0
  rawProducts.value.forEach(p => {
    if (p.stock < 10) lowStock++
    else normalStock++
  })
  warehouseChartData.value = {
    labels: ['Stock Normal', 'Bajo Stock (< 10)'],
    datasets: [{
      backgroundColor: ['#198754', '#dc3545'],
      data: [normalStock, lowStock]
    }]
  }

  // 5. Comparisons
  if (comparePeriod.value !== 'NONE') {
    calculateComparisons()
  } else {
    compareStats.value = null
  }
}

const loadData = async () => {
  try {
    const [sRes, pRes, aRes, tRes] = await Promise.allSettled([
      salesService.getSales(),
      warehouseService.getProducts(),
      hrService.getAllAttendance(),
      warehouseService.getAllTransactions()
    ])
    
    rawSales.value = sRes.status === 'fulfilled' ? sRes.value : []
    rawProducts.value = pRes.status === 'fulfilled' ? pRes.value : []
    rawAttendance.value = aRes.status === 'fulfilled' ? aRes.value : []
    rawTransactions.value = tRes.status === 'fulfilled' ? tRes.value : []

    applyFilters()
  } catch (error) {
    console.error("Error cargando datos para KPIs:", error)
  }
}

const downloadReport = () => {
  setTimeout(() => window.print(), 100)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.only-print {
  display: none;
}

@media print {
  .no-print {
    display: none !important;
  }
  .only-print {
    display: block !important;
  }
  .print-section {
    border: none !important;
    margin: 0 !important;
    padding: 0 !important;
  }
  .card {
    border: 1px solid #ddd !important;
    break-inside: avoid;
  }
  table {
    border-collapse: collapse !important;
    width: 100%;
  }
  th, td {
    border: 1px solid #000 !important;
  }
  
  canvas {
    max-height: 350px !important;
  }
  .card-body {
    padding: 0.5rem !important;
  }
}
</style>
