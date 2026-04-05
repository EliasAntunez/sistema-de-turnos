<template>
  <Teleport to="body">
    <div v-if="show" class="modal-backdrop" @click.self="cerrar">
      <div class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="titulo-cambiar-clave">
        <div class="modal-header">
          <h3 id="titulo-cambiar-clave">Cambiar Contraseña</h3>
          <button class="close-btn" @click="cerrar" :disabled="submitting" aria-label="Cerrar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        <p class="subtitle">Por seguridad, ingresá tu contraseña actual y definí una nueva.</p>

        <form class="form" @submit.prevent="submit">
          <div class="field">
            <label for="contrasena-actual">Contraseña Actual</label>
            <div class="input-wrap">
              <input
                ref="currentPasswordInputRef"
                id="contrasena-actual"
                v-model="form.contrasenaActual"
                :type="showActual ? 'text' : 'password'"
                autocomplete="current-password"
                :disabled="submitting || completed"
                required
              />
              <button type="button" class="toggle-visibility" @click="showActual = !showActual" :disabled="submitting || completed">
                {{ showActual ? 'Ocultar' : 'Ver' }}
              </button>
            </div>
          </div>

          <div class="field">
            <label for="nueva-contrasena">Nueva Contraseña</label>
            <div class="input-wrap">
              <input
                id="nueva-contrasena"
                v-model="form.nuevaContrasena"
                :type="showNueva ? 'text' : 'password'"
                autocomplete="new-password"
                minlength="8"
                :disabled="submitting || completed"
                required
              />
              <button type="button" class="toggle-visibility" @click="showNueva = !showNueva" :disabled="submitting || completed">
                {{ showNueva ? 'Ocultar' : 'Ver' }}
              </button>
            </div>
          </div>

          <div class="field">
            <label for="confirmar-contrasena">Confirmar Nueva Contraseña</label>
            <div class="input-wrap">
              <input
                id="confirmar-contrasena"
                v-model="form.confirmarNuevaContrasena"
                :type="showConfirmar ? 'text' : 'password'"
                autocomplete="new-password"
                minlength="8"
                :disabled="submitting || completed"
                required
              />
              <button type="button" class="toggle-visibility" @click="showConfirmar = !showConfirmar" :disabled="submitting || completed">
                {{ showConfirmar ? 'Ocultar' : 'Ver' }}
              </button>
            </div>
          </div>

          <p v-if="errorMessage" class="message message-error">{{ errorMessage }}</p>
          <p v-if="successMessage" class="message message-success">{{ successMessage }}</p>

          <div class="actions">
            <button type="button" class="btn-secondary" @click="cerrar" :disabled="submitting">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="submitting || completed">
              <span v-if="submitting" class="spinner" aria-hidden="true"></span>
              <span>{{ submitting ? 'Guardando...' : 'Actualizar Contraseña' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

interface Props {
  show: boolean
  logoutOnSuccess?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  logoutOnSuccess: false
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  contrasenaActual: '',
  nuevaContrasena: '',
  confirmarNuevaContrasena: ''
})

const submitting = ref(false)
const completed = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const showActual = ref(false)
const showNueva = ref(false)
const showConfirmar = ref(false)
const currentPasswordInputRef = ref<HTMLInputElement | null>(null)
const previousActiveElement = ref<HTMLElement | null>(null)

let closeTimer: ReturnType<typeof setTimeout> | null = null

function handleEscapeKey(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.show) {
    event.preventDefault()
    cerrar()
  }
}

function resetState() {
  form.contrasenaActual = ''
  form.nuevaContrasena = ''
  form.confirmarNuevaContrasena = ''
  submitting.value = false
  completed.value = false
  errorMessage.value = ''
  successMessage.value = ''
  showActual.value = false
  showNueva.value = false
  showConfirmar.value = false
  if (closeTimer) {
    clearTimeout(closeTimer)
    closeTimer = null
  }
}

function cerrar() {
  if (submitting.value) return
  emit('close')
}

