<template>
  <footer class="bg-gray-50 border-t mt-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <h3 class="text-sm font-medium text-gray-900">Ubicación</h3>
          <p class="mt-1 text-sm text-gray-600">{{ direccion || 'Dirección no disponible' }}</p>
          <p class="text-sm text-gray-600">{{ ciudad || 'Ciudad no disponible' }}</p>
        </div>
        <div>
          <h3 class="text-sm font-medium text-gray-900">Contacto</h3>
          <p v-if="telefono" class="mt-1 text-sm text-gray-600">Tel: {{ telefono }}</p>
          <p v-if="email" class="text-sm text-gray-600">Email: {{ email }}</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
interface Props {
  direccion?: string | null
  ciudad?: string | null
  telefono?: string | null
  email?: string | null
}

defineProps<Props>()
</script>