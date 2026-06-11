<template>
  <section class="support-view h-100 d-flex flex-column">
    <div class="page-header mb-3">
      <div>
        <span class="eyebrow">Soporte y Comunicación</span>
        <h1>Mesa de Ayuda y Chat</h1>
        <p>Gestiona tus peticiones de soporte y comunícate con otros empleados.</p>
      </div>
      <div class="d-flex gap-2">
        <button class="btn" :class="activeTab === 'tickets' ? 'btn-primary' : 'btn-outline-secondary'" @click="activeTab = 'tickets'">
          <i class="bi bi-ticket-detailed"></i> Mesa de Ayuda
        </button>
        <button class="btn" :class="activeTab === 'chat' ? 'btn-primary' : 'btn-outline-secondary'" @click="activeTab = 'chat'">
          <i class="bi bi-chat-dots"></i> Chat Global
        </button>
      </div>
    </div>

    <!-- Pestaña: Mesa de Ayuda -->
    <div v-if="activeTab === 'tickets'" class="row flex-grow-1 overflow-hidden">
      <!-- Lista de Tickets -->
      <div class="col-md-4 h-100 d-flex flex-column border-end pe-3">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h4 class="m-0">{{ isAdmin ? 'Buzón de Peticiones' : 'Mis Tickets' }}</h4>
          <button v-if="!isAdmin" class="btn btn-sm btn-success" @click="showNewTicketModal = true" :disabled="hasActiveTicket">
            <i class="bi bi-plus-lg"></i> Nuevo Ticket
          </button>
        </div>
        
        <div v-if="hasActiveTicket && !isAdmin" class="alert alert-warning py-2 small mb-3">
          <i class="bi bi-exclamation-triangle"></i> Tienes un ticket activo. No puedes crear otro hasta que se resuelva.
        </div>

        <div class="flex-grow-1 overflow-auto pe-2">
          <div v-if="tickets.length === 0" class="text-center text-muted mt-4">
            No hay tickets registrados.
          </div>
          <div v-for="t in tickets" :key="t.id" 
               class="card mb-2 cursor-pointer shadow-sm" 
               :class="{'border-primary': selectedTicket?.id === t.id}"
               @click="selectTicket(t)">
            <div class="card-body p-3">
              <div class="d-flex justify-content-between">
                <span class="fw-bold">#{{ t.id }} - {{ t.title }}</span>
                <span class="badge" :class="getPriorityClass(t.priority)">{{ t.priority }}</span>
              </div>
              <div class="mt-2 small text-muted">
                {{ t.createdBy }} - {{ new Date(t.createdAt).toLocaleString() }}
              </div>
              <div class="mt-2">
                <span class="badge" :class="getStatusClass(t.status)">{{ getStatusLabel(t.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Detalle de Ticket -->
      <div class="col-md-8 h-100 d-flex flex-column ps-3" v-if="selectedTicket">
        <div class="card shadow-sm flex-grow-1 d-flex flex-column overflow-hidden">
          <div class="card-header bg-light d-flex justify-content-between align-items-center py-3">
            <div>
              <h5 class="m-0">Ticket #{{ selectedTicket.id }}: {{ selectedTicket.title }}</h5>
              <small class="text-muted">Creado por: {{ selectedTicket.createdBy }} | Nivel: {{ selectedTicket.priority }}</small>
            </div>
            <div v-if="isAdmin" class="d-flex gap-2 align-items-center">
              <span class="fw-bold small text-muted">Cambiar estado:</span>
              <select class="form-select form-select-sm" :value="selectedTicket.status" @change="updateTicketStatus($event.target.value)">
                <option value="RECIBIDO">Recibido</option>
                <option value="EN_PROCESO">En Proceso</option>
                <option value="FINALIZADO">Finalizado</option>
              </select>
            </div>
            <div v-else>
               <span class="badge" :class="getStatusClass(selectedTicket.status)">{{ getStatusLabel(selectedTicket.status) }}</span>
            </div>
          </div>
          <div class="card-body overflow-auto d-flex flex-column gap-3 bg-light" ref="ticketMessagesContainer">
            <!-- Ticket Description initially -->
            <div class="alert alert-secondary">
              <strong>Descripción Original:</strong><br>
              {{ selectedTicket.description }}
            </div>

            <!-- Messages -->
            <div v-for="msg in ticketMessages" :key="msg.id" 
                 class="d-flex flex-column" 
                 :class="msg.sender === username ? 'align-items-end' : 'align-items-start'">
              <span class="small text-muted mb-1">{{ msg.sender }} - {{ new Date(msg.timestamp).toLocaleTimeString() }}</span>
              <div class="p-2 rounded bg-white shadow-sm" style="max-width: 80%; border: 1px solid #dee2e6;">
                {{ msg.content }}
              </div>
            </div>
          </div>
          <div class="card-footer bg-white">
            <form @submit.prevent="sendTicketMessage" class="d-flex gap-2">
              <input type="text" v-model="newTicketMsg" class="form-control" placeholder="Escribe un mensaje al soporte..." :disabled="selectedTicket.status === 'FINALIZADO'" required>
              <button type="submit" class="btn btn-primary" :disabled="selectedTicket.status === 'FINALIZADO' || !newTicketMsg.trim()">
                <i class="bi bi-send"></i>
              </button>
            </form>
          </div>
        </div>
      </div>
      <div class="col-md-8 h-100 d-flex justify-content-center align-items-center text-muted" v-else>
        <div class="text-center">
          <i class="bi bi-inbox fs-1"></i>
          <p class="mt-2">Selecciona un ticket para ver los detalles.</p>
        </div>
      </div>
    </div>

    <!-- Pestaña: Chat por Áreas -->
    <div v-if="activeTab === 'chat'" class="row flex-grow-1 overflow-hidden">
      <!-- Sidebar de Salas -->
      <div class="col-md-3 h-100 border-end pe-3 d-flex flex-column">
        <h4 class="mb-3">Salas de Contacto</h4>
        <div class="list-group flex-grow-1 overflow-auto">
          <button class="list-group-item list-group-item-action d-flex align-items-center" 
                  :class="{ 'active': activeRoom === 'GLOBAL' }" 
                  @click="changeRoom('GLOBAL')">
            <i class="bi bi-globe me-2"></i> Sala Global
          </button>
          <button class="list-group-item list-group-item-action d-flex align-items-center" 
                  :class="{ 'active': activeRoom === 'VENTAS' }" 
                  @click="changeRoom('VENTAS')">
            <i class="bi bi-cart me-2"></i> Atención Ventas
          </button>
          <button class="list-group-item list-group-item-action d-flex align-items-center" 
                  :class="{ 'active': activeRoom === 'ALMACEN' }" 
                  @click="changeRoom('ALMACEN')">
            <i class="bi bi-box-seam me-2"></i> Atención Almacén
          </button>
          <button class="list-group-item list-group-item-action d-flex align-items-center" 
                  :class="{ 'active': activeRoom === 'RRHH' }" 
                  @click="changeRoom('RRHH')">
            <i class="bi bi-people me-2"></i> Atención RR.HH.
          </button>
        </div>
      </div>

      <!-- Área de Mensajes de la Sala -->
      <div class="col-md-9 h-100">
        <div class="card shadow-sm h-100 d-flex flex-column">
          <div class="card-header bg-primary text-white d-flex align-items-center justify-content-between">
            <span><i class="bi bi-chat-dots-fill me-2"></i> Sala Activa: <strong>{{ getRoomName(activeRoom) }}</strong></span>
            <span class="badge bg-light text-primary">{{ chatMessages.length }} mensajes</span>
          </div>
          <div class="card-body overflow-auto d-flex flex-column gap-2 bg-light" ref="chatMessagesContainer">
            <div v-if="chatMessages.length === 0" class="text-center text-muted mt-4">No hay mensajes recientes en esta sala.</div>
            
            <div v-for="msg in chatMessages" :key="msg.id" 
                 class="d-flex flex-column" 
                 :class="msg.sender === username ? 'align-items-end' : 'align-items-start'">
              <span class="small text-muted mb-1">{{ msg.sender }} - {{ new Date(msg.timestamp).toLocaleTimeString() }}</span>
              <div class="p-2 rounded shadow-sm" 
                   :class="msg.sender === username ? 'bg-primary text-white' : 'bg-white text-dark'" 
                   style="max-width: 70%;">
                {{ msg.content }}
              </div>
            </div>
          </div>
          <div class="card-footer bg-white">
            <form @submit.prevent="sendChatMessage" class="d-flex gap-2">
              <input type="text" v-model="newChatMsg" class="form-control" :placeholder="`Escribe a la sala de ${getRoomName(activeRoom)}...`" required>
              <button type="submit" class="btn btn-primary" :disabled="!newChatMsg.trim()">
                <i class="bi bi-send"></i>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Nuevo Ticket -->
    <div v-if="showNewTicketModal" class="modal-backdrop">
      <div class="panel modal-content">
        <h2>Crear Nuevo Ticket</h2>
        <form @submit.prevent="createTicket" class="form-stack">
          <div>
            <label>Asunto</label>
            <input v-model="newTicket.title" type="text" class="form-control" required placeholder="Ej: Falla en equipo">
          </div>
          <div>
            <label>Descripción detallada</label>
            <textarea v-model="newTicket.description" class="form-control" rows="3" required></textarea>
          </div>
          <div>
            <label>Importancia</label>
            <select v-model="newTicket.priority" class="form-select" required>
              <option value="BAJO">Bajo</option>
              <option value="MEDIO">Medio</option>
              <option value="ALTO">Alto</option>
            </select>
          </div>
          <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-success w-50">Enviar Petición</button>
            <button type="button" class="btn btn-outline-secondary w-50" @click="showNewTicketModal = false">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ticketService } from '../services/ticketService'
import { chatService } from '../services/chatService'

const username = ref(sessionStorage.getItem('username') || 'Anon')
const role = ref(sessionStorage.getItem('role') || 'USER')
const isAdmin = computed(() => role.value === 'ADMIN')

const activeTab = ref('tickets')

// Ticket State
const tickets = ref([])
const selectedTicket = ref(null)
const ticketMessages = ref([])
const showNewTicketModal = ref(false)
const newTicket = ref({ title: '', description: '', priority: 'MEDIO' })
const newTicketMsg = ref('')

const hasActiveTicket = computed(() => {
  return tickets.value.some(t => t.createdBy === username.value && t.status !== 'FINALIZADO')
})

// Chat State
const chatMessages = ref([])
const newChatMsg = ref('')
const activeRoom = ref('GLOBAL')

const getAreaFromRole = (roleStr) => {
  if (!roleStr) return 'General';
  if (roleStr === 'ADMIN') return 'Admin';
  if (roleStr.includes('SALES') || roleStr.includes('VENTAS')) return 'Ventas';
  if (roleStr.includes('WAREHOUSE') || roleStr.includes('ALMACEN')) return 'Almacén';
  if (roleStr.includes('HR') || roleStr.includes('RRHH')) return 'RR.HH.';
  return 'General';
}

const changeRoom = async (room) => {
  activeRoom.value = room
  chatMessages.value = []
  await loadChatMessages()
  scrollToBottom(chatMessagesContainer)
}

const getRoomName = (room) => {
  const map = {
    'GLOBAL': 'Sala Global',
    'VENTAS': 'Atención Ventas',
    'ALMACEN': 'Atención Almacén',
    'RRHH': 'Atención RR.HH.'
  }
  return map[room] || room
}

// DOM Refs for scrolling
const chatMessagesContainer = ref(null)
const ticketMessagesContainer = ref(null)

let pollInterval = null

const loadTickets = async () => {
  try {
    let data;
    if (isAdmin.value) {
      data = await ticketService.getAllTickets()
    } else {
      data = await ticketService.getUserTickets(username.value)
    }
    // Only update if changed to avoid losing selection state completely, 
    // but Vue's reactivity handles list replacement fine.
    tickets.value = data
    
    // If we have a selected ticket, refresh its status from the list
    if (selectedTicket.value) {
      const updated = data.find(t => t.id === selectedTicket.value.id)
      if (updated) {
        selectedTicket.value.status = updated.status
      }
    }
  } catch (err) {
    console.error("Error loading tickets", err)
  }
}

const loadTicketMessages = async (ticketId) => {
  try {
    const data = await ticketService.getMessages(ticketId)
    const currentLen = ticketMessages.value.length
    ticketMessages.value = data
    if (data.length > currentLen) {
      scrollToBottom(ticketMessagesContainer)
    }
  } catch (err) {
    console.error("Error loading ticket messages", err)
  }
}

const selectTicket = async (t) => {
  selectedTicket.value = t
  await loadTicketMessages(t.id)
  scrollToBottom(ticketMessagesContainer)
}

const createTicket = async () => {
  try {
    const payload = { ...newTicket.value, createdBy: username.value }
    await ticketService.createTicket(payload)
    showNewTicketModal.value = false
    newTicket.value = { title: '', description: '', priority: 'MEDIO' }
    await loadTickets()
  } catch (err) {
    alert("No se pudo crear el ticket: " + (err.response?.data || err.message))
  }
}

const updateTicketStatus = async (status) => {
  if (!selectedTicket.value) return
  try {
    const updated = await ticketService.updateTicketStatus(selectedTicket.value.id, status)
    selectedTicket.value.status = updated.status
    await loadTickets()
  } catch (err) {
    alert("Error actualizando ticket")
  }
}

const sendTicketMessage = async () => {
  if (!newTicketMsg.value.trim() || !selectedTicket.value) return
  try {
    await ticketService.addMessage(selectedTicket.value.id, {
      sender: username.value,
      content: newTicketMsg.value
    })
    newTicketMsg.value = ''
    await loadTicketMessages(selectedTicket.value.id)
  } catch (err) {
    alert("Error enviando mensaje")
  }
}

const loadChatMessages = async () => {
  try {
    const data = await chatService.getRecentMessages(activeRoom.value)
    const currentLen = chatMessages.value.length
    chatMessages.value = data
    // Auto scroll down if new messages arrived
    if (data.length > currentLen) {
      scrollToBottom(chatMessagesContainer)
    }
  } catch (err) {
    console.error("Error loading chat", err)
  }
}

const sendChatMessage = async () => {
  if (!newChatMsg.value.trim()) return
  try {
    const areaSuffix = getAreaFromRole(role.value);
    await chatService.sendMessage({
      sender: `${username.value} (${areaSuffix})`,
      room: activeRoom.value,
      content: newChatMsg.value
    })
    newChatMsg.value = ''
    await loadChatMessages()
    scrollToBottom(chatMessagesContainer)
  } catch (err) {
    alert("Error enviando mensaje")
  }
}

const scrollToBottom = (containerRef) => {
  nextTick(() => {
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  })
}

const getPriorityClass = (priority) => {
  if (priority === 'ALTO') return 'bg-danger'
  if (priority === 'MEDIO') return 'bg-warning text-dark'
  return 'bg-info text-dark'
}

const getStatusClass = (status) => {
  if (status === 'FINALIZADO') return 'bg-secondary'
  if (status === 'EN_PROCESO') return 'bg-primary'
  return 'bg-success'
}

const getStatusLabel = (status) => {
  if (status === 'FINALIZADO') return 'Finalizado'
  if (status === 'EN_PROCESO') return 'En Proceso'
  return 'Recibido'
}

onMounted(() => {
  loadTickets()
  loadChatMessages()
  
  // Real-time polling
  pollInterval = setInterval(() => {
    loadTickets()
    if (activeTab.value === 'chat') {
      loadChatMessages()
    } else if (selectedTicket.value) {
      loadTicketMessages(selectedTicket.value.id)
    }
  }, 3000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

</script>

<style scoped>
.support-view {
  height: calc(100vh - 140px); /* Adjust based on your header/footer heights */
  padding-bottom: 1rem;
}
.cursor-pointer {
  cursor: pointer;
}
.cursor-pointer:hover {
  background-color: #f8f9fa;
}
.modal-backdrop {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  width: 100%;
  max-width: 500px;
}
</style>
