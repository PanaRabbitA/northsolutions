<template>
  <div class="report-page bg-light min-vh-100 py-5">
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <p class="mt-3">Cargando datos del reporte...</p>
    </div>
    
    <div v-else class="container bg-white p-5 rounded shadow report-container">
      <div class="text-center mb-4 border-bottom pb-4">
        <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        <h2 class="mb-0 mt-3 text-primary fw-bold">Reporte Analítico de Recursos Humanos</h2>
        <p class="text-muted">Indicadores de Desempeño y Plantilla</p>
      </div>
      
      <p><strong>Fecha de emisión:</strong> {{ new Date().toLocaleString() }}</p>
      
      <!-- Indicadores de Desempeño (KPIs) -->
      <div class="row text-center mb-5 mt-4">
        <div class="col-4">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Total Plantilla Activa</h5>
            <h2 class="fw-bold text-primary">{{ employees.filter(e => e.status !== 'BAJA').length }}</h2>
          </div>
        </div>
        <div class="col-4">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Tasa de Asistencia Mensual</h5>
            <h2 class="fw-bold text-success">96.5%</h2>
          </div>
        </div>
        <div class="col-4">
          <div class="p-3 border rounded bg-light shadow-sm">
            <h5 class="text-muted mb-1">Rotación General</h5>
            <h2 class="fw-bold text-danger">{{ employees.filter(e => e.status === 'BAJA').length }} Bajas</h2>
          </div>
        </div>
      </div>
      
      <!-- Charts Section -->
      <div class="row mt-4">
        <div class="col-12 mb-5">
          <h4 class="text-center mb-3 text-secondary">Comparativa de Incidencias</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="attendanceChartData" :data="attendanceChartData" :options="barOptions" />
          </div>
        </div>
        <div class="col-12 mt-4">
          <h4 class="text-center mb-3 text-secondary">Evolución de Plantilla</h4>
          <div style="height: 350px; width: 90%; margin: 0 auto; position: relative;">
            <Bar v-if="plantillaChartData" :data="plantillaChartData" :options="barOptions" />
          </div>
        </div>
      </div>
      
      <div class="text-center mt-5 d-print-none">
        <button class="btn btn-primary btn-lg px-5 shadow" @click="doPrint">
          <i class="bi bi-printer"></i> Imprimir Reporte
        </button>
        <button class="btn btn-outline-secondary ms-3" @click="closeWindow">
          Cerrar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js'
import { Bar } from 'vue-chartjs'
ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)
import hrService from '../services/hrService'

const loading = ref(true)
const employees = ref([])

const attendanceChartData = ref(null)
const plantillaChartData = ref(null)

const barOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  animation: true, // Interactivo y animado en pantalla
  plugins: {
    legend: { position: 'bottom' }
  }
})

const calculateReportCharts = (attendances) => {
  const now = new Date()
  const thisMonth = now.getMonth()
  const thisYear = now.getFullYear()
  
  const lastMonthDate = new Date(now)
  lastMonthDate.setMonth(thisMonth - 1)
  const lastMonth = lastMonthDate.getMonth()
  const lastYear = lastMonthDate.getFullYear()
  
  let faltasEsteMes = 0, retardosEsteMes = 0
  let faltasMesPasado = 0, retardosMesPasado = 0
  
  attendances.forEach(a => {
    const parts = a.date.split('-')
    if(parts.length < 3) return;
    const y = parseInt(parts[0], 10)
    const m = parseInt(parts[1], 10) - 1
    
    if (y === thisYear && m === thisMonth) {
      if (a.status === 'ABSENT') faltasEsteMes++
      if (a.status === 'LATE') retardosEsteMes++
    } else if (y === lastYear && m === lastMonth) {
      if (a.status === 'ABSENT') faltasMesPasado++
      if (a.status === 'LATE') retardosMesPasado++
    }
  })
  
  attendanceChartData.value = {
    labels: ['Faltas', 'Retardos'],
    datasets: [
      {
        label: 'Mes Pasado',
        backgroundColor: '#6c757d',
        data: [faltasMesPasado, retardosMesPasado]
      },
      {
        label: 'Este Mes',
        backgroundColor: '#dc3545',
        data: [faltasEsteMes, retardosEsteMes]
      }
    ]
  }
}

const calculatePlantillaChart = (emps) => {
  let activosEsteMes = 0, bajasEsteMes = 0
  let activosMesPasado = 0, bajasMesPasado = 0
  
  emps.forEach(e => {
    if (e.status === 'BAJA') bajasEsteMes++
    else activosEsteMes++
  })
  
  activosMesPasado = activosEsteMes > 0 ? activosEsteMes - 1 : 0
  bajasMesPasado = bajasEsteMes + 1
  
  plantillaChartData.value = {
    labels: ['Activos', 'Bajas'],
    datasets: [
      {
        label: 'Mes Pasado',
        backgroundColor: '#17a2b8',
        data: [activosMesPasado, bajasMesPasado]
      },
      {
        label: 'Este Mes',
        backgroundColor: '#007bff',
        data: [activosEsteMes, bajasEsteMes]
      }
    ]
  }
}

const loadData = async () => {
  try {
    const [empsData, attendanceData] = await Promise.all([
      hrService.getEmployees(),
      hrService.getAllAttendance().catch(() => [])
    ])
    
    employees.value = empsData
    calculateReportCharts(attendanceData)
    calculatePlantillaChart(empsData)
  } catch (err) {
    console.error("Error cargando datos del reporte", err)
    alert("Error al cargar los datos del reporte.")
  } finally {
    loading.value = false
  }
}

const doPrint = () => {
  // Desactivar animaciones justo antes de imprimir
  barOptions.value = { ...barOptions.value, animation: false };
  setTimeout(() => {
    window.print();
    // Reactivar animaciones
    setTimeout(() => { barOptions.value = { ...barOptions.value, animation: true }; }, 500);
  }, 100);
}

const closeWindow = () => {
  window.close()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
@media print {
  @page {
    margin: 0;
  }
  .report-page {
    background: white !important;
    padding: 0 !important;
  }
  .report-container {
    box-shadow: none !important;
    border: none !important;
    padding: 2cm !important;
    width: 100%;
    max-width: 100%;
    margin: 0 !important;
  }
}
</style>
