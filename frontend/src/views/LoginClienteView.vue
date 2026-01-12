<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Iniciar Sesión
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Accede a tu historial de turnos
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form @submit.prevent="login" class="space-y-6">
          <!-- Teléfono -->
          <div>
            <label for="telefono" class="block text-sm font-medium text-gray-700">
              Teléfono
            </label>
            <input 
              id="telefono" 
              v-model="formData.telefono" 
              type="text" 
              required
              class="mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="+54 9 11 1234-5678"
            />
          </div>

          <!-- Contraseña -->
          <div>
            <label for="contrasena" class="block text-sm font-medium text-gray-700">
              Contraseña
            </label>
            <input 
              id="contrasena" 
              v-model="formData.contrasena" 
              type="password" 
              required
              class="mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="Tu contraseña"
            />
          </div>

          <!-- Error Message -->
          <div v-if="errorMessage" class="rounded-md bg-red-50 p-4">
            <div class="flex">
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">
                  {{ errorMessage }}
                </h3>
              </div>
            </div>
          </div>

          <!-- Submit Button -->
          <div>
            <button 
              type="submit" 
              :disabled="loading"
              class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              <span v-if="loading">Iniciando sesión...</span>
              <span v-else>Iniciar Sesión</span>
            </button>
          </div>

          <!-- Link a Registro -->
          <div class="text-sm text-center">
            <router-link :to="`/empresa/${empresaSlug}/registro-cliente`" class="font-medium text-blue-600 hover:text-blue-500">
              ¿No tienes cuenta? Regístrate
            </router-link>
          </div>

          <!-- Divider -->
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">O</span>
            </div>
          </div>

          <!-- Link a Reservar (sin cuenta) -->
          <div class="text-sm text-center">
            <router-link :to="`/empresa/${empresaSlug}`" class="font-medium text-gray-600 hover:text-gray-500">
              Reservar sin crear cuenta
            </router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { useClienteStore } from '@/stores/cliente'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

const empresaSlug = ref(route.params.empresaSlug as string)

const formData = ref({
  telefono: '',
  contrasena: ''
})

const loading = ref(false)
const errorMessage = ref('')

async function login() {
  errorMessage.value = ''
  loading.value = true

  try {
    const response = await api.loginCliente(empresaSlug.value, formData.value)
    
    if (response.data.exito) {
      // Guardar datos del cliente en el store
      clienteStore.setCliente(response.data.datos)
      
      // Redirigir a mis turnos
      router.push(`/empresa/${empresaSlug.value}/mis-turnos`)
    } else {
      errorMessage.value = response.data.mensaje || 'Error al iniciar sesión'
    }
  } catch (error: any) {
    console.error('Error en login:', error)
    
    if (error.response?.data?.mensaje) {
      errorMessage.value = error.response.data.mensaje
    } else if (error.response?.status === 401) {
      errorMessage.value = 'Teléfono o contraseña incorrectos'
    } else if (error.response?.status === 404) {
      errorMessage.value = 'No existe una cuenta con este teléfono. Debes registrarte primero.'
    } else {
      errorMessage.value = 'Error al iniciar sesión. Intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}
</script>
