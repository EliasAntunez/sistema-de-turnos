<template>
  <div class="page-wrapper">
    <!-- Header de navegación -->
    <header class="top-bar">
      <button @click="router.push('/admin')" class="btn-back">
        ← Volver al panel
      </button>
      <h1 class="top-bar-title">Registrar Nueva Empresa</h1>
    </header>

    <main class="form-page">
      <!-- Panel de éxito -->
      <div v-if="exito" class="success-panel">
        <div class="success-icon">
          <svg class="h-12 w-12 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75m6 2.25a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
          </svg>
        </div>
        <h2>¡Empresa creada exitosamente!</h2>
        <p>Se creó la empresa <strong>{{ empresaCreada?.nombre }}</strong>.</p>
        <p>
          Las credenciales del dueño son:<br/>
          <strong>Email:</strong> {{ formData.dueno.email }}<br/>
          <strong>Contraseña:</strong> {{ formData.dueno.contrasena }}
        </p>
        <p v-if="formData.crearPerfilProfesional" class="note-profesional flex items-start gap-2">
          <svg class="h-5 w-5 text-indigo-600 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" />
          </svg>
          <span>También se creó su perfil de profesional. Podrá gestionar su propia agenda desde <strong>/profesional</strong>.</span>
        </p>
        <div class="success-actions">
          <button @click="resetForm" class="btn-secondary">Registrar otra empresa</button>
          <button @click="router.push('/admin')" class="btn-primary">Ir al panel</button>
        </div>
      </div>

      <!-- Formulario -->
      <form v-else @submit.prevent="submitForm" novalidate>
        <!-- Error global -->
        <div v-if="errorGeneral" class="error-banner">
          <span class="flex items-center gap-2">
            <svg class="h-5 w-5 text-rose-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0 3.75h.008v.008H12v-.008Zm8.25-.75a8.25 8.25 0 1 1-16.5 0 8.25 8.25 0 0 1 16.5 0Z" />
            </svg>
            {{ errorGeneral }}
          </span>
        </div>

        <!-- ── Sección Dueño ── -->
        <section class="form-section">
          <div class="section-header">
            <span class="section-icon">
              <svg class="h-6 w-6 text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6.75a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.118a7.5 7.5 0 0 1 15 0A17.933 17.933 0 0 1 12 21.75a17.933 17.933 0 0 1-7.5-1.632Z" />
              </svg>
            </span>
            <div>
              <h2>Datos del Dueño</h2>
              <p class="section-desc">El usuario que administrará esta empresa.</p>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group" :class="{ error: fieldErrors['dueno.nombre'] }">
              <label>Nombre <span class="req">*</span></label>
              <input v-model.trim="formData.dueno.nombre" autocomplete="given-name" placeholder="María" />
              <span class="field-error">{{ fieldErrors['dueno.nombre'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['dueno.apellido'] }">
              <label>Apellido <span class="req">*</span></label>
              <input v-model.trim="formData.dueno.apellido" autocomplete="family-name" placeholder="González" />
              <span class="field-error">{{ fieldErrors['dueno.apellido'] }}</span>
            </div>

            <div class="form-group" :class="{ error: fieldErrors['dueno.email'] }">
              <label>Email <span class="req">*</span></label>
              <input v-model.trim="formData.dueno.email" type="email" autocomplete="email" placeholder="maria@empresa.com" />
              <span class="field-error">{{ fieldErrors['dueno.email'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['dueno.telefono'] }">
              <label>Teléfono <span class="req">*</span></label>
              <input v-model.trim="formData.dueno.telefono" inputmode="tel" placeholder="3764000000" />
              <span class="field-hint">Solo dígitos, 10–15 caracteres</span>
              <span class="field-error">{{ fieldErrors['dueno.telefono'] }}</span>
            </div>

            <div class="form-group" :class="{ error: fieldErrors['dueno.contrasena'] }">
              <label>Contraseña <span class="req">*</span></label>
              <div class="input-icon-wrapper">
                <input
                  v-model="formData.dueno.contrasena"
                  :type="mostrarClave ? 'text' : 'password'"
                  autocomplete="new-password"
                  placeholder="Mínimo 8 caracteres"
                />
                <button type="button" class="toggle-password" @click="mostrarClave = !mostrarClave" tabindex="-1">
                  <svg v-if="mostrarClave" class="h-5 w-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c1.51 0 2.958-.318 4.27-.892M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.5a10.523 10.523 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m0 0a3 3 0 1 0 4.243 4.243m-4.242-4.242 4.242 4.242m0 0L21 21" />
                  </svg>
                  <svg v-else class="h-5 w-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.964-7.178Z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                  </svg>
                </button>
              </div>
              <span class="field-error">{{ fieldErrors['dueno.contrasena'] }}</span>
            </div>
            <div></div><!-- spacer grid -->

            <div class="form-group" :class="{ error: fieldErrors['dueno.tipoDocumento'] }">
              <label>Tipo de Documento <span class="req">*</span></label>
              <select v-model="formData.dueno.tipoDocumento">
                <option value="">— Seleccionar —</option>
                <option value="DNI">DNI</option>
                <option value="CUIT">CUIT</option>
                <option value="PASAPORTE">Pasaporte</option>
              </select>
              <span class="field-error">{{ fieldErrors['dueno.tipoDocumento'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['dueno.documento'] }">
              <label>Número de Documento <span class="req">*</span></label>
              <input v-model.trim="formData.dueno.documento" inputmode="numeric" placeholder="Solo dígitos (7–11)" />
              <span class="field-error">{{ fieldErrors['dueno.documento'] }}</span>
            </div>
          </div>
        </section>

        <!-- ── Sección Empresa ── -->
        <section class="form-section">
          <div class="section-header">
            <span class="section-icon">
              <svg class="h-6 w-6 text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 21h16.5M4.5 3h15a.75.75 0 0 1 .75.75v16.5h-16.5V3.75A.75.75 0 0 1 4.5 3Zm3 3.75h2.25V9H7.5V6.75Zm0 4.5h2.25v2.25H7.5v-2.25Zm0 4.5h2.25V18H7.5v-2.25Zm6-9h2.25V9H13.5V6.75Zm0 4.5h2.25v2.25H13.5v-2.25Zm0 4.5h2.25V18H13.5v-2.25Z" />
              </svg>
            </span>
            <div>
              <h2>Datos de la Empresa</h2>
              <p class="section-desc">Información pública del negocio.</p>
            </div>
          </div>

          <div class="form-grid">
            <div class="form-group col-span-2" :class="{ error: fieldErrors['empresa.nombre'] }">
              <label>Nombre de la Empresa <span class="req">*</span></label>
              <input
                v-model.trim="formData.empresa.nombre"
                @input="generarSlugDesdeNombre"
                placeholder="Peluquería Elegante"
              />
              <span class="field-error">{{ fieldErrors['empresa.nombre'] }}</span>
            </div>

            <div class="form-group col-span-2" :class="{ error: fieldErrors['empresa.slug'] }">
              <label>Slug (URL pública) <span class="req">*</span></label>
              <div class="slug-row">
                <span class="slug-prefix">/reservar/</span>
                <input
                  v-model.trim="formData.empresa.slug"
                  @input="slugEditadoManualmente = true"
                  placeholder="peluqueria-elegante"
                  pattern="[a-z0-9\-]+"
                  class="slug-input"
                />
                <button type="button" class="btn-regen" @click="forzarRegeneracion" title="Regenerar slug desde el nombre">
                  ↺
                </button>
              </div>
              <span class="field-hint">Solo minúsculas, números y guiones. Mínimo 3 caracteres.</span>
              <span class="field-error">{{ fieldErrors['empresa.slug'] }}</span>
            </div>

            <div class="form-group" :class="{ error: fieldErrors['empresa.cuit'] }">
              <label>CUIT <span class="req">*</span></label>
              <input v-model.trim="formData.empresa.cuit" inputmode="numeric" placeholder="20304050607" />
              <span class="field-hint">11 dígitos sin guiones</span>
              <span class="field-error">{{ fieldErrors['empresa.cuit'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['empresa.diasMaximosReserva'] }">
              <label>Días máximos para reservar</label>
              <input
                v-model.number="formData.empresa.diasMaximosReserva"
                type="number"
                min="1"
                max="365"
                placeholder="30"
              />
              <span class="field-hint">Horizonte de reservas (1–365 días)</span>
              <span class="field-error">{{ fieldErrors['empresa.diasMaximosReserva'] }}</span>
            </div>

            <div class="form-group" :class="{ error: fieldErrors['empresa.ciudad'] }">
              <label>Ciudad</label>
              <input v-model.trim="formData.empresa.ciudad" placeholder="Posadas" />
              <span class="field-error">{{ fieldErrors['empresa.ciudad'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['empresa.provincia'] }">
              <label>Provincia</label>
              <input v-model.trim="formData.empresa.provincia" placeholder="Misiones" />
              <span class="field-error">{{ fieldErrors['empresa.provincia'] }}</span>
            </div>

            <div class="form-group col-span-2" :class="{ error: fieldErrors['empresa.direccion'] }">
              <label>Dirección</label>
              <input v-model.trim="formData.empresa.direccion" placeholder="Av. Corrientes 1234" />
              <span class="field-error">{{ fieldErrors['empresa.direccion'] }}</span>
            </div>

            <div class="form-group" :class="{ error: fieldErrors['empresa.telefono'] }">
              <label>Teléfono de la empresa</label>
              <input v-model.trim="formData.empresa.telefono" inputmode="tel" placeholder="3764123456" />
              <span class="field-error">{{ fieldErrors['empresa.telefono'] }}</span>
            </div>
            <div class="form-group" :class="{ error: fieldErrors['empresa.email'] }">
              <label>Email de la empresa</label>
              <input v-model.trim="formData.empresa.email" type="email" placeholder="contacto@empresa.com" />
              <span class="field-error">{{ fieldErrors['empresa.email'] }}</span>
            </div>

            <div class="form-group col-span-2" :class="{ error: fieldErrors['empresa.descripcion'] }">
              <label>Descripción</label>
              <textarea
                v-model.trim="formData.empresa.descripcion"
                rows="3"
                :maxlength="500"
                placeholder="Breve descripción pública del negocio…"
              ></textarea>
              <span class="field-hint">{{ formData.empresa.descripcion?.length ?? 0 }}/500</span>
              <span class="field-error">{{ fieldErrors['empresa.descripcion'] }}</span>
            </div>
          </div>
        </section>

        <!-- ── Checkbox doble rol ── -->
        <section class="form-section rol-section">
          <label class="checkbox-card" :class="{ checked: formData.crearPerfilProfesional }">
            <input type="checkbox" v-model="formData.crearPerfilProfesional" />
            <div class="checkbox-body">
              <div class="checkbox-title">
                <span class="checkbox-icon">
                  <svg class="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" />
                  </svg>
                </span>
                El dueño también gestionará su propia agenda de turnos
              </div>
              <p class="checkbox-desc">
                Se le asignará el rol <strong>PROFESIONAL</strong> además de DUEÑO, y contará
                con un perfil de profesional en esta empresa. Podrá cargar su disponibilidad
                y recibir reservas directamente.
              </p>
            </div>
          </label>
        </section>

        <!-- Acciones -->
        <div class="form-actions">
          <button type="button" @click="router.push('/admin')" class="btn-secondary" :disabled="cargando">
            Cancelar
          </button>
          <button type="submit" class="btn-primary" :disabled="cargando">
            <span v-if="cargando" class="spinner"></span>
            {{ cargando ? 'Creando empresa…' : 'Crear Empresa' }}
          </button>
        </div>
      </form>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const router = useRouter()
const authStore = useAuthStore()

// ── Estado del formulario ───────────────────────────────────────────────────
const formData = reactive({
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
    slug: '',
    descripcion: '',
    cuit: '',
    direccion: '',
    ciudad: '',
    provincia: '',
    telefono: '',
    email: '',
    diasMaximosReserva: 30 as number | null
  },
  crearPerfilProfesional: false
})

const cargando = ref(false)
const errorGeneral = ref('')
const fieldErrors = ref<Record<string, string>>({})
const exito = ref(false)
const mostrarClave = ref(false)
const slugEditadoManualmente = ref(false)
const empresaCreada = ref<{ nombre: string } | null>(null)

// ── Generación automática de slug ──────────────────────────────────────────
function slugificar(texto: string): string {
  return texto
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // quitar tildes
    .replace(/[^a-z0-9\s-]/g, '')    // solo letras, números, espacios, guiones
    .trim()
    .replace(/\s+/g, '-')            // espacios → guiones
    .replace(/-+/g, '-')             // múltiples guiones → uno
    .slice(0, 100)
}

function generarSlugDesdeNombre() {
  if (!slugEditadoManualmente.value) {
    formData.empresa.slug = slugificar(formData.empresa.nombre)
  }
}

function forzarRegeneracion() {
  slugEditadoManualmente.value = false
  formData.empresa.slug = slugificar(formData.empresa.nombre)
}

// ── Validación del lado cliente ────────────────────────────────────────────
function validarFormulario(): boolean {
  const errors: Record<string, string> = {}
  const d = formData.dueno
  const e = formData.empresa

  if (!d.nombre.trim())    errors['dueno.nombre']    = 'El nombre es obligatorio'
  if (!d.apellido.trim())  errors['dueno.apellido']  = 'El apellido es obligatorio'
  if (!d.email.trim())     errors['dueno.email']     = 'El email es obligatorio'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(d.email)) errors['dueno.email'] = 'Formato de email inválido'
  if (!d.telefono.trim())  errors['dueno.telefono']  = 'El teléfono es obligatorio'
  else if (!/^\d{10,15}$/.test(d.telefono)) errors['dueno.telefono'] = 'Solo dígitos, 10–15 caracteres'
  if (!d.contrasena)       errors['dueno.contrasena'] = 'La contraseña es obligatoria'
  else if (d.contrasena.length < 8) errors['dueno.contrasena'] = 'Mínimo 8 caracteres'
  if (!d.tipoDocumento)    errors['dueno.tipoDocumento'] = 'Seleccioná un tipo de documento'
  if (!d.documento.trim()) errors['dueno.documento']  = 'El documento es obligatorio'
  else if (!/^\d{7,11}$/.test(d.documento)) errors['dueno.documento'] = 'Solo dígitos, 7–11 caracteres'

  if (!e.nombre.trim())    errors['empresa.nombre']  = 'El nombre de la empresa es obligatorio'
  if (!e.slug.trim())      errors['empresa.slug']    = 'El slug es obligatorio'
  else if (!/^[a-z0-9-]{3,100}$/.test(e.slug)) errors['empresa.slug'] = 'Solo minúsculas, números y guiones (3–100 caracteres)'
  if (!e.cuit.trim())      errors['empresa.cuit']    = 'El CUIT es obligatorio'
  else if (!/^\d{11}$/.test(e.cuit)) errors['empresa.cuit'] = '11 dígitos numéricos sin guiones'
  if (e.telefono && !/^\d{10,15}$/.test(e.telefono)) errors['empresa.telefono'] = 'Solo dígitos, 10–15 caracteres'
  if (e.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e.email)) errors['empresa.email'] = 'Formato de email inválido'
  if (e.diasMaximosReserva !== null && (e.diasMaximosReserva < 1 || e.diasMaximosReserva > 365)) {
    errors['empresa.diasMaximosReserva'] = 'Debe estar entre 1 y 365'
  }

  fieldErrors.value = errors
  return Object.keys(errors).length === 0
}

// ── Submit ─────────────────────────────────────────────────────────────────
async function submitForm() {
  errorGeneral.value = ''
  fieldErrors.value = {}

  if (!validarFormulario()) return

  cargando.value = true
  try {
    const response = await api.crearEmpresaConDueno({
      dueno: { ...formData.dueno },
      empresa: {
        ...formData.empresa,
        diasMaximosReserva: formData.empresa.diasMaximosReserva ?? 30
      },
      crearPerfilProfesional: formData.crearPerfilProfesional
    })

    if (response.data?.exito) {
      empresaCreada.value = { nombre: response.data.datos?.nombre ?? formData.empresa.nombre }
      exito.value = true
      // Refrescar sesión del admin para que cualquier cache de perfil esté fresco
      await authStore.cargarDesdeBackend()
    } else {
      errorGeneral.value = response.data?.mensaje ?? 'Error desconocido al crear la empresa'
    }
  } catch (err: unknown) {
    const apiError = err as { response?: { data?: { errores?: Record<string, string>; mensaje?: string } } }
    const data = apiError.response?.data
    if (data?.errores && typeof data.errores === 'object') {
      // Errores de validación por campo que devuelve el backend (@Valid)
      fieldErrors.value = data.errores as Record<string, string>
    } else {
      errorGeneral.value = data?.mensaje ?? 'Error de conexión. Verificá los datos e intentá nuevamente.'
    }
  } finally {
    cargando.value = false
  }
}

// ── Reset ──────────────────────────────────────────────────────────────────
function resetForm() {
  exito.value = false
  empresaCreada.value = null
  errorGeneral.value = ''
  fieldErrors.value = {}
  slugEditadoManualmente.value = false
  mostrarClave.value = false
  Object.assign(formData, {
    dueno: { nombre: '', apellido: '', email: '', contrasena: '', telefono: '', documento: '', tipoDocumento: 'DNI' },
    empresa: { nombre: '', slug: '', descripcion: '', cuit: '', direccion: '', ciudad: '', provincia: '', telefono: '', email: '', diasMaximosReserva: 30 },
    crearPerfilProfesional: false
  })
}
</script>

<style scoped>
/* ── Layout ── */
.page-wrapper {
  min-height: 100vh;
  background: #f8fafc;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.875rem 1.5rem;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.btn-back {
  background: none;
  border: 1px solid #cbd5e1;
  padding: 0.4rem 0.9rem;
  border-radius: 6px;
  cursor: pointer;
  color: #475569;
  font-size: 0.875rem;
  transition: all 0.2s;
}
.btn-back:hover { background: #f1f5f9; }

.top-bar-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.form-page {
  max-width: 860px;
  margin: 2rem auto;
  padding: 0 1rem 4rem;
}

/* ── Secciones ── */
.form-section {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.75rem;
  margin-bottom: 1.25rem;
}

.section-header {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f5f9;
}

.section-icon {
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.section-header h2 {
  margin: 0 0 0.25rem;
  font-size: 1.05rem;
  font-weight: 700;
  color: #1e293b;
}

.section-desc {
  margin: 0;
  font-size: 0.82rem;
  color: #64748b;
}

/* ── Grid de campos ── */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem 1.25rem;
}

@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
  .col-span-2 { grid-column: span 1; }
}

.col-span-2 { grid-column: span 2; }

/* ── Campos ── */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-group label {
  font-size: 0.82rem;
  font-weight: 600;
  color: #374151;
}

.req { color: #ef4444; }

.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.55rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  font-size: 0.9rem;
  color: #1e293b;
  background: white;
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}

.form-group.error input,
.form-group.error select,
.form-group.error textarea {
  border-color: #ef4444;
}

.field-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}

.field-error {
  font-size: 0.75rem;
  color: #ef4444;
  min-height: 1em;
}

/* ── Input con icono (contraseña) ── */
.input-icon-wrapper {
  position: relative;
  display: flex;
}

.input-icon-wrapper input {
  flex: 1;
  padding-right: 2.5rem;
}

.toggle-password {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

/* ── Slug row ── */
.slug-row {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  overflow: hidden;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.slug-row:focus-within {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
}

.slug-prefix {
  padding: 0.55rem 0.65rem;
  background: #f1f5f9;
  color: #64748b;
  font-size: 0.82rem;
  white-space: nowrap;
  border-right: 1px solid #cbd5e1;
}

.slug-input {
  flex: 1;
  border: none !important;
  box-shadow: none !important;
  border-radius: 0 !important;
}

.btn-regen {
  padding: 0.55rem 0.75rem;
  background: #f8fafc;
  border: none;
  border-left: 1px solid #cbd5e1;
  cursor: pointer;
  font-size: 1rem;
  color: #6366f1;
  transition: background 0.15s;
}
.btn-regen:hover { background: #e0e7ff; }

.form-group.error .slug-row { border-color: #ef4444; }

/* ── Checkbox de rol ── */
.rol-section { padding: 1.25rem 1.75rem; }

.checkbox-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.1rem 1.25rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
  user-select: none;
}

.checkbox-card:hover { border-color: #a5b4fc; background: #fafafa; }
.checkbox-card.checked { border-color: #6366f1; background: #eef2ff; }

.checkbox-card input[type="checkbox"] {
  margin-top: 0.2rem;
  width: 18px;
  height: 18px;
  accent-color: #6366f1;
  flex-shrink: 0;
  cursor: pointer;
}

.checkbox-body { flex: 1; }

.checkbox-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.checkbox-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.checkbox-desc {
  margin: 0;
  font-size: 0.82rem;
  color: #475569;
  line-height: 1.5;
}

/* ── Acciones ── */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding-top: 0.5rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: white;
  border: none;
  padding: 0.65rem 1.75rem;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(99,102,241,0.35); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-secondary {
  background: white;
  color: #475569;
  border: 1px solid #cbd5e1;
  padding: 0.65rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-secondary:hover:not(:disabled) { background: #f1f5f9; }
.btn-secondary:disabled { opacity: 0.6; cursor: not-allowed; }

/* ── Spinner ── */
.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Banner de error ── */
.error-banner {
  background: #fef2f2;
  border: 1px solid #fca5a5;
  color: #b91c1c;
  padding: 0.85rem 1rem;
  border-radius: 8px;
  margin-bottom: 1.25rem;
  font-size: 0.9rem;
}

/* ── Panel de éxito ── */
.success-panel {
  background: white;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
  padding: 2.5rem;
  text-align: center;
}

.success-icon {
  margin-bottom: 0.75rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.success-panel h2 {
  font-size: 1.4rem;
  font-weight: 800;
  color: #166534;
  margin: 0 0 0.75rem;
}

.success-panel p {
  color: #334155;
  line-height: 1.6;
  margin: 0.5rem 0;
}

.note-profesional {
  background: #eef2ff;
  border: 1px solid #c7d2fe;
  border-radius: 8px;
  padding: 0.85rem 1rem;
  margin: 1rem 0;
  color: #3730a3;
  font-size: 0.88rem;
  text-align: left;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-top: 1.5rem;
}
</style>
