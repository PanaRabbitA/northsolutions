<template>
  <section>
    <div class="page-header">
      <div>
        <span class="eyebrow">Validación</span>
        <h1>Gestión de Usuarios y Auth</h1>
        <p>Prueba los endpoints de ms-user y ms-auth.</p>
      </div>
    </div>

    <div class="grid-cards">
      <!-- Card Usuario -->
      <div class="panel">
        <h2>Consultar Usuario</h2>
        <form @submit.prevent="consultarUsuario" class="form-stack">
          <label>ID Usuario</label>
          <input v-model="idUsuario" type="number" class="form-control" placeholder="ID Usuario" required />
          <button type="submit" class="btn btn-primary mt-2">Buscar Usuario</button>
        </form>
        <div v-if="userResult" class="mt-3">
          <pre><code>{{ userResult }}</code></pre>
        </div>
        <div v-if="userError" class="alert alert-danger mt-3">{{ userError }}</div>
      </div>

      <!-- Card Auth -->
      <div class="panel">
        <h2>Validar Token</h2>
        <form @submit.prevent="validarToken" class="form-stack">
          <label>Token JWT</label>
          <div class="d-flex gap-2">
            <input v-model="token" type="text" class="form-control" placeholder="Token" required />
            <button type="button" class="btn btn-outline-secondary" @click="cargarMiToken" title="Autocompletar mi token actual">
              <i class="bi bi-magic"></i>
            </button>
          </div>
          <button type="submit" class="btn btn-primary mt-2">Validar</button>
        </form>
        <div v-if="authResult" class="mt-3">
          <pre><code>{{ authResult }}</code></pre>
        </div>
        <div v-if="authError" class="alert alert-danger mt-3">{{ authError }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { userService } from '../services/userService'
import { authService } from '../services/authService'
import { getErrorMessage } from '../services/api'

const idUsuario = ref('')
const userResult = ref(null)
const userError = ref('')

const token = ref('')
const authResult = ref(null)
const authError = ref('')

const cargarMiToken = () => {
  token.value = sessionStorage.getItem('token') || ''
}

const consultarUsuario = async () => {
  userError.value = ''
  userResult.value = null
  try {
    const res = await userService.obtenerUsuario(idUsuario.value)
    userResult.value = res.data
  } catch (err) {
    userError.value = getErrorMessage(err)
  }
}

const validarToken = async () => {
  authError.value = ''
  authResult.value = null
  try {
    const res = await authService.validarToken(token.value)
    authResult.value = res.data
  } catch (err) {
    authError.value = getErrorMessage(err)
  }
}
</script>
