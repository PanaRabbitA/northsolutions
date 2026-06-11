import { workflowApi } from './api'

export const workflowService = {
  aprobarDocumento: (documentId, token) => workflowApi.post(`/aprobar-documento/${documentId}?token=${token}`),
  solicitarAprobacion: (documentId, modulo, token) => workflowApi.post(`/solicitar?documentId=${documentId}&modulo=${modulo}&token=${token}`),
  getPendientes: (token) => workflowApi.get(`/pendientes?token=${token}&_t=${Date.now()}`),
  getPorModulo: (modulo, token) => workflowApi.get(`/modulo/${modulo}?token=${token}&_t=${Date.now()}`),
  rechazarDocumento: (documentId, token) => workflowApi.post(`/rechazar-documento/${documentId}?token=${token}`)
}
