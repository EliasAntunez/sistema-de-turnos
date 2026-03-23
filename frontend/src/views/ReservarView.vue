<template>
  <div class="min-h-screen bg-slate-50 bg-[radial-gradient(1200px_500px_at_50%_-20%,rgba(99,102,241,0.08),transparent)]">
    <!-- Header removed: company header moved to unified navbar to avoid duplication -->

    <!-- Contenido principal con padding -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

    <!-- Indicador de pasos (Stepper responsive) -->
    <div class="relative mb-8">
      <div class="flex items-start">
        <!-- Paso 1 -->
        <div class="flex flex-col items-center flex-1 min-w-0">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center text-sm transition-all duration-300',
            pasoActual === 1
              ? 'bg-slate-900 text-white ring-4 ring-slate-900/10 font-bold'
              : pasoActual > 1
                ? 'bg-slate-800 text-white font-semibold'
                : 'bg-slate-100 text-slate-400 border border-slate-200 font-medium'
          ]">
            <span v-if="pasoActual > 1" class="inline-flex">✓</span>
            <span v-else>1</span>
          </div>
          <span :class="[
            'mt-2 text-xs sm:text-sm text-center hidden sm:block',
            pasoActual === 1 ? 'text-slate-900 font-bold' : pasoActual > 1 ? 'text-slate-700 font-semibold' : 'text-slate-400 font-medium'
          ]">Servicio</span>
        </div>

        <div :class="['h-0.5 mt-5 flex-1 transition-colors duration-300', pasoActual > 1 ? 'bg-slate-900' : 'bg-slate-200']"></div>

        <!-- Paso 2 -->
        <div class="flex flex-col items-center flex-1 min-w-0">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center text-sm transition-all duration-300',
            pasoActual === 2
              ? 'bg-slate-900 text-white ring-4 ring-slate-900/10 font-bold'
              : pasoActual > 2
                ? 'bg-slate-800 text-white font-semibold'
                : 'bg-slate-100 text-slate-400 border border-slate-200 font-medium'
          ]">
            <span v-if="pasoActual > 2" class="inline-flex">✓</span>
            <span v-else>2</span>
          </div>
          <span :class="[
            'mt-2 text-xs sm:text-sm text-center hidden sm:block',
            pasoActual === 2 ? 'text-slate-900 font-bold' : pasoActual > 2 ? 'text-slate-700 font-semibold' : 'text-slate-400 font-medium'
          ]">Profesional</span>
        </div>

        <div :class="['h-0.5 mt-5 flex-1 transition-colors duration-300', pasoActual > 2 ? 'bg-slate-900' : 'bg-slate-200']"></div>

        <!-- Paso 3 -->
        <div class="flex flex-col items-center flex-1 min-w-0">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center text-sm transition-all duration-300',
            pasoActual === 3
              ? 'bg-slate-900 text-white ring-4 ring-slate-900/10 font-bold'
              : pasoActual > 3
                ? 'bg-slate-800 text-white font-semibold'
                : 'bg-slate-100 text-slate-400 border border-slate-200 font-medium'
          ]">
            <span v-if="pasoActual > 3" class="inline-flex">✓</span>
            <span v-else>3</span>
          </div>
          <span :class="[
            'mt-2 text-xs sm:text-sm text-center hidden sm:block',
            pasoActual === 3 ? 'text-slate-900 font-bold' : pasoActual > 3 ? 'text-slate-700 font-semibold' : 'text-slate-400 font-medium'
          ]">Fecha</span>
        </div>

        <div :class="['h-0.5 mt-5 flex-1 transition-colors duration-300', pasoActual > 3 ? 'bg-slate-900' : 'bg-slate-200']"></div>

        <!-- Paso 4 -->
        <div class="flex flex-col items-center flex-1 min-w-0">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center text-sm transition-all duration-300',
            pasoActual === 4
              ? 'bg-slate-900 text-white ring-4 ring-slate-900/10 font-bold'
              : pasoActual > 4
                ? 'bg-slate-800 text-white font-semibold'
                : 'bg-slate-100 text-slate-400 border border-slate-200 font-medium'
          ]">
            <span v-if="pasoActual > 4" class="inline-flex">✓</span>
            <span v-else>4</span>
          </div>
          <span :class="[
            'mt-2 text-xs sm:text-sm text-center hidden sm:block',
            pasoActual === 4 ? 'text-slate-900 font-bold' : pasoActual > 4 ? 'text-slate-700 font-semibold' : 'text-slate-400 font-medium'
          ]">Hora</span>
        </div>

        <div :class="['h-0.5 mt-5 flex-1 transition-colors duration-300', pasoActual > 4 ? 'bg-slate-900' : 'bg-slate-200']"></div>

        <!-- Paso 5 -->
        <div class="flex flex-col items-center flex-1 min-w-0">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center text-sm transition-all duration-300',
            pasoActual === 5
              ? 'bg-slate-900 text-white ring-4 ring-slate-900/10 font-bold'
              : 'bg-slate-100 text-slate-400 border border-slate-200 font-medium'
          ]">5</div>
          <span :class="[
            'mt-2 text-xs sm:text-sm text-center hidden sm:block',
            pasoActual === 5 ? 'text-slate-900 font-bold' : 'text-slate-400 font-medium'
          ]">Confirmar</span>
        </div>
      </div>
    </div>

    <Transition name="fade" mode="out-in">
    <div :key="`paso-${pasoActual}`">
    <!-- Paso 1: Seleccionar Servicio -->
    <div v-if="pasoActual === 1" class="paso-content">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 sm:mb-8">
        <h2 class="flex items-center gap-2 text-2xl sm:text-3xl font-bold text-slate-900 m-0">
          <svg class="h-7 w-7 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904 9 18l-.813-2.096a2.25 2.25 0 0 0-1.091-1.091L5 14l2.096-.813a2.25 2.25 0 0 0 1.091-1.091L9 10l.813 2.096a2.25 2.25 0 0 0 1.091 1.091L13 14l-2.096.813a2.25 2.25 0 0 0-1.091 1.091ZM18.259 8.715 18 9.75l-.259-1.035a1.875 1.875 0 0 0-.906-.906L15.75 7.5l1.085-.309a1.875 1.875 0 0 0 .906-.906L18 5.25l.259 1.035a1.875 1.875 0 0 0 .906.906l1.085.309-1.085.309a1.875 1.875 0 0 0-.906.906Z" />
          </svg>
          Selecciona un servicio
        </h2>
        <div></div>
      </div>
      <div v-if="cargandoServicios" class="text-center py-10 text-slate-500">Cargando servicios...</div>
      <div v-else-if="servicios.length === 0" class="text-center py-10 text-slate-500">
        No hay servicios disponibles
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="servicio in servicios"
          :key="servicio.id"
          :class="[
            'rounded-2xl border p-4 sm:p-5 cursor-pointer touch-manipulation transition-all duration-300',
            servicioSeleccionado?.id === servicio.id
              ? 'border-indigo-500 bg-indigo-50 ring-2 ring-indigo-500/20 text-indigo-900 shadow-sm'
              : 'border-slate-200 bg-white/70 hover:border-indigo-300 hover:-translate-y-1 hover:shadow-md'
          ]"
          @click="seleccionarServicio(servicio)"
        >
          <h3 class="m-0 mb-2 text-lg sm:text-xl font-semibold text-slate-900">{{ servicio.nombre }}</h3>
          <p v-if="servicio.descripcion" class="mb-4 text-sm leading-5" :class="servicioSeleccionado?.id === servicio.id ? 'text-indigo-800/90' : 'text-slate-600'">{{ servicio.descripcion }}</p>

          <div class="flex flex-wrap gap-2">
            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border"
              :class="servicioSeleccionado?.id === servicio.id ? 'bg-indigo-100 text-indigo-700 border-indigo-200' : 'bg-slate-100 text-slate-700 border-slate-200'">
              <svg class="h-4 w-4" :class="servicioSeleccionado?.id === servicio.id ? 'text-indigo-500' : 'text-slate-500'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6l4 2m6-2a10 10 0 1 1-20 0 10 10 0 0 1 20 0Z" />
              </svg>
              {{ servicio.duracionMinutos }} min
            </span>

            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border"
              :class="servicioSeleccionado?.id === servicio.id ? 'bg-emerald-100 text-emerald-700 border-emerald-200' : 'bg-emerald-50 text-emerald-700 border-emerald-200'">
              {{ formatearMonedaARS(servicio.precio) }}
            </span>

            <span
              v-if="servicio.requiereSena"
              class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border"
              :class="servicioSeleccionado?.id === servicio.id ? 'bg-amber-100 text-amber-800 border-amber-200' : 'bg-amber-50 text-amber-800 border-amber-200'"
            >
              <svg class="h-4 w-4" :class="servicioSeleccionado?.id === servicio.id ? 'text-amber-700' : 'text-amber-700'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0 3.75h.008v.008H12v-.008Zm8.25-.75a8.25 8.25 0 1 1-16.5 0 8.25 8.25 0 0 1 16.5 0Z" />
              </svg>
              {{ servicio.montoSena != null
                ? `Seña ${formatearMonedaARS(servicio.montoSena)}`
                : 'Requiere seña' }}
            </span>

            <span
              v-else
              class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold border"
              :class="servicioSeleccionado?.id === servicio.id ? 'bg-sky-100 text-sky-700 border-sky-200' : 'bg-sky-50 text-sky-700 border-sky-200'"
            >
              Pago en local
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 2: Seleccionar Profesional -->
    <div v-else-if="pasoActual === 2" class="paso-content">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 sm:mb-8">
        <h2 class="flex items-center gap-2 text-2xl sm:text-3xl font-bold text-slate-900 m-0">
          <svg class="h-7 w-7 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.742-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.035.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.203-.578-5.966-1.584A6.062 6.062 0 0 1 6 18.75m12 0a5.971 5.971 0 0 0-.941-3.197M6 18.75a5.971 5.971 0 0 1 .94-3.197m0 0a3 3 0 0 0-4.681 2.72A9.094 9.094 0 0 0 6 18.72m.94-3.197A5.997 5.997 0 0 1 12 12.75a5.997 5.997 0 0 1 5.06 2.773M12 12.75a3.75 3.75 0 1 0 0-7.5 3.75 3.75 0 0 0 0 7.5Z" />
          </svg>
          Selecciona un profesional
        </h2>
        <button class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95" @click="volverPaso(1)">← Volver</button>
      </div>
      
      <div v-if="cargandoProfesionales" class="text-center py-10 text-slate-500">Cargando profesionales...</div>
      <div v-else-if="profesionales.length === 0" class="text-center py-10 text-slate-500">
        No hay profesionales disponibles para este servicio
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div
          v-for="profesional in profesionales"
          :key="profesional.id"
          :class="[
            'rounded-2xl border p-4 sm:p-5 cursor-pointer transition-all duration-300',
            profesionalSeleccionado?.id === profesional.id
              ? 'border-indigo-500 bg-indigo-50 ring-2 ring-indigo-500/20 text-indigo-900 shadow-sm'
              : 'border-slate-200 bg-white/70 hover:border-indigo-300 hover:-translate-y-1 hover:shadow-md'
          ]"
          @click="seleccionarProfesional(profesional)"
        >
          <div class="flex items-center justify-between gap-3">
            <div class="min-w-0">
              <h3 class="text-base sm:text-lg font-semibold text-slate-900 truncate">{{ profesional.nombre }} {{ profesional.apellido }}</h3>
              <p v-if="profesional.descripcion" class="mt-1 text-sm" :class="profesionalSeleccionado?.id === profesional.id ? 'text-indigo-800/90' : 'text-slate-600'">{{ profesional.descripcion }}</p>
            </div>
            <span class="text-slate-400 text-xl">→</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 3: Seleccionar Fecha -->
    <div v-else-if="pasoActual === 3" class="paso-content">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 sm:mb-8">
        <h2 class="flex items-center gap-2 text-2xl sm:text-3xl font-bold text-slate-900 m-0">
          <svg class="h-7 w-7 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2Z" />
          </svg>
          Selecciona una fecha
        </h2>
        <button class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95" @click="volverPaso(2)">← Volver</button>
      </div>
      
      <div class="w-full max-w-[640px] mx-auto">
        <div class="flex items-center justify-between gap-3 mb-4">
          <button
            @click="mesAnterior"
            :disabled="!puedeMesAnterior"
            class="inline-flex items-center justify-center rounded-xl px-3 py-2 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >←</button>
          <h3 class="text-base sm:text-lg font-semibold text-slate-900 text-center">{{ mesActualNombre }} {{ añoActual }}</h3>
          <button
            @click="mesSiguiente"
            :disabled="!puedeMesSiguiente"
            class="inline-flex items-center justify-center rounded-xl px-3 py-2 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >→</button>
        </div>
        
        <div class="grid grid-cols-7 gap-1 sm:gap-2">
          <div class="text-center font-semibold text-slate-500 text-[11px] sm:text-xs py-1" v-for="dia in diasSemana" :key="dia">{{ dia }}</div>
          <div
            v-for="(dia, index) in diasMes"
            :key="index"
            :class="[
              'aspect-square min-h-[38px] sm:min-h-[46px] flex items-center justify-center rounded-xl border text-sm font-semibold transition-all duration-200',
              dia.disabled
                ? 'border-slate-200 bg-slate-50 text-slate-300 cursor-not-allowed'
                : dia.fecha === fechaSeleccionada
                  ? 'bg-indigo-50 border-indigo-500 ring-2 ring-indigo-500/20 text-indigo-900'
                  : dia.esHoy
                    ? 'border-indigo-300 text-slate-800 bg-white'
                    : 'border-slate-200 bg-white text-slate-700 hover:border-indigo-300 hover:-translate-y-0.5 hover:shadow-sm cursor-pointer'
            ]"
            @click="!dia.disabled && seleccionarFecha(dia.fecha)"
          >
            <span v-if="dia.numero">{{ dia.numero }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 4: Seleccionar Hora -->
    <div v-else-if="pasoActual === 4" class="paso-content">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 sm:mb-8">
        <h2 class="flex items-center gap-2 text-2xl sm:text-3xl font-bold text-slate-900 m-0">
          <svg class="h-7 w-7 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m0 0a7.5 7.5 0 1 0-15 0 7.5 7.5 0 0 0 15 0Z" />
          </svg>
          Selecciona un horario
        </h2>
        <button class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95" @click="volverPaso(3)">← Volver</button>
      </div>
      
      <p class="flex items-center justify-center gap-2 text-sm sm:text-base font-semibold text-slate-700 mb-4">
        <svg class="h-5 w-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z" />
        </svg>
        {{ fechaSeleccionadaFormateada }}
      </p>
      
      <div v-if="cargandoSlots" class="text-center py-10 text-slate-500">Cargando horarios disponibles...</div>
      <div v-else-if="slots.length === 0" class="text-center py-10 text-slate-500">
        No hay horarios disponibles para esta fecha
      </div>
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2.5">
        <button
          v-for="slot in slots"
          :key="slot.horaInicio"
          :class="[
            'rounded-xl font-medium px-4 py-2.5 transition-all duration-200 border',
            slotSeleccionado?.horaInicio === slot.horaInicio
              ? 'bg-indigo-50 border-indigo-500 ring-2 ring-indigo-500/20 text-indigo-900'
              : 'bg-white border-slate-200 text-slate-700 hover:border-indigo-300 hover:-translate-y-0.5 hover:shadow-sm'
          ]"
          @click="seleccionarSlot(slot)"
        >
          {{ formatearHora(slot.horaInicio) }}
        </button>
      </div>
    </div>

    <!-- Paso 5: Confirmación -->
    <div v-else-if="pasoActual === 5" class="paso-content">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 sm:mb-8">
        <h2 class="flex items-center gap-2 text-2xl sm:text-3xl font-bold text-slate-900 m-0">
          <svg class="h-7 w-7 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75m6 2.25a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 6.75h.008v.008H12v-.008Z" />
          </svg>
          Confirma tu reserva
        </h2>
        <button class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95" @click="volverPaso(4)">← Volver</button>
      </div>
      
      <div class="bg-slate-50 border border-slate-200 rounded-2xl p-4 sm:p-5 mb-6 space-y-2">
        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2 text-sm sm:text-base"><strong class="text-slate-700">Servicio:</strong><span class="text-slate-900 text-right">{{ servicioSeleccionado?.nombre }}</span></div>
        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2 text-sm sm:text-base"><strong class="text-slate-700">Duración:</strong><span class="text-slate-900 text-right">{{ servicioSeleccionado?.duracionMinutos }} minutos</span></div>
        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2 text-sm sm:text-base"><strong class="text-slate-700">Precio:</strong><span class="text-slate-900 text-right">{{ formatearMonedaARS(servicioSeleccionado?.precio) }}</span></div>
        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2 text-sm sm:text-base"><strong class="text-slate-700">Profesional:</strong><span class="text-slate-900 text-right">{{ profesionalSeleccionado?.nombre }} {{ profesionalSeleccionado?.apellido }}</span></div>
        <div class="flex items-center justify-between gap-3 border-b border-slate-200 pb-2 text-sm sm:text-base"><strong class="text-slate-700">Fecha:</strong><span class="text-slate-900 text-right">{{ fechaSeleccionadaFormateada }}</span></div>
        <div class="flex items-center justify-between gap-3 text-sm sm:text-base"><strong class="text-slate-700">Hora:</strong><span class="text-slate-900 text-right">{{ slotSeleccionado ? formatearHora(slotSeleccionado.horaInicio) : '' }}</span></div>
      </div>

      <div v-if="servicioSeleccionado?.requiereSena" class="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 text-sm text-amber-900 mb-5">
        Este servicio requiere una seña de {{ formatearMonedaARS(servicioSeleccionado?.montoSena) }} para confirmar la reserva. Una vez reservado, coordina el pago con el local.
      </div>

      <!-- Cliente autenticado: datos pre-llenados -->
      <div v-if="clienteAutenticado">
        <div class="bg-slate-50 border border-slate-200 rounded-2xl p-6 mb-6">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-slate-900 rounded-full flex items-center justify-center">
                <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
              </div>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-bold text-slate-900 mb-2">Reservando como usuario registrado</h3>
              <div class="space-y-1 text-sm text-slate-700">
                <p><strong>Nombre:</strong> {{ clienteStore.cliente?.nombre }}</p>
                <p><strong>Teléfono:</strong> {{ clienteStore.cliente?.telefono }}</p>
                <p v-if="clienteStore.cliente?.email"><strong>Email:</strong> {{ clienteStore.cliente.email }}</p>
              </div>
              <div class="mt-3 flex items-center gap-2 text-xs text-slate-600">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <span>Este turno se agregará automáticamente a tu historial</span>
              </div>
            </div>
          </div>
        </div>
        <button @click="confirmarReserva" :disabled="cargando" class="w-full inline-flex items-center justify-center rounded-xl px-5 py-3.5 text-base font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95 disabled:bg-slate-300 disabled:text-slate-100 disabled:hover:scale-100 disabled:cursor-not-allowed">
          <span v-if="!cargando" class="inline-flex items-center gap-2">
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75 10.5 18l9-13.5" />
            </svg>
            Confirmar Turno
          </span>
          <span v-else>Procesando...</span>
        </button>
      </div>

      <!-- Cliente invitado: formulario completo -->
      <div v-else class="mt-6">
        <h3 class="mb-4 text-xl font-semibold text-slate-900">Tus datos</h3>
        <form @submit.prevent="confirmarReserva" class="space-y-5">
          <div class="mb-6">
            <label class="block mb-1.5 text-sm font-semibold text-slate-700">Nombre *</label>
            <input v-model="datosCliente.nombre" type="text" required placeholder="Ingresa tu nombre completo" class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-300" />
          </div>
          <div class="mb-6">
            <label class="block mb-1.5 text-sm font-semibold text-slate-700">Email *</label>
            <input v-model="datosCliente.email" type="email" required placeholder="tu@email.com" class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-300" />
          </div>
          <div class="mb-6">
            <label class="block mb-1.5 text-sm font-semibold text-slate-700">Teléfono (opcional)</label>
            <input v-model="datosCliente.telefono" type="tel" placeholder="Ej: +54 9 11 1234-5678" class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-300" />
          </div>
          
          <div class="mt-6 mb-8">
            <label class="flex items-start gap-2 text-sm text-slate-700 leading-5 cursor-pointer">
              <input v-model="datosCliente.aceptaCondiciones" type="checkbox" required class="mt-0.5 h-4 w-4 rounded border-slate-300 text-slate-900 focus:ring-slate-300" />
              <span>
                Acepto las
                <span 
                  class="ml-1 text-slate-700 underline font-medium cursor-pointer hover:text-slate-900"
                  @click="abrirModalPoliticas"
                >
                  Ver Políticas
                </span>
                *
              </span>
            </label>
          </div>

          <PoliticasModal
            v-if="mostrarModalPoliticas"
            :politicas="politicasActivas"
            @close="mostrarModalPoliticas = false"
          />
          
          <button type="submit" :disabled="cargando" class="w-full inline-flex items-center justify-center rounded-xl px-5 py-3.5 text-base font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95 disabled:bg-slate-300 disabled:text-slate-100 disabled:hover:scale-100 disabled:cursor-not-allowed">
            <span v-if="!cargando">Confirmar Turno</span>
            <span v-else>Procesando...</span>
          </button>
        </form>
      </div>
    </div>
    </div>
    </Transition>

    <!-- Modal de confirmación final -->
    <div v-if="mostrarModalFinal" class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
      <div class="bg-white/95 backdrop-blur-md border border-slate-200 rounded-2xl p-5 sm:p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-sm" @click.stop>
        <h2 class="flex items-center gap-2 text-2xl font-bold text-slate-900">
          <svg class="h-7 w-7" :class="turnoCreado?.estado === 'PENDIENTE_PAGO' ? 'text-amber-600' : 'text-emerald-600'" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75m6 2.25a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
          </svg>
          {{ turnoCreado?.estado === 'PENDIENTE_PAGO' ? '¡Reserva iniciada!' : '¡Turno confirmado!' }}
        </h2>
        <p class="mt-2 text-sm sm:text-base text-slate-600">
          {{ turnoCreado?.estado === 'PENDIENTE_PAGO'
            ? 'Tu turno fue creado correctamente. Para confirmarlo, falta abonar la seña.'
            : 'Tu turno quedó agendado con éxito. ¡Te esperamos!' }}
        </p>
        
        <div v-if="turnoCreado" class="bg-slate-50 border border-slate-200 rounded-2xl p-4 sm:p-5 my-5">
          <div class="flex items-center justify-between gap-3 mb-4">
            <span class="text-sm font-semibold text-slate-700">Detalle de la reserva</span>
            <span class="text-sm text-slate-500">Ref: #{{ turnoCreado.id }}</span>
          </div>
          
          <hr class="border-slate-200" />
          
          <div class="space-y-2 my-3 text-sm text-slate-700">
            <p><strong>Servicio:</strong> {{ turnoCreado.servicioNombre }}</p>
            <p><strong>Profesional:</strong> {{ turnoCreado.profesionalNombre }} {{ turnoCreado.profesionalApellido }}</p>
            <p><strong>Fecha:</strong> {{ formatearFechaCompleta(turnoCreado.fecha) }}</p>
            <p><strong>Hora de inicio:</strong> {{ turnoCreado.horaInicio }}</p>
            <p><strong>Duración:</strong> {{ turnoCreado.duracionMinutos }} minutos</p>
            <p><strong>Precio:</strong> {{ formatearMonedaARS(turnoCreado.precio) }}</p>
            <p><strong>Estado:</strong>
              <span class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium border"
                    :class="turnoCreado.estado === 'PENDIENTE_PAGO' ? 'bg-amber-100 text-amber-800 border-amber-200' : 'bg-emerald-100 text-emerald-800 border-emerald-200'">
                {{ turnoCreado.estado }}
              </span>
            </p>
          </div>
          
          <hr class="border-slate-200" />
          
          <div class="space-y-2 mt-3 text-sm text-slate-700">
            <p v-if="turnoCreado?.clienteNombre"><strong>Cliente:</strong> {{ turnoCreado.clienteNombre }}</p>
            <p v-if="turnoCreado?.clienteTelefono"><strong>Teléfono:</strong> {{ turnoCreado.clienteTelefono }}</p>
            <p v-if="turnoCreado?.clienteEmail"><strong>Email:</strong> {{ turnoCreado.clienteEmail }}</p>
          </div>

          <div v-if="turnoCreado?.estado === 'PENDIENTE_PAGO'" class="mt-5 bg-slate-50 border border-slate-200 rounded-xl p-4">
            <p class="flex items-center gap-2 text-sm font-semibold text-slate-900">
              <svg class="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 8.25h19.5m-18 0A1.5 1.5 0 0 0 2.25 9.75v7.5a1.5 1.5 0 0 0 1.5 1.5h16.5a1.5 1.5 0 0 0 1.5-1.5v-7.5a1.5 1.5 0 0 0-1.5-1.5m-18 0V6.75A1.5 1.5 0 0 1 3.75 5.25h16.5a1.5 1.5 0 0 1 1.5 1.5v1.5" />
              </svg>
              Datos para tu seña
            </p>
            <div class="mt-3 bg-white border border-slate-200 rounded-lg p-3">
              <p class="text-sm text-slate-700 font-mono whitespace-pre-line">{{ obtenerDatosBancarios() }}</p>
            </div>
            <button
              type="button"
              class="mt-4 inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold bg-[#25D366] text-white hover:bg-[#128C7E] transition-all duration-200"
              @click="abrirWhatsApp"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path d="M20.52 3.48A11.79 11.79 0 0 0 12.06 0C5.53 0 .22 5.31.22 11.84c0 2.08.54 4.1 1.57 5.88L0 24l6.45-1.69a11.77 11.77 0 0 0 5.61 1.43h.01c6.53 0 11.84-5.31 11.84-11.84 0-3.16-1.23-6.12-3.39-8.42Zm-8.46 18.2h-.01a9.9 9.9 0 0 1-5.05-1.38l-.36-.22-3.83 1 1.02-3.73-.24-.38a9.89 9.89 0 0 1-1.51-5.23c0-5.45 4.43-9.88 9.88-9.88 2.64 0 5.12 1.03 6.99 2.9a9.81 9.81 0 0 1 2.89 6.98c0 5.45-4.43 9.88-9.88 9.88Zm5.42-7.42c-.3-.15-1.76-.87-2.03-.97-.27-.1-.47-.15-.67.15-.2.3-.77.97-.94 1.17-.17.2-.35.22-.65.07-.3-.15-1.25-.46-2.39-1.46-.88-.79-1.47-1.76-1.64-2.06-.17-.3-.02-.46.13-.61.14-.14.3-.35.45-.52.15-.17.2-.3.3-.5.1-.2.05-.37-.02-.52-.07-.15-.67-1.61-.92-2.2-.24-.58-.49-.5-.67-.5h-.57c-.2 0-.52.07-.79.37-.27.3-1.04 1.02-1.04 2.49 0 1.47 1.07 2.89 1.22 3.09.15.2 2.1 3.2 5.08 4.49.71.31 1.26.49 1.69.62.71.23 1.35.2 1.86.12.57-.09 1.76-.72 2.01-1.42.25-.7.25-1.3.17-1.42-.07-.12-.27-.2-.57-.35Z"/>
              </svg>
              Enviar Comprobante
            </button>
          </div>
        </div>
        
        <div v-if="!clienteAutenticado" class="mt-8 text-center">
          <button
            class="w-full inline-flex items-center justify-center rounded-2xl px-5 py-3.5 text-base font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95"
            @click="irCrearCuenta"
          >
            Crear mi cuenta gratis
          </button>

          <button
            type="button"
            class="mt-3 w-full inline-flex items-center justify-center rounded-xl px-5 py-3 text-sm font-semibold border border-slate-300 text-slate-700 bg-white hover:bg-slate-50 transition-all duration-200"
            @click="reservarOtroTurno"
          >
            Cerrar y volver al inicio
          </button>
        </div>
        
        <div v-else class="mt-6 flex flex-col sm:flex-row gap-3">
          <button
            class="inline-flex items-center justify-center rounded-xl px-5 py-3.5 text-sm font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95"
            @click="router.push({ name: 'MisTurnos', params: { empresaSlug } })"
          >
            Ir a Mis Turnos
          </button>
          <button
            class="inline-flex items-center justify-center rounded-xl px-5 py-3 text-sm font-semibold border border-slate-300 text-slate-700 bg-white hover:bg-slate-50 transition-all duration-200"
            @click="reservarOtroTurno"
          >
            Reservar otro turno
          </button>
        </div>
      </div>
    </div>

    </main>

    <!-- Footer -->
    <Footer 
      v-if="empresa"
      :direccion="empresa.direccion"
      :ciudad="empresa.ciudad"
      :telefono="empresa.telefono"
      :email="empresa.email"
    />
  </div>

  <!-- Toast global -->
  <Toast />

  <!-- Modal: Cuenta registrada (409 email) -->
  <div v-if="mostrarModal409Email" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
    <div class="bg-white rounded-lg shadow-xl max-w-sm w-full p-6">
      <h3 class="text-lg font-bold text-gray-900 mb-3">Cuenta ya registrada</h3>
      <p class="text-sm text-gray-600 mb-6">{{ mensaje409Email }}</p>
      <div class="flex gap-3">
        <button
          @click="descartarModal409"
          class="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 text-sm font-medium transition-colors"
        >
          Cancelar
        </button>
        <button
          @click="confirmarIrAlLogin409"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium transition-colors"
        >
          Ir al Login
        </button>
      </div>
    </div>
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
import Footer from '@/components/Footer.vue'
import PoliticasModal from '@/components/PoliticasModal.vue'
import PoliticasService from '@/services/politicasCancelacion'
import type { PoliticaCancelacion } from '@/types/politicasCancelacion'
import Toast from '@/components/Toast.vue'
import { useToastStore } from '@/composables/useToast'
import { formatCurrencyARS as formatearMonedaARS } from '@/utils/currency'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()
const toastStore = useToastStore()

