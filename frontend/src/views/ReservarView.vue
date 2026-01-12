<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header empresa con diseño mobile-first -->
    <header v-if="empresa" class="bg-white shadow-sm sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <!-- Mobile: Stack vertical -->
        <div class="py-4 space-y-4">
          <!-- Fila 1: Nombre y botones principales -->
          <div class="flex items-center justify-between">
            <!-- Logo/Nombre empresa -->
            <div class="flex-1 min-w-0">
              <h1 class="text-xl sm:text-2xl lg:text-3xl font-bold text-gray-900 truncate">
                {{ empresa.nombre }}
              </h1>
            </div>
            
            <!-- Botones de autenticación (móvil: iconos compactos) -->
            <div class="flex items-center gap-2 ml-4">
              <template v-if="clienteAutenticado">
                <!-- Móvil: Solo iconos | Desktop: Botones completos -->
                <button 
                  @click="irAMisTurnos"
                  class="inline-flex items-center justify-center px-3 py-2 sm:px-4 sm:py-2 text-sm font-medium text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-md transition-colors"
                  title="Mis Turnos"
                >
                  <svg class="w-5 h-5 sm:mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span class="hidden sm:inline">Mis Turnos</span>
                </button>
                <button 
                  @click="cerrarSesion"
                  class="inline-flex items-center justify-center px-3 py-2 sm:px-4 sm:py-2 text-sm font-medium text-gray-600 hover:text-gray-700 hover:bg-gray-100 rounded-md transition-colors"
                  title="Cerrar Sesión"
                >
                  <svg class="w-5 h-5 sm:mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  <span class="hidden sm:inline">Salir</span>
                </button>
              </template>
              <template v-else>
                <button 
                  @click="irALogin"
                  class="inline-flex items-center justify-center px-3 py-2 sm:px-4 sm:py-2 text-sm font-medium text-blue-600 hover:text-blue-700 hover:bg-blue-50 rounded-md transition-colors"
                >
                  <span class="hidden xs:inline">Ingresar</span>
                  <span class="xs:hidden">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                    </svg>
                  </span>
                </button>
                <button 
                  @click="irARegistro"
                  class="inline-flex items-center justify-center px-3 py-2 sm:px-4 sm:py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md shadow-sm transition-colors"
                >
                  Registrarse
                </button>
              </template>
            </div>
          </div>
          
          <!-- Fila 2: Descripción (opcional, solo si existe) -->
          <p v-if="empresa.descripcion" class="text-sm sm:text-base text-gray-600 line-clamp-2">
            {{ empresa.descripcion }}
          </p>
          
          <!-- Fila 3: Info de contacto + Badge cliente autenticado -->
          <div class="flex flex-wrap items-center justify-between gap-3">
            <!-- Info empresa -->
            <div class="flex flex-wrap items-center gap-3 text-xs sm:text-sm text-gray-500">
              <span v-if="empresa.ciudad" class="inline-flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {{ empresa.ciudad }}
              </span>
              <span v-if="empresa.telefono" class="inline-flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                </svg>
                {{ empresa.telefono }}
              </span>
            </div>
            
            <!-- Badge usuario autenticado (solo móvil) -->
            <div v-if="clienteAutenticado" class="sm:hidden">
              <span class="inline-flex items-center gap-1.5 px-2.5 py-1 bg-blue-50 text-blue-700 rounded-full text-xs font-medium">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
                {{ clienteStore.cliente?.nombre }}
              </span>
            </div>
            
            <!-- Badge usuario autenticado (desktop) -->
            <div v-if="clienteAutenticado" class="hidden sm:block">
              <span class="inline-flex items-center gap-2 px-3 py-1.5 bg-blue-50 text-blue-700 rounded-full text-sm font-medium">
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
                {{ clienteStore.cliente?.nombre }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- Banner informativo de sesión activa -->
    <div v-if="clienteAutenticado" class="bg-gradient-to-r from-blue-50 to-indigo-50 border-b-2 border-blue-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="flex-shrink-0">
              <svg class="w-6 h-6 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-semibold text-blue-900">
                ✨ Sesión activa: {{ clienteStore.cliente?.nombre }}
              </p>
              <p class="text-xs text-blue-700 mt-0.5">
                Tus datos se completarán automáticamente en el paso final
              </p>
            </div>
          </div>
          <button 
            @click="irAMisTurnos"
            class="hidden sm:flex items-center gap-1.5 text-sm font-medium text-blue-700 hover:text-blue-800 hover:underline transition-colors"
          >
            <span>Ver mis turnos</span>
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Contenido principal con padding -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

    <!-- Indicador de pasos (Stepper responsive) -->
    <div class="relative mb-8">
      <!-- Línea de progreso -->
      <div class="absolute top-5 left-0 right-0 h-0.5 bg-gray-200 hidden sm:block" style="left: 10%; right: 10%;"></div>
      
      <!-- Pasos -->
      <div class="flex justify-between items-start relative">
        <!-- Paso 1 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 1 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 1 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 1">✓</span>
            <span v-else>1</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center">
            <span class="hidden sm:inline">Servicio</span>
            <span class="sm:hidden">1</span>
          </span>
        </div>

        <!-- Paso 2 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 2 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 2 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 2">✓</span>
            <span v-else>2</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center">
            <span class="hidden sm:inline">Profesional</span>
            <span class="sm:hidden">2</span>
          </span>
        </div>

        <!-- Paso 3 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 3 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 3 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 3">✓</span>
            <span v-else>3</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center">
            <span class="hidden sm:inline">Fecha</span>
            <span class="sm:hidden">3</span>
          </span>
        </div>

        <!-- Paso 4 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 4 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 4 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 4">✓</span>
            <span v-else>4</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center">
            <span class="hidden sm:inline">Hora</span>
            <span class="sm:hidden">4</span>
          </span>
        </div>

        <!-- Paso 5 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 5 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 'bg-gray-200 text-gray-600'
          ]">
            5
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center">
            <span class="hidden sm:inline">Confirmar</span>
            <span class="sm:hidden">5</span>
          </span>
        </div>
      </div>
    </div>

    <!-- Paso 1: Seleccionar Servicio -->
    <div v-if="pasoActual === 1" class="paso-content">
      <h2>Selecciona un servicio</h2>
      <div v-if="cargandoServicios" class="loading">Cargando servicios...</div>
      <div v-else-if="servicios.length === 0" class="empty-state">
        No hay servicios disponibles
      </div>
      <div v-else class="servicios-grid">
        <div
          v-for="servicio in servicios"
          :key="servicio.id"
          class="servicio-card"
          @click="seleccionarServicio(servicio)"
        >
          <h3>{{ servicio.nombre }}</h3>
          <p v-if="servicio.descripcion" class="descripcion">{{ servicio.descripcion }}</p>
          <div class="servicio-details">
            <span class="duracion">⏱️ {{ servicio.duracionMinutos }} min</span>
            <span class="precio">${{ servicio.precio }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 2: Seleccionar Profesional -->
    <div v-if="pasoActual === 2" class="paso-content">
      <h2>Selecciona un profesional</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(1)">← Volver</button>
      
      <div v-if="cargandoProfesionales" class="loading">Cargando profesionales...</div>
      <div v-else-if="profesionales.length === 0" class="empty-state">
        No hay profesionales disponibles para este servicio
      </div>
      <div v-else class="profesionales-list">
        <div
          v-for="profesional in profesionales"
          :key="profesional.id"
          class="profesional-card"
          @click="seleccionarProfesional(profesional)"
        >
          <div class="profesional-info">
            <h3>{{ profesional.nombre }} {{ profesional.apellido }}</h3>
            <p v-if="profesional.descripcion">{{ profesional.descripcion }}</p>
          </div>
          <span class="arrow">→</span>
        </div>
      </div>
    </div>

    <!-- Paso 3: Seleccionar Fecha -->
    <div v-if="pasoActual === 3" class="paso-content">
      <h2>Selecciona una fecha</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(2)">← Volver</button>
      
      <div class="calendario-container">
        <div class="calendario-header">
          <button @click="mesAnterior" :disabled="!puedeMesAnterior">←</button>
          <h3>{{ mesActualNombre }} {{ añoActual }}</h3>
          <button @click="mesSiguiente" :disabled="!puedeMesSiguiente">→</button>
        </div>
        
        <div class="calendario-grid">
          <div class="dia-header" v-for="dia in diasSemana" :key="dia">{{ dia }}</div>
          <div
            v-for="(dia, index) in diasMes"
            :key="index"
            :class="['dia-cell', {
              'disabled': dia.disabled,
              'selected': dia.fecha === fechaSeleccionada,
              'hoy': dia.esHoy
            }]"
            @click="!dia.disabled && seleccionarFecha(dia.fecha)"
          >
            <span v-if="dia.numero">{{ dia.numero }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 4: Seleccionar Hora -->
    <div v-if="pasoActual === 4" class="paso-content">
      <h2>Selecciona un horario</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(3)">← Volver</button>
      
      <p class="fecha-seleccionada">📅 {{ fechaSeleccionadaFormateada }}</p>
      
      <div v-if="cargandoSlots" class="loading">Cargando horarios disponibles...</div>
      <div v-else-if="slots.length === 0" class="empty-state">
        No hay horarios disponibles para esta fecha
      </div>
      <div v-else class="slots-list">
        <button
          v-for="slot in slots"
          :key="slot.horaInicio"
          class="slot-btn"
          @click="seleccionarSlot(slot)"
        >
          {{ formatearHora(slot.horaInicio) }}
        </button>
      </div>
    </div>

    <!-- Paso 5: Confirmación -->
    <div v-if="pasoActual === 5" class="paso-content">
      <h2>Confirma tu reserva</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(4)">← Volver</button>
      
      <div class="resumen-reserva">
        <div class="resumen-item">
          <strong>Servicio:</strong>
          <span>{{ servicioSeleccionado?.nombre }}</span>
        </div>
        <div class="resumen-item">
          <strong>Duración:</strong>
          <span>{{ servicioSeleccionado?.duracionMinutos }} minutos</span>
        </div>
        <div class="resumen-item">
          <strong>Precio:</strong>
          <span>${{ servicioSeleccionado?.precio }}</span>
        </div>
        <div class="resumen-item">
          <strong>Profesional:</strong>
          <span>{{ profesionalSeleccionado?.nombre }} {{ profesionalSeleccionado?.apellido }}</span>
        </div>
        <div class="resumen-item">
          <strong>Fecha:</strong>
          <span>{{ fechaSeleccionadaFormateada }}</span>
        </div>
        <div class="resumen-item">
          <strong>Hora:</strong>
          <span>{{ slotSeleccionado ? formatearHora(slotSeleccionado.horaInicio) : '' }}</span>
        </div>
      </div>

      <!-- Cliente autenticado: datos pre-llenados -->
      <div v-if="clienteAutenticado" class="cliente-autenticado-box">
        <div class="bg-gradient-to-r from-green-50 to-emerald-50 border-2 border-green-200 rounded-lg p-6 mb-6">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
              </div>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-bold text-green-900 mb-2">Reservando como usuario registrado</h3>
              <div class="space-y-1 text-sm text-green-800">
                <p><strong>Nombre:</strong> {{ clienteStore.cliente?.nombre }}</p>
                <p><strong>Teléfono:</strong> {{ clienteStore.cliente?.telefono }}</p>
                <p v-if="clienteStore.cliente?.email"><strong>Email:</strong> {{ clienteStore.cliente.email }}</p>
              </div>
              <div class="mt-3 flex items-center gap-2 text-xs text-green-700">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <span>Este turno se agregará automáticamente a tu historial</span>
              </div>
            </div>
          </div>
        </div>
        <button @click="confirmarReserva" class="btn-primary btn-confirmar" :disabled="cargando">
          <span v-if="!cargando">✓ Confirmar Turno</span>
          <span v-else>Procesando...</span>
        </button>
      </div>

      <!-- Cliente invitado: formulario completo -->
      <div v-else class="formulario-cliente">
        <h3>Tus datos</h3>
        <form @submit.prevent="confirmarReserva">
          <div class="form-group">
            <label>Nombre *</label>
            <input v-model="datosCliente.nombre" type="text" required placeholder="Ingresa tu nombre completo" />
          </div>
          <div class="form-group">
            <label>Teléfono *</label>
            <input v-model="datosCliente.telefono" type="tel" required placeholder="Ej: +54 9 11 1234-5678" />
          </div>
          <div class="form-group">
            <label>Email (opcional)</label>
            <input v-model="datosCliente.email" type="email" placeholder="tu@email.com" />
          </div>
          
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input v-model="datosCliente.aceptaCondiciones" type="checkbox" required />
              <span>Acepto la política de cancelación e inasistencias *</span>
            </label>
          </div>
          
          <button type="submit" class="btn-primary btn-confirmar" :disabled="cargando">
            <span v-if="!cargando">Confirmar Turno</span>
            <span v-else>Procesando...</span>
          </button>
        </form>
      </div>
    </div>

    <!-- Modal de confirmación final -->
    <div v-if="mostrarModalFinal" class="modal-overlay" @click="cerrarModal">
      <div class="modal-content" @click.stop>
        <h2>✅ ¡Turno reservado exitosamente!</h2>
        
        <div v-if="turnoCreado" class="resumen-completo">
          <div class="turno-numero">
            <span class="label">Número de turno:</span>
            <span class="numero">#{{ turnoCreado.id }}</span>
          </div>
          
          <hr />
          
          <p><strong>Servicio:</strong> {{ turnoCreado.servicioNombre }}</p>
          <p><strong>Profesional:</strong> {{ turnoCreado.profesionalNombre }} {{ turnoCreado.profesionalApellido }}</p>
          <p><strong>Fecha:</strong> {{ formatearFechaCompleta(turnoCreado.fecha) }}</p>
          <p><strong>Hora de inicio:</strong> {{ turnoCreado.horaInicio }}</p>
          <p><strong>Duración:</strong> {{ turnoCreado.duracionMinutos }} minutos</p>
          <p><strong>Precio:</strong> ${{ turnoCreado.precio }}</p>
          <p><strong>Estado:</strong> <span class="badge-estado">{{ turnoCreado.estado }}</span></p>
          
          <hr />
          
          <p><strong>Cliente:</strong> {{ turnoCreado.clienteNombre }}</p>
          <p><strong>Teléfono:</strong> {{ turnoCreado.clienteTelefono }}</p>
          <p v-if="turnoCreado.clienteEmail"><strong>Email:</strong> {{ turnoCreado.clienteEmail }}</p>
        </div>
        
        <div class="info-box">
          <p class="info-text">
            📱 Recibirás una confirmación por WhatsApp/SMS al número registrado.
          </p>
        </div>
        
        <div class="cta-cuenta">
          <div class="cta-icon">🎯</div>
          <h3>¿Querés gestionar tus turnos más fácil?</h3>
          <p>Creá tu cuenta y:</p>
          <ul>
            <li>✓ Visualizá todos tus turnos en un solo lugar</li>
            <li>✓ Cancelá o modificá turnos cuando quieras</li>
            <li>✓ Recibí recordatorios automáticos</li>
            <li>✓ Accedé a tu historial completo</li>
          </ul>
          <button class="btn-secondary" @click="irCrearCuenta">Crear mi cuenta gratis</button>
        </div>
        
        <div class="modal-actions">
          <button class="btn-outline" @click="finalizarYSalir">Finalizar</button>
          <button class="btn-primary" @click="reservarOtroTurno">Reservar otro turno</button>
        </div>
      </div>
    </div>

    <!-- Modal de teléfono registrado (detección pasiva) -->
    <div v-if="mostrarModalTelefonoRegistrado" class="modal-overlay" @click="cerrarModalTelefono">
      <div class="modal-content modal-telefono-registrado" @click.stop>
        <div class="flex items-center mb-4">
          <svg class="w-12 h-12 text-blue-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 class="text-lg font-bold text-gray-900">Teléfono ya registrado</h3>
            <p class="text-sm text-gray-600 mt-1">
              El teléfono {{ telefonoConflicto }} tiene una cuenta activa
            </p>
          </div>
        </div>
        
        <p class="text-gray-700 mb-4">
          Detectamos que este número de teléfono ya tiene una cuenta registrada. 
          Podés elegir:
        </p>
        
        <div class="options-container mb-4">
          <div class="option-card">
            <div class="option-icon">🔐</div>
            <h4>Iniciar Sesión (Recomendado)</h4>
            <p>Accedé a tu cuenta para ver tu historial de turnos y gestionar tus reservas.</p>
          </div>
          <div class="option-card">
            <div class="option-icon">👤</div>
            <h4>Continuar como Invitado</h4>
            <p>Reservá sin iniciar sesión. Este turno no se asociará a tu cuenta.</p>
          </div>
        </div>
        
        <div class="flex gap-3">
          <button 
            @click="irALoginDesdeConflicto"
            class="flex-1 bg-blue-600 text-white px-4 py-3 rounded-md hover:bg-blue-700 font-medium transition-colors shadow-sm"
          >
            Iniciar Sesión
          </button>
          <button 
            @click="continuarComoInvitado"
            class="flex-1 bg-gray-200 text-gray-700 px-4 py-3 rounded-md hover:bg-gray-300 font-medium transition-colors"
          >
            Continuar sin cuenta
          </button>
        </div>
        
        <button 
          @click="cerrarModalTelefono"
          class="mt-3 w-full text-gray-500 hover:text-gray-700 text-sm underline"
        >
          Cancelar y volver
        </button>
      </div>
    </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useClienteStore } from '@/stores/cliente'
