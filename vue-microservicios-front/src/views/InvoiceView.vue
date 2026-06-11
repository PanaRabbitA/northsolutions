<template>
  <div class="invoice-page">
    <div v-if="!invoice" class="text-center p-5">
      <p>Cargando factura...</p>
    </div>
    <div v-else class="invoice-container">
      <div class="invoice-header d-flex justify-content-between align-items-center mb-4">
        <div>
          <img src="../assets/images/logo.png" alt="North Solutions Logo" style="max-height: 80px;" />
        </div>
        <div class="text-end">
          <h2 class="mb-0 text-primary">FACTURA COMERCIAL</h2>
          <p class="text-muted mb-0">Comprobante Fiscal</p>
          <strong>Folio: #{{ invoice.id.toString().padStart(6, '0') }}</strong>
        </div>
      </div>

      <div class="row mb-4">
        <div class="col-sm-6">
          <h6 class="text-muted">EMISOR</h6>
          <strong>NORTH SOLUTIONS SA DE CV</strong><br>
          San Felipe del Progreso<br>
          Estado de México, CP 50640<br>
          RFC: NSO210415XXX
        </div>
        <div class="col-sm-6 text-end">
          <h6 class="text-muted">DETALLES DE EXPEDICIÓN</h6>
          <strong>Fecha:</strong> {{ new Date(invoice.saleDate).toLocaleString() }}<br>
          <strong>Vendedor:</strong> {{ invoice.createdBy }}<br>
          <strong>Moneda:</strong> MXN
        </div>
      </div>

      <table class="table table-bordered mb-4">
        <thead class="table-light">
          <tr>
            <th>CANT.</th>
            <th>PRODUCTO</th>
            <th class="text-end">PRECIO UNITARIO</th>
            <th class="text-end">IMPORTE</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in invoice.details" :key="index">
            <td>{{ item.quantity }}</td>
            <td>{{ item.productName }}</td>
            <td class="text-end">${{ item.price.toFixed(2) }}</td>
            <td class="text-end">${{ (item.quantity * item.price).toFixed(2) }}</td>
          </tr>
        </tbody>
      </table>

      <div class="row">
        <div class="col-8">
          <p class="text-muted small">
            <em>Condiciones de pago: Contado. Este documento es una representación impresa de una venta.</em>
          </p>
        </div>
        <div class="col-4">
          <table class="table table-sm table-borderless">
            <tr>
              <td class="text-end"><strong>Subtotal:</strong></td>
              <td class="text-end">${{ (invoice.total / 1.16).toFixed(2) }}</td>
            </tr>
            <tr>
              <td class="text-end"><strong>IVA (16%):</strong></td>
              <td class="text-end">${{ (invoice.total - (invoice.total / 1.16)).toFixed(2) }}</td>
            </tr>
            <tr class="border-top">
              <td class="text-end"><strong>TOTAL:</strong></td>
              <td class="text-end"><strong>${{ invoice.total.toFixed(2) }}</strong></td>
            </tr>
          </table>
        </div>
      </div>
      
      <div class="text-center mt-5">
        <p class="mb-0"><strong>¡Gracias por su preferencia!</strong></p>
        <p class="text-muted">www.northsolutions.com</p>
      </div>

      <!-- Botón de impresión (solo visible en pantalla) -->
      <div class="text-center mt-5 d-print-none">
        <button class="btn btn-primary" @click="doPrint">
          <i class="bi bi-printer"></i> Imprimir Ahora
        </button>
        <button class="btn btn-outline-secondary ms-2" @click="closeWindow">
          Cerrar
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

const invoice = ref(null)

onMounted(() => {
  const data = localStorage.getItem('print_invoice_data')
  if (data) {
    try {
      invoice.value = JSON.parse(data)
      // Esperar a que Vue actualice el DOM con los datos de la factura
      nextTick(() => {
        // Disparar la impresión automáticamente al cargar, dando un poco más de tiempo para fuentes/imágenes
        setTimeout(() => {
          window.print()
        }, 800)
      })
    } catch (e) {
      console.error("Error al leer datos de factura", e)
    }
  } else {
    alert("No se encontraron datos para la factura.")
  }
})

const doPrint = () => {
  window.print()
}

const closeWindow = () => {
  window.close()
}
</script>

<style scoped>
.invoice-page {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding: 40px 20px;
}
.invoice-container {
  background: white;
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

@media print {
  .invoice-page {
    background-color: white;
    padding: 0;
  }
  .invoice-container {
    box-shadow: none;
    padding: 0;
    max-width: 100%;
  }
}
</style>
