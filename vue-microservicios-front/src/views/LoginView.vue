<template>
  <div class="login-container">
    <div class="background-watermark"></div>
    <div class="login-card panel">
      <div class="text-center mb-4">
        <h2><i class="bi bi-shield-lock-fill text-primary"></i> Acceso al Sistema</h2>
        <p class="text-muted">Introduce tus credenciales para continuar</p>
      </div>

      <form @submit.prevent="handleLogin" class="form-stack">
        <div class="mb-3">
          <label class="form-label">Usuario</label>
          <input v-model="username" type="text" class="form-control" required />
        </div>
        <div class="mb-4">
          <label class="form-label">Contraseña</label>
          <input v-model="password" type="password" class="form-control" required />
        </div>

        <button type="submit" class="btn btn-primary w-100" :disabled="isLoading">
          <span v-if="isLoading">Cargando...</span>
          <span v-else>Iniciar Sesión</span>
        </button>

        <div v-if="error" class="alert alert-danger mt-3">{{ error }}</div>
        
        <div v-if="sessionConflict" class="mt-3">
          <button type="button" class="btn btn-warning w-100 fw-bold" @click="forceLogin" :disabled="isLoading">
            <i class="bi bi-box-arrow-in-right"></i> Cerrar la otra sesión y entrar aquí
          </button>
        </div>
      </form>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { authService } from '../services/authService'
import hrService from '../services/hrService'

const username = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)
const sessionConflict = ref(false)
const router = useRouter()

const AUTH_URL = import.meta.env.VITE_AUTH_API_URL || '/api/auth'

const handleLogin = async () => {
  error.value = ''
  sessionConflict.value = false
  isLoading.value = true
  try {
    const res = await axios.post(`${AUTH_URL}/login`, {
      username: username.value.trim(),
      password: password.value.trim()
    })
    
    // Store session info
    sessionStorage.setItem('token', res.data.token)
    sessionStorage.setItem('role', res.data.role)
    sessionStorage.setItem('username', res.data.username)
    
    // Verificar si está suspendido en RH
    if (res.data.role !== 'ADMIN') {
      try {
        const emps = await hrService.getEmployees()
        const myEmp = emps.find(e => e.username === res.data.username)
        if (myEmp && myEmp.status && myEmp.status !== 'ACTIVO') {
          // Destruir la sesión recién creada
          await authService.forzarCierreSesion(res.data.username)
          sessionStorage.clear()
          error.value = "Usuario suspendido temporalmente, comunicarse con rh."
          isLoading.value = false
          return
        }
      } catch (e) {
        console.warn('No se pudo verificar el estatus en RH', e)
      }
    }
    
    // Navigate based on role (using window.location.href to force full reload and update App.vue state)
    if (res.data.role === 'ADMIN') {
      window.location.href = '/'
    } else if (res.data.role === 'ROLE_SALES' || res.data.role === 'VENTAS') {
      window.location.href = '/sales'
    } else if (res.data.role === 'ROLE_WAREHOUSE' || res.data.role === 'ALMACEN') {
      window.location.href = '/warehouse'
    } else if (res.data.role === 'ROLE_HR' || res.data.role === 'RRHH') {
      window.location.href = '/hr'
    } else {
      window.location.href = '/'
    }
  } catch (err) {
    if (err.response) {
      const errMsg = err.response.data?.error || JSON.stringify(err.response.data)
      error.value = `Error del servidor (${err.response.status}): ${errMsg}`
      
      // Detectar conflicto de sesión activa
      if (err.response.status === 403 && errMsg.includes('activa')) {
        sessionConflict.value = true
      }
    } else if (err.request) {
      error.value = 'No se recibió respuesta del servidor (Error de red o CORS).'
    } else {
      error.value = `Error en la solicitud: ${err.message}`
    }
  } finally {
    isLoading.value = false
  }
}

const forceLogin = async () => {
  isLoading.value = true
  error.value = ''
  try {
    await authService.forzarCierreSesion(username.value.trim())
    // Intentar iniciar sesión de nuevo
    await handleLogin()
  } catch (err) {
    error.value = 'No se pudo forzar el cierre de la otra sesión.'
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: var(--surface-color);
  overflow: hidden;
}

.background-watermark {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60%;
  height: 60%;
  background-image: url('../assets/images/logo.png');
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
  opacity: 0.12;
  pointer-events: none;
  z-index: 0;
  transition: all 0.8s ease;
}

.login-container:hover .background-watermark {
  transform: translate(-50%, -50%) scale(1.05);
  opacity: 0.20;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  border: 1px solid var(--border-color);
}
</style>
