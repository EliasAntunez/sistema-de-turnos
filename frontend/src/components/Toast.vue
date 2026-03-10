<template>
  <div class="fixed top-4 right-4 z-50 space-y-2">
    <transition-group name="toast" tag="div">
      <div
        v-for="t in toasts"
        :key="t.id"
        :class="[
          'max-w-sm w-full px-4 py-3 rounded shadow-lg',
          t.type === 'success'
            ? 'bg-green-600 text-white'
            : t.type === 'error'
              ? 'bg-red-600 text-white'
              : 'bg-gray-800 text-white'
        ]"
      >
        <div class="flex items-start gap-2">
          <div class="flex-1 text-sm">
            <p class="font-medium leading-snug">{{ t.message }}</p>
            <ul v-if="t.detalles && t.detalles.length" class="mt-2 pl-4 list-disc space-y-1 text-sm opacity-90">
              <li v-for="(detalle, i) in t.detalles" :key="i">{{ detalle }}</li>
            </ul>
          </div>
          <button
            @click="toastStore.dismiss(t.id)"
            class="shrink-0 text-white/80 hover:text-white text-base leading-none mt-0.5"
            aria-label="Cerrar notificación"
          >✕</button>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { useToastStore } from '@/composables/useToast'
const toastStore = useToastStore()
const { toasts } = toastStore
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all .18s ease }
.toast-enter-from { opacity: 0; transform: translateY(-6px) }
.toast-enter-to { opacity: 1; transform: translateY(0) }
.toast-leave-from { opacity: 1 }
.toast-leave-to { opacity: 0; transform: translateY(-6px) }
</style>