async function logoutSegunConfiguracion() {
  if (!props.logoutOnSuccess) return
  try {
    await api.logout()
  } finally {
    authStore.logout()
    router.push('/login')
  }
}

async function submit() {
  errorMessage.value = ''
  successMessage.value = ''

  if (form.nuevaContrasena !== form.confirmarNuevaContrasena) {
    errorMessage.value = 'La nueva contraseña y su confirmación no coinciden.'
    return
  }

  if (form.nuevaContrasena.length < 8) {
    errorMessage.value = 'La nueva contraseña debe tener al menos 8 caracteres.'
    return
  }

  submitting.value = true
  try {
    const response = await api.cambiarContrasena({
      contrasenaActual: form.contrasenaActual,
      nuevaContrasena: form.nuevaContrasena,
      confirmarNuevaContrasena: form.confirmarNuevaContrasena
    })

    successMessage.value = response.data?.mensaje || 'Contraseña actualizada correctamente.'
    completed.value = true
    emit('success')

    closeTimer = setTimeout(async () => {
      await logoutSegunConfiguracion()
      emit('close')
    }, 2000)
  } catch (error: unknown) {
    const err = error as {
      response?: { data?: { mensaje?: string; message?: string; error?: string } }
      message?: string
    }
    errorMessage.value = err.response?.data?.mensaje || err.response?.data?.message || err.response?.data?.error || err.message || 'No se pudo actualizar la contraseña.'
  } finally {
    submitting.value = false
  }
}

watch(() => props.show, (visible) => {
  if (visible) {
    previousActiveElement.value = document.activeElement as HTMLElement | null
    document.addEventListener('keydown', handleEscapeKey)
    nextTick(() => {
      currentPasswordInputRef.value?.focus()
    })
    return
  }

  document.removeEventListener('keydown', handleEscapeKey)
  if (previousActiveElement.value && typeof previousActiveElement.value.focus === 'function') {
    previousActiveElement.value.focus()
    previousActiveElement.value = null
  }

  if (!visible) {
    resetState()
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleEscapeKey)
  if (closeTimer) {
    clearTimeout(closeTimer)
  }
})
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(6px);
}

.modal-panel {
  width: 100%;
  max-width: 460px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  box-shadow: 0 22px 50px -12px rgba(15, 23, 42, 0.3);
  padding: 1rem 1rem 1.25rem;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.35rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: #0f172a;
}

.close-btn {
  border: none;
  background: transparent;
  color: #64748b;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  padding: 0.35rem;
  cursor: pointer;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.subtitle {
  margin: 0 0 1rem;
  font-size: 0.9rem;
  color: #475569;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field label {
  font-size: 0.83rem;
  font-weight: 600;
  color: #334155;
}

.input-wrap {
  display: flex;
  align-items: center;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.input-wrap:focus-within {
  border-color: #334155;
  box-shadow: 0 0 0 3px rgba(15, 23, 42, 0.08);
}

.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  padding: 0.68rem 0.75rem;
  font-size: 0.92rem;
  color: #0f172a;
}

.toggle-visibility {
  border: none;
  background: #f8fafc;
  border-left: 1px solid #e2e8f0;
  color: #334155;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.68rem 0.7rem;
  cursor: pointer;
}

.toggle-visibility:hover {
  background: #f1f5f9;
}

.message {
  margin: 0;
  padding: 0.65rem 0.8rem;
  border-radius: 10px;
  font-size: 0.88rem;
}

.message-error {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.message-success {
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.6rem;
  margin-top: 0.4rem;
}

.btn-secondary,
.btn-primary {
  border: none;
  border-radius: 10px;
  padding: 0.62rem 0.95rem;
  font-size: 0.86rem;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
}

.btn-secondary {
  background: #f1f5f9;
  color: #0f172a;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.btn-primary {
  background: #0f172a;
  color: #ffffff;
}

.btn-primary:hover {
  background: #1e293b;
}

.btn-primary:disabled,
.btn-secondary:disabled,
.close-btn:disabled,
.toggle-visibility:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.55);
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
