<template>
  <div class="dueno-container">
    <!-- Header -->
    <header class="dueno-header">
      <!-- Mostrar el nombre de la empresa del dueño -->
      <h1>{{ nombreEmpresa }}</h1>
      <div class="user-info" ref="userMenuRef">
        <button
          v-if="authStore.isProfesional"
          @click="router.push('/profesional')"
          class="btn-switch-rol"
          title="Ir a mi agenda de profesional"
        >
          📅 Mi Agenda
        </button>

        <button class="user-menu-trigger" @click.stop="toggleUserMenu">
          <span>{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
          <span class="user-menu-arrow">▾</span>
        </button>

        <div v-if="showUserMenu" class="user-menu-dropdown" @click.stop>
          <button class="user-menu-item" @click="abrirModalEditarEmpresa">Editar Empresa</button>
          <button class="user-menu-item user-menu-item-danger" @click="handleLogout">Cerrar Sesión</button>
        </div>
      </div>
    </header>

    <!-- Tabs -->
    <div class="tabs">
      <button 
        :class="['tab', { active: activeTab === 'profesionales' }]" 
        @click="activeTab = 'profesionales'"
      >
        Profesionales
      </button>
      <button 
        :class="['tab', { active: activeTab === 'servicios' }]" 
        @click="activeTab = 'servicios'"
      >
        Servicios
      </button>
      <button 
        :class="['tab', { active: activeTab === 'horarios' }]" 
        @click="activeTab = 'horarios'"
      >
        Horarios de la Empresa
      </button>
      <button 
        :class="['tab', { active: activeTab === 'politicas' }]" 
        @click="activeTab = 'politicas'"
      >
        Políticas de Cancelación
      </button>
      <button 
        :class="['tab', { active: activeTab === 'configuracion' }]" 
        @click="activeTab = 'configuracion'"
      >
        Configuración
      </button>
    </div>

    <!-- Tab: Profesionales -->
    <main v-if="activeTab === 'profesionales'" class="dueno-content">
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
            <span :class="['badge-status', profesional.activo ? 'activo' : 'inactivo']">
              {{ profesional.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="card-body">
            <div class="info-item">
              <strong>Email:</strong> {{ profesional.email }}
            </div>
            <div class="info-item">
              <strong>Teléfono:</strong> {{ profesional.telefono || 'No especificado' }}
            </div>
            <div class="info-item" v-if="profesional.descripcion">
              <strong>Descripción:</strong> {{ profesional.descripcion }}
            </div>
          </div>
          <div class="card-actions">
            <button @click="openModal(profesional)" class="btn-edit">Editar</button>
            <button 
              @click="confirmarToggleProfesional(profesional)" 
              :class="profesional.activo ? 'btn-delete' : 'btn-activate'"
            >
              {{ profesional.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay profesionales registrados</p>
        <p class="empty-hint">Haz clic en "Agregar Profesional" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Servicios -->
    <main v-if="activeTab === 'servicios'" class="dueno-content">
      <div class="section-header">
        <h2>Servicios de la Empresa</h2>
        <button @click="openModalServicio()" class="btn-add">+ Agregar Servicio</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingServicios" class="loading">Cargando...</div>

      <!-- Servicios Grid -->
      <div v-else-if="servicios.length > 0" class="profesionales-grid">
        <div v-for="servicio in servicios" :key="servicio.id" class="profesional-card">
          <div class="card-header">
            <h3>{{ servicio.nombre }}</h3>
            <span :class="['badge-status', servicio.activo ? 'activo' : 'inactivo']">
              {{ servicio.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="card-body">
            <div class="info-item" v-if="servicio.descripcion">
              <strong>Descripción:</strong> {{ servicio.descripcion }}
            </div>
            <div class="info-item">
              <strong>Duración:</strong> {{ servicio.duracionMinutos }} minutos
            </div>
            <div class="info-item">
              <strong>Precio:</strong> {{ formatearMonedaARS(servicio.precio) }}
            </div>
            <div class="info-item">
              <strong>Requiere seña:</strong> {{ servicio.requiereSena ? 'Sí' : 'No' }}
            </div>
            <div v-if="servicio.requiereSena && servicio.montoSena !== null" class="info-item">
              <strong>Monto seña:</strong> {{ formatearMonedaARS(servicio.montoSena) }}
            </div>
          </div>
          <div class="card-actions">
            <button @click="openModalServicio(servicio)" class="btn-edit">Editar</button>
            <button 
              @click="confirmarToggleServicioActivo(servicio)" 
              :class="servicio.activo ? 'btn-delete' : 'btn-activate'"
            >
              {{ servicio.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay servicios registrados</p>
        <p class="empty-hint">Haz clic en "Agregar Servicio" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Horarios de la Empresa -->
    <main v-if="activeTab === 'horarios'" class="dueno-content">
      <div class="section-header">
        <h2>Horarios de Atención</h2>
        <button @click="openModalHorario()" class="btn-add">+ Agregar Horario</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingHorarios" class="loading">Cargando...</div>

      <!-- Horarios por Día -->
      <div v-else-if="horariosAgrupados && Object.keys(horariosAgrupados).length > 0" class="horarios-container">
        <div v-for="(dia, index) in diasSemana" :key="dia" class="horario-dia-card">
          <div class="dia-header">
            <div class="dia-header-left">
              <h3>{{ nombresDias[dia] }}</h3>
              <span v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0" class="count-badge">
                {{ horariosAgrupados[dia].length }} {{ horariosAgrupados[dia].length === 1 ? 'horario' : 'horarios' }}
              </span>
            </div>
            <button 
              v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0"
              @click="abrirModalCopiar(dia)" 
              class="btn-copiar-horarios"
              title="Copiar a otros días">
              📋 Copiar
            </button>
          </div>
          <div class="dia-body">
            <div v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0" class="horarios-list">
              <div v-for="horario in horariosAgrupados[dia]" :key="horario.id" class="horario-item">
                <div class="horario-info">
                  <span class="horario-time">{{ horario.horaInicio }} - {{ horario.horaFin }}</span>
                </div>
                <div class="horario-actions">
                  <button @click="openModalHorario(horario)" class="btn-edit-small">✏️</button>
                  <button @click="confirmarEliminarHorario(horario)" class="btn-delete-small">🗑️</button>
                </div>
              </div>
            </div>
            <div v-else class="no-horarios">
              <span>Sin horarios configurados</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay horarios configurados</p>
        <p class="empty-hint">Configura los horarios de atención de tu empresa por día de la semana</p>
      </div>
    </main>

    <!-- Tab: Políticas de Cancelación -->
<main v-if="activeTab === 'politicas'" class="dueno-content">
  <div class="section-header">
    <h2>Políticas de Cancelación e Inasistencias</h2>
    <button class="btn-add" @click="openModalPolitica()">+ Nueva Política</button>
  </div>
  <!-- Loading State -->
  <div v-if="loadingPoliticas" class="loading">Cargando...</div>

  <!-- Políticas Grid -->
  <div v-else-if="politicas.length > 0" class="profesionales-grid">
     <div v-for="politica in politicas.filter(p => p.empresaId === empresaId)" :key="politica.id" class="profesional-card">
      <div class="card-header">
        <h3>{{ politica.tipo === 'CANCELACION' ? 'Cancelación' : (politica.tipo === 'INASISTENCIA' ? 'Inasistencia' : 'Ambos') }}</h3>
        <span :class="['badge-status', politica.activa ? 'activo' : 'inactivo']">
          {{ politica.activa ? 'Activa' : 'Inactiva' }}
        </span>
      </div>
      <div class="card-body">
        <div class="info-item card-descripcion" :title="politica.descripcion">
          <strong>Descripción:</strong>
          <span class="truncate-text">{{ politica.descripcion }}</span>
        </div>
        <div class="info-item">
          <strong>Horas límite:</strong> {{ politica.horasLimiteCancelacion }} horas
        </div>
        <div class="info-item" v-if="politica.fechaCreacion">
          <strong>Creada:</strong> {{ formatearFecha(politica.fechaCreacion) }}
        </div>
      </div>
      <div class="card-actions">
        <button 
          @click="confirmarTogglePoliticaActiva(politica)" 
          :class="politica.activa ? 'btn-delete' : 'btn-activate'"
        >
          {{ politica.activa ? 'Desactivar' : 'Activar' }}
        </button>
        <button 
          @click="openModalPolitica(politica)" 
          class="btn-edit"
        >
          Editar
        </button>
      </div>
    </div>
  </div>

  <!-- Empty State -->
  <div v-else class="empty-state">
    <p>No hay políticas registradas</p>
    <p class="empty-hint">Haz clic en "Nueva Política" para comenzar</p>
  </div>
</main>

    <!-- Tab: Configuración -->
    <main v-if="activeTab === 'configuracion'" class="dueno-content">
      <div class="section-header">
        <h2>Configuración de la Empresa</h2>
      </div>

      <!-- Loading State -->
      <div v-if="loadingConfiguracion" class="loading">Cargando configuración...</div>

      <!-- Formulario de Configuración -->
      <div v-else class="configuracion-container">
        <form @submit.prevent="submitConfiguracion" class="configuracion-form">
          
          <!-- Sección: Configuración Operativa -->
          <div class="config-section">
            <h3 class="section-title">⚙️ Configuración Operativa</h3>
            
            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrorsConfiguracion.bufferPorDefecto }">
                <label>Buffer por defecto (minutos)</label>
                <input 
                  v-model.number="formDataConfiguracion.bufferPorDefecto" 
                  type="number" 
                  min="0"
                  max="120"
                  required
                  placeholder="5"
                />
                <small>Tiempo de descanso entre turnos consecutivos</small>
                <span v-if="fieldErrorsConfiguracion.bufferPorDefecto" class="field-error">
                  {{ fieldErrorsConfiguracion.bufferPorDefecto }}
                </span>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos }">
                <label>Anticipación mínima (minutos)</label>
                <input 
                  v-model.number="formDataConfiguracion.tiempoMinimoAnticipacionMinutos" 
                  type="number" 
                  min="0"
                  max="1440"
                  required
                  placeholder="30"
                />
                <small>Tiempo mínimo para reservar antes del turno</small>
                <span v-if="fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos" class="field-error">
                  {{ fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos }}
                </span>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsConfiguracion.diasMaximosReserva }">
                <label>Días máximos de reserva</label>
                <input 
                  v-model.number="formDataConfiguracion.diasMaximosReserva" 
                  type="number" 
                  min="1"
                  max="365"
                  required
                  placeholder="30"
                />
                <small>Hasta cuántos días adelante pueden reservar</small>
                <span v-if="fieldErrorsConfiguracion.diasMaximosReserva" class="field-error">
                  {{ fieldErrorsConfiguracion.diasMaximosReserva }}
                </span>
              </div>
            </div>
          </div>

          <!-- Sección: Recordatorios -->
          <div class="config-section">
            <h3 class="section-title">📧 Recordatorios por Email</h3>
            
            <div class="form-row">
              <div class="form-group" :class="{ 'has-error': fieldErrorsConfiguracion.horasAntesRecordatorio }">
                <label>Enviar recordatorio con (horas de anticipación)</label>
                <input 
                  v-model.number="formDataConfiguracion.horasAntesRecordatorio" 
                  type="number" 
                  min="1"
                  max="168"
                  required
                  placeholder="24"
                />
                <small>1 hora mínimo, 168 horas (7 días) máximo</small>
                <span v-if="fieldErrorsConfiguracion.horasAntesRecordatorio" class="field-error">
                  {{ fieldErrorsConfiguracion.horasAntesRecordatorio }}
                </span>
              </div>

              <div class="form-group form-group-switch">
                <label class="switch-label">
                  <input 
                    v-model="formDataConfiguracion.enviarRecordatorios" 
                    type="checkbox"
                    class="switch-input"
                  />
                  <span class="switch-slider"></span>
                  <span class="switch-text">
                    {{ formDataConfiguracion.enviarRecordatorios ? 'Recordatorios Activos' : 'Recordatorios Desactivados' }}
                  </span>
                </label>
                <small>Habilita o deshabilita el envío de recordatorios automáticos</small>
              </div>
            </div>
          </div>

          <div class="config-section">
            <h3 class="section-title">💳 Datos para Señas por Transferencia</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Datos bancarios (opcional)</label>
                <textarea
                  v-model="formDataConfiguracion.datosBancarios"
                  rows="4"
                  placeholder="Ej: Alias: mi.negocio.cobros\nCBU: 0000003100012345678901\nTitular: Mi Empresa SRL"
                ></textarea>
                <small>Este texto se mostrará al cliente en Mis Turnos cuando tenga una reserva en pendiente de pago.</small>
              </div>
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="errorConfiguracion" class="error-message">{{ errorConfiguracion }}</div>

          <!-- Botones de Acción -->
          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-submit-config" 
              :disabled="submittingConfiguracion"
            >
              {{ submittingConfiguracion ? 'Guardando...' : '💾 Guardar Configuración' }}
            </button>
          </div>
        </form>
      </div>
    </main>

        <!-- Modal Form Empresa -->
        <div v-if="showModalEmpresa" class="modal-overlay" @click="cerrarModalEditarEmpresa">
          <div class="modal" @click.stop>
            <div class="modal-header">
              <h2>Editar Empresa</h2>
              <button @click="cerrarModalEditarEmpresa" class="btn-close">&times;</button>
            </div>
            <form @submit.prevent="submitFormEmpresa" class="modal-form">
              <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.nombre }">
                <label>Nombre *</label>
                <input
                  v-model="formDataEmpresa.nombre"
                  type="text"
                  required
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Nombre de la empresa"
                />
                <span v-if="fieldErrorsEmpresa.nombre" class="field-error">{{ fieldErrorsEmpresa.nombre }}</span>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.email }">
                <label>Email</label>
                <input
                  v-model="formDataEmpresa.email"
                  type="email"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="contacto@empresa.com"
                />
                <span v-if="fieldErrorsEmpresa.email" class="field-error">{{ fieldErrorsEmpresa.email }}</span>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.telefono }">
                <label>Teléfono</label>
                <input
                  v-model="formDataEmpresa.telefono"
                  type="tel"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Solo números (10-15 dígitos)"
                />
                <span v-if="fieldErrorsEmpresa.telefono" class="field-error">{{ fieldErrorsEmpresa.telefono }}</span>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.direccion }">
                <label>Dirección</label>
                <input
                  v-model="formDataEmpresa.direccion"
                  type="text"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Dirección comercial"
                />
                <span v-if="fieldErrorsEmpresa.direccion" class="field-error">{{ fieldErrorsEmpresa.direccion }}</span>
              </div>

              <div class="form-row">
                <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.ciudad }">
                  <label>Ciudad</label>
                  <input
                    v-model="formDataEmpresa.ciudad"
                    type="text"
                    class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Ciudad"
                  />
                  <span v-if="fieldErrorsEmpresa.ciudad" class="field-error">{{ fieldErrorsEmpresa.ciudad }}</span>
                </div>

                <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.provincia }">
                  <label>Provincia</label>
                  <input
                    v-model="formDataEmpresa.provincia"
                    type="text"
                    class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Provincia"
                  />
                  <span v-if="fieldErrorsEmpresa.provincia" class="field-error">{{ fieldErrorsEmpresa.provincia }}</span>
                </div>
              </div>

              <div class="form-group" :class="{ 'has-error': fieldErrorsEmpresa.descripcion }">
                <label>Descripción</label>
                <textarea
                  v-model="formDataEmpresa.descripcion"
                  rows="3"
                  class="w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Descripción breve de la empresa"
                ></textarea>
                <span v-if="fieldErrorsEmpresa.descripcion" class="field-error">{{ fieldErrorsEmpresa.descripcion }}</span>
              </div>

              <div v-if="errorEmpresa" class="error-message">{{ errorEmpresa }}</div>

              <div class="modal-actions">
                <button type="button" @click="cerrarModalEditarEmpresa" class="btn-cancel">Cancelar</button>
                <button type="submit" class="btn-submit" :disabled="!canSubmitEmpresa">
                  {{ submittingEmpresa ? 'Guardando...' : 'Guardar' }}
                </button>
              </div>
            </form>
          </div>
        </div>

