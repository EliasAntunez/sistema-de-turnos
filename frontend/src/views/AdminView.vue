<template>
  <div class="admin-container">
    <!-- Header -->
    <header class="admin-header">
      <h1>Panel de Administración</h1>
      <div class="user-info">
        <span>{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
        <button @click="handleLogout" class="btn-logout">Cerrar Sesión</button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="admin-content">
      <!-- Botón para crear empresa -->
      <div class="actions-bar">
        <button @click="mostrarFormulario = true" class="btn-primary">
          ➕ Nueva Empresa
        </button>
      </div>

      <!-- Formulario de creación -->
      <div v-if="mostrarFormulario" class="modal-overlay" @click.self="cerrarFormulario">
        <div class="modal-content">
          <h2>Registrar Nueva Empresa</h2>
          
          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="exito" class="success-message">{{ exito }}</div>

          <form @submit.prevent="crearEmpresa">
            <h3>Datos del Dueño</h3>
            
            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.nombre'] }">
                <label>Nombre *</label>
                <input v-model="formData.dueno.nombre" required />
                <span v-if="fieldErrors['dueno.nombre']" class="field-error">{{ fieldErrors['dueno.nombre'] }}</span>
              </div>
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.apellido'] }">
                <label>Apellido *</label>
                <input v-model="formData.dueno.apellido" required />
                <span v-if="fieldErrors['dueno.apellido']" class="field-error">{{ fieldErrors['dueno.apellido'] }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.email'] }">
                <label>Email *</label>
                <input v-model="formData.dueno.email" type="email" required />
                <span v-if="fieldErrors['dueno.email']" class="field-error">{{ fieldErrors['dueno.email'] }}</span>
              </div>
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.telefono'] }">
                <label>Teléfono *</label>
                <input v-model="formData.dueno.telefono" required placeholder="Solo números (10-15 dígitos)" />
                <span v-if="fieldErrors['dueno.telefono']" class="field-error">{{ fieldErrors['dueno.telefono'] }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.contrasena'] }">
                <label>Contraseña *</label>
                <input v-model="formData.dueno.contrasena" type="password" required placeholder="Mínimo 8 caracteres" />
                <span v-if="fieldErrors['dueno.contrasena']" class="field-error">{{ fieldErrors['dueno.contrasena'] }}</span>
              </div>
              <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.tipoDocumento'] }">
                <label>Tipo Doc *</label>
                <input v-model="formData.dueno.tipoDocumento" placeholder="DNI" required />
                <span v-if="fieldErrors['dueno.tipoDocumento']" class="field-error">{{ fieldErrors['dueno.tipoDocumento'] }}</span>
              </div>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrors['dueno.documento'] }">
              <label>Documento *</label>
              <input v-model="formData.dueno.documento" required />
              <span v-if="fieldErrors['dueno.documento']" class="field-error">{{ fieldErrors['dueno.documento'] }}</span>
            </div>

            <h3>Datos de la Empresa</h3>

            <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.nombre'] }">
              <label>Nombre de la Empresa *</label>
              <input v-model="formData.empresa.nombre" required />
              <span v-if="fieldErrors['empresa.nombre']" class="field-error">{{ fieldErrors['empresa.nombre'] }}</span>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.cuit'] }">
              <label>CUIT *</label>
              <input v-model="formData.empresa.cuit" placeholder="Solo números (11 dígitos)" required />
              <span v-if="fieldErrors['empresa.cuit']" class="field-error">{{ fieldErrors['empresa.cuit'] }}</span>
            </div>

            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.ciudad'] }">
                <label>Ciudad</label>
                <input v-model="formData.empresa.ciudad" />
                <span v-if="fieldErrors['empresa.ciudad']" class="field-error">{{ fieldErrors['empresa.ciudad'] }}</span>
              </div>
              <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.provincia'] }">
                <label>Provincia</label>
                <input v-model="formData.empresa.provincia" />
                <span v-if="fieldErrors['empresa.provincia']" class="field-error">{{ fieldErrors['empresa.provincia'] }}</span>
              </div>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.direccion'] }">
              <label>Dirección</label>
              <input v-model="formData.empresa.direccion" />
              <span v-if="fieldErrors['empresa.direccion']" class="field-error">{{ fieldErrors['empresa.direccion'] }}</span>
            </div>

            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.telefono'] }">
                <label>Teléfono</label>
                <input v-model="formData.empresa.telefono" placeholder="Solo números (10-15 dígitos)" />
                <span v-if="fieldErrors['empresa.telefono']" class="field-error">{{ fieldErrors['empresa.telefono'] }}</span>
              </div>
              <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.email'] }">
                <label>Email</label>
                <input v-model="formData.empresa.email" type="email" />
                <span v-if="fieldErrors['empresa.email']" class="field-error">{{ fieldErrors['empresa.email'] }}</span>
              </div>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrors['empresa.descripcion'] }">
              <label>Descripción</label>
              <textarea v-model="formData.empresa.descripcion" rows="3"></textarea>
              <span v-if="fieldErrors['empresa.descripcion']" class="field-error">{{ fieldErrors['empresa.descripcion'] }}</span>
            </div>

            <div class="form-actions">
              <button type="button" @click="cerrarFormulario" class="btn-secondary">
                Cancelar
              </button>
              <button type="submit" :disabled="cargando" class="btn-primary">
                {{ cargando ? 'Guardando...' : 'Guardar Empresa' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Lista de empresas -->
      <div class="empresas-grid">
        <div v-if="cargandoEmpresas" class="loading">Cargando empresas...</div>
        
        <div v-else-if="empresas.length === 0" class="empty-state">
          No hay empresas registradas. Crea la primera empresa.
        </div>

        <div v-else v-for="empresa in empresas" :key="empresa.id" class="empresa-card">
          <div class="empresa-header">
            <h3>{{ empresa.nombre }}</h3>
            <span :class="['badge', empresa.activa ? 'badge-active' : 'badge-inactive']">
              {{ empresa.activa ? 'Activa' : 'Inactiva' }}
            </span>
          </div>
          
          <div class="empresa-info">
            <p><strong>CUIT:</strong> {{ empresa.cuit }}</p>
            <p><strong>Dueño:</strong> {{ empresa.dueno.nombre }} {{ empresa.dueno.apellido }}</p>
            <p><strong>Email:</strong> {{ empresa.dueno.email }}</p>
            <p><strong>Teléfono:</strong> {{ empresa.dueno.telefono }}</p>
            <p v-if="empresa.ciudad"><strong>Ubicación:</strong> {{ empresa.ciudad }}, {{ empresa.provincia }}</p>
          </div>

          <div class="empresa-actions">
            <button 
              v-if="empresa.activa"
              @click="cambiarEstado(empresa.id, false)"
              class="btn-danger-sm"
            >
              Desactivar
            </button>
            <button 
              v-else
              @click="cambiarEstado(empresa.id, true)"
              class="btn-success-sm"
            >
              Activar
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const mostrarFormulario = ref(false)
const empresas = ref<any[]>([])
const cargando = ref(false)
const cargandoEmpresas = ref(false)
const error = ref('')
const exito = ref('')
const fieldErrors = ref<Record<string, string>>({})

const formData = ref({
  dueno: {
    nombre: '',
    apellido: '',
    email: '',
    contrasena: '',
    telefono: '',
    documento: '',
    tipoDocumento: 'DNI'
  },
  empresa: {
    nombre: '',
    descripcion: '',
    cuit: '',
    direccion: '',
    ciudad: '',
    provincia: '',
    telefono: '',
    email: ''
  }
})

onMounted(() => {
  cargarEmpresas()
})

async function cargarEmpresas() {
  cargandoEmpresas.value = true
  try {
    const response = await api.obtenerEmpresas()
    if (response.data.exito) {
      empresas.value = response.data.datos
    }
  } catch (err) {
    console.error('Error al cargar empresas:', err)
  } finally {
    cargandoEmpresas.value = false
  }
}

async function crearEmpresa() {
  error.value = ''
  exito.value = ''
  fieldErrors.value = {}
  cargando.value = true

  try {
    const response = await api.crearEmpresaConDueno(formData.value)
    
    if (response.data.exito) {
      exito.value = 'Empresa creada exitosamente'
      setTimeout(() => {
        cerrarFormulario()
        cargarEmpresas()
      }, 1500)
    }
  } catch (err: any) {
    console.error('Error completo:', err)
    console.error('Response data:', err.response?.data)
    console.error('Response status:', err.response?.status)
    
    // Extraer y asignar errores por campo
    if (err.response?.data?.errores) {
      // Formato ErrorValidacion con errores por campo
      fieldErrors.value = err.response.data.errores
      error.value = 'Por favor corrija los errores en el formulario'
    } else if (err.response?.data?.mensaje) {
      // Formato RespuestaApi con mensaje general
      error.value = err.response.data.mensaje
    } else if (err.response?.data?.error) {
      error.value = err.response.data.error
    } else if (typeof err.response?.data === 'string') {
      error.value = err.response.data
    } else {
      error.value = 'Error al crear la empresa. Por favor intente nuevamente.'
    }
  } finally {
    cargando.value = false
  }
}

async function cambiarEstado(id: number, activa: boolean) {
  try {
    if (activa) {
      await api.activarEmpresa(id)
    } else {
      await api.desactivarEmpresa(id)
    }
    cargarEmpresas()
  } catch (err: any) {
    console.error('Error al cambiar estado:', err)
    alert('Error al cambiar el estado de la empresa')
  }
}

function cerrarFormulario() {
  mostrarFormulario.value = false
  error.value = ''
  exito.value = ''
  fieldErrors.value = {}
  // Resetear formulario
  formData.value = {
    dueno: {
      nombre: '',
      apellido: '',
      email: '',
      contrasena: '',
      telefono: '',
      documento: '',
      tipoDocumento: 'DNI'
    },
    empresa: {
      nombre: '',
      descripcion: '',
      cuit: '',
      direccion: '',
      ciudad: '',
      provincia: '',
      telefono: '',
      email: ''
    }
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f7fafc;
}

.admin-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h1 {
  font-size: 1.5rem;
  color: #2d3748;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-logout {
  padding: 0.5rem 1rem;
  background: #fc8181;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.admin-content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.actions-bar {
  margin-bottom: 2rem;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow-y: auto;
  padding: 2rem;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-bottom: 1.5rem;
  color: #2d3748;
}

.modal-content h3 {
  margin: 1.5rem 0 1rem;
  color: #4a5568;
  font-size: 1.1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.95rem;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

/* Field Error Styles */
.form-group.has-error input,
.form-group.has-error textarea {
  border-color: #fc8181;
  background-color: #fff5f5;
}

.form-group.has-error input:focus,
.form-group.has-error textarea:focus {
  border-color: #f56565;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1);
}

.field-error {
  display: block;
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.35rem;
  font-weight: 500;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.empresas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.empresa-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empresa-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.empresa-header h3 {
  color: #2d3748;
  font-size: 1.2rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
}

.badge-active {
  background: #c6f6d5;
  color: #22543d;
}

.badge-inactive {
  background: #fed7d7;
  color: #742a2a;
}

.empresa-info p {
  margin: 0.5rem 0;
  color: #4a5568;
  font-size: 0.95rem;
}

.empresa-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.5rem;
}

.btn-danger-sm,
.btn-success-sm {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  font-weight: 500;
}

.btn-danger-sm {
  background: #fc8181;
  color: white;
}

.btn-success-sm {
  background: #68d391;
  color: white;
}

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-weight: 500;
}

.success-message {
  background: #d4edda;
  color: #155724;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.loading,
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  color: #718096;
  font-size: 1.1rem;
}
</style>
