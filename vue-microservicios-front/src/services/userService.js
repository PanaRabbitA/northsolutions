import { userApi } from './api'

export const userService = {
  obtenerUsuario: (id) => userApi.get(`/${id}`).then(res => res.data),
  obtenerUsuarioPorUsername: (username) => userApi.get(`/username/${username}`).then(res => res.data),
  createUser: (user) => userApi.post('', user).then(res => res.data),
  updateUser: (username, user) => userApi.put(`/${username}`, user).then(res => res.data),
  getUsers: () => userApi.get('').then(res => res.data)
}

export default userService
