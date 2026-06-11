import axios from 'axios'

const workflowBaseURL = import.meta.env.VITE_WORKFLOW_API_URL || '/api/procesos'
const userBaseURL = import.meta.env.VITE_USER_API_URL || '/api/usuarios'
const authBaseURL = import.meta.env.VITE_AUTH_API_URL || '/api/auth'

const createClient = (baseURL) => axios.create({
  baseURL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const workflowApi = createClient(workflowBaseURL)
export const userApi = createClient(userBaseURL)
export const authApi = createClient(authBaseURL)

export const getErrorMessage = (error) => {
  if (error.response?.data?.error) return error.response.data.error
  if (error.response?.data?.message) return error.response.data.message
  if (typeof error.response?.data === 'string') return error.response.data
  if (error.code === 'ERR_NETWORK') return 'No se pudo conectar con el microservicio. Verifique que esté encendido y que CORS esté habilitado.'
  return error.message || 'Error inesperado en la solicitud.'
}