<!-- Modal Form Políticas -->
<div v-if="showModalPolitica" class="modal-overlay" @click="closeModalPolitica">
  <div class="modal" @click.stop>
    <div class="modal-header">
      <h2>Nueva Política de Cancelación</h2>
      <button @click="closeModalPolitica" class="btn-close">&times;</button>
    </div>
    <form @submit.prevent="submitFormPolitica" class="modal-form">
      <div class="form-group" :class="{ 'has-error': fieldErrorsPolitica.descripcion }">
        <label>Descripción *</label>
        <textarea 
          v-model="formDataPolitica.descripcion" 
          required 
          rows="3"
          placeholder="Descripción de la política de cancelación"
        ></textarea>
        <span v-if="fieldErrorsPolitica.descripcion" class="field-error">{{ fieldErrorsPolitica.descripcion }}</span>
      </div>
      <div class="form-row">
        <div
          v-if="formDataPolitica.tipo !== 'INASISTENCIA'"
          class="form-group"
          :class="{ 'has-error': fieldErrorsPolitica.horasLimiteCancelacion }"
        >
          <label>Horas límite para cancelar *</label>
          <input 
            v-model.number="formDataPolitica.horasLimiteCancelacion" 
            type="number" 
            min="1" 
            placeholder="24"
          />
          <span v-if="fieldErrorsPolitica.horasLimiteCancelacion" class="field-error">{{ fieldErrorsPolitica.horasLimiteCancelacion }}</span>
        </div>

        <div class="form-group" :class="{ 'has-error': fieldErrorsPolitica.tipo }">
          <label>Tipo de política *</label>
          <select v-model="formDataPolitica.tipo" required>
            <option value="CANCELACION">Cancelación</option>
            <option value="INASISTENCIA">Inasistencia</option>
            <option value="AMBOS">Ambos</option>
          </select>
          <span v-if="fieldErrorsPolitica.tipo" class="field-error">{{ fieldErrorsPolitica.tipo }}</span>
        </div>
      </div>

      <div v-if="errorPolitica" class="error-message">{{ errorPolitica }}</div>

      <div class="modal-actions">
        <button type="button" @click="closeModalPolitica" class="btn-cancel">Cancelar</button>
        <button type="submit" class="btn-submit" :disabled="submittingPolitica">
          {{ submittingPolitica ? 'Guardando...' : (editingPolitica ? 'Actualizar' : 'Crear') }}
        </button>
      </div>
    </form>
  </div>
