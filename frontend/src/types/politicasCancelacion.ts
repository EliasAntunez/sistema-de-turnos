// empresaId ya no debe enviarse nunca en requests, el backend lo infiere del usuario autenticado
export interface PoliticaCancelacionRequest {
  // empresaId?: number; // DEPRECATED: no enviar nunca
  tipo: 'CANCELACION' | 'INASISTENCIA' | 'AMBOS';
  descripcion: string;
  horasLimiteCancelacion: number;
  penalizacion: 'NINGUNA' | 'ADVERTENCIA' | 'BLOQUEO' | 'MULTA';
  mensajeCliente?: string;
  activa: boolean;
}

export interface PoliticaCancelacionResponse {
  id: number;
  empresaId?: number; // Solo para mostrar, nunca enviar
  tipo: 'CANCELACION' | 'INASISTENCIA' | 'AMBOS';
  descripcion: string;
  horasLimiteCancelacion: number;
  penalizacion: 'NINGUNA' | 'ADVERTENCIA' | 'BLOQUEO' | 'MULTA';
  mensajeCliente?: string;
  activa: boolean;
  fechaCreacion?: string;
  fechaModificacion?: string;
}

// Para compatibilidad y uso en listados, exportar el tipo base
export type PoliticaCancelacion = PoliticaCancelacionResponse;
