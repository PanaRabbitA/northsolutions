import axios from 'axios'

const chatClient = axios.create({
  baseURL: '/chat',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const chatService = {
  getRecentMessages: (room = 'GLOBAL') => chatClient.get('', { params: { room } }).then(res => res.data),
  sendMessage: (message) => chatClient.post('', message).then(res => res.data)
}
