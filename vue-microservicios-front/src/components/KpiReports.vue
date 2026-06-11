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
      <template v-if="comparePeriod === 'NONE'">
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
      </template>
      <template v-else>
        <div class="col-md-3">
          <label class="form-label fw-bold"><i class="bi bi-calendar-event"></i> Periodo 1:</label>
          <input v-if="comparePeriod === 'TWO_DAYS'" type="date" class="form-control" v-model="compareDate1" @change="applyFilters">
          <input v-if="comparePeriod === 'TWO_WEEKS'" type="week" class="form-control" v-model="compareDate1" @change="applyFilters">
          <input v-if="comparePeriod === 'TWO_MONTHS'" type="month" class="form-control" v-model="compareDate1" @change="applyFilters">
        </div>
        <div class="col-md-3">
          <label class="form-label fw-bold"><i class="bi bi-calendar-event"></i> Periodo 2:</label>
          <input v-if="comparePeriod === 'TWO_DAYS'" type="date" class="form-control" v-model="compareDate2" @change="applyFilters">
          <input v-if="comparePeriod === 'TWO_WEEKS'" type="week" class="form-control" v-model="compareDate2" @change="applyFilters">
          <input v-if="comparePeriod === 'TWO_MONTHS'" type="month" class="form-control" v-model="compareDate2" @change="applyFilters">
        </div>
      </template>
      
      <div class="col-md-3">
        <label class="form-label fw-bold"><i class="bi bi-arrow-left-right"></i> Modo de Comparación:</label>
        <select class="form-select" v-model="comparePeriod" @change="applyFilters">
          <option value="NONE">Sin Comparar (Rango Libre)</option>
          <option value="TWO_DAYS">Comparar 2 Días</option>
          <option value="TWO_WEEKS">Comparar 2 Semanas</option>
          <option value="TWO_MONTHS">Comparar 2 Meses</option>
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
                <Bar v-if="hrChartData && comparePeriod !== 'NONE'" :data="hrChartData" :options="barChartOptions" />
                <Pie v-else-if="hrChartData" :data="hrChartData" :options="pieChartOptions" />
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
              <Bar v-if="hrChartData && comparePeriod !== 'NONE'" :data="hrChartData" :options="barChartOptions" />
              <Pie v-else-if="hrChartData" :data="hrChartData" :options="pieChartOptions" />
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
                <span class="badge" :class="att.status === 'PRESENT' ? 'bg-success' : 'bg-danger'">
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

const barLabelsPlugin = {
  id: 'barLabels',
  afterDatasetsDraw(chart) {
    const { ctx } = chart;
    chart.data.datasets.forEach((dataset, i) => {
      const meta = chart.getDatasetMeta(i);
      if (meta.type !== 'bar') return;
      meta.data.forEach((bar, index) => {
        const val = dataset.data[index];
        if (!val) return;
        ctx.save();
        ctx.fillStyle = '#495057';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'bottom';
        let text = '$' + Number(val).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (val >= 1000000) text = '$' + (val / 1000000).toFixed(1) + 'M';
        else if (val >= 1000) text = '$' + (val / 1000).toFixed(1) + 'k';
        ctx.fillText(text, bar.x, bar.y - 5);
        ctx.restore();
      });
    });
  }
}
ChartJS.register(barLabelsPlugin)

const currentUserName = ref(sessionStorage.getItem('username') || 'Usuario')

// Filters State
const selectedArea = ref('ALL')
const filterStartDate = ref('')
const filterEndDate = ref('')
const salesTimeGroup = ref('MONTH')
const comparePeriod = ref('NONE')
const compareDate1 = ref('')
const compareDate2 = ref('')

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
  const labels = { TWO_DAYS: 'Comparativa de 2 Días', TWO_WEEKS: 'Comparativa de 2 Semanas', TWO_MONTHS: 'Comparativa de 2 Meses' }
  return labels[val] || val
}

const getCustomBounds = (val, type) => {
  if (!val) return null;
  let start = new Date();
  let end = new Date();
  if (type === 'TWO_DAYS') {
    start = new Date(val + 'T00:00:00');
    end = new Date(val + 'T23:59:59');
  } else if (type === 'TWO_MONTHS') {
    const parts = val.split('-');
    start = new Date(parts[0], parseInt(parts[1])-1, 1);
    end = new Date(parts[0], parseInt(parts[1]), 0, 23, 59, 59);
  } else if (type === 'TWO_WEEKS') {
    const year = parseInt(val.substring(0, 4));
    const week = parseInt(val.substring(6));
    const simpleDate = new Date(year, 0, 1 + (week - 1) * 7);
    start = getStartOfWeek(simpleDate);
    end = new Date(start);
    end.setDate(end.getDate() + 6);
    end.setHours(23,59,59,999);
  }
  return { start, end };
}

const applyFilters = () => {
  if (comparePeriod.value !== 'NONE') {
    const bounds1 = getCustomBounds(compareDate1.value, comparePeriod.value);
    const bounds2 = getCustomBounds(compareDate2.value, comparePeriod.value);

    let sales1 = 0, sales2 = 0;
    let hrPres1 = 0, hrAbs1 = 0, hrPres2 = 0, hrAbs2 = 0;
    
    if (bounds1 && bounds2) {
      rawSales.value.forEach(s => {
        const d = new Date(s.saleDate);
        if (d >= bounds1.start && d <= bounds1.end) sales1 += s.total || 0;
        if (d >= bounds2.start && d <= bounds2.end) sales2 += s.total || 0;
      });

      rawAttendance.value.forEach(a => {
        const d = new Date(a.date);
        if (d >= bounds1.start && d <= bounds1.end) {
          if (a.status === 'PRESENT') hrPres1++; else hrAbs1++;
        }
        if (d >= bounds2.start && d <= bounds2.end) {
          if (a.status === 'PRESENT') hrPres2++; else hrAbs2++;
        }
      });
      
      filteredSales.value = rawSales.value.filter(s => {
        const d = new Date(s.saleDate);
        return (d >= bounds1.start && d <= bounds1.end) || (d >= bounds2.start && d <= bounds2.end);
      });
      filteredAttendance.value = rawAttendance.value.filter(a => {
        const d = new Date(a.date);
        return (d >= bounds1.start && d <= bounds1.end) || (d >= bounds2.start && d <= bounds2.end);
      });
    } else {
      filteredSales.value = [];
      filteredAttendance.value = [];
    }

    const label1 = compareDate1.value || 'Periodo 1';
    const label2 = compareDate2.value || 'Periodo 2';

    salesChartData.value = {
      labels: [label1, label2],
      datasets: [{
        label: 'Ingresos Totales ($)',
        backgroundColor: ['#0d6efd', '#198754'],
        data: [sales1, sales2]
      }]
    };

    hrChartData.value = {
      labels: [label1, label2],
      datasets: [
        { label: 'Asistencias', backgroundColor: '#0dcaf0', data: [hrPres1, hrPres2] },
        { label: 'Faltas', backgroundColor: '#ffc107', data: [hrAbs1, hrAbs2] }
      ]
    };

    let lowStock = 0, normalStock = 0
    rawProducts.value.forEach(p => {
      if (p.stock < 10) lowStock++
      else normalStock++
    })
    warehouseChartData.value = {
      labels: ['Stock Normal', 'Bajo Stock (< 10)'],
      datasets: [{ backgroundColor: ['#198754', '#dc3545'], data: [normalStock, lowStock] }]
    }

    compareStats.value = null;
    return;
  }

  // --- Normal Logic ---
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
    if (a.status === 'PRESENT') present++
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

  compareStats.value = null
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
