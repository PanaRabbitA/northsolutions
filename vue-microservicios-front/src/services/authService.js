import { authApi } from './api'

export const authService = {
  login: (username, password) => authApi.post('/login', { username, password }),
  verifyPassword: (username, password) => authApi.post('/verify-password', { username, password }),
  validarToken: (token) => authApi.post('/validate', { token }),
  logout: (token) => authApi.post('/logout', { token }),
  obtenerUsuariosConectados: () => authApi.get('/active-sessions'),
  forzarCierreSesion: (username) => authApi.delete(`/active-sessions/${username}`)
}