// Estado para el modal de políticas
const mostrarModalPoliticas = ref(false)
const politicasActivas = ref<PoliticaCancelacion[]>([])

async function abrirModalPoliticas() {
  if (!empresaSlug.value) {
    console.error('[Politicas] No hay slug de empresa, no se puede buscar políticas')
    politicasActivas.value = []
    mostrarModalPoliticas.value = true
    return
  }
  try {
    const politicas = await publicoService.obtenerPoliticasCancelacionActivas(empresaSlug.value)
    if (!Array.isArray(politicas)) {
      console.error('[Politicas] El formato de la respuesta no es un array:', politicas)
      politicasActivas.value = []
      mostrarModalPoliticas.value = true
      return
    }
    if (politicas.length === 0) {
      if (import.meta.env.DEV) console.warn('[Politicas] El backend devolvió un array vacío de políticas activas')
    }
    politicasActivas.value = politicas
    mostrarModalPoliticas.value = true
  } catch (e) {
    console.error('[Politicas] Error al obtener políticas activas:', e)
    politicasActivas.value = []
    mostrarModalPoliticas.value = true
  }
}

// Estado
const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
const empresaIdActual = ref<number | null>(null)
const empresaCargada = ref(false)
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
const fechaSeleccionada = ref<string | null>(null)

