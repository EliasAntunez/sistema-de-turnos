<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="emitCancel"
  >
    <div class="bg-white rounded-xl shadow-lg w-full max-w-md" @click.stop>
      <div class="flex items-start justify-between px-6 pt-6 pb-2">
        <h3 class="text-lg font-semibold text-gray-900">{{ titulo }}</h3>
        <button
          type="button"
          class="text-gray-400 hover:text-gray-600 text-2xl leading-none"
          @click="emitCancel"
          aria-label="Cerrar"
        >
          &times;
        </button>
      </div>

      <div class="px-6 pb-2">
        <p class="text-sm text-gray-700 whitespace-pre-line">{{ mensaje }}</p>
        <div v-if="$slots.body" class="mt-3">
          <slot name="body" />
        </div>
      </div>

      <div class="px-6 py-4 flex justify-end gap-2">
        <button
          type="button"
          class="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors"
          @click="emitCancel"
        >
          {{ textoCancelar }}
        </button>
        <button
          type="button"
          :class="['px-4 py-2 text-white rounded-md transition-colors', colorBoton]"
          @click="emitConfirm"
        >
          {{ textoConfirmar }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface ConfirmModalProps {
  show: boolean
  titulo: string
  mensaje: string
  textoConfirmar?: string
  textoCancelar?: string
  colorBoton?: string
}

withDefaults(defineProps<ConfirmModalProps>(), {
  textoConfirmar: 'Confirmar',
  textoCancelar: 'Cancelar',
  colorBoton: 'bg-red-600 hover:bg-red-700'
})

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

function emitConfirm() {
  emit('confirm')
}

function emitCancel() {
  emit('cancel')
}
</script>