import api from '@/services/api'
import publicoService, { 
  type EmpresaPublica, 
  type ServicioPublico, 
  type ProfesionalPublico,
  type SlotDisponible,
  type CrearTurnoRequest,
  type TurnoResponse
} from '@/services/publico'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

// Estado
const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
const pasoActual = ref(1)

// Paso 1: Servicios
const servicios = ref<ServicioPublico[]>([])
const servicioSeleccionado = ref<ServicioPublico | null>(null)
const cargandoServicios = ref(false)

// Paso 2: Profesionales
const profesionales = ref<ProfesionalPublico[]>([])
const profesionalSeleccionado = ref<ProfesionalPublico | null>(null)
const cargandoProfesionales = ref(false)

// Paso 3: Fecha
const mesActual = ref(new Date().getMonth())
const añoActual = ref(new Date().getFullYear())
const fechaSeleccionada = ref<Date | null>(null)

// Paso 4: Slots
const slots = ref<SlotDisponible[]>([])
const slotSeleccionado = ref<SlotDisponible | null>(null)
const cargandoSlots = ref(false)

// Paso 5: Datos cliente
const usuarioLogueado = ref(false) // TODO: integrar con auth store
const datosCliente = ref({
  nombre: '',
  telefono: '',
  email: '',
  aceptaCondiciones: false
})