</div>
    
    <!-- Modal Form Profesionales -->
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

          <div class="form-group" :class="{ 'has-error': fieldErrors.descripcion }">
            <label>Descripción</label>
            <textarea 
              v-model="formData.descripcion" 
              rows="3"
              placeholder="Descripción adicional del profesional"
            ></textarea>
            <span v-if="fieldErrors.descripcion" class="field-error">{{ fieldErrors.descripcion }}</span>
          </div>

          <!-- Gestión de Servicios (solo al editar) -->
          <div v-if="editingProfesional" class="form-group">
            <label>Servicios Disponibles</label>
            <button 
              type="button" 
              @click="cargarServiciosProfesional" 
              class="btn-secondary"
              :disabled="cargandoServicios"
            >
              {{ cargandoServicios ? 'Cargando...' : 'Gestionar Servicios' }}
            </button>
            
            <div v-if="serviciosProfesional.length > 0" class="servicios-profesional-list">
              <div 
                v-for="servicio in serviciosProfesional" 
                :key="servicio.servicioId" 
                class="servicio-item"
                :class="{ 'desactivado': !servicio.disponible }"
              >
                <div class="servicio-info">
                  <strong>{{ servicio.nombre }}</strong>
                  <span class="servicio-meta">{{ servicio.duracionMinutos }} min - {{ formatearMonedaARS(servicio.precio) }}</span>
                </div>
                <button 
                  type="button"
                  @click="confirmarToggleServicioProfesional(servicio)"
                  :class="['btn-toggle', servicio.disponible ? 'activo' : 'inactivo']"
                  :disabled="submittingToggle"
                >
                  {{ servicio.disponible ? (submittingToggle ? 'Guardando...' : 'Desactivar') : (submittingToggle ? 'Guardando...' : 'Activar') }}
                </button>
              </div>
            </div>
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

    <!-- Modal Form Servicios -->
    <div v-if="showModalServicio" class="modal-overlay" @click="closeModalServicio">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ editingServicio ? 'Editar' : 'Nuevo' }} Servicio</h2>
          <button @click="closeModalServicio" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormServicio" class="modal-form">
          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.nombre }">
            <label>Nombre del Servicio *</label>
            <input 
              v-model="formDataServicio.nombre" 
              type="text" 
              required
              placeholder="Ej: Corte de cabello"
            />
            <span v-if="fieldErrorsServicio.nombre" class="field-error">{{ fieldErrorsServicio.nombre }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.descripcion }">
            <label>Descripción</label>
            <textarea 
              v-model="formDataServicio.descripcion" 
              rows="3"
              placeholder="Descripción del servicio"
            ></textarea>
            <span v-if="fieldErrorsServicio.descripcion" class="field-error">{{ fieldErrorsServicio.descripcion }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.duracionMinutos }">
              <label>Duración (minutos) *</label>
              <input 
                v-model.number="formDataServicio.duracionMinutos" 
                type="number" 
                required
                min="1"
                placeholder="45"
              />
              <span v-if="fieldErrorsServicio.duracionMinutos" class="field-error">{{ fieldErrorsServicio.duracionMinutos }}</span>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.precio }">
              <label>Precio ($) *</label>
              <input 
                v-model.number="formDataServicio.precio" 
                type="number" 
                required
                min="0"
                step="0.01"
                placeholder="5000"
              />
              <span v-if="fieldErrorsServicio.precio" class="field-error">{{ fieldErrorsServicio.precio }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.bufferMinutos }">
            <label>Buffer (minutos)</label>
            <input 
              v-model.number="formDataServicio.bufferMinutos" 
              type="number" 
              min="0"
              placeholder="Tiempo de preparación después del servicio (opcional)"
            />
            <small class="field-hint">Si no se especifica, se usará el buffer del profesional o de la empresa</small>
            <span v-if="fieldErrorsServicio.bufferMinutos" class="field-error">{{ fieldErrorsServicio.bufferMinutos }}</span>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                v-model="formDataServicio.requiereSena"
                type="checkbox"
              />
              <span>Requiere seña para confirmar la reserva</span>
            </label>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.montoSena }">
            <label>Monto de seña ($)</label>
            <input
              v-model.number="formDataServicio.montoSena"
              type="number"
              min="0"
              step="0.01"
              :disabled="!formDataServicio.requiereSena"
              placeholder="Ej: 3000"
            />
            <small class="field-hint">Solo se habilita si el servicio requiere seña</small>
            <span v-if="fieldErrorsServicio.montoSena" class="field-error">{{ fieldErrorsServicio.montoSena }}</span>
          </div>

          <div v-if="errorServicio" class="error-message">{{ errorServicio }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeModalServicio" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingServicio">
              {{ submittingServicio ? 'Guardando...' : (editingServicio ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Horarios -->
    <div v-if="showModalHorario" class="modal-overlay" @click="closeModalHorario">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>{{ editingHorario ? 'Editar' : 'Nuevo' }} Horario</h2>
          <button @click="closeModalHorario" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormHorario" class="modal-form">
          <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.diaSemana }">
            <label>Día de la Semana *</label>
            <select v-model="formDataHorario.diaSemana" required>
              <option value="">Seleccione un día</option>
              <option v-for="dia in diasSemana" :key="dia" :value="dia">
                {{ nombresDias[dia] }}
              </option>
            </select>
            <span v-if="fieldErrorsHorario.diaSemana" class="field-error">{{ fieldErrorsHorario.diaSemana }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.horaInicio }">
              <label>Hora Inicio *</label>
              <input 
                v-model="formDataHorario.horaInicio" 
                type="time" 
                required
              />
              <span v-if="fieldErrorsHorario.horaInicio" class="field-error">{{ fieldErrorsHorario.horaInicio }}</span>
            </div>
            <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.horaFin }">
              <label>Hora Fin *</label>
              <input 
                v-model="formDataHorario.horaFin" 
                type="time" 
                required
              />
              <span v-if="fieldErrorsHorario.horaFin" class="field-error">{{ fieldErrorsHorario.horaFin }}</span>
            </div>
          </div>

          <div v-if="errorHorario" class="error-message">
            <p>{{ errorHorario }}</p>
            <ul v-if="horarioConflictoDetalles.length" class="lista-bloqueantes" style="margin-top:0.5rem;">
              <li v-for="item in horarioConflictoDetalles" :key="item">{{ item }}</li>
            </ul>
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeModalHorario" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingHorario">
              {{ submittingHorario ? 'Guardando...' : (editingHorario ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Copiar Horarios -->
    <div v-if="showModalCopiar" class="modal-overlay" @click="cerrarModalCopiar">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>Copiar Horarios de {{ nombresDias[diaFuenteCopia] }}</h2>
          <button @click="cerrarModalCopiar" class="btn-close">&times;</button>
        </div>
        <div class="modal-form">
          <div class="form-group">
            <label>Seleccione los días destino:</label>
            <div class="checkbox-group">
              <label v-for="dia in diasSemana" :key="dia" class="checkbox-label">
                <input 
                  type="checkbox" 
                  :value="dia"
                  v-model="diasDestinoSeleccionados"
                  :disabled="dia === diaFuenteCopia"
                />
                <span :class="{ 'disabled-day': dia === diaFuenteCopia }">
                  {{ nombresDias[dia] }}
                  <span v-if="dia === diaFuenteCopia" class="badge-fuente">(fuente)</span>
                </span>
              </label>
            </div>
          </div>

          <div v-if="diasConConflicto.length > 0" class="warning-message">
            ⚠️ Los siguientes días ya tienen horarios configurados y serán reemplazados:
            <strong>{{ diasConConflicto.map(d => nombresDias[d]).join(', ') }}</strong>
          </div>

          <div v-if="errorCopiar" class="error-message">{{ errorCopiar }}</div>

          <div class="modal-actions">
            <button type="button" @click="cerrarModalCopiar" class="btn-cancel">Cancelar</button>
            <button 
              type="button" 
              @click="confirmarCopiarHorarios" 
              class="btn-submit" 
              :disabled="submittingCopiar || diasDestinoSeleccionados.length === 0">
              {{ submittingCopiar ? 'Copiando...' : (diasConConflicto.length > 0 ? 'Reemplazar y Copiar' : 'Copiar Horarios') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <ConfirmModal
    :show="showConfirmDeleteHorario"
    titulo="Eliminar Horario"
    :mensaje="`¿Estás seguro de eliminar el horario de ${nombresDias[horarioPendienteEliminar?.diaSemana]} (${horarioPendienteEliminar?.horaInicio} - ${horarioPendienteEliminar?.horaFin})?\nEsta acción no se puede deshacer.`"
    textoConfirmar="Eliminar"
    colorBoton="bg-red-600 hover:bg-red-700"
    @confirm="ejecutarEliminarHorario"
    @cancel="cerrarConfirmDeleteHorario"
  />

  <ConfirmModal
    :show="showConfirmToggleProfesional"
    :titulo="`${profesionalPendienteToggle?.activo ? 'Desactivar' : 'Activar'} Profesional`"
    :mensaje="`¿Estás seguro de ${profesionalPendienteToggle?.activo ? 'desactivar' : 'activar'} a ${profesionalPendienteToggle?.nombre} ${profesionalPendienteToggle?.apellido}?`"
    :textoConfirmar="profesionalPendienteToggle?.activo ? 'Desactivar' : 'Activar'"
    :colorBoton="profesionalPendienteToggle?.activo ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'"
    @confirm="ejecutarToggleProfesional"
    @cancel="cerrarConfirmToggleProfesional"
  />

  <ConfirmModal
    :show="showConfirmDeletePolitica"
    titulo="Eliminar Política"
    mensaje="¿Estás seguro de eliminar esta política? Esta acción no se puede deshacer."
    textoConfirmar="Eliminar"
    colorBoton="bg-red-600 hover:bg-red-700"
    @confirm="ejecutarEliminarPolitica"
    @cancel="cerrarConfirmDeletePolitica"
  />

  <ConfirmModal
    :show="showConfirmToggleServicio"
    :titulo="`${servicioPendienteToggle?.activo ? 'Desactivar' : 'Activar'} Servicio`"
    :mensaje="servicioPendienteToggle?.activo ? `¿Estás seguro de que deseas desactivar el servicio ${servicioPendienteToggle?.nombre}? Dejará de estar disponible para nuevas reservas.` : `¿Estás seguro de que deseas activar el servicio ${servicioPendienteToggle?.nombre}?`"
    :textoConfirmar="submittingToggleServicio ? 'Guardando...' : (servicioPendienteToggle?.activo ? 'Desactivar' : 'Activar')"
    :colorBoton="servicioPendienteToggle?.activo ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'"
    @confirm="ejecutarToggleServicioActivo"
    @cancel="cerrarConfirmToggleServicio"
  />

  <ConfirmModal
    :show="showConfirmTogglePolitica"
    :titulo="`${politicaPendienteToggle?.activa ? 'Desactivar' : 'Activar'} Política`"
    :mensaje="politicaPendienteToggle?.activa ? '¿Estás seguro de que deseas desactivar esta política de cancelación? Dejará de aplicarse a los nuevos turnos.' : '¿Estás seguro de que deseas activar esta política de cancelación? Comenzará a aplicarse a los nuevos turnos.'"
    :textoConfirmar="submittingTogglePolitica ? 'Guardando...' : (politicaPendienteToggle?.activa ? 'Desactivar' : 'Activar')"
    :colorBoton="politicaPendienteToggle?.activa ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'"
    @confirm="ejecutarTogglePoliticaActiva"
    @cancel="cerrarConfirmTogglePolitica"
  />

  <ConfirmModal
    :show="showConfirmToggleServicioProfesional"
    :titulo="`${servicioProfesionalPendienteToggle?.disponible ? 'Desactivar' : 'Activar'} Servicio en Agenda`"
    :mensaje="`¿Confirmas ${servicioProfesionalPendienteToggle?.disponible ? 'desactivar' : 'activar'} ${servicioProfesionalPendienteToggle?.nombre} para ${editingProfesional?.nombre} ${editingProfesional?.apellido}?`"
    :textoConfirmar="submittingToggle ? 'Guardando...' : (servicioProfesionalPendienteToggle?.disponible ? 'Desactivar' : 'Activar')"
    :colorBoton="servicioProfesionalPendienteToggle?.disponible ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'"
    @confirm="ejecutarToggleServicioProfesional"
    @cancel="cerrarConfirmToggleServicioProfesional"
  />

  <!-- Toast para notificaciones -->
  <Toast />
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import { servicioService, type ServicioRequest, type ServicioResponse } from '../services/servicios'
import PoliticasService from '../services/politicasCancelacion'
import type { PoliticaCancelacionRequest, PoliticaCancelacionResponse } from '../types/politicasCancelacion'
import { useToastStore } from '../composables/useToast'
import Toast from '../components/Toast.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import { formatCurrencyARS as formatearMonedaARS } from '../utils/currency'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToastStore()

const nombreEmpresa = ref('')
const empresaId = ref<number | null>(null)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const TELEFONO_REGEX = /^\d{10,15}$/

const showModalEmpresa = ref(false)
const submittingEmpresa = ref(false)
const errorEmpresa = ref('')
const fieldErrorsEmpresa = ref<Record<string, string>>({})

const empresaOriginal = ref({
  nombre: '',
  descripcion: '',
  direccion: '',
  ciudad: '',
  provincia: '',
  telefono: '',
  email: ''
})

const formDataEmpresa = ref({
  nombre: '',
  descripcion: '',
  direccion: '',
  ciudad: '',
  provincia: '',
  telefono: '',
  email: ''
})

// Tab activo
const activeTab = ref<'profesionales' | 'servicios' | 'horarios' | 'politicas' | 'configuracion'>('profesionales')

// Estado para el formulario de política de cancelación
const mostrarFormularioPolitica = ref(false)
// Estado para Políticas de Cancelación
const politicas = ref<PoliticaCancelacionResponse[]>([])
const loadingPoliticas = ref(false)
const showModalPolitica = ref(false)
const errorPolitica = ref('')
const submittingPolitica = ref(false)
const submittingTogglePolitica = ref(false)
const fieldErrorsPolitica = ref<Record<string, string>>({})

const formDataPolitica = ref<PoliticaCancelacionRequest>({
  tipo: 'CANCELACION',
  descripcion: '',
  horasLimiteCancelacion: 24,
  penalizacion: 'ADVERTENCIA',
  activa: true
})

// Estado para Profesionales
const profesionales = ref<any[]>([])
const serviciosProfesional = ref<any[]>([])
const cargandoServicios = ref(false)
const submittingToggle = ref(false)
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
  descripcion: ''
})

// Estado para Servicios
const servicios = ref<ServicioResponse[]>([])
const loadingServicios = ref(false)
const showModalServicio = ref(false)
const editingServicio = ref<ServicioResponse | null>(null)
const showConfirmToggleServicio = ref(false)
const servicioPendienteToggle = ref<ServicioResponse | null>(null)
const submittingToggleServicio = ref(false)
const errorServicio = ref('')
const submittingServicio = ref(false)
const fieldErrorsServicio = ref<Record<string, string>>({})

const formDataServicio = ref<ServicioRequest>({
  nombre: '',
  descripcion: '',
  duracionMinutos: 0,
  bufferMinutos: undefined,
  precio: 0,
  requiereSena: false,
  montoSena: null
})

// Estado para Horarios
const horarios = ref<any[]>([])
const loadingHorarios = ref(false)
const showModalHorario = ref(false)
const editingHorario = ref<any>(null)
const errorHorario = ref('')
const submittingHorario = ref(false)
const fieldErrorsHorario = ref<Record<string, string>>({})

// Estado para modal de confirmación de eliminación de horario
const showConfirmDeleteHorario = ref(false)
const horarioPendienteEliminar = ref<any>(null)
const eliminandoHorario = ref(false)

const showConfirmTogglePolitica = ref(false)
const politicaPendienteToggle = ref<PoliticaCancelacionResponse | null>(null)

const showConfirmToggleServicioProfesional = ref(false)
const servicioProfesionalPendienteToggle = ref<any>(null)

// Detalles de conflicto 409 en el formulario de horario
const horarioConflictoDetalles = ref<string[]>([])

const formDataHorario = ref({
  diaSemana: '',
  horaInicio: '',
  horaFin: ''
})

const diasSemana = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO']
const nombresDias: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

// Agrupar horarios por día
const horariosAgrupados = ref<Record<string, any[]>>({})

// Estado para Copiar Horarios
const showModalCopiar = ref(false)
const diaFuenteCopia = ref<string>('')
const diasDestinoSeleccionados = ref<string[]>([])
const submittingCopiar = ref(false)
const errorCopiar = ref('')

// Computed para detectar conflictos
const diasConConflicto = computed(() => {
  return diasDestinoSeleccionados.value.filter(dia => {
    return horariosAgrupados.value[dia] && horariosAgrupados.value[dia].length > 0
  })
})

// Estado para Configuración
const loadingConfiguracion = ref(false)
const submittingConfiguracion = ref(false)
const errorConfiguracion = ref('')
const fieldErrorsConfiguracion = ref<Record<string, string>>({})

const formDataConfiguracion = ref({
  bufferPorDefecto: 5,
  tiempoMinimoAnticipacionMinutos: 30,
  diasMaximosReserva: 30,
  horasAntesRecordatorio: 24,
  enviarRecordatorios: true,
  timezone: 'America/Argentina/Buenos_Aires',
  datosBancarios: '' as string | null
})

function agruparHorariosPorDia() {
  const agrupados: Record<string, any[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = horarios.value.filter(h => h.diaSemana === dia)
  })
  horariosAgrupados.value = agrupados
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutsideUserMenu)
  await cargarNombreEmpresa()
  cargarProfesionales()
  cargarServicios()
  cargarHorarios()
  await cargarPoliticasCancelacion()
  cargarConfiguracion()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutsideUserMenu)
})

watch(
  formDataEmpresa,
  () => {
    if (!showModalEmpresa.value) return
    fieldErrorsEmpresa.value = validarFormularioEmpresa()
  },
  { deep: true }
)

const formEmpresaDirty = computed(() => {
  return Object.keys(empresaOriginal.value).some((campo) => {
    const key = campo as keyof typeof empresaOriginal.value
    return (formDataEmpresa.value[key] ?? '').trim() !== (empresaOriginal.value[key] ?? '').trim()
  })
})

const formEmpresaValido = computed(() => Object.keys(fieldErrorsEmpresa.value).length === 0)
const canSubmitEmpresa = computed(() => formEmpresaDirty.value && formEmpresaValido.value && !submittingEmpresa.value)

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleClickOutsideUserMenu(event: MouseEvent) {
  if (!userMenuRef.value) return
  if (!userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

function mapearEmpresaAFormulario(data: any) {
  const mapped = {
    nombre: data?.nombre ?? '',
    descripcion: data?.descripcion ?? '',
    direccion: data?.direccion ?? '',
    ciudad: data?.ciudad ?? '',
    provincia: data?.provincia ?? '',
    telefono: data?.telefono ?? '',
    email: data?.email ?? ''
  }

  empresaOriginal.value = { ...mapped }
  formDataEmpresa.value = { ...mapped }
}

function abrirModalEditarEmpresa() {
  showUserMenu.value = false
  errorEmpresa.value = ''
  formDataEmpresa.value = { ...empresaOriginal.value }
  fieldErrorsEmpresa.value = validarFormularioEmpresa()
  showModalEmpresa.value = true
}

function cerrarModalEditarEmpresa() {
  showModalEmpresa.value = false
  errorEmpresa.value = ''
  fieldErrorsEmpresa.value = {}
}

function normalizarCampoTexto(value: string) {
  return (value ?? '').trim()
}

function normalizarCampoOpcional(value: string) {
  const limpio = normalizarCampoTexto(value)
  return limpio.length > 0 ? limpio : null
}

function validarFormularioEmpresa(): Record<string, string> {
  const errores: Record<string, string> = {}
  const nombre = normalizarCampoTexto(formDataEmpresa.value.nombre)
  const descripcion = normalizarCampoTexto(formDataEmpresa.value.descripcion)
  const direccion = normalizarCampoTexto(formDataEmpresa.value.direccion)
  const ciudad = normalizarCampoTexto(formDataEmpresa.value.ciudad)
  const provincia = normalizarCampoTexto(formDataEmpresa.value.provincia)
  const telefono = normalizarCampoTexto(formDataEmpresa.value.telefono)
  const email = normalizarCampoTexto(formDataEmpresa.value.email)

  if (!nombre) {
    errores.nombre = 'El nombre de la empresa es obligatorio.'
  } else if (nombre.length < 2 || nombre.length > 200) {
    errores.nombre = 'El nombre debe tener entre 2 y 200 caracteres.'
  }

  if (descripcion.length > 500) {
    errores.descripcion = 'La descripción no puede exceder 500 caracteres.'
  }

  if (direccion.length > 200) {
    errores.direccion = 'La dirección no puede exceder 200 caracteres.'
  }

  if (ciudad.length > 100) {
    errores.ciudad = 'La ciudad no puede exceder 100 caracteres.'
  }

  if (provincia.length > 50) {
    errores.provincia = 'La provincia no puede exceder 50 caracteres.'
  }

  if (telefono && !TELEFONO_REGEX.test(telefono)) {
    errores.telefono = 'El teléfono debe tener entre 10 y 15 dígitos numéricos.'
  }

  if (email && !EMAIL_REGEX.test(email)) {
    errores.email = 'El email no es válido.'
  } else if (email.length > 150) {
    errores.email = 'El email no puede exceder 150 caracteres.'
  }

  return errores
}

async function submitFormEmpresa() {
  fieldErrorsEmpresa.value = validarFormularioEmpresa()
  errorEmpresa.value = ''

  if (!canSubmitEmpresa.value) {
    return
  }

  submittingEmpresa.value = true
  try {
    const payload = {
      nombre: normalizarCampoTexto(formDataEmpresa.value.nombre),
      descripcion: normalizarCampoOpcional(formDataEmpresa.value.descripcion),
      direccion: normalizarCampoOpcional(formDataEmpresa.value.direccion),
      ciudad: normalizarCampoOpcional(formDataEmpresa.value.ciudad),
      provincia: normalizarCampoOpcional(formDataEmpresa.value.provincia),
      telefono: normalizarCampoOpcional(formDataEmpresa.value.telefono),
      email: normalizarCampoOpcional(formDataEmpresa.value.email)
    }

    const response = await api.actualizarEmpresaDueno(payload)
    const data = response.data?.datos ?? response.data

    nombreEmpresa.value = data?.nombre ?? nombreEmpresa.value
    mapearEmpresaAFormulario(data)

    cerrarModalEditarEmpresa()
    toast.showSuccess('Empresa actualizada correctamente')
  } catch (err: any) {
    if (err.response?.data?.errores) {
      fieldErrorsEmpresa.value = err.response.data.errores
      errorEmpresa.value = 'Por favor corrija los errores en el formulario.'
    } else if (err.response?.data?.mensaje) {
      errorEmpresa.value = err.response.data.mensaje
    } else {
      errorEmpresa.value = 'Error al actualizar la empresa. Intente nuevamente.'
    }
  } finally {
    submittingEmpresa.value = false
  }
}

// ====Funcion para cargar Nombre de la empresa====
async function cargarNombreEmpresa() {
  try {
    const response = await api.get('/dueno/empresa')
    let data = response.data
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data)
        if (import.meta.env.DEV) console.warn('[FRONTEND] Se forzó parseo de string a objeto:', data)
      } catch (e) {
        console.error('[FRONTEND] Error al parsear data:', e, data)
        data = {}
      }
    }
    nombreEmpresa.value = data?.nombre || 'Mi Empresa'
    empresaId.value = data?.id || null
    mapearEmpresaAFormulario(data)
    if (!empresaId.value) {
      if (import.meta.env.DEV) console.warn('[FRONTEND] No se obtuvo el id de la empresa en la respuesta:', data)
    }
  } catch (err) {
    console.error('[FRONTEND] Error al obtener empresa:', err)
    nombreEmpresa.value = 'Mi Empresa'
    empresaId.value = null
    mapearEmpresaAFormulario({ nombre: 'Mi Empresa' })
  }
}

