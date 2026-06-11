import axios from 'axios'

const API_URL = import.meta.env.VITE_SALES_API_URL || '/api/sales'

export default {
  getSales() {
    return axios.get(API_URL).then(res => res.data)
  },
  
  createSale(saleRequest) {
    return axios.post(API_URL, saleRequest).then(res => res.data)
  }
}
