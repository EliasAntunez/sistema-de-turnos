<template>
  <ClienteNavbar v-if="showClientNavbar" />
  <UnifiedNavbar v-if="!showClientNavbar && !isUserView" />
  <router-view />
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import UnifiedNavbar from '@/components/UnifiedNavbar.vue'
import ClienteNavbar from '@/components/ClienteNavbar.vue'
// No reconstruir `cliente` aquí: las vistas públicas se encargarán de asociar
// la sesión del cliente según el `empresaSlug` en contexto.
onMounted(() => {})

const route = useRoute()
const showClientNavbar = computed(() => !!route.meta?.clientView)
const isUserView = computed(() => ['Admin', 'Dueno', 'Profesional', 'Login'].includes(route.name as string))
</script>
