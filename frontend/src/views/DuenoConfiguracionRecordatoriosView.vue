<template>
  <div class="container mx-auto px-4 py-8 max-w-2xl">
    <div class="bg-white rounded-lg shadow-md p-6">
      <h1 class="text-3xl font-bold mb-6 text-gray-800">
        Configuración de Recordatorios
      </h1>

      <div v-if="mensaje" :class="[
        'p-4 mb-6 rounded-md',
        mensaje.tipo === 'exito' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
      ]">
        {{ mensaje.texto }}
      </div>

      <div v-if="cargando" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
        <p class="mt-4 text-gray-600">Cargando configuración...</p>
      </div>

      <form v-else @submit.prevent="guardarConfiguracion" class="space-y-6">
        <!-- Activar recordatorios -->
        <div class="border-b border-gray-200 pb-6">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Activar Recordatorios por WhatsApp
              </label>
              <p class="text-sm text-gray-500">
                Los clientes recibirán un mensaje de WhatsApp antes de su turno para confirmar asistencia
              </p>
            </div>
            <button
              type="button"
              @click="configuracion.activarRecordatorios = !configuracion.activarRecordatorios"
              :class="[
                'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ml-4',
                configuracion.activarRecordatorios ? 'bg-blue-600' : 'bg-gray-200'
              ]"
            >
              <span
                :class="[
                  'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
                  configuracion.activarRecordatorios ? 'translate-x-5' : 'translate-x-0'
                ]"
              />
            </button>
          </div>
        </div>

        <!-- Horas de anticipación -->
        <div>
          <label for="horasAnticipacion" class="block text-sm font-medium text-gray-700 mb-1">
            Horas de Anticipación
          </label>
          <p class="text-sm text-gray-500 mb-3">
            Con cuántas horas de anticipación se enviará el recordatorio antes del turno
          </p>
          <div class="relative">
            <input
              id="horasAnticipacion"
              v-model.number="configuracion.horasAnticipacion"
              type="number"
              min="1"
              max="168"
              required
              class="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm px-4 py-2"
              :disabled="!configuracion.activarRecordatorios"
            />
            <div class="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
              <span class="text-gray-500 sm:text-sm">horas</span>
            </div>
          </div>
          <p class="mt-2 text-xs text-gray-500">
            Valores sugeridos: 24 horas (1 día), 48 horas (2 días), 72 horas (3 días)
          </p>
        </div>

        <!-- Información adicional -->
        <div class="bg-blue-50 border border-blue-200 rounded-md p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3 flex-1">
              <h3 class="text-sm font-medium text-blue-800">
                ¿Cómo funcionan los recordatorios?
              </h3>
              <div class="mt-2 text-sm text-blue-700">
                <ul class="list-disc pl-5 space-y-1">
                  <li>El sistema envía automáticamente un mensaje de WhatsApp a cada cliente</li>
                  <li>El cliente puede responder "SI" para confirmar o "NO" para cancelar</li>
                  <li>Los recordatorios se envían diariamente a las 8:00 AM</li>
                  <li>Solo se envían a turnos en estado "Creado" (no confirmados previamente)</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Botones -->
        <div class="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            @click="cargarConfiguracion"
            class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            :disabled="guardando"
          >
            Cancelar
          </button>
          <button
            type="submit"
            class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="guardando"
          >
            <span v-if="guardando">Guardando...</span>
            <span v-else>Guardar Cambios</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/services/api';

interface ConfiguracionRecordatorios {
  activarRecordatorios: boolean;
  horasAnticipacion: number;
}

interface Mensaje {
  tipo: 'exito' | 'error';
  texto: string;
}

const configuracion = ref<ConfiguracionRecordatorios>({
  activarRecordatorios: false,
  horasAnticipacion: 24
});

const cargando = ref(false);
const guardando = ref(false);
const mensaje = ref<Mensaje | null>(null);

const cargarConfiguracion = async () => {
  cargando.value = true;
  mensaje.value = null;
  
  try {
    const response = await api.get('/dueno/empresa/configuracion-recordatorios');
    configuracion.value = response.data;
  } catch (error: any) {
    mensaje.value = {
      tipo: 'error',
      texto: error.response?.data?.mensaje || 'Error al cargar la configuración'
    };
  } finally {
    cargando.value = false;
  }
};

const guardarConfiguracion = async () => {
  guardando.value = true;
  mensaje.value = null;
  
  try {
    await api.put('/dueno/empresa/configuracion-recordatorios', configuracion.value);
    mensaje.value = {
      tipo: 'exito',
      texto: 'Configuración guardada exitosamente'
    };
    
    // Ocultar mensaje después de 3 segundos
    setTimeout(() => {
      mensaje.value = null;
    }, 3000);
  } catch (error: any) {
    mensaje.value = {
      tipo: 'error',
      texto: error.response?.data?.mensaje || 'Error al guardar la configuración'
    };
  } finally {
    guardando.value = false;
  }
};

onMounted(() => {
  cargarConfiguracion();
});
</script>