// ==================== FUNCIONES PARA POLÍTICAS ====================

async function cargarPoliticasCancelacion() {
  loadingPoliticas.value = true
  errorPolitica.value = ''
  try {
    if (import.meta.env.DEV) console.log('[FRONTEND] empresaId antes de cargar políticas:', empresaId.value)
    if (!empresaId.value) {
      if (import.meta.env.DEV) console.warn('[FRONTEND] No se pudo obtener el id de la empresa para cargar políticas')
      politicas.value = []
      toast.showError('No se pudo obtener el id de la empresa.')
      return
    }
    // Nuevo endpoint backend-driven: no requiere empresaId
    const response = await PoliticasService.getTodas()
    let politicasData = response.data
    if (typeof politicasData === 'string') {
      try {
        politicasData = JSON.parse(politicasData)
        if (import.meta.env.DEV) console.warn('[FRONTEND] Se forzó parseo de string a array de políticas:', politicasData)
      } catch (e) {
        console.error('[FRONTEND] Error al parsear políticas:', e, politicasData)
        politicasData = []
      }
    }
    if (import.meta.env.DEV) console.log('[FRONTEND] Respuesta de getTodasPorEmpresa:', politicasData)
    politicas.value = Array.isArray(politicasData) ? politicasData : []
  } catch (err: any) {
    console.error('[FRONTEND] Error al cargar políticas:', err)
    politicas.value = []
    toast.showError('Error al cargar las políticas de cancelación.')
  } finally {
    loadingPoliticas.value = false
  }
}

