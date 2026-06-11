import axios from 'axios'

const API_URL = import.meta.env.VITE_HR_API_URL || '/api/hr'

export default {
  getEmployees() {
    return axios.get(`${API_URL}/employees`).then(res => res.data)
  },
  
  addEmployee(employee) {
    return axios.post(`${API_URL}/employees`, employee).then(res => res.data)
  },
  
  updateEmployee(id, employee) {
    return axios.put(`${API_URL}/employees/${id}`, employee).then(res => res.data)
  },
  
  changeStatus(id, status) {
    return axios.put(`${API_URL}/employees/${id}/status`, { status }).then(res => res.data)
  },
  
  registerAttendance(employeeId, status, registeredBy) {
    return axios.post(`${API_URL}/attendance`, { employeeId, status, registeredBy }).then(res => res.data)
  },
  
  getAttendance(employeeId) {
    return axios.get(`${API_URL}/employees/${employeeId}/attendance`).then(res => res.data)
  },
  
  getAllAttendance() {
    return axios.get(`${API_URL}/attendance`).then(res => res.data)
  },
  
  uploadDocument(employeeId, file, type) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)
    return axios.post(`${API_URL}/employees/${employeeId}/documents`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  getEmployeeDocuments(employeeId) {
    return axios.get(`${API_URL}/employees/${employeeId}/documents`).then(res => res.data)
  },
  
  getDocumentUrl(employeeId, type) {
    return `${API_URL}/employees/${employeeId}/documents/${type}`
  }
}
