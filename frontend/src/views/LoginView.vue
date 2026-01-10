<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Sistema de Turnos</h1>
      <h2>Iniciar Sesión</h2>
      
      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="admin@sistema.com"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            id="password"
            v-model="contrasena"
            type="password"
            placeholder="••••••••"
            required
          />
        </div>

        <button type="submit" :disabled="cargando">
          {{ cargando ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const contrasena = ref('')
const error = ref('')
const cargando = ref(false)

async function handleLogin() {
  error.value = ''
  cargando.value = true

  try {
    const response = await api.login({
      email: email.value,
      contrasena: contrasena.value
    })

    if (response.data.exito) {
      // ACTUALIZADO: Solo guardar datos del usuario (NO credenciales)
      // Spring Security gestiona la sesión con cookies HttpOnly
      authStore.setUsuario(response.data.datos)

      // Obtener CSRF token antes de redirigir
      // Esto asegura que la cookie XSRF-TOKEN esté disponible para requests subsiguientes
      try {
        await api.getCsrfToken()
      } catch (csrfError) {
        console.warn('No se pudo obtener CSRF token, pero continuando...', csrfError)
      }

      // Redirigir según rol
      if (response.data.datos.rol === 'SUPER_ADMIN') {
        router.push('/admin')
      } else if (response.data.datos.rol === 'DUENO') {
        router.push('/dueno')
      } else if (response.data.datos.rol === 'PROFESIONAL') {
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
  } finally {
    cargando.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h1 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 0.5rem;
  text-align: center;
}

h2 {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 2rem;
  text-align: center;
  font-weight: normal;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #667eea;
}

button {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

button:hover:not(:disabled) {
  transform: translateY(-2px);
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  background: #fee;
  color: #c33;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}
</style>
