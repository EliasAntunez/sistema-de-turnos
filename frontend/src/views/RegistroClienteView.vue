<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Crear Cuenta
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Registra tu cuenta para ver tu historial de turnos
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form @submit.prevent="registrar" class="space-y-6">
          <!-- Teléfono (editable si no viene pre-cargado) -->
          <div>
            <label for="telefono" class="block text-sm font-medium text-gray-700">
              Teléfono *
            </label>
            <input 
              id="telefono" 
              v-model="formData.telefono" 
              type="tel" 
              required
              :readonly="telefonoPrecargado"
              :class="[
                'mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm',
                telefonoPrecargado ? 'bg-gray-100' : 'bg-white placeholder-gray-400'
              ]"
              :placeholder="telefonoPrecargado ? '' : 'Ej: +54 9 11 1234-5678'"
            />
            <p v-if="telefonoPrecargado" class="mt-1 text-xs text-gray-500">
              Este es el teléfono con el que reservaste turnos anteriormente
            </p>
            <p v-else class="mt-1 text-xs text-gray-500">
              Usa el mismo teléfono que usaste en tus reservas anteriores (si las tienes)
            </p>
          </div>

          <!-- Email -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input 
              id="email" 
              v-model="formData.email" 
              type="email" 
              required
              class="mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="tu@email.com"
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
              placeholder="Mínimo 8 caracteres"
            />
            <p class="mt-1 text-xs text-gray-500">
              Debe contener mayúscula, minúscula y número
            </p>
          </div>

          <!-- Confirmar Contraseña -->
          <div>
            <label for="confirmarContrasena" class="block text-sm font-medium text-gray-700">
              Confirmar Contraseña
            </label>
            <input 
              id="confirmarContrasena" 
              v-model="formData.confirmarContrasena" 
              type="password" 
              required
              class="mt-1 appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="Repite tu contraseña"
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

          <!-- Success Message -->
          <div v-if="successMessage" class="rounded-md bg-green-50 p-4">
            <div class="flex">
              <div class="ml-3">
                <h3 class="text-sm font-medium text-green-800">
                  {{ successMessage }}
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
              <span v-if="loading">Registrando...</span>
              <span v-else>Crear Cuenta</span>
            </button>
          </div>

          <!-- Link a Login -->
          <div class="text-sm text-center">
            <router-link :to="`/empresa/${empresaSlug}/login-cliente`" class="font-medium text-blue-600 hover:text-blue-500">
              ¿Ya tienes cuenta? Inicia sesión
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
const telefono = ref(route.query.telefono as string || '')
const telefonoPrecargado = ref(!!route.query.telefono) // true si viene de una reserva

const formData = ref({
  telefono: telefono.value,
  email: '',
  contrasena: '',
  confirmarContrasena: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

async function registrar() {
  errorMessage.value = ''
  successMessage.value = ''

  // Validaciones básicas
  if (formData.value.contrasena !== formData.value.confirmarContrasena) {
    errorMessage.value = 'Las contraseñas no coinciden'
    return
  }

  if (formData.value.contrasena.length < 8) {
    errorMessage.value = 'La contraseña debe tener al menos 8 caracteres'
    return
  }

  loading.value = true

  try {
    const response = await api.registrarCliente(empresaSlug.value, formData.value)
    
    if (response.data.exito) {
      successMessage.value = 'Cuenta creada exitosamente. Iniciando sesión...'
      
      // Guardar datos del cliente en el store
      clienteStore.setCliente(response.data.datos)
      
      // Redirigir a mis turnos después de 1 segundo
      setTimeout(() => {
        router.push(`/empresa/${empresaSlug.value}/mis-turnos`)
      }, 1000)
    } else {
      errorMessage.value = response.data.mensaje || 'Error al registrar'
    }
  } catch (error: any) {
    console.error('Error en registro:', error)
    console.error('Response data:', error.response?.data)
    
    if (error.response?.data?.errores) {
      // Errores de validación como objeto
      const errores = error.response.data.errores
      const mensajes = Object.values(errores).flat() as string[]
      errorMessage.value = mensajes.join('. ')
    } else if (error.response?.data?.mensaje) {
      errorMessage.value = error.response.data.mensaje
    } else if (error.response?.status === 400) {
      errorMessage.value = 'Datos inválidos. Verifica que el email sea válido y la contraseña cumpla los requisitos.'
    } else if (error.response?.status === 409) {
      errorMessage.value = 'Ya existe una cuenta con este teléfono o email'
    } else {
      errorMessage.value = 'Error al crear la cuenta. Intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}
</script>
