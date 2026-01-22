<template>
  <div class="fixed top-4 right-4 z-50 space-y-2">
    <transition-group name="toast" tag="div">
      <div v-for="t in toasts" :key="t.id" class="max-w-sm w-full bg-gray-800 text-white px-4 py-2 rounded shadow">
        <div class="flex items-start gap-2">
          <div class="flex-1 text-sm">{{ t.message }}</div>
          <button @click="$emit('dismiss', t.id)" class="text-white/80 hover:text-white">✕</button>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useToastStore } from '@/composables/useToast'
const { toasts } = useToastStore()
</script>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all .18s ease }
.toast-enter-from { opacity: 0; transform: translateY(-6px) }
.toast-enter-to { opacity: 1; transform: translateY(0) }
.toast-leave-from { opacity: 1 }
.toast-leave-to { opacity: 0; transform: translateY(-6px) }
</style>
