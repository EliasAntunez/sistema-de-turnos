<template>
  <transition-group
    tag="div"
    name="toast"
    class="pointer-events-none fixed left-0 top-16 z-[9999] flex w-full flex-col items-center gap-2 px-4 sm:left-auto sm:right-4 sm:top-20 sm:w-[22rem] sm:items-end"
    aria-live="polite"
    aria-atomic="false"
  >
    <div
      v-for="t in toasts"
      :key="t.id"
      :class="[
        'pointer-events-auto flex w-full items-start gap-3 rounded-xl border border-slate-100 bg-white px-4 py-3 shadow-lg',
        accentClass(t.type)
      ]"
      role="alert"
    >
      <span
        :class="['mt-0.5 shrink-0', iconColor(t.type)]"
        v-html="iconSvg(t.type)"
        aria-hidden="true"
      ></span>

      <div class="min-w-0 flex-1">
        <p :class="['text-sm font-medium leading-snug', textColor(t.type)]">
          {{ t.message }}
        </p>
        <ul
          v-if="t.detalles && t.detalles.length"
          class="mt-1.5 list-disc space-y-0.5 pl-4 text-xs text-slate-500"
        >
          <li v-for="(d, i) in t.detalles" :key="i">{{ d }}</li>
        </ul>
      </div>

      <button
        @click="toastStore.dismiss(t.id)"
        class="mt-0.5 shrink-0 text-slate-400 transition-colors hover:text-slate-600"
        aria-label="Cerrar notificación"
        type="button"
      >
        <svg width="14" height="14" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
        </svg>
      </button>
    </div>
  </transition-group>
</template>

<script setup lang="ts">
import { useToastStore } from '@/composables/useToast'
import type { ToastType } from '@/composables/useToast'

const toastStore = useToastStore()
const { toasts } = toastStore

function accentClass(type: ToastType): string {
  switch (type) {
    case 'success': return 'border-l-4 border-l-emerald-500 bg-emerald-50/30'
    case 'error':   return 'border-l-4 border-l-rose-500 bg-rose-50/30'
    case 'warning': return 'border-l-4 border-l-amber-500 bg-amber-50/30'
    default:        return 'border-l-4 border-l-sky-500 bg-sky-50/30'
  }
}

function iconColor(type: ToastType): string {
  switch (type) {
    case 'success': return 'text-emerald-500'
    case 'error':   return 'text-rose-500'
    case 'warning': return 'text-amber-500'
    default:        return 'text-sky-500'
  }
}

function textColor(type: ToastType): string {
  switch (type) {
    case 'success': return 'text-emerald-900'
    case 'error':   return 'text-rose-900'
    case 'warning': return 'text-amber-900'
    default:        return 'text-sky-900'
  }
}

function iconSvg(type: ToastType): string {
  switch (type) {
    case 'success':
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>`
    case 'error':
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
    case 'warning':
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>`
    default:
      return `<svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/></svg>`
  }
}
</script>

<style scoped>
.toast-enter-active {
  transition: all 0.3s cubic-bezier(0.21, 1.02, 0.73, 1);
}
.toast-leave-active {
  transition: all 0.2s ease-in;
}
/* En celular entra desde arriba, en PC desde la derecha */
.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}
.toast-enter-to {
  opacity: 1;
  transform: translateY(0);
}
.toast-leave-from {
  opacity: 1;
  transform: translateY(0);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@media (min-width: 640px) {
  .toast-enter-from, .toast-leave-to {
    transform: translateX(20px);
  }
  .toast-enter-to, .toast-leave-from {
    transform: translateX(0);
  }
}
</style>