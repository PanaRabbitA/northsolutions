import axios from 'axios'

const API_URL = import.meta.env.VITE_WAREHOUSE_API_URL || '/api/warehouse'

export default {
  getProducts() {
    return axios.get(`${API_URL}/products`).then(res => res.data)
  },
  
  createProduct: (product) => axios.post(`${API_URL}/products`, product).then(res => res.data),
  
  registerEntry: (productId, quantity, reason, createdBy) => 
    axios.post(`${API_URL}/inventory/in`, { productId, quantity, reason, createdBy }).then(res => res.data),
    
  registerExit: (productId, quantity, reason, createdBy) => 
    axios.post(`${API_URL}/inventory/out`, { productId, quantity, reason, createdBy }).then(res => res.data),
    
  getProductTransactions: (productId) => 
    axios.get(`${API_URL}/products/${productId}/transactions`).then(res => res.data),

  getAllTransactions: () => 
    axios.get(`${API_URL}/inventory/transactions`).then(res => res.data)
}
