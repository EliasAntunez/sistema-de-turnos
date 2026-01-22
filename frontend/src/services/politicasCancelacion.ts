
import api from './api';
import type { PoliticaCancelacionRequest } from '@/types/politicasCancelacion';

const PoliticasService = {
  // Nuevos métodos backend-driven: no requieren empresaId
  getActivas() {
    return api.get('/politicas-cancelacion/activas');
  },
  getTodas() {
    return api.get('/politicas-cancelacion');
  },
  crear(politica: PoliticaCancelacionRequest) {
    return api.post('/politicas-cancelacion', politica);
  },
  desactivar(id: number) {
    return api.put(`/politicas-cancelacion/${id}/desactivar`);
  },
  activar(id: number) {
    return api.put(`/politicas-cancelacion/${id}/activar`);
  },
  eliminar(id: number) {
    return api.delete(`/politicas-cancelacion/${id}`);
  },
  actualizar(id: number, politica: PoliticaCancelacionRequest) {
  return api.put(`/politicas-cancelacion/${id}`, politica);
  },
    // Consulta pública: obtener políticas activas por empresaId
  getActivasPorEmpresa(empresaId: number) {
    return api.get(`/politicas-cancelacion/empresa/${empresaId}/activas`);
  }
};

export default PoliticasService;
