<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Aprobación</span>
        <h1>Bandeja de Aprobaciones</h1>
        <p>Revisa y autoriza las solicitudes enviadas por otras áreas operativas.</p>
      </div>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <div class="panel">
      <h2>Pendientes de Autorización</h2>
      <div class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>ID Solicitud</th>
              <th>Módulo</th>
              <th>ID Doc.</th>
              <th>Solicitante</th>
              <th>Descripción</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="pendientes.length === 0">
              <td colspan="6" class="text-center text-muted py-4">No hay solicitudes pendientes en este momento.</td>
            </tr>
            <tr v-for="req in pendientes" :key="req.id">
              <td><strong>#{{ req.id }}</strong></td>
              <td><span class="badge bg-info text-dark">{{ req.modulo }}</span></td>
              <td>#{{ req.documentId }}</td>
              <td>{{ req.responsable }}</td>
              <td>{{ req.descripcion }}</td>
              <td>
                <button class="btn btn-sm btn-success me-2 mb-1" @click="aprobar(req.documentId)" :disabled="loading">
                  <i class="bi bi-check2-circle"></i> Autorizar
                </button>
                <button class="btn btn-sm btn-danger mb-1" @click="rechazar(req.documentId)" :disabled="loading">
                  <i class="bi bi-x-circle"></i> Rechazar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <div v-if="resultado" class="panel mt-4">
      <h2>Resultado del Flujo</h2>
      <pre><code>{{ resultado }}</code></pre>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { workflowService } from '../services/workflowService'
import { getErrorMessage } from '../services/api'

const pendientes = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')
const resultado = ref(null)

const loadPendientes = async () => {
  try {
    const currentToken = sessionStorage.getItem('token')
    const res = await workflowService.getPendientes(currentToken)
    pendientes.value = res.data || []
  } catch (err) {
    console.error("Error cargando pendientes", err)
  }
}

const aprobar = async (documentId) => {
  loading.value = true
  error.value = ''
  success.value = ''
  resultado.value = null
  
  try {
    const currentToken = sessionStorage.getItem('token')
    const res = await workflowService.aprobarDocumento(documentId, currentToken)
    success.value = 'Documento procesado correctamente (Autorizado)'
    resultado.value = res.data
    window.dispatchEvent(new Event('workflow-updated'))
    await loadPendientes()
  } catch (err) {
    error.value = getErrorMessage(err)
    if(err.response?.data) {
        resultado.value = err.response.data
    }
  } finally {
    loading.value = false
  }
}

const rechazar = async (documentId) => {
  if (!confirm(`¿Estás seguro de que deseas rechazar el documento #${documentId}?`)) return
  
  loading.value = true
  error.value = ''
  success.value = ''
  resultado.value = null
  
  try {
    const currentToken = sessionStorage.getItem('token')
    const res = await workflowService.rechazarDocumento(documentId, currentToken)
    success.value = 'Documento procesado correctamente (Rechazado)'
    resultado.value = res.data
    window.dispatchEvent(new Event('workflow-updated'))
    await loadPendientes()
  } catch (err) {
    error.value = getErrorMessage(err)
    if(err.response?.data) {
        resultado.value = err.response.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPendientes()
})
</script>