const mostrarModalFinal = ref(false)
const turnoCreado = ref<TurnoResponse | null>(null)
const cargando = ref(false)
const mensajeError = ref<string | null>(null)

// Modal de teléfono registrado
const mostrarModalTelefonoRegistrado = ref(false)
const telefonoConflicto = ref('')

// Computed
const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const mesesNombre = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
]

const mesActualNombre = computed(() => mesesNombre[mesActual.value])

const diasMes = computed(() => {
  const primerDia = new Date(añoActual.value, mesActual.value, 1)
  const ultimoDia = new Date(añoActual.value, mesActual.value + 1, 0)
  const diasEnMes = ultimoDia.getDate()
  const primerDiaSemana = primerDia.getDay()

  const dias: Array<{ numero: number | null; fecha: string | null; disabled: boolean; esHoy: boolean }> = []

  // Días vacíos al inicio
  for (let i = 0; i < primerDiaSemana; i++) {
    dias.push({ numero: null, fecha: null, disabled: true, esHoy: false })
  }

  // Días del mes
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0, 0, 0, 0)

  for (let dia = 1; dia <= diasEnMes; dia++) {
    const fecha = new Date(añoActual.value, mesActual.value, dia)
    fecha.setHours(0, 0, 0, 0)
    const fechaStr = formatearFechaISO(fecha)
    const esHoy = fecha.getTime() === hoy.getTime()
    const disabled = fecha < hoy || fecha > fechaMaxima

    dias.push({ numero: dia, fecha: fechaStr, disabled, esHoy })
  }

  return dias
})

