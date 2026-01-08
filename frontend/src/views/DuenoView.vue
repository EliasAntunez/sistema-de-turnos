<template>
  <div class="dueno-container">
    <!-- Header -->
    <header class="dueno-header">
      <h1>Mi Empresa - Gestión de Profesionales</h1>
      <div class="user-info">
        <span>{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
        <span class="badge-dueno">Dueño</span>
        <button @click="handleLogout" class="btn-logout">Cerrar Sesión</button>
      </div>
    </header>

    <!-- Main Content -->
    <main class="dueno-content">
      <div class="section-header">
        <h2>Profesionales de la Empresa</h2>
        <button @click="openModal()" class="btn-add">+ Agregar Profesional</button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading">Cargando...</div>

      <!-- Profesionales Grid -->
      <div v-else-if="profesionales.length > 0" class="profesionales-grid">
        <div v-for="profesional in profesionales" :key="profesional.id" class="profesional-card">
          <div class="card-header">
            <h3>{{ profesional.nombre }} {{ profesional.apellido }}</h3>
          </div>
          <div class="card-body">
            <div class="info-item">
              <strong>Email:</strong> {{ profesional.email }}
            </div>
            <div class="info-item">
              <strong>Teléfono:</strong> {{ profesional.telefono || 'No especificado' }}
            </div>
            <div class="info-item">
              <strong>Especialidades:</strong> 
              <span class="especialidades-tags">
                <span v-for="(esp, index) in profesional.especialidades" :key="index" class="tag-especialidad">
                  {{ esp }}
                </span>
              </span>
            </div>
            <div class="info-item" v-if="profesional.descripcion">
              <strong>Descripción:</strong> {{ profesional.descripcion }}
            </div>
          </div>
          <div class="card-actions">
            <button @click="openModal(profesional)" class="btn-edit">Editar</button>
            <button @click="confirmarEliminacion(profesional)" class="btn-delete">Eliminar</button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay profesionales registrados</p>
        <p class="empty-hint">Haz clic en "Agregar Profesional" para comenzar</p>
      </div>
    </main>

    <!-- Modal Form -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ editingProfesional ? 'Editar' : 'Nuevo' }} Profesional</h2>
          <button @click="closeModal" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitForm" class="modal-form">
          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrors.nombre }">
              <label>Nombre *</label>
              <input 
                v-model="formData.nombre" 
                type="text" 
                required
                placeholder="Nombre"
              />
              <span v-if="fieldErrors.nombre" class="field-error">{{ fieldErrors.nombre }}</span>
            </div>
            <div class="form-group" :class="{ 'has-error': fieldErrors.apellido }">
              <label>Apellido *</label>
              <input 
                v-model="formData.apellido" 
                type="text" 
                required
                placeholder="Apellido"
              />
              <span v-if="fieldErrors.apellido" class="field-error">{{ fieldErrors.apellido }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.email }">
            <label>Email *</label>
            <input 
              v-model="formData.email" 
              type="email" 
              required
              placeholder="email@ejemplo.com"
            />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.contrasena }">
            <label>{{ editingProfesional ? 'Nueva Contraseña (opcional)' : 'Contraseña *' }}</label>
            <input 
              v-model="formData.contrasena" 
              type="password" 
              :required="!editingProfesional"
              placeholder="Contraseña (mínimo 8 caracteres)"
            />
            <span v-if="fieldErrors.contrasena" class="field-error">{{ fieldErrors.contrasena }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrors.telefono }">
              <label>Teléfono</label>
              <input 
                v-model="formData.telefono" 
                type="tel" 
                placeholder="Solo números (10-15 dígitos)"
              />
              <span v-if="fieldErrors.telefono" class="field-error">{{ fieldErrors.telefono }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.especialidades }">
            <label>Especialidades * (separadas por comas)</label>
            <input 
              v-model="formData.especialidadesStr" 
              type="text" 
              required
              placeholder="Ej: Médico General, Cardiólogo"
            />
            <small>Ingrese las especialidades separadas por comas</small>
            <span v-if="fieldErrors.especialidades" class="field-error">{{ fieldErrors.especialidades }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.descripcion }">
            <label>Descripción</label>
            <textarea 
              v-model="formData.descripcion" 
              rows="3"
              placeholder="Descripción adicional del profesional"
            ></textarea>
            <span v-if="fieldErrors.descripcion" class="field-error">{{ fieldErrors.descripcion }}</span>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submitting">
              {{ submitting ? 'Guardando...' : (editingProfesional ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

const profesionales = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const editingProfesional = ref<any>(null)
const error = ref('')
const submitting = ref(false)
const fieldErrors = ref<Record<string, string>>({})

const formData = ref({
  nombre: '',
  apellido: '',
  email: '',
  contrasena: '',
  telefono: '',
  especialidadesStr: '',
  descripcion: ''
})

onMounted(() => {
  cargarProfesionales()
})

async function cargarProfesionales() {
  loading.value = true
  try {
    const response = await api.get('/dueno/profesionales')
    profesionales.value = response.data
  } catch (err: any) {
    console.error('Error al cargar profesionales:', err)
    error.value = 'Error al cargar la lista de profesionales'
  } finally {
    loading.value = false
  }
}

function openModal(profesional: any = null) {
  editingProfesional.value = profesional
  if (profesional) {
    formData.value = {
      nombre: profesional.nombre,
      apellido: profesional.apellido,
      email: profesional.email,
      contrasena: '',
      telefono: profesional.telefono || '',
      especialidadesStr: profesional.especialidades.join(', '),
      descripcion: profesional.descripcion || ''
    }
  } else {
    formData.value = {
      nombre: '',
      apellido: '',
      email: '',
      contrasena: '',
      telefono: '',
      especialidadesStr: '',
      descripcion: ''
    }
  }
  error.value = ''
  fieldErrors.value = {}
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingProfesional.value = null
  error.value = ''
  fieldErrors.value = {}
}

async function submitForm() {
  error.value = ''
  fieldErrors.value = {}
  submitting.value = true
  
  try {
    // Convertir especialidades string a array
    const especialidades = formData.value.especialidadesStr
      .split(',')
      .map(esp => esp.trim())
      .filter(esp => esp.length > 0)

    const payload = {
      nombre: formData.value.nombre,
      apellido: formData.value.apellido,
      email: formData.value.email,
      contrasena: formData.value.contrasena,
      telefono: formData.value.telefono,
      especialidades: especialidades,
      descripcion: formData.value.descripcion
      // empresaId ya no es necesario - el backend lo obtiene automáticamente del dueño autenticado
    }

    if (editingProfesional.value) {
      // Actualizar profesional existente
      await api.put(`/dueno/profesionales/${editingProfesional.value.id}`, payload)
    } else {
      // Crear nuevo profesional
      await api.post('/dueno/profesionales', payload)
    }
    closeModal()
    await cargarProfesionales()
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
      error.value = 'Error al guardar el profesional. Por favor intente nuevamente.'
    }
  } finally {
    submitting.value = false
  }
}

async function confirmarEliminacion(profesional: any) {
  if (confirm(`¿Está seguro de eliminar al profesional ${profesional.nombre} ${profesional.apellido}?`)) {
    try {
      await api.delete(`/dueno/profesionales/${profesional.id}`)
      alert('Profesional eliminado correctamente')
      await cargarProfesionales()
    } catch (err) {
      console.error('Error al eliminar profesional:', err)
      alert('Error al eliminar el profesional')
    }
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.dueno-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 2rem;
}

.dueno-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dueno-header h1 {
  font-size: 1.5rem;
  color: #2d3748;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.badge-dueno {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.btn-logout {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-logout:hover {
  background: #c53030;
  transform: translateY(-2px);
}

.dueno-content {
  max-width: 1400px;
  margin: 2rem auto;
  padding: 0 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-header h2 {
  color: white;
  font-size: 1.8rem;
  margin: 0;
}

.btn-add {
  background: white;
  color: #667eea;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.loading {
  text-align: center;
  color: white;
  font-size: 1.2rem;
  padding: 3rem;
}

.profesionales-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.profesional-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.profesional-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f7fafc;
}

.card-header h3 {
  color: #2d3748;
  font-size: 1.2rem;
  margin: 0;
}

.card-body {
  margin-bottom: 1rem;
}

.info-item {
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-size: 0.95rem;
}

.info-item strong {
  color: #2d3748;
}

.especialidades-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.tag-especialidad {
  background: #e6fffa;
  color: #234e52;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-edit,
.btn-delete {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.btn-edit {
  background: #4299e1;
  color: white;
}

.btn-edit:hover {
  background: #3182ce;
}

.btn-delete {
  background: #f56565;
  color: white;
}

.btn-delete:hover {
  background: #e53e3e;
}

.empty-state {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.empty-state p {
  color: #4a5568;
  font-size: 1.2rem;
  margin: 0.5rem 0;
}

.empty-hint {
  color: #718096;
  font-size: 1rem;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 2px solid #f7fafc;
}

.modal-header h2 {
  color: #2d3748;
  font-size: 1.5rem;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #a0aec0;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #2d3748;
}

.modal-form {
  padding: 1.5rem;
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
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: border-color 0.3s ease;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group small {
  display: block;
  color: #718096;
  font-size: 0.75rem;
  margin-top: 0.25rem;
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

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .profesionales-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>
