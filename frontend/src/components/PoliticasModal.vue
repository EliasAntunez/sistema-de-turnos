<template>
  <div class="politicas-modal-overlay" @click.self="cerrar">
    <div class="politicas-modal">
      <h2>Políticas de Cancelación e Inasistencias</h2>
      <div v-if="politicasFiltradas.length === 0" class="empty">No hay políticas activas configuradas.</div>
      <div v-else>
        <div v-for="(p, idx) in politicasFiltradas" :key="p.id" class="politica-item">
          <h3 v-if="politicasFiltradas.length > 1">{{ tipoLabel(p.tipo) }}</h3>
          <div class="descripcion" v-html="formatearDescripcion(p.descripcion)"></div>
        </div>
      </div>
      <button class="btn-cerrar" @click="cerrar">Cerrar</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ politicas: any[] }>()
const emit = defineEmits(['close'])

// Mostrar solo políticas activas. Si hay una de tipo AMBOS activa, mostrar solo esa.
const politicasFiltradas = computed(() => {
  if (!props.politicas) return []
  const activas = props.politicas.filter(p => p.activa)
  const ambos = activas.find(p => p.tipo === 'AMBOS')
  if (ambos) return [ambos]
  return activas
})

function cerrar() {
  emit('close')
}
function tipoLabel(tipo: string) {
  if (tipo === 'CANCELACION') return 'Política de Cancelación'
  if (tipo === 'INASISTENCIA') return 'Política de Inasistencias'
  return 'Política de Cancelación e Inasistencias'
}
function formatearDescripcion(desc: string) {
  // Puedes mejorar esto para soportar saltos de línea, links, etc.
  return desc.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.politicas-modal-overlay {
  position: fixed; z-index: 1000; left: 0; top: 0; width: 100vw; height: 100vh;
  background: rgba(30,40,80,0.25); display: flex; align-items: center; justify-content: center;
}
.politicas-modal {
  background: white; border-radius: 1rem; max-width: 480px; width: 95vw; padding: 2rem 1.5rem;
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
}
.politicas-modal h2 {
  color: #2563eb; font-size: 1.3rem; margin-bottom: 1.2rem; text-align: center;
}
.politica-item { margin-bottom: 1.5rem; }
.politica-item h3 { color: #2563eb; font-size: 1.1rem; margin-bottom: 0.5rem; }
.descripcion { color: #222; font-size: 1rem; line-height: 1.6; background: #f8fafc; border-radius: 0.5rem; padding: 1rem; }
.btn-cerrar {
  display: block; margin: 1.5rem auto 0 auto; background: #2563eb; color: white; border: none; border-radius: 0.5rem; padding: 0.6rem 2.5rem; font-size: 1rem; cursor: pointer;
  transition: background 0.2s;
}
.btn-cerrar:hover { background: #1d4ed8; }
.empty { color: #888; text-align: center; margin: 2rem 0; }
</style>