const editingPolitica = ref<PoliticaCancelacionResponse | null>(null)

function openModalPolitica(politica?: PoliticaCancelacionResponse) {
  errorPolitica.value = ''
  if (politica) {
    formDataPolitica.value = { 
      tipo: politica.tipo,
      descripcion: politica.descripcion,
      horasLimiteCancelacion: politica.horasLimiteCancelacion,
      penalizacion: 'ADVERTENCIA',
      activa: politica.activa
    };
    editingPolitica.value = politica;
  
  } else {
    formDataPolitica.value = {
      tipo: 'CANCELACION',
      descripcion: '',
      horasLimiteCancelacion: 24,
      penalizacion: 'ADVERTENCIA',
      activa: true
    };
      editingPolitica.value = null;
    }
  showModalPolitica.value = true
}

function closeModalPolitica() {
  showModalPolitica.value = false
  formDataPolitica.value = {
    tipo: 'CANCELACION',
    descripcion: '',
    horasLimiteCancelacion: 24,
    penalizacion: 'ADVERTENCIA',
    activa: true
  }
  fieldErrorsPolitica.value = {}
  errorPolitica.value = ''
}

async function submitFormPolitica() {
  submittingPolitica.value = true
  fieldErrorsPolitica.value = {}
  errorPolitica.value = ''
  
  try {
    if (editingPolitica.value) {
      // Validación básica
      if (!formDataPolitica.value.descripcion) {
        fieldErrorsPolitica.value.descripcion = 'La descripción es obligatoria.'
        submittingPolitica.value = false
        return
      }
      if (formDataPolitica.value.tipo !== 'INASISTENCIA' && (!formDataPolitica.value.horasLimiteCancelacion || formDataPolitica.value.horasLimiteCancelacion < 1)) {
        fieldErrorsPolitica.value.horasLimiteCancelacion = 'Debe indicar las horas límite.'
        submittingPolitica.value = false
        return
      }
      formDataPolitica.value.penalizacion = 'ADVERTENCIA'
      /* Actualizar política existente con metodo */
      await PoliticasService.actualizar(editingPolitica.value!.id, formDataPolitica.value);
    } else {
      // Validación básica
      if (!formDataPolitica.value.descripcion) {
        fieldErrorsPolitica.value.descripcion = 'La descripción es obligatoria.'
        submittingPolitica.value = false
        return
      }
      if (formDataPolitica.value.tipo !== 'INASISTENCIA' && (!formDataPolitica.value.horasLimiteCancelacion || formDataPolitica.value.horasLimiteCancelacion < 1)) {
        fieldErrorsPolitica.value.horasLimiteCancelacion = 'Debe indicar las horas límite.'
        submittingPolitica.value = false
        return
      }
      formDataPolitica.value.penalizacion = 'ADVERTENCIA'
      await PoliticasService.crear(formDataPolitica.value);
    }
    const wasEditing = !!editingPolitica.value
    await cargarPoliticasCancelacion()
    showModalPolitica.value = false
    toast.showSuccess(wasEditing ? 'Política actualizada correctamente' : 'Política creada correctamente')
  } catch (err: any) {
    console.error('Error al guardar política:', err);
    if (err.response?.data?.errores) {
      fieldErrorsPolitica.value = err.response.data.errores
      errorPolitica.value = 'Por favor corrija los errores en el formulario.'
    } else if (err.response?.data?.mensaje) {
      errorPolitica.value = err.response.data.mensaje
    } else {
      errorPolitica.value = 'Error al crear la política.'
    }
  } finally {
    submittingPolitica.value = false
  }
}

