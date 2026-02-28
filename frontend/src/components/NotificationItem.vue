<template>
  <div 
    class="notification-item" 
    :class="{ 
      'unread': !notificacion.leida,
      [`tipo-${notificacion.tipo.toLowerCase()}`]: true
    }"
    @click="$emit('click')">
    
    <!-- Icono según el tipo -->
    <div class="notification-icon" :class="`icon-${notificacion.tipo.toLowerCase()}`">
      {{ iconoPorTipo }}
    </div>

    <!-- Contenido -->
    <div class="notification-content">
      <h4 class="notification-title">{{ notificacion.titulo }}</h4>
      <p class="notification-message">{{ notificacion.mensaje }}</p>
      
      <!-- Información adicional del JSON -->
      <div v-if="infoAdicional" class="notification-details">
        <span v-if="infoAdicional.nombreCliente" class="detail-item">
          👤 {{ infoAdicional.nombreCliente }}
        </span>
        <span v-if="infoAdicional.servicio" class="detail-item">
          💼 {{ infoAdicional.servicio }}
        </span>
        <span v-if="infoAdicional.fecha" class="detail-item">
          📅 {{ formatearFecha(infoAdicional.fecha) }}
        </span>
        <span v-if="infoAdicional.hora" class="detail-item">
          🕐 {{ infoAdicional.hora }}
        </span>
      </div>

      <!-- Timestamp -->
      <span class="notification-time">{{ tiempoRelativo }}</span>
    </div>

    <!-- Indicador de no leída -->
    <div v-if="!notificacion.leida" class="unread-indicator"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Notificacion } from '../services/notificaciones'

interface Props {
  notificacion: Notificacion
}

const props = defineProps<Props>()

defineEmits<{
  (e: 'click'): void
}>()

/**
 * Información adicional parseada del JSON.
 * Se parsea solo cuando cambia el contenidoJson (optimización M2)
 */
const infoAdicional = ref<Record<string, any> | null>(null)

watch(
  () => props.notificacion.contenidoJson,
  (contenidoJson) => {
    if (!contenidoJson) {
      infoAdicional.value = null
      return
    }
    
    try {
      infoAdicional.value = JSON.parse(contenidoJson)
    } catch {
      infoAdicional.value = null
    }
  },
  { immediate: true }
)

/**
 * Icono según el tipo de notificación
 */
const iconoPorTipo = computed(() => {
  const iconos: Record<string, string> = {
    'NUEVO_TURNO': '📅',
    'CANCELACION_CLIENTE': '❌',
    'CANCELACION_EMPRESA': '🚫',
    'REPROGRAMACION_CLIENTE': '🔄',
    'REPROGRAMACION_EMPRESA': '⏰',
    'RECORDATORIO_TURNO': '⏰',
    'TURNO_PASADO_INASISTENCIA': '⚠️',
    'BLOQUEO_HORARIO': '🔒'
  }
  
  return iconos[props.notificacion.tipo] || '📢'
})

/**
 * Tiempo relativo desde la creación
 */
const tiempoRelativo = computed(() => {
  const ahora = new Date()
  const fecha = new Date(props.notificacion.fechaCreacion)
  const diffMs = ahora.getTime() - fecha.getTime()
  
  const minutos = Math.floor(diffMs / 60000)
  const horas = Math.floor(minutos / 60)
  const dias = Math.floor(horas / 24)
  
  if (minutos < 1) return 'Ahora'
  if (minutos < 60) return `Hace ${minutos} min`
  if (horas < 24) return `Hace ${horas}h`
  if (dias === 1) return 'Ayer'
  if (dias < 7) return `Hace ${dias} días`
  
  return fecha.toLocaleDateString('es-AR', { 
    day: '2-digit', 
    month: 'short' 
  })
})

/**
 * Formatear fecha legible
 */
function formatearFecha(fechaStr: string): string {
  try {
    const fecha = new Date(fechaStr)
    return fecha.toLocaleDateString('es-AR', {
      weekday: 'short',
      day: '2-digit',
      month: 'short'
    })
  } catch {
    return fechaStr
  }
}
</script>

<style scoped>
.notification-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  position: relative;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: #eff6ff;
}

.notification-item.unread:hover {
  background: #dbeafe;
}

.notification-icon {
  flex-shrink: 0;
  width: 2.5rem;
  height: 2.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  border-radius: 50%;
  background: #f3f4f6;
}

/* Colores por tipo */
.icon-nuevo_turno {
  background: #dbeafe;
}

.icon-cancelacion_cliente,
.icon-cancelacion_empresa {
  background: #fee2e2;
}

.icon-reprogramacion_cliente,
.icon-reprogramacion_empresa {
  background: #fef3c7;
}

.icon-recordatorio_turno {
  background: #e0e7ff;
}

.icon-turno_pasado_inasistencia {
  background: #fed7aa;
}

.icon-bloqueo_horario {
  background: #f3e8ff;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  margin: 0 0 0.25rem 0;
  font-size: 0.875rem;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.3;
}

.notification-message {
  margin: 0 0 0.5rem 0;
  font-size: 0.8125rem;
  color: #4b5563;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-details {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.detail-item {
  font-size: 0.75rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.125rem 0.5rem;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.notification-time {
  font-size: 0.75rem;
  color: #9ca3af;
  font-weight: 500;
}

.unread-indicator {
  position: absolute;
  left: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 0.5rem;
  height: 0.5rem;
  background: #3b82f6;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

/* Borde izquierdo coloreado según tipo */
.tipo-nuevo_turno {
  border-left: 3px solid #3b82f6;
}

.tipo-cancelacion_cliente,
.tipo-cancelacion_empresa {
  border-left: 3px solid #ef4444;
}

.tipo-reprogramacion_cliente,
.tipo-reprogramacion_empresa {
  border-left: 3px solid #f59e0b;
}

.tipo-recordatorio_turno {
  border-left: 3px solid #6366f1;
}

.tipo-turno_pasado_inasistencia {
  border-left: 3px solid #f97316;
}

.tipo-bloqueo_horario {
  border-left: 3px solid #a855f7;
}
</style>
