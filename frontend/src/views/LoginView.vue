<template>
  <div class="login-container">
    <div class="login-card">
      <div class="header-section">
        <div class="app-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="calendar-icon"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
        </div>
        <h1>Sistema de Turnos</h1>
        <h2>Bienvenido de nuevo</h2>
      </div>
      
      <div v-if="error" class="error-message">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
        <span>{{ error }}</span>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="ejemplo@gmail.com"
            required
            :disabled="cargando"
          />
        </div>

        <div class="form-group">
          <div class="label-row">
            <label for="password">Contraseña</label>
            </div>
          <div class="password-wrapper">
            <input
              id="password"
              v-model="contrasena"
              :type="mostrarContrasena ? 'text' : 'password'"
              placeholder="••••••••"
              required
              :disabled="cargando"
            />
            <button 
              type="button" 
              class="btn-toggle-password" 
              @click="mostrarContrasena = !mostrarContrasena"
              aria-label="Alternar visibilidad de contraseña"
            >
              <svg v-if="!mostrarContrasena" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#a0aec0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
            </button>
          </div>
        </div>

        <button type="submit" class="btn-submit" :disabled="cargando">
          <span v-if="!cargando">Iniciar Sesión</span>
          <span v-else class="loading-state">
            <svg class="spinner" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
            Ingresando...
          </span>
        </button>
      </form>

      <div class="footer-branding">
        <span class="powered-by">Powered by</span>
        <div class="ansa-brand">
          <span class="ansa-text">AnSa</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useClienteStore } from '../stores/cliente'
import api from '../services/api'
import { useToastStore } from '../composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const clienteStore = useClienteStore()
const toastStore = useToastStore()

const email = ref('')
const contrasena = ref('')
const error = ref('')
const cargando = ref(false)
const mostrarContrasena = ref(false) // NUEVO: Estado para el input de contraseña

async function handleLogin() {
  error.value = ''
  cargando.value = true

  try {
    const response = await api.login({
      email: email.value,
      contrasena: contrasena.value
    })

    if (response.data.exito) {
      if (clienteStore.isAuthenticated) {
        clienteStore.logout()
      }

      authStore.setUsuario(response.data.datos)

      try {
        await api.getCsrfToken()
      } catch (csrfError) {
        if (import.meta.env.DEV) console.warn('No se pudo obtener CSRF token...', csrfError)
      }

      try {
        await authStore.cargarDesdeBackend()
      } catch (perfilError) {
        if (import.meta.env.DEV) console.warn('No se pudo cargar el perfil completo...', perfilError)
      }

      const roles: string[] = response.data.datos.roles ?? []
      if (roles.includes('SUPER_ADMIN')) {
        router.push('/admin')
      } else if (roles.includes('DUENO')) {
        router.push('/dueno')
      } else if (roles.includes('PROFESIONAL')) {
        router.push('/profesional')
      } else {
        error.value = 'No tienes permisos para acceder'
      }
    } else {
      error.value = response.data.mensaje || 'Error al iniciar sesión'
    }
  } catch (err: any) {
    console.error('Error de login:', err)
    error.value = err.response?.data?.mensaje || 'Email o contraseña incorrectos'
    if (error.value === 'Tu cuenta ha sido desactivada. Por favor, comunícate con el local.') {
      toastStore.showError(error.value)
    }
  } finally {
    cargando.value = false
  }
}
</script>

<style scoped>
/* Contenedor principal responsive con 100dvh para arreglar el bug del celu */
.login-container {
  min-height: 100dvh; 
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Subimos la tarjeta para que no quede tan al medio */
  padding: 5rem 1rem 2rem 1rem;
  
  /* NUEVO FONDO: Gradiente lila/morado muy suave y profesional */
  background: linear-gradient(135deg, #f5f3ff 0%, #eaddf8 100%);
}

/* Tarjeta principal más fachera y limpia */
.login-card {
  background: white;
  padding: 2.5rem 2rem;
  border-radius: 20px;
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.05), 0 0 20px -5px rgba(102, 126, 234, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.8);
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
}

/* Cabecera con iconito */
.header-section {
  text-align: center;
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-icon {
  background: linear-gradient(135deg, #e0e7ff 0%, #ede9fe 100%);
  color: #667eea;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.5);
}

h1 {
  font-size: 1.5rem;
  color: #1a202c;
  margin-bottom: 0.25rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

h2 {
  font-size: 0.95rem;
  color: #718096;
  font-weight: 500;
  margin: 0;
}

/* Manejo de errores */
.error-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: #fff5f5;
  color: #e53e3e;
  padding: 0.875rem 1rem;
  border-radius: 12px;
  border: 1px solid #fed7d7;
  margin-bottom: 1.5rem;
  font-size: 0.85rem;
  font-weight: 500;
  animation: shake 0.4s ease-in-out;
}

/* Formulario */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

label {
  color: #4a5568;
  font-weight: 600;
  font-size: 0.85rem;
  letter-spacing: 0.3px;
}

/* Inputs premium */
input {
  width: 100%;
  padding: 0.875rem 1rem;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 0.95rem;
  color: #2d3748;
  background-color: #f8fafc;
  transition: all 0.2s ease;
}

input:focus {
  outline: none;
  border-color: #667eea;
  background-color: #ffffff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background-color: #edf2f7;
}

/* Ojito de la contraseña */
.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper input {
  padding-right: 3rem;
}

.btn-toggle-password {
  position: absolute;
  right: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-toggle-password:hover {
  background-color: #edf2f7;
}

/* Botón de Submit */
.btn-submit {
  margin-top: 0.75rem;
  padding: 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(102, 126, 234, 0.35);
}

.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}

.btn-submit:disabled {
  background: #cbd5e0;
  box-shadow: none;
}

/* Estados de carga */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.spinner {
  animation: rotate 2s linear infinite;
  width: 20px;
  height: 20px;
}

.spinner .path {
  stroke: #ffffff;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

/* Branding AnSa Footer */
.footer-branding {
  margin-top: 2.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.powered-by {
  color: #a0aec0;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.ansa-brand {
  display: flex;
  align-items: center;
  justify-content: center;
}

.ansa-text {
  font-size: 1.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1a202c 0%, #4a5568 100%);
  -webkit-background-clip: text;
  background-clip: text; /* NUEVO: La propiedad estándar para sacar el warning */
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.5px;
}

/* .ansa-logo-img {
  height: 24px;
  width: auto;
} */

/* Animaciones */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@keyframes rotate { 100% { transform: rotate(360deg); } }
@keyframes dash {
  0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; }
}

/* Ajustes para móviles */
@media (max-width: 480px) {
  .login-container {
    padding-top: 3rem; /* En celular lo bajamos un poquitito nomás */
  }
  .login-card {
    padding: 2rem 1.5rem;
    border-radius: 16px;
  }
}
</style>