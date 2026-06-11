
// We use the same configuration pattern, but a different base URL for tickets
import axios from 'axios'

const ticketClient = axios.create({
  baseURL: '/tickets',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const ticketService = {
  createTicket: (ticket) => ticketClient.post('', ticket).then(res => res.data),
  getAllTickets: () => ticketClient.get('').then(res => res.data),
  getUserTickets: (username) => ticketClient.get(`/user/${username}`).then(res => res.data),
  getTicket: (id) => ticketClient.get(`/${id}`).then(res => res.data),
  updateTicketStatus: (id, status) => ticketClient.put(`/${id}/status`, null, { params: { status } }).then(res => res.data),
  addMessage: (id, message) => ticketClient.post(`/${id}/messages`, message).then(res => res.data),
  getMessages: (id) => ticketClient.get(`/${id}/messages`).then(res => res.data)
}