const puedeMesAnterior = computed(() => {
  const primeroDelMes = new Date(añoActual.value, mesActual.value, 1)
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  return primeroDelMes > hoy
})

const puedeMesSiguiente = computed(() => {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const ultimoDelMes = new Date(añoActual.value, mesActual.value + 1, 0)
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  return ultimoDelMes < fechaMaxima
})

const fechaSeleccionadaFormateada = computed(() => {
  if (!fechaSeleccionada.value) return ''
  return fechaSeleccionada.value.toLocaleDateString('es-AR', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
})

// Métodos
onMounted(async () => {
  try {
    empresa.value = await publicoService.obtenerEmpresa(empresaSlug.value)
    
    // Intentar recuperar sesión activa si no está en el store
    if (!clienteStore.isAuthenticated) {
      try {
        const response = await api.obtenerPerfilCliente()
        if (response.data.exito) {
          clienteStore.setCliente(response.data.datos)
        }
      } catch {
        // Sin sesión activa, continuar como invitado
      }
    }
    
    await cargarServicios()
  } catch (error: any) {
    alert('Error al cargar la empresa: ' + (error.response?.data?.mensaje || error.message))
    router.push('/')
  }
})

async function cargarServicios() {
  try {
    cargandoServicios.value = true
    servicios.value = await publicoService.obtenerServicios(empresaSlug.value)
  } catch (error: any) {
    alert('Error al cargar servicios: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoServicios.value = false
  }
}

async function seleccionarServicio(servicio: ServicioPublico) {
  servicioSeleccionado.value = servicio
  pasoActual.value = 2
  await cargarProfesionales()
}

async function cargarProfesionales() {
  if (!servicioSeleccionado.value) return
  
  try {
    cargandoProfesionales.value = true
    profesionales.value = await publicoService.obtenerProfesionales(
      empresaSlug.value,
      servicioSeleccionado.value.id
    )
  } catch (error: any) {
    alert('Error al cargar profesionales: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoProfesionales.value = false
  }
}

function seleccionarProfesional(profesional: ProfesionalPublico) {
  profesionalSeleccionado.value = profesional
  pasoActual.value = 3
}

function mesAnterior() {
  if (mesActual.value === 0) {
    mesActual.value = 11
    añoActual.value--
  } else {
    mesActual.value--
  }
}

function mesSiguiente() {
  if (mesActual.value === 11) {
    mesActual.value = 0
    añoActual.value++
  } else {
    mesActual.value++
  }
}

async function seleccionarFecha(fecha: string | null) {
  if (!fecha) return
  fechaSeleccionada.value = new Date(fecha + 'T00:00:00')
  pasoActual.value = 4
  await cargarSlots()
}

async function cargarSlots() {
  if (!servicioSeleccionado.value || !profesionalSeleccionado.value || !fechaSeleccionada.value) return
  
  const servicio = servicioSeleccionado.value!
  const profesional = profesionalSeleccionado.value!
  const fecha = fechaSeleccionada.value!
  const fechaISO: string = fecha.toISOString().split('T')[0]!
  
  try {
    cargandoSlots.value = true
    slots.value = await publicoService.obtenerDisponibilidad(
      empresaSlug.value,
      servicio.id,
      profesional.id,
      fechaISO
    )
  } catch (error: any) {
    alert('Error al cargar horarios: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoSlots.value = false
  }
}

function seleccionarSlot(slot: SlotDisponible) {
  slotSeleccionado.value = slot
  pasoActual.value = 5
}

async function confirmarReserva() {
  if (!usuarioLogueado.value) {
    // Validar datos del cliente
    if (!datosCliente.value.nombre || !datosCliente.value.telefono || 
        !datosCliente.value.aceptaCondiciones) {
      alert('Por favor completa todos los campos obligatorios')
      return
    }

    // Detección pasiva: verificar si el teléfono tiene cuenta registrada
    try {
      const tieneUsuario = await publicoService.verificarTelefonoRegistrado(
        empresaSlug.value,
        datosCliente.value.telefono
      )
      
      if (tieneUsuario) {
        // Mostrar modal informativo sin bloquear
        telefonoConflicto.value = datosCliente.value.telefono
        mostrarModalTelefonoRegistrado.value = true
        return // Esperar decisión del usuario
      }
    } catch (error) {
      // Si falla la verificación, continuar con la reserva
      console.warn('No se pudo verificar el teléfono, continuando...')
    }
  }

  // Crear turno real
  await procesarReserva()
}

async function procesarReserva() {
  try {
    cargando.value = true
    mensajeError.value = null

    if (!servicioSeleccionado.value || !profesionalSeleccionado.value || 
        !fechaSeleccionada.value || !slotSeleccionado.value) {
      alert('Datos de reserva incompletos')
      return
    }

    const servicio = servicioSeleccionado.value!
    const profesional = profesionalSeleccionado.value!
    const fecha = fechaSeleccionada.value!
    const slot = slotSeleccionado.value!

    const fechaISO: string = fecha.toISOString().split('T')[0]!
    const horaPartes = slot.horaInicio.split('T')
    const horaISO: string = (horaPartes[1]?.substring(0, 5) || slot.horaInicio.substring(0, 5))

    // Si está autenticado, usar datos del store; si no, del formulario
    const request: CrearTurnoRequest = {
      servicioId: servicio.id,
      profesionalId: profesional.id,
      fecha: fechaISO,
      horaInicio: horaISO,
      nombreCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.nombre 
        : datosCliente.value.nombre,
      telefonoCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.telefono 
        : datosCliente.value.telefono,
      emailCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.email 
        : (datosCliente.value.email?.trim() || undefined)
    }

    const respuesta = await publicoService.crearTurno(empresaSlug.value, request)
    turnoCreado.value = respuesta
    
    // Mostrar modal de confirmación con datos reales
    mostrarModalFinal.value = true
  } catch (err: any) {
    console.error('Error al crear turno:', err)
    mensajeError.value = err.response?.data?.mensaje || 'Error al crear la reserva. Por favor intenta nuevamente.'
    alert(mensajeError.value)
  } finally {
    cargando.value = false
  }
}

function cerrarModal() {
  mostrarModalFinal.value = false
}

function cerrarModalTelefono() {
  mostrarModalTelefonoRegistrado.value = false
  telefonoConflicto.value = ''
}

function continuarComoInvitado() {
  // Cerrar modal y procesar la reserva de todos modos
  mostrarModalTelefonoRegistrado.value = false
  procesarReserva()
}

function irALoginDesdeConflicto() {
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug.value },
    query: { 
      redirect: `/reservar/${empresaSlug.value}`,
      telefono: telefonoConflicto.value
    }
  })
}

function finalizarYSalir() {
  mostrarModalFinal.value = false
  // Redirigir al login de cliente
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug.value }
  })
}

