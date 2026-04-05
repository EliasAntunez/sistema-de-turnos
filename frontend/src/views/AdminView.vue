<template>
  <div class="admin-layout">
    <header class="admin-navbar">
      <div class="navbar-brand">
        <div class="logo-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"></path><path d="M2 17l10 5 10-5"></path><path d="M2 12l10 5 10-5"></path></svg>
        </div>
        <h1>AnSa Admin Panel</h1>
      </div>
      <div class="navbar-user" ref="userMenuRef">
        <button ref="userMenuButtonRef" class="user-menu-trigger" @click.stop="toggleUserMenu">
          <div class="user-profile">
            <div class="avatar">
              {{ authStore.usuario?.nombre?.charAt(0) }}{{ authStore.usuario?.apellido?.charAt(0) }}
            </div>
            <div class="user-details">
              <span class="user-name">{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
              <span class="user-role">Super Admin</span>
            </div>
          </div>
          <svg class="menu-caret" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.939a.75.75 0 111.08 1.04l-4.25 4.511a.75.75 0 01-1.08 0L5.21 8.269a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
          </svg>
        </button>

        <div v-if="showUserMenu" class="user-dropdown" @click.stop>
          <button class="dropdown-item" @click="abrirModalCambiarContrasena">Cambiar Contraseña</button>
          <div class="dropdown-separator"></div>
          <button class="dropdown-item dropdown-item-danger" @click="handleLogout">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </div>
    </header>

    <div class="admin-body">
      <aside class="admin-sidebar">
        <nav class="sidebar-nav">
          <button 
            :class="['nav-item', { active: activeTab === 'empresas' }]" 
            @click="activeTab = 'empresas'"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
            Gestión de Empresas
          </button>
          </nav>
      </aside>

      <main class="admin-content">
        <div v-if="activeTab === 'empresas'" class="content-wrapper">
          
          <div class="section-header">
            <div>
              <h2>Directorio de Empresas</h2>
              <p class="text-subtitle">Administra los locales y sus dueños asociados.</p>
            </div>
            <button @click="router.push('/admin/registrar-empresa')" class="btn-primary">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
              Nueva Empresa
            </button>
          </div>

          <div v-if="cargandoEmpresas" class="loading-state">
            <svg class="spinner" viewBox="0 0 50 50"><circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle></svg>
            <p>Cargando empresas...</p>
          </div>
          
          <div v-else-if="empresas.length === 0" class="empty-state">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
            <h3>No hay empresas registradas</h3>
            <p>Comienza creando el primer local para que puedan usar el sistema.</p>
          </div>

          <div v-else class="empresas-grid">
            <div v-for="empresa in empresas" :key="empresa.id" class="empresa-card">
              
              <div class="card-header">
                <div class="card-title">
                  <div class="company-icon">
                    {{ empresa.nombre.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <h3>{{ empresa.nombre }}</h3>
                    <span :class="['badge', empresa.activa ? 'badge-active' : 'badge-inactive']">
                      {{ empresa.activa ? 'Activa' : 'Suspendida' }}
                    </span>
                  </div>
                </div>
                </div>
              
              <div class="card-body">
                <div class="info-row">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                  <div class="info-text">
                    <span class="label">Dueño</span>
                    <span class="value">{{ empresa.dueno.nombre }} {{ empresa.dueno.apellido }}</span>
                  </div>
                </div>
                
                <div class="info-row">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                  <div class="info-text">
                    <span class="label">Contacto</span>
                    <span class="value">{{ empresa.dueno.email }}</span>
                  </div>
                </div>

                <div class="info-row">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                  <div class="info-text">
                    <span class="label">Ubicación</span>
                    <span class="value">{{ empresa.ciudad ? `${empresa.ciudad}, ${empresa.provincia}` : 'No especificada' }}</span>
                  </div>
                </div>
              </div>

              <div class="card-footer">
                <button 
                  v-if="empresa.activa"
                  @click="confirmarCambioEstado(empresa, false)"
                  class="btn-action btn-danger"
                >
                  Desactivar Empresa
                </button>
                <button 
                  v-else
                  @click="confirmarCambioEstado(empresa, true)"
                  class="btn-action btn-success"
                >
                  Reactivar Empresa
                </button>
              </div>

            </div>
          </div>

          <ConfirmModal
            :show="showConfirmToggleEmpresa"
            :titulo="`${nuevoEstadoEmpresa ? 'Activar' : 'Suspender'} Empresa`"
            :mensaje="`¿Estás seguro de que deseas ${nuevoEstadoEmpresa ? 'reactivar' : 'suspender'} el acceso al sistema para ${empresaPendienteToggle?.nombre}?`"
            :textoConfirmar="cambiandoEstadoEmpresa ? 'Procesando...' : (nuevoEstadoEmpresa ? 'Sí, Activar' : 'Sí, Suspender')"
            :colorBoton="nuevoEstadoEmpresa ? 'bg-green-600 hover:bg-green-700' : 'bg-red-600 hover:bg-red-700'"
            @confirm="ejecutarCambioEstadoEmpresa"
            @cancel="cerrarConfirmToggleEmpresa"
          />
        </div>
      </main>
    </div>
  </div>
  <CambiarContrasenaModal
    :show="showChangePasswordModal"
    :logoutOnSuccess="true"
    @close="cerrarModalCambiarContrasena"
  />
  <Toast />
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import Toast from '../components/Toast.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import CambiarContrasenaModal from '../components/CambiarContrasenaModal.vue'
import { useToastStore } from '../composables/useToast'

const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()

interface DuenoAdmin {
  nombre: string
  apellido: string
  email: string
  telefono: string
}

interface EmpresaAdmin {
  id: number
  nombre: string
  cuit: string
  activa: boolean
  dueno: DuenoAdmin
  ciudad?: string | null
  provincia?: string | null
}

interface ApiErrorAdmin {
  response?: {
    data?: {
      errores?: Record<string, string>
      mensaje?: string
      error?: string
    }
  }
  message?: string
}

const activeTab = ref<'empresas'>('empresas')
const mostrarFormulario = ref(false)
const empresas = ref<EmpresaAdmin[]>([])
const cargando = ref(false)
const cargandoEmpresas = ref(false)
const error = ref('')
const exito = ref('')
const fieldErrors = ref<Record<string, string>>({})
const showConfirmToggleEmpresa = ref(false)
const empresaPendienteToggle = ref<EmpresaAdmin | null>(null)
const nuevoEstadoEmpresa = ref(false)
const cambiandoEstadoEmpresa = ref(false)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)
const userMenuButtonRef = ref<HTMLButtonElement | null>(null)
const showChangePasswordModal = ref(false)

const formData = ref({
  dueno: { nombre: '', apellido: '', email: '', contrasena: '', telefono: '', documento: '', tipoDocumento: 'DNI' },
  empresa: { nombre: '', slug: '', descripcion: '', cuit: '', direccion: '', ciudad: '', provincia: '', telefono: '', email: '', diasMaximosReserva: 30 }
})

onMounted(() => {
  document.addEventListener('click', handleClickOutsideUserMenu)
  document.addEventListener('keydown', handleEscapeKey)
  cargarEmpresas()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutsideUserMenu)
  document.removeEventListener('keydown', handleEscapeKey)
})

function closeUserMenu(restoreFocus = false) {
  if (!showUserMenu.value) return
  showUserMenu.value = false
  if (restoreFocus) {
    setTimeout(() => userMenuButtonRef.value?.focus(), 0)
  }
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleEscapeKey(event: KeyboardEvent) {
  if (event.key === 'Escape' && showUserMenu.value) {
    closeUserMenu(true)
  }
}

function handleClickOutsideUserMenu(event: MouseEvent) {
  if (!userMenuRef.value) return
  if (!userMenuRef.value.contains(event.target as Node)) {
    closeUserMenu(true)
  }
}

function abrirModalCambiarContrasena() {
  closeUserMenu(false)
  showChangePasswordModal.value = true
}

function cerrarModalCambiarContrasena() {
  showChangePasswordModal.value = false
  setTimeout(() => userMenuButtonRef.value?.focus(), 0)
}

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
  error.value = ''; exito.value = ''; fieldErrors.value = {}; cargando.value = true;
  try {
    const response = await api.crearEmpresaConDueno(formData.value)
    if (response.data.exito) {
      exito.value = 'Empresa creada exitosamente'
      setTimeout(() => { cerrarFormulario(); cargarEmpresas() }, 1500)
    }
  } catch (err: unknown) {
    const apiError = err as ApiErrorAdmin
    const data = apiError.response?.data
    if (data?.errores) { fieldErrors.value = data.errores; error.value = 'Corrija los errores' }
    else if (data?.mensaje) error.value = data.mensaje
    else if (data?.error) error.value = data.error
    else error.value = 'Error al crear la empresa.'
  } finally { cargando.value = false }
}

async function cambiarEstado(id: number, activa: boolean) {
  try {
    if (activa) await api.activarEmpresa(id); else await api.desactivarEmpresa(id);
    cargarEmpresas()
  } catch { toastStore.showError('Error al cambiar el estado') }
}

function confirmarCambioEstado(empresa: EmpresaAdmin, activa: boolean) {
  empresaPendienteToggle.value = empresa; nuevoEstadoEmpresa.value = activa; showConfirmToggleEmpresa.value = true;
}
function cerrarConfirmToggleEmpresa() { showConfirmToggleEmpresa.value = false; empresaPendienteToggle.value = null; }
async function ejecutarCambioEstadoEmpresa() {
  if (!empresaPendienteToggle.value) return;
  try { cambiandoEstadoEmpresa.value = true; await cambiarEstado(empresaPendienteToggle.value.id, nuevoEstadoEmpresa.value); cerrarConfirmToggleEmpresa(); } finally { cambiandoEstadoEmpresa.value = false; }
}
function cerrarFormulario() {
  mostrarFormulario.value = false; error.value = ''; exito.value = ''; fieldErrors.value = {};
  formData.value = { dueno: { nombre: '', apellido: '', email: '', contrasena: '', telefono: '', documento: '', tipoDocumento: 'DNI' }, empresa: { nombre: '', slug: '', descripcion: '', cuit: '', direccion: '', ciudad: '', provincia: '', telefono: '', email: '', diasMaximosReserva: 30 } }
}
function handleLogout() {
  closeUserMenu(false)
  api.logout().then(() => { authStore.logout(); router.push('/login') }).catch(() => { authStore.logout(); router.push('/login') })
}
</script>

<style scoped>
/* Layout Base estilo Dashboard */
.admin-layout { min-height: 100vh; background-color: #f8fafc; display: flex; flex-direction: column; }

/* Navbar Superior */
.admin-navbar { background-color: #ffffff; border-bottom: 1px solid #e2e8f0; padding: 0 2rem; height: 70px; display: flex; justify-content: space-between; align-items: center; position: sticky; top: 0; z-index: 40; }
.navbar-brand { display: flex; align-items: center; gap: 1rem; }
.logo-icon { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); color: white; padding: 0.5rem; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.navbar-brand h1 { font-size: 1.25rem; font-weight: 700; color: #1e293b; margin: 0; letter-spacing: -0.5px; }
.navbar-user { display: flex; align-items: center; gap: 1.5rem; }
.user-profile { display: flex; align-items: center; gap: 0.75rem; }
.avatar { background-color: #e2e8f0; color: #475569; font-weight: 700; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; text-transform: uppercase; }
.user-details { display: flex; flex-direction: column; }
.user-name { font-size: 0.9rem; font-weight: 600; color: #1e293b; }
.user-role { font-size: 0.75rem; color: #64748b; }
.user-menu-trigger { background: transparent; border: 1px solid #e2e8f0; border-radius: 12px; padding: 0.35rem 0.6rem; cursor: pointer; display: inline-flex; align-items: center; gap: 0.6rem; transition: all 0.2s; }
.user-menu-trigger:hover { background-color: #f8fafc; border-color: #cbd5e1; }
.menu-caret { width: 16px; height: 16px; color: #64748b; }
.user-dropdown { position: absolute; top: calc(100% + 10px); right: 0; width: 230px; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 22px 36px -18px rgba(15, 23, 42, 0.35); overflow: hidden; z-index: 60; }
.dropdown-item { width: 100%; border: none; background: transparent; text-align: left; padding: 0.72rem 0.9rem; color: #334155; font-size: 0.88rem; font-weight: 600; cursor: pointer; transition: background-color 0.15s; display: flex; align-items: center; gap: 0.45rem; }
.dropdown-item:hover { background-color: #f8fafc; }
.dropdown-separator { height: 1px; background: #e2e8f0; margin: 0.15rem 0; }
.dropdown-item-danger { color: #dc2626; }
.dropdown-item-danger:hover { background-color: #fef2f2; }

/* Cuerpo Principal: Sidebar + Content */
.admin-body { display: flex; flex: 1; }

/* Sidebar Estilizado */
.admin-sidebar { width: 260px; background-color: #ffffff; border-right: 1px solid #e2e8f0; padding: 1.5rem 1rem; }
.nav-item { width: 100%; display: flex; align-items: center; gap: 0.75rem; padding: 0.875rem 1rem; background: transparent; border: none; border-radius: 8px; color: #64748b; font-size: 0.95rem; font-weight: 500; cursor: pointer; transition: all 0.2s; text-align: left; }
.nav-item:hover { background-color: #f1f5f9; color: #0f172a; }
.nav-item.active { background-color: #eff6ff; color: #2563eb; font-weight: 600; }

/* Contenido Principal */
.admin-content { flex: 1; padding: 2rem; overflow-y: auto; }
.content-wrapper { max-width: 1200px; margin: 0 auto; }

/* Header de la Sección */
.section-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; }
.section-header h2 { font-size: 1.5rem; font-weight: 700; color: #0f172a; margin: 0 0 0.25rem 0; }
.text-subtitle { color: #64748b; font-size: 0.95rem; margin: 0; }
.btn-primary { display: flex; align-items: center; gap: 0.5rem; background-color: #2563eb; color: white; border: none; padding: 0.75rem 1.25rem; border-radius: 8px; font-weight: 600; font-size: 0.95rem; cursor: pointer; transition: background-color 0.2s, transform 0.1s; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2); }
.btn-primary:hover { background-color: #1d4ed8; transform: translateY(-1px); }
.btn-primary:disabled { background-color: #94a3b8; cursor: not-allowed; box-shadow: none; transform: none; }

/* Grid de Empresas */
.empresas-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 1.5rem; }

/* Tarjeta de Empresa */
.empresa-card { background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 12px; display: flex; flex-direction: column; transition: box-shadow 0.2s, border-color 0.2s; }
.empresa-card:hover { box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 4px 6px -2px rgba(0, 0, 0, 0.025); border-color: #cbd5e1; }
.card-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: flex-start; }
.card-title { display: flex; gap: 1rem; align-items: center; }
.company-icon { width: 48px; height: 48px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; font-weight: 700; color: #475569; }
.card-title h3 { margin: 0 0 0.25rem 0; font-size: 1.1rem; font-weight: 700; color: #1e293b; }

.badge { display: inline-block; padding: 0.25rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
.badge-active { background-color: #dcfce7; color: #166534; }
.badge-inactive { background-color: #fee2e2; color: #991b1b; }

.card-body { padding: 1.25rem 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
.info-row { display: flex; align-items: flex-start; gap: 0.75rem; color: #64748b; }
.info-text { display: flex; flex-direction: column; line-height: 1.4; }
.info-text .label { font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; color: #94a3b8; }
.info-text .value { font-size: 0.95rem; color: #334155; font-weight: 500; }
.card-footer { padding: 1rem 1.5rem; background-color: #f8fafc; border-top: 1px solid #f1f5f9; border-radius: 0 0 12px 12px; }

.btn-action { width: 100%; padding: 0.6rem; border-radius: 6px; font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: background-color 0.2s; border: 1px solid transparent; }
.btn-danger { background-color: #ffffff; color: #ef4444; border-color: #fca5a5; }
.btn-danger:hover { background-color: #fef2f2; }
.btn-success { background-color: #ffffff; color: #10b981; border-color: #6ee7b7; }
.btn-success:hover { background-color: #ecfdf5; }

/* Estados Vacíos y de Carga */
.loading-state, .empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem 2rem; background-color: #ffffff; border-radius: 12px; border: 1px dashed #cbd5e1; color: #64748b; text-align: center; }
.empty-state h3 { margin: 1rem 0 0.5rem 0; color: #1e293b; font-size: 1.25rem; }
.spinner { animation: rotate 2s linear infinite; width: 32px; height: 32px; margin-bottom: 1rem;}
.spinner .path { stroke: #3b82f6; stroke-linecap: round; animation: dash 1.5s ease-in-out infinite; }

/* ========================================= */
/* RESPONSIVE (MÓVILES Y TABLETS)            */
/* ========================================= */
@media (max-width: 768px) {
  /* 1. Achicamos el Navbar y ocultamos textos innecesarios para ganar espacio */
  .admin-navbar { padding: 0 1rem; }
  .navbar-brand h1 { display: none; } /* Solo queda el loguito de AnSa */
  .user-details { display: none; } /* Solo queda el circulito con tus iniciales EA */
  
  /* 2. El layout ya no es de lado a lado, sino de arriba a abajo */
  .admin-body { flex-direction: column; }
  
  /* 3. El Sidebar lateral se convierte en una barra horizontal tipo "Tabs" arriba */
  .admin-sidebar { 
    width: 100%; 
    border-right: none; 
    border-bottom: 1px solid #e2e8f0; 
    padding: 0.5rem 1rem; 
    display: flex; 
    overflow-x: auto; /* Por si en el futuro agregás más pestañas, se hace scroll horizontal */
  }
  .nav-item { width: auto; white-space: nowrap; padding: 0.5rem 1rem; }

  /* 4. Contenido Principal */
  .admin-content { padding: 1rem; }
  
  .section-header { 
    flex-direction: column; 
    gap: 1rem; 
    align-items: flex-start; 
  }
  
  /* El botón de Nueva Empresa ocupa todo el ancho en celular */
  .btn-primary { width: 100%; justify-content: center; }

  /* 5. El Grid de Empresas pasa a ser de 1 sola columna */
  .empresas-grid { grid-template-columns: 1fr; }

  /* 6. El Formulario Modal */
  .modal-overlay { padding: 1rem; }
  .modal-content { padding: 1.5rem; }
  
  /* En lugar de 2 inputs a la par, se apilan de a 1 */
  .form-row { grid-template-columns: 1fr; gap: 0; }
  .form-actions { flex-direction: column-reverse; gap: 0.5rem; }
  .form-actions button { width: 100%; }
}

</style>