// Paso 4: Slots
const slots = ref<SlotDisponible[]>([])
const slotSeleccionado = ref<SlotDisponible | null>(null)
const cargandoSlots = ref(false)

// Paso 5: Datos cliente
const datosCliente = ref({
  nombre: '',
  telefono: '',
  email: '',
  aceptaCondiciones: false
})

const mostrarModalFinal = ref(false)
const turnoCreado = ref<TurnoResponse | null>(null)
const cargando = ref(false)

// Modal de decisión 409 (email registrado)
const mostrarModal409Email = ref(false)
const mensaje409Email = ref('')
const empresaSlug409 = ref('')

function esConflictoEmailRegistrado(mensaje: string) {
  const texto = mensaje.toLowerCase()
  return texto.includes('cuenta registrada') || texto.includes('iniciá sesión') || texto.includes('inicia sesión')
}

function confirmarIrAlLogin409() {
  mostrarModal409Email.value = false
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug409.value },
    query: { redirect: `/reservar/${empresaSlug409.value}` }
  })
}

function descartarModal409() {
  mostrarModal409Email.value = false
}

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
  return primeroDelMes >= hoy // Permite ir al mes anterior si el mes actual es el primero posible
})


const puedeMesSiguiente = computed(() => {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const ultimoDelMes = new Date(añoActual.value, mesActual.value + 1, 0)
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0,0,0,0) // Asegurarse de comparar solo la fecha
  
  // Si el último día del mes actual es anterior o igual a la fecha máxima permitida
  return ultimoDelMes <= fechaMaxima
})