function confirmarTogglePoliticaActiva(politica: PoliticaCancelacionResponse) {
  politicaPendienteToggle.value = politica
  showConfirmTogglePolitica.value = true
}

function cerrarConfirmTogglePolitica() {
  showConfirmTogglePolitica.value = false
  politicaPendienteToggle.value = null
}

async function ejecutarTogglePoliticaActiva() {
  const politica = politicaPendienteToggle.value
  if (!politica) return

  try {
    submittingTogglePolitica.value = true
    if (politica.activa) {
      await PoliticasService.desactivar(politica.id)
      toast.showSuccess('Política desactivada correctamente')
    } else {
      await PoliticasService.activar(politica.id)
      toast.showSuccess('Política activada correctamente')
    }
    cerrarConfirmTogglePolitica()
    await cargarPoliticasCancelacion()
  } catch (err: any) {
    console.error('Error al cambiar estado de política:', err)
    toast.showError(err?.response?.data?.mensaje || err?.message || 'Error al cambiar el estado de la política.')
  } finally {
    submittingTogglePolitica.value = false
  }
}

const politicaPendienteEliminar = ref<number | null>(null)
const showConfirmDeletePolitica = ref(false)

function confirmarEliminarPolitica(id: number) {
  politicaPendienteEliminar.value = id
  showConfirmDeletePolitica.value = true
}

function cerrarConfirmDeletePolitica() {
  showConfirmDeletePolitica.value = false
  politicaPendienteEliminar.value = null
}

async function ejecutarEliminarPolitica() {
  if (politicaPendienteEliminar.value === null) return
  showConfirmDeletePolitica.value = false
  try {
    await PoliticasService.eliminar(politicaPendienteEliminar.value)
    await cargarPoliticasCancelacion()
    toast.showSuccess('Política eliminada correctamente')
  } catch (err: any) {
    console.error('Error al eliminar política:', err)
    toast.showError(err?.response?.data?.mensaje || 'Error al eliminar la política.')
  } finally {
    politicaPendienteEliminar.value = null
  }
}

// ==================== FUNCIONES AUXILIARES ====================

function formatearPenalizacion(penalizacion: string): string {
  const formatos: Record<string, string> = {
    'NINGUNA': 'Ninguna',
    'ADVERTENCIA': 'Advertencia',
    'BLOQUEO': 'Bloqueo temporal',
    'MULTA': 'Multa económica'
  }
  return formatos[penalizacion] || penalizacion
}

function formatearFecha(fecha: string): string {
  if (!fecha) return ''
  try {
    const date = new Date(fecha)
    return date.toLocaleDateString('es-AR', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    })
  } catch {
    return fecha
  }
}

async function cargarProfesionales() {
  loading.value = true
  try {
    const response = await api.get('/dueno/profesionales')
    // El backend retorna directamente el array, no envuelto en ApiResponse
    profesionales.value = Array.isArray(response.data) ? response.data : []
  } catch (err: any) {
    console.error('Error al cargar profesionales:', err)
    profesionales.value = [] // Limpiar en caso de error
    error.value = 'Error al cargar la lista de profesionales'
  } finally {
    loading.value = false
  }
}