function reservarOtroTurno() {
  // Resetear formulario pero mantener la empresa
  mostrarModalFinal.value = false
  servicioSeleccionado.value = null
  profesionalSeleccionado.value = null
  fechaSeleccionada.value = null
  slotSeleccionado.value = null
  slots.value = []
  profesionales.value = []
  datosCliente.value = {
    nombre: '',
    telefono: '',
    email: '',
    aceptaCondiciones: false
  }
  turnoCreado.value = null
  pasoActual.value = 1
  // Scroll al inicio
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function irCrearCuenta() {
  if (turnoCreado.value && datosCliente.value.telefono) {
    // Redirigir a registro de cliente con el teléfono pre-cargado
    router.push({
      name: 'RegistroCliente',
      params: { empresaSlug: empresaSlug.value },
      query: { telefono: datosCliente.value.telefono }
    })
  } else {
    cerrarModal()
    router.push({
      name: 'RegistroCliente',
      params: { empresaSlug: empresaSlug.value }
    })
  }
}

function volverPaso(paso: number) {
  pasoActual.value = paso
}

function formatearFechaISO(fecha: Date): string {
  const año = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${año}-${mes}-${dia}`
}

function formatearFechaCompleta(fechaISO: string): string {
  const fecha = new Date(fechaISO + 'T00:00:00')
  const dia = fecha.getDate()
  const mes = mesesNombre[fecha.getMonth()]
  const año = fecha.getFullYear()
  const diaSemana = diasSemana[fecha.getDay()]
  return `${diaSemana} ${dia} de ${mes} ${año}`
}

function formatearHora(isoString: string): string {
  const fecha = new Date(isoString)
  return fecha.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })
}

// Computed para verificar si el cliente está autenticado
const clienteAutenticado = computed(() => clienteStore.isAuthenticated)

// Métodos de navegación de autenticación
function irALogin() {
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug.value }
  })
}

function irARegistro() {
  router.push({
    name: 'RegistroCliente',
    params: { empresaSlug: empresaSlug.value }
  })
}

function irAMisTurnos() {
  router.push({
    name: 'MisTurnos',
    params: { empresaSlug: empresaSlug.value }
  })
}

async function cerrarSesion() {
  await clienteStore.logout()
  // Recargar la página para actualizar el estado
  window.location.reload()
}

</script>

<style scoped>
/* Los estilos del header y stepper ahora usan Tailwind CSS */
/* Solo mantenemos estilos para componentes internos */

.paso-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.paso-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
}

.btn-back {
  margin-bottom: 20px;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* Servicios Grid */
.servicios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.servicio-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.servicio-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
  transform: translateY(-2px);
}

.servicio-card h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.servicio-card .descripcion {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.servicio-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.duracion {
  color: #666;
}

.precio {
  font-size: 20px;
  color: #28a745;
}

/* Profesionales List */
.profesionales-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.profesional-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.profesional-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
}

.profesional-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.profesional-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.arrow {
  font-size: 24px;
  color: #007bff;
}

/* Calendario */
.calendario-container {
  max-width: 600px;
  margin: 0 auto;
}

.calendario-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendario-header button {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 18px;
}

.calendario-header button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.calendario-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.dia-header {
  text-align: center;
  font-weight: bold;
  padding: 10px;
  color: #666;
}

.dia-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.dia-cell:not(.disabled):hover {
  border-color: #007bff;
  background: #e7f3ff;
}

.dia-cell.disabled {
  color: #ccc;
  cursor: not-allowed;
  background: #f8f9fa;
}

.dia-cell.selected {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.dia-cell.hoy {
  border-color: #28a745;
  font-weight: bold;
}

/* Slots List */
.fecha-seleccionada {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.slots-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.slot-btn {
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.slot-btn:hover {
  border-color: #007bff;
  background: #e7f3ff;
  transform: translateY(-2px);
}

/* Resumen */
.resumen-reserva {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.resumen-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}

.resumen-item:last-child {
  border-bottom: none;
}

.formulario-cliente {
  margin-top: 30px;
}

.formulario-cliente h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 4px;
  font-size: 16px;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
}

.info-text {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 4px;
  padding: 15px;
  margin: 20px 0;
  color: #856404;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-outline {
  background: transparent;
  color: #6c757d;
  border: 2px solid #6c757d;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.btn-outline:hover {
  background: #6c757d;
  color: white;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.btn-confirmar {
  width: 100%;
  padding: 15px;
  font-size: 18px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  color: #28a745;
}

.resumen-completo {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.resumen-completo p {
  margin: 10px 0;
}

.resumen-completo hr {
  margin: 15px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}

.turno-numero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.turno-numero .label {
  font-size: 14px;
  font-weight: normal;
}

.turno-numero .numero {
  font-size: 24px;
  font-weight: bold;
}

.badge-estado {
  background: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.info-box {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.info-text {
  margin: 0;
  color: #1976d2;
  font-size: 14px;
}

.cta-cuenta {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  padding: 25px;
  border-radius: 12px;
  margin: 20px 0;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.cta-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.cta-cuenta h3 {
  color: #2d3436;
  margin: 10px 0;
  font-size: 20px;
}

.cta-cuenta p {
  color: #636e72;
  margin: 10px 0;
  font-size: 16px;
}

.cta-cuenta ul {
  list-style: none;
  padding: 0;
  margin: 15px 0;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.cta-cuenta li {
  padding: 8px 0;
  color: #2d3436;
  font-size: 14px;
}

.btn-secondary {
  background: #6c5ce7;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  margin-top: 15px;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #5f4dd1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
}

.advertencia {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

/* Modal de teléfono registrado */
.modal-telefono-registrado {
  max-width: 550px;
}

.modal-telefono-registrado .flex {
  display: flex;
}

.modal-telefono-registrado .items-center {
  align-items: center;
}

.modal-telefono-registrado .mb-4 {
  margin-bottom: 1rem;
}

.modal-telefono-registrado .mr-3 {
  margin-right: 0.75rem;
}

.modal-telefono-registrado .mt-1 {
  margin-top: 0.25rem;
}

.modal-telefono-registrado .w-12 {
  width: 3rem;
}

.modal-telefono-registrado .h-12 {
  height: 3rem;
}

.modal-telefono-registrado .text-blue-500 {
  color: #3b82f6;
}

.modal-telefono-registrado .text-lg {
  font-size: 1.125rem;
}

.modal-telefono-registrado .text-sm {
  font-size: 0.875rem;
}

.modal-telefono-registrado .text-gray-900 {
  color: #111827;
}

.modal-telefono-registrado .text-gray-700 {
  color: #374151;
}

.modal-telefono-registrado .text-gray-600 {
  color: #4b5563;
}

.modal-telefono-registrado .text-gray-500 {
  color: #6b7280;
}

.modal-telefono-registrado .font-bold {
  font-weight: 700;
}

.modal-telefono-registrado .font-medium {
  font-weight: 500;
}

.options-container {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.option-card {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  transition: all 0.2s;
}

.option-card:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.option-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.option-card h4 {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  margin: 0.5rem 0;
}

.option-card p {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.modal-telefono-registrado .gap-3 {
  gap: 0.75rem;
}

.modal-telefono-registrado .flex-1 {
  flex: 1;
}

.modal-telefono-registrado .bg-blue-600 {
  background-color: #2563eb;
}

.modal-telefono-registrado .text-white {
  color: white;
}

.modal-telefono-registrado .px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.modal-telefono-registrado .py-3 {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.modal-telefono-registrado .rounded-md {
  border-radius: 0.375rem;
}

.modal-telefono-registrado .hover\:bg-blue-700:hover {
  background-color: #1d4ed8;
}

.modal-telefono-registrado .bg-gray-200 {
  background-color: #e5e7eb;
}

.modal-telefono-registrado .hover\:bg-gray-300:hover {
  background-color: #d1d5db;
}

.modal-telefono-registrado .shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.modal-telefono-registrado .transition-colors {
  transition-property: color, background-color, border-color;
  transition-duration: 200ms;
}

.modal-telefono-registrado .mt-3 {
  margin-top: 0.75rem;
}

.modal-telefono-registrado .w-full {
  width: 100%;
}

.modal-telefono-registrado .hover\:text-gray-700:hover {
  color: #374151;
}

.modal-telefono-registrado .underline {
  text-decoration: underline;
}
</style>