const fechaSeleccionadaFormateada = computed(() => {
  if (!fechaSeleccionada.value) return ''
  const fecha = new Date(`${fechaSeleccionada.value}T00:00:00`)
  return fecha.toLocaleDateString('es-AR', { 
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
    empresaCargada.value = true
    empresaIdActual.value = empresa.value.id
    
    // Intentar recuperar sesión activa de CLIENTE
    try {
      const response = await api.obtenerPerfilCliente()
      if (import.meta.env.DEV) console.debug('[ReservarView] obtenerPerfilCliente response:', response)
      // Solo setear cliente si la respuesta es 200, payload válido y pertenece a esta empresa
      if (response && response.status === 200) {
        const data = response.data?.datos ?? response.data
        if (data && (data.id !== undefined || data.empresaId !== undefined || data.telefono !== undefined)) {
          try {
            // Verificar que NO es un usuario del sistema (profesional, dueño, admin)
            // Usamos 'roles' (array) en lugar del campo deprecated 'rol' (string singular):
            // - ClienteAutenticadoResponse NO tiene campo 'roles' → undefined → es cliente
            // - PerfilUsuarioResponse SÍ tiene 'roles' (ej: ['PROFESIONAL']) → es usuario del sistema
            if (data.roles && Array.isArray(data.roles) && !data.roles.includes('CLIENTE')) {
              // Es un usuario del sistema (PROFESIONAL, DUENO, ADMIN), no setear como cliente
              if (import.meta.env.DEV) console.debug('[ReservarView] Sesión de usuario del sistema detectada, no setear como cliente:', data.roles)
              clienteStore.logout()
            } else if (data.empresaId === empresa.value.id || data.empresaSlug === empresa.value.slug) {
              // Verificar que el perfil recuperado corresponde a la empresa actual
              clienteStore.setCliente(data)
              if (import.meta.env.DEV) console.debug('[ReservarView] cliente seteado en store:', clienteStore.cliente)
              // Precargar datos del cliente en el formulario
              datosCliente.value.nombre = data.nombre || ''
              datosCliente.value.telefono = data.telefono || ''
              datosCliente.value.email = data.email || ''
            } else {
              // Cliente pertenece a otra empresa, logout
              clienteStore.logout()
            }
          } catch (e) {
            // Si por alguna razón no tenemos empresa cargada, logout
            clienteStore.logout()
          }
        } else {
          // Payload inválido, logout
          clienteStore.logout()
        }
      } else {
        // No hay sesión, logout
        clienteStore.logout()
      }
    } catch (error) {
      // Error al obtener perfil, logout
      if (import.meta.env.DEV) console.debug('[ReservarView] Error al obtener perfil, logout:', error)
      clienteStore.logout()
    }
    
    await cargarServicios()
  } catch (error: any) {
    toastStore.showError('Error al cargar la empresa: ' + (error.response?.data?.mensaje || error.message))
    router.push('/')
  }
})

async function cargarServicios() {
  try {
    cargandoServicios.value = true
    servicios.value = await publicoService.obtenerServicios(empresaSlug.value)
  } catch (error: any) {
    toastStore.showError('Error al cargar servicios: ' + (error.response?.data?.mensaje || error.message))
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
    toastStore.showError('Error al cargar profesionales: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoProfesionales.value = false
  }
}

function seleccionarProfesional(profesional: ProfesionalPublico) {
  profesionalSeleccionado.value = profesional
  pasoActual.value = 3
}

function mesAnterior() {
  // Validar si podemos retroceder más allá del mes actual (si hoy es el primer día del mes)
  const hoy = new Date()
  hoy.setHours(0,0,0,0)
  const primerDiaDelMesActual = new Date(añoActual.value, mesActual.value, 1)
  
  if (primerDiaDelMesActual <= hoy) return; // Ya estamos en el mes más antiguo posible

  if (mesActual.value === 0) {
    mesActual.value = 11
    añoActual.value--
  } else {
    mesActual.value--
  }
}

function mesSiguiente() {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0,0,0,0) // Asegurarse de comparar solo la fecha

  const ultimoDiaDelMesActual = new Date(añoActual.value, mesActual.value + 1, 0)

  // Si el último día del mes actual ya supera la fecha máxima permitida, no avanzar
  if (ultimoDiaDelMesActual > fechaMaxima) return;

  if (mesActual.value === 11) {
    mesActual.value = 0
    añoActual.value++
  } else {
    mesActual.value++
  }
}

async function seleccionarFecha(fecha: string | null) {
  if (!fecha) return
  fechaSeleccionada.value = fecha
  pasoActual.value = 4
  await cargarSlots()
}

async function cargarSlots() {
  if (!servicioSeleccionado.value || !profesionalSeleccionado.value || !fechaSeleccionada.value) return
  
  const servicio = servicioSeleccionado.value!
  const profesional = profesionalSeleccionado.value!
  const fecha = fechaSeleccionada.value!
  
  try {
    cargandoSlots.value = true
    slots.value = await publicoService.obtenerDisponibilidad(
      empresaSlug.value,
      servicio.id,
      profesional.id,
      fecha
    )
  } catch (error: any) {
    toastStore.showError('Error al cargar horarios: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoSlots.value = false
  }
}

function seleccionarSlot(slot: SlotDisponible) {
  slotSeleccionado.value = slot
  pasoActual.value = 5
}

async function confirmarReserva() {
  // Si el cliente no está autenticado, realizamos la validación de datos
  if (!clienteAutenticado.value) {
    // Validar datos del cliente (email es obligatorio, teléfono es opcional)
    if (!datosCliente.value.nombre || !datosCliente.value.email || 
        !datosCliente.value.aceptaCondiciones) {
      toastStore.showError('Por favor completa todos los campos obligatorios para continuar.')
      return
    }
  }

  // Procesar la reserva directamente
  await procesarReserva()
}

async function procesarReserva() {
  try {
    cargando.value = true
    mostrarModal409Email.value = false
    mensaje409Email.value = ''
    
    if (!servicioSeleccionado.value || !profesionalSeleccionado.value || 
        !fechaSeleccionada.value || !slotSeleccionado.value) {
      toastStore.showError('Error interno: Faltan datos de reserva.')
      return
    }

    const servicio = servicioSeleccionado.value!
    const profesional = profesionalSeleccionado.value!
    const fecha = fechaSeleccionada.value!
    const slot = slotSeleccionado.value!

    // Extraer solo la hora y minutos del slot.horaInicio (que ya viene en formato ISO)
    // Asegurarse de que sea HH:MM
    const horaISO: string = slot.horaInicio.substring(11, 16)

    // Preparar el objeto de solicitud para crear el turno
    const request: CrearTurnoRequest = {
      servicioId: servicio.id,
      profesionalId: profesional.id,
      fecha,
      horaInicio: horaISO,
      nombreCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.nombre 
        : datosCliente.value.nombre,
      emailCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.email 
        : datosCliente.value.email,
      telefonoCliente: clienteAutenticado.value 
        ? (clienteStore.cliente!.telefono || datosCliente.value.telefono?.trim() || '') 
        : (datosCliente.value.telefono?.trim() || '')
    }

    const respuesta = await publicoService.crearTurno(empresaSlug.value, request)
    turnoCreado.value = respuesta
    
    // Mostrar modal de confirmación con los datos del turno creado
    mostrarModalFinal.value = true
  } catch (err: any) {
    console.error('Error al crear turno:', err)

    const status = err.response?.status
    const mensajeBackend = err.response?.data?.mensaje || ''

    // El modal de login solo aplica al conflicto de cuenta existente por email.
    if (status === 409 && esConflictoEmailRegistrado(mensajeBackend)) {
      mensaje409Email.value = mensajeBackend || 'El email proporcionado pertenece a una cuenta registrada. Por favor, inicia sesión para continuar.'
      empresaSlug409.value = empresaSlug.value
      mostrarModal409Email.value = true
      return
    }

    // Para conflictos de agenda o cualquier otro error de reserva:
    // - nunca abrir el modal de cuenta registrada
    // - mostrar toast visible
    // - refrescar slots cuando el backend indica que el horario dejó de estar disponible
    mostrarModal409Email.value = false
    const msgError = status === 409
      ? 'El horario ya no está disponible'
      : (mensajeBackend || 'Error al crear la reserva. Por favor intenta nuevamente.')

    toastStore.showError(msgError)

    // Si fue un conflicto de slot (409) o una validación de disponibilidad (400),
    // recargar slots para reflejar el estado actual de la agenda.
    if (status === 409 || status === 400) {
      await cargarSlots()
    }
  } finally {
    cargando.value = false
  }
}

function cerrarModal() {
  mostrarModalFinal.value = false
  // Opcionalmente, podrías redirigir o resetear el formulario aquí
}

function finalizarYSalir() {
  if (!clienteAutenticado.value) {
    reservarOtroTurno()
    return
  }

  mostrarModalFinal.value = false
  router.push({
    name: 'MisTurnos',
    params: { empresaSlug: empresaSlug.value }
  })
}

function reservarOtroTurno() {
  // Resetear todo el estado del componente para iniciar una nueva reserva
  mostrarModalFinal.value = false
  servicioSeleccionado.value = null
  profesionalSeleccionado.value = null
  fechaSeleccionada.value = null
  slotSeleccionado.value = null
  slots.value = []
  profesionales.value = []
  datosCliente.value = { // Resetear campos de invitado
    nombre: '',
    telefono: '',
    email: '',
    aceptaCondiciones: false
  }
  turnoCreado.value = null
  pasoActual.value = 1 // Volver al primer paso

  // Scroll suave al principio de la página
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function irCrearCuenta() {
  // Si tenemos datos del cliente (sea del login o del formulario), pre-cargarlos en el registro
  const telefonoParaRegistro = datosCliente.value.telefono || clienteStore.cliente?.telefono || ''
  
  router.push({
    name: 'RegistroCliente',
    params: { empresaSlug: empresaSlug.value },
    query: { telefono: telefonoParaRegistro } // Pasar el teléfono si está disponible
  })
  cerrarModal() // Cerrar el modal de confirmación
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
  // Asegurarse de que la fecha ISO sea procesada correctamente, añadiendo hora 00:00:00
  const fecha = new Date(fechaISO + 'T00:00:00')
  const dia = fecha.getDate()
  const mes = mesesNombre[fecha.getMonth()]
  const año = fecha.getFullYear()
  const diaSemana = diasSemana[fecha.getDay()]
  return `${diaSemana} ${dia} de ${mes} ${año}`
}

function formatearHora(isoString: string): string {
  if (!isoString || isoString.length < 16) return isoString
  return isoString.substring(11, 16)
}

function obtenerDatosBancarios() {
  return empresa.value?.datosBancarios || 'Contacta al local para coordinar el pago.'
}

function abrirWhatsApp() {
  const telefonoRaw = empresa.value?.telefono || ''
  const telefono = telefonoRaw.replace(/\D/g, '')

  if (!telefono) {
    toastStore.showError('No hay un teléfono de contacto configurado para WhatsApp.')
    return
  }

  window.open(`https://wa.me/${telefono}`, '_blank')
}

// Computed para verificar si el cliente está autenticado Y pertenece a la empresa actual
const clienteValido = computed(() => {
  if (!clienteStore.isAuthenticated) return false
  if (!empresaCargada.value || !empresa.value) return false // Asegurarse de que la empresa esté cargada
  const clienteEmpresaId = clienteStore.cliente?.empresaId
  // Verificar que el cliente tenga una empresa asignada y que coincida con la empresa actual
  return clienteEmpresaId !== undefined && clienteEmpresaId !== null && clienteEmpresaId === empresa.value.id
})

// Indica si el cliente está autenticado Y su cuenta está asociada a la empresa actual
const clienteAutenticado = computed(() => clienteStore.isAuthenticated && clienteValido.value)

</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.paso-content {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  padding: 16px;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

@media (min-width: 640px) {
  .paso-content {
    padding: 24px;
  }
}

@media (min-width: 1024px) {
  .paso-content {
    padding: 30px;
  }
}
</style>