function openModal(profesional: any = null) {
  editingProfesional.value = profesional
  serviciosProfesional.value = [] // Limpiar servicios al abrir modal
  if (profesional) {
    formData.value = {
      nombre: profesional.nombre,
      apellido: profesional.apellido,
      email: profesional.email,
      contrasena: '',
      telefono: profesional.telefono || '',
      descripcion: profesional.descripcion || ''
    }
  } else {
    formData.value = {
      nombre: '',
      apellido: '',
      email: '',
      contrasena: '',
      telefono: '',
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
  serviciosProfesional.value = []
  error.value = ''
  fieldErrors.value = {}
}

async function cargarServiciosProfesional() {
  if (!editingProfesional.value) return
  
  cargandoServicios.value = true
  try {
    const response = await api.get(`/dueno/profesionales/${editingProfesional.value.id}/servicios`)
    serviciosProfesional.value = response.data.datos
  } catch (err: any) {
    console.error('Error al cargar servicios:', err)
    toast.showError('Error al cargar servicios del profesional')
  } finally {
    cargandoServicios.value = false
  }
}

async function toggleServicio(servicio: any) {
  if (!editingProfesional.value) return
  
  submittingToggle.value = true
  try {
    await api.patch(`/dueno/profesionales/${editingProfesional.value.id}/servicios/toggle`, {
      servicioId: servicio.servicioId,
      disponible: !servicio.disponible
    })
    
    // Actualizar localmente
    servicio.disponible = !servicio.disponible
  } catch (err: any) {
    console.error('Error al cambiar servicio:', err)
    toast.showError(err.response?.data?.mensaje || 'Error al cambiar el estado del servicio')
  } finally {
    submittingToggle.value = false
  }
}

function confirmarToggleServicioProfesional(servicio: any) {
  servicioProfesionalPendienteToggle.value = servicio
  showConfirmToggleServicioProfesional.value = true
}

function cerrarConfirmToggleServicioProfesional() {
  showConfirmToggleServicioProfesional.value = false
  servicioProfesionalPendienteToggle.value = null
}

async function ejecutarToggleServicioProfesional() {
  const servicio = servicioProfesionalPendienteToggle.value
  if (!servicio) return
  cerrarConfirmToggleServicioProfesional()
  await toggleServicio(servicio)
}

async function submitForm() {
  error.value = ''
  fieldErrors.value = {}
  submitting.value = true
  
  try {
    const payload = {
      nombre: formData.value.nombre,
      apellido: formData.value.apellido,
      email: formData.value.email,
      contrasena: formData.value.contrasena,
      telefono: formData.value.telefono,
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
    const wasEditing = !!editingProfesional.value
    closeModal()
    await cargarProfesionales()
    toast.showSuccess(wasEditing ? 'Profesional actualizado correctamente' : 'Profesional creado correctamente')
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

const profesionalPendienteToggle = ref<any>(null)
const showConfirmToggleProfesional = ref(false)

function confirmarToggleProfesional(profesional: any) {
  profesionalPendienteToggle.value = profesional
  showConfirmToggleProfesional.value = true
}

function cerrarConfirmToggleProfesional() {
  showConfirmToggleProfesional.value = false
  profesionalPendienteToggle.value = null
}

async function ejecutarToggleProfesional() {
  const profesional = profesionalPendienteToggle.value
  if (!profesional) return
  showConfirmToggleProfesional.value = false
  const accion = profesional.activo ? 'desactivar' : 'activar'
  try {
    if (profesional.activo) {
      await api.patch(`/dueno/profesionales/${profesional.id}/desactivar`)
      toast.showSuccess(`Profesional ${profesional.nombre} ${profesional.apellido} desactivado correctamente`, 5000)
    } else {
      await api.patch(`/dueno/profesionales/${profesional.id}/activar`)
      toast.showSuccess(`Profesional ${profesional.nombre} ${profesional.apellido} activado correctamente`, 5000)
    }
    await cargarProfesionales()
  } catch (err: any) {
    console.error(`Error al ${accion} profesional:`, err)
    const mensajeError = err.response?.data?.mensaje 
      || err.response?.data?.error 
      || err.message 
      || `Error al ${accion} el profesional`
    toast.showError(mensajeError)
  } finally {
    profesionalPendienteToggle.value = null
  }
}

function handleLogout() {
  showUserMenu.value = false
  // Llamar al endpoint de logout para invalidar sesión del servidor
  api.logout()
    .then(() => {
      authStore.logout()
      router.push('/login')
    })
    .catch(() => {
      // Incluso si falla, limpiar estado local
      authStore.logout()
      router.push('/login')
    })
}

// ==================== FUNCIONES PARA SERVICIOS ====================

async function cargarServicios() {
  loadingServicios.value = true
  try {
    servicios.value = await servicioService.obtenerServicios()
  } catch (err: any) {
    console.error('Error al cargar servicios:', err)
    servicios.value = [] // Limpiar in caso de error
    errorServicio.value = 'Error al cargar la lista de servicios'
  } finally {
    loadingServicios.value = false
  }
}

function openModalServicio(servicio: ServicioResponse | null = null) {
  editingServicio.value = servicio
  if (servicio) {
    formDataServicio.value = {
      nombre: servicio.nombre,
      descripcion: servicio.descripcion,
      duracionMinutos: servicio.duracionMinutos,
      precio: servicio.precio,
      bufferMinutos: servicio.bufferMinutos,
      requiereSena: servicio.requiereSena,
      montoSena: servicio.montoSena
    }
  } else {
    formDataServicio.value = {
      nombre: '',
      descripcion: '',
      duracionMinutos: 0,
      precio: 0,
      bufferMinutos: undefined,
      requiereSena: false,
      montoSena: null
    }
  }
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
  showModalServicio.value = true
}

function closeModalServicio() {
  showModalServicio.value = false
  editingServicio.value = null
  formDataServicio.value = {
    nombre: '',
    descripcion: '',
    duracionMinutos: 0,
    precio: 0,
    bufferMinutos: undefined,
    requiereSena: false,
    montoSena: null
  }
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
}

async function submitFormServicio() {
  submittingServicio.value = true
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
  
  try {
    if (formDataServicio.value.requiereSena && (!formDataServicio.value.montoSena || formDataServicio.value.montoSena <= 0)) {
      errorServicio.value = 'Debe ingresar un monto de seña mayor a 0 cuando el servicio requiere seña'
      return
    }

    if (!formDataServicio.value.requiereSena) {
      formDataServicio.value.montoSena = null
    }

    if (editingServicio.value) {
      await servicioService.actualizarServicio(editingServicio.value.id, formDataServicio.value)
    } else {
      await servicioService.crearServicio(formDataServicio.value)
    }
    const wasEditing = !!editingServicio.value
    closeModalServicio()
    await cargarServicios()
    toast.showSuccess(wasEditing ? 'Servicio actualizado correctamente' : 'Servicio creado correctamente')
  } catch (err: any) {
    console.error('Error completo:', err)
    
    if (err.response?.data?.errores) {
      fieldErrorsServicio.value = err.response.data.errores
      errorServicio.value = 'Por favor corrija los errores en el formulario'
    } else if (err.response?.data?.mensaje) {
      errorServicio.value = err.response.data.mensaje
    } else if (err.response?.data?.error) {
      errorServicio.value = err.response.data.error
    } else {
      errorServicio.value = 'Error al guardar el servicio. Por favor intente nuevamente.'
    }
  } finally {
    submittingServicio.value = false
  }
}

async function toggleServicioActivo(servicio: ServicioResponse) {
  if (submittingToggleServicio.value) return
  try {
    submittingToggleServicio.value = true
    if (servicio.activo) {
      await servicioService.desactivarServicio(servicio.id)
      toast.showSuccess(`Servicio "${servicio.nombre}" oculto para nuevas reservas. Los turnos existentes se mantienen.`)
    } else {
      await servicioService.activarServicio(servicio.id)
      toast.showSuccess(`Servicio "${servicio.nombre}" activado correctamente`)
    }
    cerrarConfirmToggleServicio()
    await cargarServicios()
  } catch (err: any) {
    console.error('Error al cambiar estado del servicio:', err)
    toast.showError(err.response?.data?.mensaje || 'Error al cambiar el estado del servicio')
  } finally {
    submittingToggleServicio.value = false
  }
}

function confirmarToggleServicioActivo(servicio: ServicioResponse) {
  servicioPendienteToggle.value = servicio
  showConfirmToggleServicio.value = true
}

function cerrarConfirmToggleServicio() {
  showConfirmToggleServicio.value = false
  servicioPendienteToggle.value = null
}

async function ejecutarToggleServicioActivo() {
  const servicio = servicioPendienteToggle.value
  if (!servicio) return
  await toggleServicioActivo(servicio)
}
// ==================== FUNCIONES PARA HORARIOS ====================

async function cargarHorarios() {
  loadingHorarios.value = true
  try {
    const response = await api.obtenerHorariosEmpresa()
    // El backend retorna directamente el array
    horarios.value = Array.isArray(response.data) ? response.data : []
    agruparHorariosPorDia()
  } catch (err: any) {
    console.error('Error al cargar horarios:', err)
    horarios.value = []
    agruparHorariosPorDia()
    toast.showError('Error al cargar los horarios de la empresa')
  } finally {
    loadingHorarios.value = false
  }
}

function openModalHorario(horario: any = null) {
  if (horario) {
    editingHorario.value = horario
    formDataHorario.value = {
      diaSemana: horario.diaSemana,
      horaInicio: horario.horaInicio,
      horaFin: horario.horaFin
    }
  } else {
    editingHorario.value = null
    formDataHorario.value = {
      diaSemana: '',
      horaInicio: '',
      horaFin: ''
    }
  }
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
  showModalHorario.value = true
}

function closeModalHorario() {
  showModalHorario.value = false
  editingHorario.value = null
  formDataHorario.value = {
    diaSemana: '',
    horaInicio: '',
    horaFin: ''
  }
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
  horarioConflictoDetalles.value = []
}

async function submitFormHorario() {
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
  submittingHorario.value = true

  try {
    if (editingHorario.value) {
      await api.actualizarHorarioEmpresa(editingHorario.value.id, formDataHorario.value)
    } else {
      await api.crearHorarioEmpresa(formDataHorario.value)
    }
    
    const wasEditing = !!editingHorario.value
    await cargarHorarios()
    closeModalHorario()
    toast.showSuccess(wasEditing ? 'Horario actualizado correctamente' : 'Horario creado correctamente')
  } catch (err: any) {
    console.error('Error al guardar horario:', err)
    horarioConflictoDetalles.value = []
    
    if (err.response?.status === 409) {
      // Conflicto de disponibilidad de profesionales o turnos activos
      const data = err.response.data
      if (data?.turnosAfectados?.length) {
        errorHorario.value = data.mensaje || 'No se puede guardar el horario: hay turnos afectados.'
        horarioConflictoDetalles.value = data.turnosAfectados as string[]
      } else if (data?.profesionalesAfectados?.length) {
        errorHorario.value = data.mensaje || 'No se puede guardar el horario: hay profesionales afectados.'
        horarioConflictoDetalles.value = data.profesionalesAfectados as string[]
      } else {
        errorHorario.value = data?.mensaje || 'El horario no puede modificarse porque hay conflictos activos.'
      }
    } else if (err.response?.status === 400) {
      const mensaje = err.response.data.mensaje || err.response.data.message
      errorHorario.value = mensaje || 'Error al validar los datos del horario'
    } else {
      errorHorario.value = 'Error al guardar el horario'
    }
  } finally {
    submittingHorario.value = false
  }
}

function confirmarEliminarHorario(horario: any) {
  horarioPendienteEliminar.value = horario
  showConfirmDeleteHorario.value = true
}

function cerrarConfirmDeleteHorario() {
  showConfirmDeleteHorario.value = false
  horarioPendienteEliminar.value = null
}

async function ejecutarEliminarHorario() {
  if (!horarioPendienteEliminar.value) return
  eliminandoHorario.value = true
  try {
    await api.eliminarHorarioEmpresa(horarioPendienteEliminar.value.id)
    await cargarHorarios()
    cerrarConfirmDeleteHorario()
    toast.showSuccess('Horario eliminado correctamente')
  } catch (err: any) {
    console.error('Error al eliminar horario:', err)
    cerrarConfirmDeleteHorario()
    if (err.response?.status === 409) {
      const data = err.response.data
      if (data?.turnosAfectados?.length) {
        toast.showErrorConDetalles(
          data.mensaje || 'No se puede eliminar el horario por turnos activos.',
          data.turnosAfectados as string[]
        )
      } else if (data?.profesionalesAfectados?.length) {
        toast.showErrorConDetalles(
          data.mensaje || 'No se puede eliminar el horario por profesionales afectados.',
          data.profesionalesAfectados as string[]
        )
      } else {
        toast.showError(data?.mensaje || 'No se puede eliminar: hay conflictos activos en ese horario.')
      }
    } else {
      toast.showError('Error inesperado al eliminar el horario. Intente nuevamente.')
    }
  } finally {
    eliminandoHorario.value = false
  }
}

// ==================== FUNCIONES PARA COPIAR HORARIOS ====================

function abrirModalCopiar(diaFuente: string) {
  diaFuenteCopia.value = diaFuente
  diasDestinoSeleccionados.value = []
  errorCopiar.value = ''
  showModalCopiar.value = true
}

function cerrarModalCopiar() {
  showModalCopiar.value = false
  diaFuenteCopia.value = ''
  diasDestinoSeleccionados.value = []
  errorCopiar.value = ''
}

async function confirmarCopiarHorarios() {
  if (diasDestinoSeleccionados.value.length === 0) {
    errorCopiar.value = 'Debe seleccionar al menos un día destino'
    return
  }

  // Los días con conflicto ya se muestran como advertencia en el modal (warning-message)
  // El usuario ve el aviso y confirma al hacer clic en "Copiar Horarios"

  submittingCopiar.value = true
  errorCopiar.value = ''

  try {
    await api.copiarHorariosAOtrosDias(
      diaFuenteCopia.value,
      diasDestinoSeleccionados.value,
      diasConConflicto.value.length > 0 // reemplazar solo si hay conflictos confirmados
    )
    
    await cargarHorarios()
    cerrarModalCopiar()
    toast.showSuccess('Horarios copiados correctamente')
  } catch (err: any) {
    console.error('Error al copiar horarios:', err)
    
    if (err.response?.status === 400) {
      const mensaje = err.response.data.mensaje || err.response.data.message
      errorCopiar.value = mensaje || 'Error al copiar los horarios'
    } else {
      errorCopiar.value = 'Error al copiar los horarios'
    }
  } finally {
    submittingCopiar.value = false
  }
}

// ==================== FUNCIONES PARA CONFIGURACIÓN ====================

async function cargarConfiguracion() {
  loadingConfiguracion.value = true
  errorConfiguracion.value = ''
  
  try {
    const response = await api.obtenerConfiguracion()
    const data = response.data
    
    // Mapear respuesta a formData
    formDataConfiguracion.value = {
      bufferPorDefecto: data.bufferPorDefecto ?? 5,
      tiempoMinimoAnticipacionMinutos: data.tiempoMinimoAnticipacionMinutos ?? 30,
      diasMaximosReserva: data.diasMaximosReserva ?? 30,
      horasAntesRecordatorio: data.horasAntesRecordatorio ?? 24,
      enviarRecordatorios: data.enviarRecordatorios ?? true,
      timezone: data.timezone ?? 'America/Argentina/Buenos_Aires',
      datosBancarios: data.datosBancarios ?? ''
    }
  } catch (err: any) {
    console.error('Error al cargar configuración:', err)
    errorConfiguracion.value = 'Error al cargar la configuración de la empresa'
  } finally {
    loadingConfiguracion.value = false
  }
}

async function submitConfiguracion() {
  submittingConfiguracion.value = true
  errorConfiguracion.value = ''
  fieldErrorsConfiguracion.value = {}
  
  try {
    const response = await api.actualizarConfiguracion(formDataConfiguracion.value)
    
    // Mostrar mensaje de éxito
    toast.showSuccess('Configuración guardada exitosamente')
    
    // Recargar configuración para reflejar cambios
    await cargarConfiguracion()
  } catch (err: any) {
    console.error('Error al guardar configuración:', err)
    
    // Manejar errores de validación por campo
    if (err.response?.data?.errores) {
      fieldErrorsConfiguracion.value = err.response.data.errores
      errorConfiguracion.value = 'Por favor corrija los errores en el formulario'
    } else if (err.response?.data?.mensaje) {
      errorConfiguracion.value = err.response.data.mensaje
    } else {
      errorConfiguracion.value = 'Error al guardar la configuración. Por favor intente nuevamente.'
    }
  } finally {
    submittingConfiguracion.value = false
  }
}

</script>

<style scoped>
.dueno-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 2rem;
}

.card-descripcion .truncate-text,
.card-mensaje .truncate-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  max-width: 100%;
}
.card-body {
  min-height: 80px;
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

/* Tabs */
.tabs {
  background: white;
  display: flex;
  gap: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.tab {
  flex: 1;
  padding: 1rem 2rem;
  border: none;
  background: transparent;
  color: #718096;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab:hover {
  background: #f7fafc;
  color: #667eea;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f7fafc;
}

.user-info {
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-switch-rol {
  background: #4c51bf;
  color: white;
  border: none;
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-switch-rol:hover {
  background: #434190;
  transform: translateY(-2px);
}

.user-menu-trigger {
  background: white;
  border: 2px solid #e2e8f0;
  color: #2d3748;
  padding: 0.5rem 0.9rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.2s ease;
}

.user-menu-trigger:hover {
  border-color: #cbd5e0;
  background: #f8fafc;
}

.user-menu-arrow {
  font-size: 0.85rem;
  color: #4a5568;
}

.user-menu-dropdown {
  position: absolute;
  margin-top: 0.5rem;
  top: 100%;
  right: 0;
  width: 12rem;
  background: white;
  border-radius: 0.375rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  padding: 0.25rem 0;
  z-index: 1050;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.user-menu-item {
  width: 100%;
  border: none;
  background: transparent;
  color: #2d3748;
  text-align: left;
  padding: 0.65rem 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.user-menu-item:hover {
  background: #f1f5f9;
}

.user-menu-item-danger {
  color: #c53030;
}

.user-menu-item-danger:hover {
  background: #fff5f5;
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

.text-muted {
  color: #a0aec0;
  font-style: italic;
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

.btn-activate {
  background: #48bb78;
  color: white;
}

.btn-activate:hover {
  background: #38a169;
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

/* Checkbox Group Styles */
.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #fff;
}

.checkbox-item:hover {
  border-color: #4299e1;
  background-color: #ebf8ff;
}

.checkbox-item input[type="checkbox"] {
  cursor: pointer;
  width: 18px;
  height: 18px;
  margin: 0;
}

.checkbox-item span {
  font-size: 0.9rem;
  color: #2d3748;
  user-select: none;
}

.loading-text {
  color: #718096;
  font-style: italic;
  padding: 0.5rem;
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

/* Servicios del Profesional */
.btn-secondary {
  padding: 0.6rem 1rem;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: #3182ce;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.servicios-profesional-list {
  margin-top: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.servicio-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s;
}

.servicio-item:last-child {
  border-bottom: none;
}

.servicio-item:hover {
  background: #f7fafc;
}

.servicio-item.desactivado {
  opacity: 0.6;
  background: #fff5f5;
}

.servicio-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.servicio-info strong {
  color: #2d3748;
  font-size: 0.95rem;
}

.servicio-meta {
  color: #718096;
  font-size: 0.85rem;
}

.btn-toggle {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-toggle.activo {
  background: #fc8181;
  color: white;
}

.btn-toggle.activo:hover:not(:disabled) {
  background: #f56565;
}

.btn-toggle.inactivo {
  background: #48bb78;
  color: white;
}

.btn-toggle.inactivo:hover:not(:disabled) {
  background: #38a169;
}

.btn-toggle:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Estilos adicionales para servicios */
.badge-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-status.activo {
  background: #c6f6d5;
  color: #22543d;
}

.badge-status.inactivo {
  background: #fed7d7;
  color: #c53030;
}

.btn-activate {
  background: #48bb78;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-activate:hover {
  background: #38a169;
  transform: translateY(-2px);
}

.btn-remove-esp {
  background: transparent;
  border: none;
  color: #c53030;
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
  margin-left: 0.25rem;
}

.btn-remove-esp:hover {
  color: #9b2c2c;
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

  .tabs {
    flex-direction: column;
  }
  
  .tab {
    border-bottom: 1px solid #e2e8f0;
    border-left: 3px solid transparent;
  }
  
  .tab.active {
    border-left-color: #667eea;
    border-bottom-color: transparent;
  }
}

/* ==================== ESTILOS PARA HORARIOS ==================== */

.horarios-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.horario-dia-card {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dia-header {
  background-color: #667eea;
  color: white;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dia-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dia-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.btn-copiar-horarios {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-copiar-horarios:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.count-badge {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
}

.dia-body {
  padding: 1rem;
}

.horarios-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.horario-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
}

.horario-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.1);
}

.horario-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.horario-time {
  font-weight: 600;
  color: #1f2937;
  font-size: 1rem;
}

.horario-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit-small,
.btn-delete-small {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit-small {
  background-color: #f3f4f6;
  color: #4b5563;
}

.btn-edit-small:hover {
  background-color: #e5e7eb;
  color: #1f2937;
}

.btn-delete-small {
  background-color: #fee2e2;
  color: #dc2626;
}

.btn-delete-small:hover {
  background-color: #fecaca;
  color: #991b1b;
}

.no-horarios {
  color: #6b7280;
  font-size: 0.875rem;
  text-align: center;
  padding: 1rem;
  background-color: white;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
}

.modal-small {
  max-width: 500px;
}

.modal-small .modal-content {
  max-width: 500px;
}

/* Estilos para Modal de Copiar */
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.checkbox-label:hover {
  background-color: #f3f4f6;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"]:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.disabled-day {
  color: #9ca3af;
}

.badge-fuente {
  font-size: 0.75rem;
  color: #6b7280;
  font-style: italic;
}

.warning-message {
  background-color: #fef3c7;
  border: 1px solid #fbbf24;
  color: #92400e;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-top: 1rem;
}

.warning-message strong {
  font-weight: 600;
}

/* ==================== ESTILOS PARA CONFIGURACIÓN ==================== */

.configuracion-container {
  max-width: 900px;
  margin: 0 auto;
}

.configuracion-form {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  overflow: hidden;
}

.config-section {
  padding: 2rem;
  border-bottom: 2px solid #f7fafc;
}

.config-section:last-of-type {
  border-bottom: none;
}

.section-title {
  color: #2d3748;
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0 0 1.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.form-group-switch {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  user-select: none;
}

.switch-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 28px;
  background-color: #cbd5e0;
  border-radius: 28px;
  transition: background-color 0.3s ease;
}

.switch-slider::before {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  left: 3px;
  top: 3px;
  background-color: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
}

.switch-input:checked + .switch-slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.switch-input:checked + .switch-slider::before {
  transform: translateX(24px);
}

.switch-text {
  font-weight: 600;
  color: #2d3748;
  font-size: 1rem;
}

.info-box {
  background: #e6fffa;
  border-left: 4px solid #38b2ac;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-top: 1.5rem;
}

.info-box strong {
  color: #234e52;
  display: block;
  margin-bottom: 0.5rem;
}

.info-box ul {
  margin: 0.5rem 0 0 1.5rem;
  color: #2c7a7b;
}

.info-box li {
  margin-bottom: 0.25rem;
  line-height: 1.5;
}

.config-info-section {
  background: #f7fafc;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.info-card {
  background: white;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.info-label {
  color: #718096;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  color: #2d3748;
  font-size: 1.1rem;
  font-weight: 700;
}

.form-actions {
  padding: 2rem;
  background: #f7fafc;
  display: flex;
  justify-content: center;
}

.btn-submit-config {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 1rem 3rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 700;
  font-size: 1.1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-submit-config:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit-config:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Ajustes para campo hint */
.field-hint {
  color: #718096;
  font-size: 0.8rem;
  font-style: italic;
}

.info-box {
  background: #f0f9ff;
  border-left: 4px solid #3b82f6;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.error-box {
  background: #fef2f2;
  border-left: 4px solid #ef4444;
  padding: 1rem;
  border-radius: 0.5rem;
}

.stat-badge {
  background: white;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-weight: 600;
  color: #1e293b;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  font-size: 0.95rem;
}

.btn-danger {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: #dc2626;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #b91c1c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.4);
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.lista-bloqueantes {
  margin: 0.75rem 0 0;
  padding-left: 1.25rem;
  list-style: disc;
  font-size: 0.875rem;
  color: #374151;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
</style>


