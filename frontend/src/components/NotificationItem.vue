<template>
  <div 
    class="notification-item flex items-start gap-3 p-3 transition hover:bg-slate-50" 
    :class="{ 'unread': !notificacion.leida }"
    @click="$emit('click')">
    
    <div :class="['flex h-10 w-10 shrink-0 items-center justify-center rounded-full', iconoData.bgClass, iconoData.textClass]">
      <span class="inline-flex h-6 w-6 items-center justify-center" aria-hidden="true">
        <svg v-if="iconoData.tipo === 'NUEVO'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25M3 18.75A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75M3 18.75v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25" />
          <path stroke-linecap="round" stroke-linejoin="round" d="m9 12.75 2.25 2.25L15 11.25" />
        </svg>
        <svg v-else-if="iconoData.tipo === 'CANCELADO'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 9.75 14.25 14.25M14.25 9.75l-4.5 4.5" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
        </svg>
        <svg v-else-if="iconoData.tipo === 'REPROGRAMADO'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M16.023 9.348h4.992V4.356m-1.366 14.288A9 9 0 0 1 5.106 5.106m2.245 13.539A8.966 8.966 0 0 1 3 12c0-1.589.413-3.082 1.137-4.377" />
        </svg>
        <svg v-else-if="iconoData.tipo === 'PAGO'" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 8.25h19.5m-18 7.5h4.5m-4.5 3h3m-1.5-15h15a1.5 1.5 0 0 1 1.5 1.5v13.5a1.5 1.5 0 0 1-1.5 1.5h-15a1.5 1.5 0 0 1-1.5-1.5V5.25a1.5 1.5 0 0 1 1.5-1.5z" />
        </svg>
        <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75v-.7V9a6 6 0 1 0-12 0v.05c0 .232 0 .467-.002.7a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.08 5.454 1.31m5.717 0a24.255 24.255 0 0 1-5.717 0m5.717 0a3 3 0 1 1-5.717 0" />
        </svg>
      </span>
    </div>

    <!-- Contenido -->
    <div class="notification-content flex-1 min-w-0">
      <p class="text-sm font-semibold text-slate-900">{{ notificacion.titulo }}</p>
      <p class="mt-0.5 text-xs text-slate-500 line-clamp-2">{{ notificacion.mensaje }}</p>
      
      <!-- Timestamp -->
      <p class="mt-1 text-[10px] font-medium text-slate-400">{{ formatearTiempo(notificacion.fechaCreacion) }}</p>
    </div>

    <!-- Indicador de no leída -->
    <div v-if="!notificacion.leida" class="unread-indicator"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Notificacion } from '../services/notificaciones'

interface Props {
  notificacion: Notificacion
}

const props = defineProps<Props>()

defineEmits<{
  (e: 'click'): void
}>()

type TipoIcono = 'NUEVO' | 'CANCELADO' | 'REPROGRAMADO' | 'PAGO' | 'DEFAULT'

interface IconoData {
  tipo: TipoIcono
  textClass: string
  bgClass: string
}

function getIconoData(titulo: string): IconoData {
  const tituloNormalizado = (titulo || '').toLowerCase()

  if (tituloNormalizado.includes('nuevo') || tituloNormalizado.includes('reserva')) {
    return {
      tipo: 'NUEVO',
      textClass: 'text-emerald-600',
      bgClass: 'bg-emerald-100'
    }
  }

  if (tituloNormalizado.includes('cancelado') || tituloNormalizado.includes('rechazado') || tituloNormalizado.includes('inasistencia')) {
    return {
      tipo: 'CANCELADO',
      textClass: 'text-rose-600',
      bgClass: 'bg-rose-100'
    }
  }

  if (tituloNormalizado.includes('reprogramado') || tituloNormalizado.includes('cambio')) {
    return {
      tipo: 'REPROGRAMADO',
      textClass: 'text-sky-600',
      bgClass: 'bg-sky-100'
    }
  }

  if (tituloNormalizado.includes('pago') || tituloNormalizado.includes('seña') || tituloNormalizado.includes('sena')) {
    return {
      tipo: 'PAGO',
      textClass: 'text-amber-600',
      bgClass: 'bg-amber-100'
    }
  }

  return {
    tipo: 'DEFAULT',
    textClass: 'text-slate-600',
    bgClass: 'bg-slate-100'
  }
}

const iconoData = computed(() => getIconoData(props.notificacion.titulo || ''))

/**
 * Tiempo relativo desde la creación
 */
function formatearTiempo(fechaCreacion: string): string {
  const ahora = new Date()
  const fecha = new Date(fechaCreacion)
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
}

</script>

<style scoped>
.notification-item {
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
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
</style>
