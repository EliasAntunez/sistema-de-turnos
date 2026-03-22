<template>
  <div class="min-h-screen bg-slate-50 pb-24 md:pb-8 md:pl-64">
    <aside class="fixed inset-y-0 left-0 z-40 hidden w-64 border-r border-slate-200 bg-white md:flex md:flex-col">
      <div class="border-b border-slate-100 px-4 py-5">
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Panel Profesional</p>
        <h2 class="mt-1 truncate text-base font-bold text-slate-900">{{ authStore.usuario?.empresaNombre || 'Empresa' }}</h2>
      </div>
      <nav class="flex-1 space-y-1 p-3">
        <button @click="seccionActiva = 'turnos'" :class="['flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors', seccionActiva === 'turnos' ? 'bg-teal-800 text-white' : 'text-slate-700 hover:bg-slate-100']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" /></svg>
          <span>Mis Turnos</span>
        </button>
        <button @click="seccionActiva = 'disponibilidad'" :class="['flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors', seccionActiva === 'disponibilidad' ? 'bg-teal-800 text-white' : 'text-slate-700 hover:bg-slate-100']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 9h10m-12 9h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
          <span>Disponibilidad</span>
        </button>
        <button @click="seccionActiva = 'bloqueos'" :class="['flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors', seccionActiva === 'bloqueos' ? 'bg-teal-800 text-white' : 'text-slate-700 hover:bg-slate-100']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 5.636l-12.728 12.728M5.636 5.636l12.728 12.728" /></svg>
          <span>Bloqueos</span>
        </button>
      </nav>
    </aside>

    <header class="sticky top-0 z-30 bg-teal-800 text-white">
      <div class="flex items-center justify-between gap-3 px-4 py-3 md:px-6">
        <h1 class="min-w-0 truncate text-base font-semibold sm:text-lg">
          <span class="sm:hidden">Panel Profesional</span>
          <span class="hidden sm:inline">{{ authStore.usuario?.empresaNombre || 'Empresa' }} - Panel de Profesional</span>
        </h1>
        <div class="relative flex items-center gap-2" ref="userMenuRef">
          <button v-if="authStore.isDueno" @click="router.push('/dueno')" class="inline-flex items-center gap-2 rounded-md border border-teal-400 bg-teal-700 px-3 py-2 text-xs font-semibold text-white transition hover:bg-teal-600 sm:text-sm" title="Ir a gestión de empresa">
            <span class="hidden sm:inline">Mi Empresa</span>
            <span class="sm:hidden">Empresa</span>
          </button>
          <NotificationBell />
          <button class="inline-flex max-w-[11rem] items-center gap-2 rounded-md border border-teal-500 bg-teal-700 px-3 py-2 text-xs font-semibold text-white transition hover:bg-teal-600 sm:max-w-none sm:text-sm" @click.stop="toggleUserMenu">
            <span class="truncate">{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
            <svg class="h-4 w-4 shrink-0" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.939a.75.75 0 111.08 1.04l-4.25 4.511a.75.75 0 01-1.08 0L5.21 8.269a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
            </svg>
          </button>
          <div v-if="showUserMenu" class="absolute right-0 top-full z-50 mt-2 w-56 max-w-[calc(100vw-1rem)] overflow-hidden rounded-lg border border-slate-200 bg-white shadow-xl" @click.stop>
            <button class="block w-full px-3 py-2 text-left text-sm font-medium text-rose-600 transition hover:bg-rose-50" @click="cerrarSesion">Cerrar Sesión</button>
          </div>
        </div>
      </div>
    </header>

    <main class="main-content">
      <section v-show="seccionActiva === 'disponibilidad'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <div class="mb-5 flex items-center justify-between gap-3">
          <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
            <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25" /></svg>
            Mi Disponibilidad
          </h2>
          <button v-if="disponibilidades.length > 0" @click="abrirModalDisponibilidad()" class="inline-flex items-center rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-700">
            + Agregar Horario
          </button>
        </div>

        <div v-if="loadingDisponibilidad" class="py-10 text-center text-slate-600">Cargando disponibilidad...</div>

        <div v-else-if="disponibilidades.length === 0" class="text-center py-10">
          <div class="mx-auto max-w-lg mb-6 flex items-start gap-4 rounded-xl border border-blue-200 bg-blue-50 p-4 text-left">
            <svg class="h-8 w-8 shrink-0 text-blue-500" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m11.25 11.25.041-.02a.75.75 0 0 1 1.063.852l-.708 2.836a.75.75 0 0 0 1.063.853l.041-.021M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9-3.75h.008v.008H12V8.25Z" /></svg>
            <div>
              <h3 class="text-lg font-semibold text-blue-900">No tenés horarios propios</h3>
              <p class="mt-1 text-sm text-blue-800">La disponibilidad se calcula usando los horarios de la empresa como fallback.</p>
            </div>
          </div>
          <button @click="inicializarDesdeEmpresa" :disabled="submittingInicializar" class="inline-flex items-center rounded-lg bg-teal-800 px-6 py-3 text-base font-semibold text-white shadow-sm transition hover:bg-teal-700 disabled:opacity-50">
            <svg class="mr-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21v-3.375c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21M3 3h12m-.75 4.5H21m-3.75 3.75h.008v.008h-.008v-.008Zm0 3h.008v.008h-.008v-.008Zm0 3h.008v.008h-.008v-.008Z" /></svg>
            {{ submittingInicializar ? 'Inicializando...' : 'Usar Horarios de la Empresa' }}
          </button>
          <div v-if="errorInicializar" class="mt-4 rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorInicializar }}</div>
          
          <div v-if="horariosEmpresa.length > 0" class="mt-10 border-t border-slate-200 pt-8 text-left">
            <h3 class="mb-6 flex items-center text-lg font-semibold text-slate-800">
              <svg class="mr-2 h-5 w-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0 0 13.56 2.25h-3.12a2.25 2.25 0 0 0-2.105 1.638m7.331 0c.066.22.105.45.105.682v.47m-7.331 0c-.04-.232-.06-.462-.06-.682v-.47m7.331 0A2.25 2.25 0 0 1 19.5 6v12a2.25 2.25 0 0 1-2.25 2.25h-10.5A2.25 2.25 0 0 1 4.5 18V6a2.25 2.25 0 0 1 2.25-2.25m7.331 0h-3.12" /></svg>
              Horarios de la Empresa (referencia)
            </h3>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <div v-for="dia in diasSemana" :key="dia" class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                <h4 class="mb-3 font-semibold text-slate-900">{{ nombresDias[dia] }}</h4>
                <div v-if="horariosEmpresaAgrupados[dia] && horariosEmpresaAgrupados[dia].length > 0" class="space-y-2">
                  <div v-for="horario in horariosEmpresaAgrupados[dia]" :key="horario.id" class="text-sm text-slate-600">
                    {{ horario.horaInicio }} - {{ horario.horaFin }}
                  </div>
                </div>
                <div v-else class="text-sm italic text-slate-400">Sin horarios</div>
              </div>
            </div>
          </div>
        </div>

        <div v-else>
          <div class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
            <div v-for="dia in diasSemana" :key="dia" class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
              <div class="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-4 py-3">
                <h3 class="text-sm font-semibold text-slate-800">{{ nombresDias[dia] }}</h3>
                <span v-if="disponibilidadesAgrupadas[dia] && disponibilidadesAgrupadas[dia].length > 0" class="inline-flex rounded-full bg-slate-200 px-2 py-0.5 text-xs font-medium text-slate-700">
                  {{ disponibilidadesAgrupadas[dia].length }}
                </span>
              </div>
              <div>
                <div v-if="disponibilidadesAgrupadas[dia] && disponibilidadesAgrupadas[dia].length > 0">
                  <div v-for="disp in disponibilidadesAgrupadas[dia]" :key="disp.id ?? `temp-${disp.diaSemana}-${disp.horaInicio}`" class="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                    <span class="text-sm font-semibold text-slate-800">{{ disp.horaInicio }} - {{ disp.horaFin }}</span>
                    <div class="flex items-center gap-1">
                      <button @click="abrirModalDisponibilidad(disp)" class="rounded-md p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-teal-600">
                        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" /></svg>
                      </button>
                      <button @click="confirmarEliminarDisponibilidad(disp)" class="rounded-md p-1.5 text-slate-400 transition hover:bg-rose-50 hover:text-rose-600">
                        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" /></svg>
                      </button>
                    </div>
                  </div>
                </div>
                <div v-else class="px-4 py-6 text-center text-sm italic text-slate-400">Sin horarios</div>
              </div>
            </div>
          </div>
          <details class="mt-8 rounded-xl border border-slate-200 bg-slate-50 p-4">
            <summary class="flex cursor-pointer items-center font-semibold text-slate-700 hover:text-teal-700">
              <svg class="mr-2 h-5 w-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0 0 13.56 2.25h-3.12a2.25 2.25 0 0 0-2.105 1.638m7.331 0c.066.22.105.45.105.682v.47m-7.331 0c-.04-.232-.06-.462-.06-.682v-.47m7.331 0A2.25 2.25 0 0 1 19.5 6v12a2.25 2.25 0 0 1-2.25 2.25h-10.5A2.25 2.25 0 0 1 4.5 18V6a2.25 2.25 0 0 1 2.25-2.25m7.331 0h-3.12" /></svg>
              Ver Horarios de la Empresa (referencia)
            </summary>
            <div class="mt-4 grid grid-cols-1 gap-2 sm:grid-cols-2 md:grid-cols-3">
              <div v-for="dia in diasSemana" :key="dia" class="text-sm text-slate-600">
                <strong class="text-slate-900">{{ nombresDias[dia] }}:</strong>
                <span v-if="horariosEmpresaAgrupados[dia] && horariosEmpresaAgrupados[dia].length > 0" class="ml-1">
                  {{ horariosEmpresaAgrupados[dia].map(h => `${h.horaInicio}-${h.horaFin}`).join(', ') }}
                </span>
                <span v-else class="ml-1 italic text-slate-400">Sin horarios</span>
              </div>
            </div>
          </details>
        </div>
      </section>

      <section v-show="seccionActiva === 'turnos'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <div class="mb-5 flex items-center justify-between gap-3">
          <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
            <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25" /></svg>
            Mis Turnos
          </h2>        
        </div>

        <div class="mb-6 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 grid grid-cols-4 gap-2">
            <button
              @click="filtrosTurnos.periodo = 'todos'; onCambioPeriodo()"
              :class="[
                'rounded-lg px-3 py-2 text-center text-sm font-medium transition-colors',
                filtrosTurnos.periodo === 'todos' ? 'bg-teal-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              ]"
            >
              Todos
            </button>
            <button
              @click="filtrosTurnos.periodo = 'hoy'; onCambioPeriodo()"
              :class="[
                'rounded-lg px-3 py-2 text-center text-sm font-medium transition-colors',
                filtrosTurnos.periodo === 'hoy' ? 'bg-teal-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              ]"
            >
              Hoy
            </button>
            <button
              @click="filtrosTurnos.periodo = 'rango'; onCambioPeriodo()"
              :class="[
                'rounded-lg px-3 py-2 text-center text-sm font-medium transition-colors',
                filtrosTurnos.periodo === 'rango' ? 'bg-teal-800 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              ]"
            >
              Rango
            </button>
            <button
              @click="mostrarFiltros = !mostrarFiltros"
              class="inline-flex items-center justify-center rounded-lg bg-slate-100 px-3 py-2 text-center text-sm font-medium text-slate-600 transition hover:bg-slate-200"
              title="Filtros avanzados"
            >
              <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.42 3.47a1 1 0 011.16 0l1.57 1.12a1 1 0 00.87.12l1.88-.75a1 1 0 011.05.24l1.33 1.33a1 1 0 01.24 1.05l-.75 1.88a1 1 0 00.12.87l1.12 1.57a1 1 0 010 1.16l-1.12 1.57a1 1 0 00-.12.87l.75 1.88a1 1 0 01-.24 1.05l-1.33 1.33a1 1 0 01-1.05.24l-1.88-.75a1 1 0 00-.87.12l-1.57 1.12a1 1 0 01-1.16 0l-1.57-1.12a1 1 0 00-.87-.12l-1.88.75a1 1 0 01-1.05-.24l-1.33-1.33a1 1 0 01-.24-1.05l.75-1.88a1 1 0 00-.12-.87L3.47 12.58a1 1 0 010-1.16l1.12-1.57a1 1 0 00.12-.87l-.75-1.88a1 1 0 01.24-1.05l1.33-1.33a1 1 0 011.05-.24l1.88.75a1 1 0 00.87-.12l1.57-1.12z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>

          <div class="mb-4">
            <input
              v-model="filtrosTurnos.clienteNombre"
              @input="aplicarFiltrosTurnos"
              type="text"
              placeholder="Buscar cliente..."
              class="block w-full rounded-lg border border-slate-300 bg-white p-2.5 text-sm text-slate-900 shadow-sm transition-all placeholder:text-slate-400 focus:border-teal-500 focus:ring-teal-500"
            />
          </div>

          <div v-if="filtrosTurnos.periodo === 'rango'" class="mb-4 grid grid-cols-2 gap-3">
            <div class="flex flex-col gap-1">
              <label class="text-xs font-medium text-slate-600">Desde</label>
              <input
                v-model="filtrosTurnos.fechaDesde"
                @change="aplicarFiltrosTurnos"
                type="date"
                class="block w-full rounded-lg border border-slate-300 bg-white p-2.5 text-sm text-slate-900 shadow-sm transition-all focus:border-teal-500 focus:ring-teal-500"
              />
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs font-medium text-slate-600">Hasta</label>
              <input
                v-model="filtrosTurnos.fechaHasta"
                @change="aplicarFiltrosTurnos"
                type="date"
                class="block w-full rounded-lg border border-slate-300 bg-white p-2.5 text-sm text-slate-900 shadow-sm transition-all focus:border-teal-500 focus:ring-teal-500"
              />
            </div>
          </div>

          <div v-show="mostrarFiltros" class="grid grid-cols-1 gap-3 border-t border-slate-100 pt-4 sm:grid-cols-2">
            <div class="flex flex-col gap-1">
              <label class="text-xs font-medium text-slate-600">Estado</label>
              <select
                v-model="filtrosTurnos.estado"
                @change="aplicarFiltrosTurnos"
                class="block w-full rounded-lg border border-slate-300 bg-white p-2.5 text-sm text-slate-900 shadow-sm transition-all focus:border-teal-500 focus:ring-teal-500"
              >
                <option value="">Todos los estados</option>
                <option value="PENDIENTE_PAGO">Seña pendiente</option>
                <option value="PENDIENTE_CONFIRMACION">Pendiente de confirmación</option>
                <option value="CONFIRMADO">Confirmado</option>
                <option value="ATENDIDO">Atendido</option>
                <option value="NO_ASISTIO">No asistió</option>
                <option value="REPROGRAMADO">Reprogramado</option>
                <option value="CANCELADO">Cancelado</option>
              </select>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs font-medium text-slate-600">Servicio</label>
              <select
                v-model="filtrosTurnos.servicioId"
                @change="aplicarFiltrosTurnos"
                class="block w-full rounded-lg border border-slate-300 bg-white p-2.5 text-sm text-slate-900 shadow-sm transition-all focus:border-teal-500 focus:ring-teal-500"
              >
                <option value="">Todos los servicios</option>
                <option v-for="servicio in servicioOpcionesTurnos" :key="servicio.id" :value="servicio.id">
                  {{ servicio.nombre }}
                </option>
              </select>
            </div>
          </div>
        </div>

        <div v-if="cantidadTurnosSinResolver > 0" class="mb-6 flex items-center gap-3 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm font-medium text-amber-800">
          <svg class="h-5 w-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 4h.01M10.29 3.86l-8.18 14.2A1 1 0 003 19.5h18a1 1 0 00.89-1.44l-8.18-14.2a1 1 0 00-1.74 0z" />
          </svg>
          <p>Atención: Tienes {{ cantidadTurnosSinResolver }} turnos en el pasado sin resolver. Actualiza su estado para mantener consistencia.</p>
        </div>

        <div v-if="loadingTurnos" class="loading">Cargando turnos...</div>

        <div v-else-if="turnos.length > 0" class="flex flex-col gap-3 mt-4">
          <div v-for="turno in turnos" :key="turno.id" class="group flex items-start gap-3 sm:gap-4">
            
            <div class="w-14 shrink-0 pt-3 text-right sm:w-20">
              <div class="text-sm font-bold text-slate-800 sm:text-base">{{ turno.horaInicio }}</div>
              <div class="text-xs font-medium text-slate-400">{{ turno.horaFin }}</div>
            </div>

            <div 
              @click="abrirModalDetalle(turno)"
              class="relative flex-1 cursor-pointer overflow-hidden rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition-all hover:border-teal-300 hover:shadow-md"
            >
              <div 
                :class="[
                  'absolute bottom-0 left-0 top-0 w-1.5',
                  turno.estado === 'PENDIENTE_CONFIRMACION' ? 'bg-amber-400' :
                  turno.estado === 'PENDIENTE_PAGO' ? 'bg-yellow-400' :
                  turno.estado === 'CONFIRMADO' ? 'bg-emerald-500' :
                  turno.estado === 'ATENDIDO' ? 'bg-violet-500' :
                  turno.estado === 'NO_ASISTIO' || turno.estado === 'NO_ASISTIDO' ? 'bg-rose-500' :
                  turno.estado === 'CANCELADO' ? 'bg-red-600' :
                  turno.estado === 'REPROGRAMADO' ? 'bg-sky-400' :
                  'bg-slate-400'
                ]"
              ></div>

              <div class="pl-2">
                <div class="flex flex-col items-start justify-between gap-2 sm:flex-row sm:items-center">
                  <div class="min-w-0 flex-1">
                    <h3 class="break-words text-base font-bold text-slate-900 leading-tight">{{ turno.clienteNombre }}</h3>
                    <p class="mt-0.5 text-sm font-medium text-teal-700">{{ turno.servicioNombre }}</p>
                    <p v-if="filtrosTurnos.periodo !== 'hoy'" class="mt-1 text-xs font-medium text-slate-400 uppercase tracking-wider">{{ formatearFechaLegible(turno.fecha) }}</p>
                  </div>
                  
                  <span class="shrink-0 inline-flex items-center rounded-full bg-slate-100 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider text-slate-700">
                    {{ obtenerLabelEstado(turno.estado) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-10 text-slate-500">
          <p>No hay turnos para el período seleccionado</p>
        </div>

        <div v-if="totalPagesTurnos > 0" class="flex justify-center items-center gap-4 mt-8">
          <button
            @click="paginaAnteriorTurnos"
            :disabled="currentPageTurnos === 0 || loadingTurnos"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-white border border-slate-300 text-slate-700 hover:bg-slate-100 disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed transition-all"
          >
            Anterior
          </button>
          <span class="text-sm font-medium text-slate-700">Página {{ currentPageTurnos + 1 }} de {{ totalPagesTurnos }}</span>
          <button
            @click="paginaSiguienteTurnos"
            :disabled="currentPageTurnos >= totalPagesTurnos - 1 || loadingTurnos"
            class="px-4 py-2 rounded-lg text-sm font-medium bg-teal-800 border border-teal-800 text-white hover:bg-teal-700 disabled:bg-slate-100 disabled:border-slate-300 disabled:text-slate-400 disabled:cursor-not-allowed transition-all"
          >
            Siguiente
          </button>
        </div>
      </section>

      <section v-show="seccionActiva === 'bloqueos'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <div class="mb-5 flex items-center justify-between gap-3">
          <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
            <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 0 0 5.636 5.636m12.728 12.728A9 9 0 0 1 5.636 5.636m12.728 12.728L5.636 5.636" /></svg>
            Bloqueos de Fechas
          </h2>
          <button @click="abrirModalBloqueo()" class="inline-flex items-center rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-teal-700">
            + Agregar Bloqueo
          </button>
        </div>

        <div v-if="loadingBloqueos" class="py-10 text-center text-slate-600">Cargando bloqueos...</div>

        <div v-else-if="bloqueos.length > 0" class="flex flex-col gap-4 mt-4">
          <div v-for="bloqueo in bloqueosOrdenados" :key="bloqueo.id" class="flex flex-col sm:flex-row sm:items-center justify-between rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md">
            <div class="flex-1">
              <div class="mb-2 flex flex-wrap items-center gap-2 sm:mb-1">
                <span class="inline-flex rounded-md bg-teal-50 px-2 py-1 text-sm font-bold text-teal-800 ring-1 ring-inset ring-teal-600/20">{{ formatearFecha(bloqueo.fechaInicio) }}</span>
                <span v-if="bloqueo.fechaFin && bloqueo.fechaFin !== bloqueo.fechaInicio" class="text-slate-400">
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" /></svg>
                </span>
                <span v-if="bloqueo.fechaFin && bloqueo.fechaFin !== bloqueo.fechaInicio" class="inline-flex rounded-md bg-teal-50 px-2 py-1 text-sm font-bold text-teal-800 ring-1 ring-inset ring-teal-600/20">{{ formatearFecha(bloqueo.fechaFin) }}</span>
              </div>
              <div v-if="bloqueo.motivo" class="text-sm font-medium text-slate-600">{{ bloqueo.motivo }}</div>
            </div>
            <div class="mt-4 flex items-center gap-2 sm:mt-0 sm:pl-4">
              <button @click="abrirModalBloqueo(bloqueo)" class="rounded-lg border border-slate-300 p-2 text-slate-400 transition hover:bg-slate-50 hover:text-teal-700">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" /></svg>
              </button>
              <button @click="confirmarEliminarBloqueo(bloqueo)" class="rounded-lg border border-slate-300 p-2 text-slate-400 transition hover:bg-rose-50 hover:text-rose-700">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" /></svg>
              </button>
            </div>
          </div>
        </div>

        <div v-else class="rounded-xl border border-slate-200 bg-white p-10 text-center shadow-sm">
          <p class="text-base font-medium text-slate-700">No hay bloqueos configurados</p>
          <p class="mt-1 text-sm text-slate-500">Los bloqueos te permiten marcar fechas donde no estarás disponible.</p>
        </div>
      </section>
    </main>

    <div v-if="showModalDetalleTurno" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalDetalle">
      <div class="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">Detalle del Turno</h2>
          <button @click="cerrarModalDetalle" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>
        <div class="p-6 space-y-4" v-if="turnoDetalleSeleccionado">
          <div class="rounded-xl bg-slate-50 p-4 space-y-2 border border-slate-100">
            <div class="flex justify-between items-start">
              <div>
                <p class="font-bold text-slate-900">{{ turnoDetalleSeleccionado.clienteNombre }}</p>
                <p class="text-sm text-slate-600">{{ turnoDetalleSeleccionado.servicioNombre }}</p>
              </div>
              <span class="inline-flex rounded-full bg-teal-100 px-2.5 py-0.5 text-xs font-semibold text-teal-800 uppercase">{{ obtenerLabelEstado(turnoDetalleSeleccionado.estado) }}</span>
            </div>
            <div class="grid grid-cols-2 gap-4 pt-2 border-t border-slate-200 mt-2 text-sm text-slate-700">
              <div><span class="block text-xs text-slate-500">Fecha</span>{{ formatearFechaLegible(turnoDetalleSeleccionado.fecha) }}</div>
              <div><span class="block text-xs text-slate-500">Horario</span>{{ turnoDetalleSeleccionado.horaInicio }} - {{ turnoDetalleSeleccionado.horaFin }}</div>
              <div><span class="block text-xs text-slate-500">Duración</span>{{ turnoDetalleSeleccionado.duracionMinutos }}m</div>
              <div><span class="block text-xs text-slate-500">Precio</span>{{ formatearMonedaARS(turnoDetalleSeleccionado.precio) }}</div>
            </div>
            <div v-if="turnoDetalleSeleccionado.clienteTelefono || turnoDetalleSeleccionado.clienteEmail" class="pt-2 border-t border-slate-200 mt-2 text-sm text-slate-700">
              <div v-if="turnoDetalleSeleccionado.clienteTelefono" class="flex items-center gap-2">
                <svg class="h-4 w-4 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 6.75c0 7.456 6.044 13.5 13.5 13.5h2.25a2.25 2.25 0 0 0 2.25-2.25v-1.372c0-.516-.351-.966-.852-1.091l-4.423-1.106a1.125 1.125 0 0 0-1.173.417l-.97 1.293a1.125 1.125 0 0 1-1.21.38 12.035 12.035 0 0 1-7.143-7.143 1.125 1.125 0 0 1 .38-1.21l1.293-.97a1.125 1.125 0 0 0 .417-1.173L5.463 2.852A1.125 1.125 0 0 0 4.372 2H3A2.25 2.25 0 0 0 .75 4.25v2.5Z" />
                </svg>
                {{ turnoDetalleSeleccionado.clienteTelefono }}
              </div>
              <div v-if="turnoDetalleSeleccionado.clienteEmail" class="flex items-center gap-2">
                <svg class="h-4 w-4 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 0 1-2.25 2.25h-15a2.25 2.25 0 0 1-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0 0 19.5 4.5h-15a2.25 2.25 0 0 0-2.25 2.25m19.5 0v.243a2.25 2.25 0 0 1-.965 1.852l-7.5 5.25a2.25 2.25 0 0 1-2.57 0l-7.5-5.25A2.25 2.25 0 0 1 2.25 6.993V6.75" />
                </svg>
                {{ turnoDetalleSeleccionado.clienteEmail }}
              </div>
            </div>
          </div>
          <div v-if="turnoDetalleSeleccionado.observaciones" class="rounded-lg bg-amber-50 p-3 text-sm text-amber-800 border border-amber-100">
            <strong>Observaciones:</strong> <br/> {{ turnoDetalleSeleccionado.observaciones }}
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-4">
             <select v-if="estadosDisponibles(turnoDetalleSeleccionado).length > 0" @change="solicitarCambioEstado(turnoDetalleSeleccionado, ($event.target as HTMLSelectElement).value, ($event.target as HTMLSelectElement))" class="text-center text-center-last rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm focus:border-teal-500 focus:ring-teal-500 w-full">
                <option value="">Estado...</option>
                <option v-for="estado in estadosDisponibles(turnoDetalleSeleccionado)" :key="estado.valor" :value="estado.valor">{{ estado.label }}</option>
             </select>
             <button v-if="!esEstadoFinalTurno(turnoDetalleSeleccionado.estado) && !esTurnoPasado(turnoDetalleSeleccionado)" @click="abrirModalReprogramacion(turnoDetalleSeleccionado)" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Reprogramar</button>
             <button v-if="turnoDetalleSeleccionado.estado === 'PENDIENTE_PAGO'" @click="abrirModalPago(turnoDetalleSeleccionado)" class="rounded-lg bg-emerald-600 px-3 py-2 text-sm font-medium text-white hover:bg-emerald-700">Registrar Pago</button>
             <button v-if="turnoDetalleSeleccionado.clienteId" @click="solicitarToggleCliente(turnoDetalleSeleccionado)" :class="['rounded-lg px-3 py-2 text-sm font-medium transition', turnoDetalleSeleccionado.clienteActivo ? 'border border-rose-200 bg-rose-50 text-rose-700 hover:bg-rose-100' : 'border border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100']">{{ turnoDetalleSeleccionado.clienteActivo ? 'Suspender Cliente' : 'Reactivar Cliente' }}</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showModalObservaciones" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalObservaciones">
      <div class="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">Agregar Observación</h2>
          <button @click="cerrarModalObservaciones" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>
        <form @submit.prevent="submitObservaciones" class="space-y-4 p-6">
          <div v-if="turnoSeleccionado" class="space-y-2 rounded-xl border border-slate-100 bg-slate-50 p-4 text-sm text-slate-700">
            <p><strong>Cliente:</strong> {{ turnoSeleccionado.clienteNombre }}</p>
            <p><strong>Servicio:</strong> {{ turnoSeleccionado.servicioNombre }}</p>
            <p><strong>Fecha:</strong> {{ formatearFechaLegible(turnoSeleccionado.fecha) }} - {{ turnoSeleccionado.horaInicio }}</p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Observación *</label>
            <textarea 
              v-model="nuevaObservacion" 
              rows="4"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
              placeholder="Escribe las observaciones del turno..."
              required
            ></textarea>
          </div>
          <div v-if="errorObservaciones" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorObservaciones }}</div>
          <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
            <button type="button" @click="cerrarModalObservaciones" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-teal-700 disabled:opacity-50" :disabled="submittingObservaciones">
              {{ submittingObservaciones ? 'Guardando...' : 'Agregar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showModalPago" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalPago">
      <div class="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">Registrar Pago</h2>
          <button @click="cerrarModalPago" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>
        <form @submit.prevent="submitPagoManual" class="space-y-4 p-6">
          <div v-if="turnoPagoSeleccionado" class="space-y-2 rounded-xl border border-slate-100 bg-slate-50 p-4 text-sm text-slate-700">
            <p><strong>Cliente:</strong> {{ turnoPagoSeleccionado.clienteNombre }}</p>
            <p><strong>Servicio:</strong> {{ turnoPagoSeleccionado.servicioNombre }}</p>
            <p><strong>Fecha:</strong> {{ formatearFechaLegible(turnoPagoSeleccionado.fecha) }} - {{ turnoPagoSeleccionado.horaInicio }}</p>
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Método de pago</label>
            <select v-model="metodoPagoManual" required class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500">
              <option value="EFECTIVO">Efectivo</option>
              <option value="TRANSFERENCIA">Transferencia</option>
            </select>
          </div>
          <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
            <button type="button" @click="cerrarModalPago" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-teal-700 disabled:opacity-50" :disabled="submittingPagoManual">
              {{ submittingPagoManual ? 'Registrando...' : 'Confirmar pago' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showModalReprogramacion" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalReprogramacion">
      <div class="w-full max-w-lg overflow-hidden rounded-2xl bg-white shadow-2xl max-h-[90vh] overflow-y-auto" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h3 class="text-lg font-semibold text-slate-800">Reprogramar reserva</h3>
          <button @click="cerrarModalReprogramacion" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
             <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>

        <div class="p-6 space-y-4">
          <div v-if="turnoReprogramacionSeleccionado" class="space-y-2 rounded-xl border border-slate-100 bg-slate-50 p-4 text-sm text-slate-700">
            <p><strong>Cliente:</strong> {{ turnoReprogramacionSeleccionado.clienteNombre }}</p>
            <p><strong>Servicio:</strong> {{ turnoReprogramacionSeleccionado.servicioNombre }}</p>
            <p>
              <strong>Actual:</strong>
              {{ formatearFechaLegible(turnoReprogramacionSeleccionado.fecha) }}
              {{ turnoReprogramacionSeleccionado.horaInicio }} - {{ turnoReprogramacionSeleccionado.horaFin }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Selecciona una nueva fecha</label>
            <input
              type="date"
              v-model="reprogramacionFecha"
              :min="fechaMinima"
              @change="cargarSlotsReprogramacion"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
            />
          </div>

          <div v-if="reprogramacionFecha">
            <label class="block text-sm font-medium text-slate-700 mb-2">Horarios disponibles</label>

            <div v-if="loadingSlotsReprogramacion" class="text-center py-4">
              <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-teal-600"></div>
              <p class="mt-2 text-sm text-slate-600">Cargando horarios...</p>
            </div>

            <div v-else-if="slotsDisponiblesReprogramacion.length === 0" class="text-center py-4 bg-slate-50 rounded-md">
              <p class="text-sm text-slate-600">No hay horarios disponibles para esta fecha.</p>
              <p class="text-xs text-slate-500 mt-1">Intenta con otra fecha.</p>
            </div>

            <div v-else class="grid grid-cols-3 gap-2 max-h-60 overflow-y-auto p-1">
              <button
                v-for="(slot, index) in slotsDisponiblesReprogramacion"
                :key="slot.horaInicio + index"
                @click="seleccionarSlotReprogramacion(slot)"
                :class="[
                  'px-3 py-2 rounded-lg text-sm border transition-colors font-medium',
                  slotReprogramacionSeleccionado === slot
                    ? 'bg-teal-800 text-white border-teal-800'
                    : 'bg-white text-slate-700 border-slate-300 hover:border-teal-500 hover:bg-teal-50'
                ]"
              >
                {{ formatearHoraSlotReprogramacion(slot.horaInicio) }}
              </button>
            </div>
          </div>

          <div v-if="slotReprogramacionSeleccionado" class="rounded-lg bg-teal-50 p-4 border border-teal-100">
            <p class="text-sm text-teal-800">
              <strong>Nuevo horario:</strong>
              {{ formatearHoraSlotReprogramacion(slotReprogramacionSeleccionado.horaInicio) }} -
              {{ calcularFinServicioSlotReprogramacion(slotReprogramacionSeleccionado.horaInicio) }}
            </p>
          </div>

          <p v-if="errorReprogramacion" class="text-sm text-rose-600">{{ errorReprogramacion }}</p>
        </div>

        <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4">
          <button @click="cerrarModalReprogramacion" class="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
            Cancelar
          </button>
          <button
            :disabled="!slotReprogramacionSeleccionado || submittingReprogramacion || loadingSlotsReprogramacion"
            @click="submitReprogramacion"
            :class="[
              'rounded-lg px-4 py-2 text-sm font-semibold text-white shadow-sm transition',
              !slotReprogramacionSeleccionado || submittingReprogramacion || loadingSlotsReprogramacion
                ? 'bg-slate-400 cursor-not-allowed'
                : 'bg-teal-800 hover:bg-teal-700'
            ]"
          >
            {{ submittingReprogramacion ? 'Reprogramando...' : 'Reprogramar' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showModalDisponibilidad" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalDisponibilidad">
      <div class="w-full max-w-sm overflow-hidden rounded-2xl bg-white shadow-2xl" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ editingDisponibilidad ? 'Editar' : 'Nuevo' }} Horario</h2>
          <button @click="cerrarModalDisponibilidad" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
             <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>
        <form @submit.prevent="submitFormDisponibilidad" class="space-y-4 p-6">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Día de la Semana *</label>
            <select v-model="formDataDisponibilidad.diaSemana" required class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500">
              <option value="">Seleccione un día</option>
              <option v-for="dia in diasSemana" :key="dia" :value="dia">
                {{ nombresDias[dia] }}
              </option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Hora Inicio *</label>
              <input 
                v-model="formDataDisponibilidad.horaInicio" 
                type="time" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
              />
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Hora Fin *</label>
              <input 
                v-model="formDataDisponibilidad.horaFin" 
                type="time" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
              />
            </div>
          </div>

          <div v-if="errorDisponibilidad" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">
            <p>{{ errorDisponibilidad }}</p>
            <ul v-if="disponibilidadConflictoDetalles.length" class="mt-2 list-disc pl-5 text-xs">
              <li v-for="item in disponibilidadConflictoDetalles" :key="item">{{ item }}</li>
            </ul>
          </div>

          <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
            <button type="button" @click="cerrarModalDisponibilidad" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-teal-700 disabled:opacity-50" :disabled="submittingDisponibilidad">
              {{ submittingDisponibilidad ? 'Guardando...' : (editingDisponibilidad ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showModalBloqueo" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalBloqueo">
      <div class="w-full max-w-sm overflow-hidden rounded-2xl bg-white shadow-2xl" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ editingBloqueo ? 'Editar' : 'Nuevo' }} Bloqueo</h2>
          <button @click="cerrarModalBloqueo" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" /></svg>
          </button>
        </div>
        <form @submit.prevent="submitFormBloqueo" class="space-y-4 p-6">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Fecha Inicio *</label>
            <input 
              v-model="formDataBloqueo.fechaInicio" 
              type="date" 
              :min="fechaMinima"
              required
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
            />
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Fecha Fin (opcional)</label>
            <input 
              v-model="formDataBloqueo.fechaFin" 
              type="date" 
              :min="formDataBloqueo.fechaInicio || fechaMinima"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
            />
            <small class="mt-1 block text-xs text-slate-500">Dejar vacío para bloquear solo la fecha de inicio</small>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Motivo (opcional)</label>
            <textarea 
              v-model="formDataBloqueo.motivo" 
              rows="3"
              placeholder="Ej: Vacaciones, día personal, etc."
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-teal-500 focus:ring-teal-500"
            ></textarea>
          </div>

          <div v-if="errorBloqueo" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorBloqueo }}</div>

          <div class="flex justify-end gap-3 border-t border-slate-100 pt-4">
            <button type="button" @click="cerrarModalBloqueo" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-teal-800 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-teal-700 disabled:opacity-50" :disabled="submittingBloqueo">
              {{ submittingBloqueo ? 'Guardando...' : (editingBloqueo ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <BloqueoConflictosModal
      :show="showModalConflictos"
      :conflictos-data="conflictosData"
      :bloqueo-pendiente="bloqueoPendiente"
      @cerrar="cerrarModalConflictos"
      @bloqueo-creado="onBloqueoCreado"
    />

    <nav class="fixed bottom-0 left-0 z-50 w-full bg-white md:hidden">
      <div class="grid grid-cols-3 border-t border-slate-200">
        <button @click="seccionActiva = 'turnos'" :class="['flex min-h-[60px] flex-col items-center justify-center gap-1 px-2 text-[11px] font-semibold transition-colors', seccionActiva === 'turnos' ? 'text-teal-600' : 'text-slate-500']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" /></svg>
          <span>Turnos</span>
        </button>
        <button @click="seccionActiva = 'disponibilidad'" :class="['flex min-h-[60px] flex-col items-center justify-center gap-1 px-2 text-[11px] font-semibold transition-colors', seccionActiva === 'disponibilidad' ? 'text-teal-600' : 'text-slate-500']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" /></svg>
          <span>Dispon.</span>
        </button>
        <button @click="seccionActiva = 'bloqueos'" :class="['flex min-h-[60px] flex-col items-center justify-center gap-1 px-2 text-[11px] font-semibold transition-colors', seccionActiva === 'bloqueos' ? 'text-teal-600' : 'text-slate-500']">
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 0 0 5.636 5.636m12.728 12.728A9 9 0 0 1 5.636 5.636m12.728 12.728L5.636 5.636" /></svg>
          <span>Bloqueos</span>
        </button>
      </div>
    </nav>

    <Toast />
  </div>

  <ConfirmModal
    :show="showConfirmDeleteDisponibilidad"
    titulo="Eliminar Disponibilidad"
    :mensaje="`¿Estás seguro de eliminar la disponibilidad del ${nombresDias[disponibilidadPendienteEliminar?.diaSemana ?? '']} (${disponibilidadPendienteEliminar?.horaInicio} - ${disponibilidadPendienteEliminar?.horaFin})?\nEsta acción no se puede deshacer.`"
    textoConfirmar="Eliminar"
    colorBoton="bg-rose-600 hover:bg-rose-700"
    @confirm="ejecutarEliminarDisponibilidad"
    @cancel="cerrarConfirmDeleteDisponibilidad"
  />

  <ConfirmModal
    :show="showConfirmDeleteBloqueo"
    titulo="Eliminar Bloqueo"
    :mensaje="`¿Estás seguro de eliminar el bloqueo del ${formatearFecha(bloqueoAPendienteEliminar?.fechaInicio ?? '')}${bloqueoAPendienteEliminar?.fechaFin && bloqueoAPendienteEliminar.fechaFin !== bloqueoAPendienteEliminar.fechaInicio ? ` - ${formatearFecha(bloqueoAPendienteEliminar.fechaFin)}` : ''}?`"
    textoConfirmar="Eliminar"
    colorBoton="bg-rose-600 hover:bg-rose-700"
    @confirm="ejecutarEliminarBloqueo"
    @cancel="showConfirmDeleteBloqueo = false"
  />

  <ConfirmModal
    :show="showConfirmCambioEstadoTurno"
    titulo="Confirmar cambio de estado"
    :mensaje="`¿Confirmas el cambio de estado a ${obtenerLabelEstado(nuevoEstadoPendienteTurno)} para el turno de ${turnoPendienteCambioEstado?.clienteNombre}?`"
    textoConfirmar="Confirmar"
    :colorBoton="nuevoEstadoPendienteTurno === 'CANCELADO' ? 'bg-rose-600 hover:bg-rose-700' : 'bg-teal-600 hover:bg-teal-700'"
    @confirm="confirmarCambioEstadoTurno"
    @cancel="cerrarConfirmCambioEstadoTurno"
  />

  <ConfirmModal
    :show="showConfirmDesactivarCliente"
    :titulo="turnoPendienteDesactivarCliente?.clienteActivo ? 'Desactivar cliente' : 'Reactivar cliente'"
    :mensaje="turnoPendienteDesactivarCliente?.clienteActivo ? '¿Estás seguro de desactivar a este cliente? No podrá iniciar sesión ni reservar.' : '¿Estás seguro de reactivar a este cliente? Podrá iniciar sesión y reservar nuevamente.'"
    :textoConfirmar="turnoPendienteDesactivarCliente?.clienteActivo ? 'Desactivar' : 'Reactivar'"
    :colorBoton="turnoPendienteDesactivarCliente?.clienteActivo ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'"
    @confirm="confirmarDesactivarCliente"
    @cancel="cerrarConfirmDesactivarCliente"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useNotificacionesStore } from '../stores/notificaciones'
import NotificationBell from '../components/NotificationBell.vue'
import Toast from '../components/Toast.vue'
import BloqueoConflictosModal from '../components/BloqueoConflictosModal.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import { disponibilidadService, type DisponibilidadRequest, type DisponibilidadResponse, diasSemana } from '../services/disponibilidad'
import { bloqueosService, type BloqueoRequest, type BloqueoResponse, type ConflictosBloqueoResponse } from '../services/bloqueos'
import api from '../services/api'
import { useToastStore } from '../composables/useToast'
import { formatCurrencyARS as formatearMonedaARS } from '../utils/currency'

type EstadoTurno =
  | 'PENDIENTE_CONFIRMACION'
  | 'PENDIENTE_PAGO'
  | 'CONFIRMADO'
  | 'ATENDIDO'
  | 'NO_ASISTIO'
  | 'NO_ASISTIDO'
  | 'REPROGRAMADO'
  | 'CANCELADO'
  | string

type MetodoPago = 'EFECTIVO' | 'TRANSFERENCIA'

interface TurnoProfesional {
  id: number
  clienteNombre: string
  servicioNombre: string
  estado: EstadoTurno
  fecha: string
  horaInicio: string
  horaFin: string
  duracionMinutos: number
  bufferMinutos: number | null
  precio: number
  clienteTelefono: string | null
  clienteEmail: string | null
  observaciones: string | null
  clienteId: number | null
  clienteActivo: boolean
  servicioId?: number
}

interface HorarioEmpresa {
  id?: number | string
  diaSemana: string
  horaInicio: string
  horaFin: string
}

interface ServicioOpcion {
  id: number
  nombre: string
}

interface ServicioEmpresa {
  id: number | string
  nombre: string
  activo?: boolean
}

interface FiltrosTurnos {
  periodo: 'todos' | 'hoy' | 'semana' | 'rango'
  estado: string
  servicioId: string
  fechaDesde: string
  fechaHasta: string
  clienteNombre: string
}

interface EstadoDisponible {
  valor: EstadoTurno
  label: string
}

interface ApiResponseEnvelope<T> {
  datos: T
}

interface ApiPaged<T> {
  content: T[]
  totalPages: number
  number: number
}

interface ApiClient {
  obtenerCantidadTurnosSinResolver: () => Promise<{ data?: ApiResponseEnvelope<number> }>
  obtenerTurnosProfesional: (params: Record<string, string | number>) => Promise<{ data: ApiResponseEnvelope<ApiPaged<TurnoProfesional>> }>
  confirmarPagoManual: (turnoId: number, payload: { metodoPago: MetodoPago }) => Promise<unknown>
  obtenerSlotsReprogramacionProfesional: (turnoId: number, fecha: string) => Promise<{ data?: ApiResponseEnvelope<SlotReprogramacion[]> }>
  reprogramarTurnoProfesional: (turnoId: number, payload: { fecha: string; horaInicio: string }) => Promise<unknown>
  cambiarEstadoTurno: (turnoId: number, payload: { nuevoEstado: EstadoTurno; observaciones: string | null }) => Promise<unknown>
  agregarObservacionesTurno: (turnoId: number, payload: { observaciones: string }) => Promise<{ data: ApiResponseEnvelope<TurnoProfesional> }>
  actualizarEstadoCliente: (clienteId: number, activo: boolean) => Promise<unknown>
}

interface ApiErrorData {
  mensaje?: string
  message?: string
  turnosAfectados?: string[]
}

type UnknownRecord = Record<string, unknown>

function isRecord(value: unknown): value is UnknownRecord {
  return typeof value === 'object' && value !== null
}

function esServicioEmpresa(value: unknown): value is ServicioEmpresa {
  if (!isRecord(value)) return false
  const id = value.id
  const nombre = value.nombre
  const activo = value.activo
  const idValido = typeof id === 'number' || typeof id === 'string'
  const nombreValido = typeof nombre === 'string'
  const activoValido = activo === undefined || typeof activo === 'boolean'
  return idValido && nombreValido && activoValido
}

function getApiErrorData(error: unknown): ApiErrorData | null {
  if (!isRecord(error)) return null
  const response = error.response
  if (!isRecord(response)) return null
  const data = response.data
  if (!isRecord(data)) return null
  return data as ApiErrorData
}

function getApiErrorStatus(error: unknown): number | null {
  if (!isRecord(error)) return null
  const response = error.response
  if (!isRecord(response)) return null
  return typeof response.status === 'number' ? response.status : null
}

function getApiErrorMessage(error: unknown): string | null {
  const data = getApiErrorData(error)
  if (data?.mensaje) return data.mensaje
  if (data?.message) return data.message

  if (!isRecord(error)) return null
  return typeof error.message === 'string' ? error.message : null
}

const router = useRouter()
const authStore = useAuthStore()
const notificacionesStore = useNotificacionesStore()
const toastStore = useToastStore()
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)

function handleApiError(error: unknown, fallbackMessage: string, options?: { showToast?: boolean }): string {
  const message = getApiErrorMessage(error) ?? fallbackMessage
  const showToast = options?.showToast ?? true
  if (showToast) {
    toastStore.showError(message)
  }
  return message
}

const diaSemanaVacio = '' as unknown as DisponibilidadRequest['diaSemana']

function crearFormularioDisponibilidadInicial(): DisponibilidadRequest {
  return {
    diaSemana: diaSemanaVacio,
    horaInicio: '',
    horaFin: ''
  }
}

// Hacer api disponible globalmente para las funciones
const apiClient = api as unknown as ApiClient

// Estado de navegación
const seccionActiva = ref<'disponibilidad' | 'turnos' | 'bloqueos'>('turnos')

// Estado para Disponibilidad
const disponibilidades = ref<DisponibilidadResponse[]>([])
const horariosEmpresa = ref<HorarioEmpresa[]>([])
const loadingDisponibilidad = ref(false)
const showModalDisponibilidad = ref(false)
const editingDisponibilidad = ref<DisponibilidadResponse | null>(null)
const errorDisponibilidad = ref('')
const submittingDisponibilidad = ref(false)
const submittingInicializar = ref(false)
const errorInicializar = ref('')

// Estado para modal de confirmación de eliminación de disponibilidad
const showConfirmDeleteDisponibilidad = ref(false)
const disponibilidadPendienteEliminar = ref<DisponibilidadResponse | null>(null)
const eliminandoDisponibilidad = ref(false)
const showConfirmCambioEstadoTurno = ref(false)
const turnoPendienteCambioEstado = ref<TurnoProfesional | null>(null)
const nuevoEstadoPendienteTurno = ref('')
const showConfirmDesactivarCliente = ref(false)
const turnoPendienteDesactivarCliente = ref<TurnoProfesional | null>(null)

// Detalles de conflicto 409 en el formulario de disponibilidad
const disponibilidadConflictoDetalles = ref<string[]>([])

const formDataDisponibilidad = ref<DisponibilidadRequest>(crearFormularioDisponibilidadInicial())

const nombresDias: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

// Agrupar disponibilidades por día
const disponibilidadesAgrupadas = computed(() => {
  const agrupados: Record<string, DisponibilidadResponse[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = disponibilidades.value.filter(d => d.diaSemana === dia)
  })
  return agrupados
})

// Agrupar horarios empresa por día
const horariosEmpresaAgrupados = computed(() => {
  const agrupados: Record<string, HorarioEmpresa[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = horariosEmpresa.value.filter(h => h.diaSemana === dia)
  })
  return agrupados
})

// Estado para Bloqueos
const bloqueos = ref<BloqueoResponse[]>([])
const loadingBloqueos = ref(false)
const showModalBloqueo = ref(false)
const editingBloqueo = ref<BloqueoResponse | null>(null)
const errorBloqueo = ref('')
const submittingBloqueo = ref(false)

const formDataBloqueo = ref<BloqueoRequest>({
  fechaInicio: '',
  fechaFin: null,
  motivo: null
})

// Estado para Conflictos de Bloqueos
const showModalConflictos = ref(false)
const conflictosData = ref<ConflictosBloqueoResponse | null>(null)
const bloqueoPendiente = ref<BloqueoRequest | null>(null)

const fechaMinima = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// Ordenar bloqueos cronológicamente (próximos primero)
const bloqueosOrdenados = computed(() => {
  return [...bloqueos.value].sort((a, b) => {
    return new Date(a.fechaInicio).getTime() - new Date(b.fechaInicio).getTime()
  })
})

// Estado para Turnos
const turnos = ref<TurnoProfesional[]>([])
const loadingTurnos = ref(false)
const filtrosTurnos = ref<FiltrosTurnos>({
  periodo: 'hoy',
  estado: 'CONFIRMADO',
  servicioId: '',
  fechaDesde: '',
  fechaHasta: '',
  clienteNombre: ''
})
const mostrarFiltros = ref(false)
const servicioOpcionesTurnos = ref<ServicioOpcion[]>([])
const currentPageTurnos = ref(0)
const totalPagesTurnos = ref(0)
const pageSizeTurnos = ref(10)
const showModalObservaciones = ref(false)
const turnoSeleccionado = ref<TurnoProfesional | null>(null)
const nuevaObservacion = ref('')
const errorObservaciones = ref('')
const submittingObservaciones = ref(false)
const showModalPago = ref(false)
const turnoPagoSeleccionado = ref<TurnoProfesional | null>(null)
const metodoPagoManual = ref<MetodoPago>('EFECTIVO')
const submittingPagoManual = ref(false)
const cantidadTurnosSinResolver = ref(0)
const turnoDetalleSeleccionado = ref<TurnoProfesional | null>(null)
const showModalDetalleTurno = ref(false)

interface SlotReprogramacion {
  horaInicio: string
  horaFin: string
  profesionalId: number
  profesionalNombre: string
}

const showModalReprogramacion = ref(false)
const turnoReprogramacionSeleccionado = ref<TurnoProfesional | null>(null)
const reprogramacionFecha = ref('')
const slotReprogramacionSeleccionado = ref<SlotReprogramacion | null>(null)
const slotsDisponiblesReprogramacion = ref<SlotReprogramacion[]>([])
const loadingSlotsReprogramacion = ref(false)
const submittingReprogramacion = ref(false)
const errorReprogramacion = ref('')
const OFFSET_ARGENTINA = '-03:00'

function obtenerTimestampInicioTurnoArgentina(turno: TurnoProfesional): number {
  return new Date(`${turno.fecha}T${turno.horaInicio}:00${OFFSET_ARGENTINA}`).getTime()
}

function esTurnoPasado(turno: TurnoProfesional): boolean {
  return obtenerTimestampInicioTurnoArgentina(turno) < Date.now()
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutsideUserMenu)

  const hoy = toIsoDate(new Date())
  filtrosTurnos.value.fechaDesde = hoy
  filtrosTurnos.value.fechaHasta = hoy

  // Cargar datos iniciales
  await Promise.all([
    cargarDisponibilidad(),
    cargarHorariosEmpresa(),
    cargarBloqueos(),
    cargarServicioOpcionesTurnosGlobal(),
    cargarTurnos(),
    cargarCantidadTurnosSinResolver()
  ])

  // Refrescar notificaciones al entrar a la vista.
  // El WebSocket es inicializado/destruido globalmente en App.vue para que
  // permanezca activo incluso cuando el usuario multi-rol navega a otra vista.
  await Promise.all([
    notificacionesStore.cargarRecientes(),
    notificacionesStore.actualizarContador()
  ])
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutsideUserMenu)
})

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
}

function handleClickOutsideUserMenu(event: MouseEvent) {
  if (!userMenuRef.value) return
  if (!userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

async function cargarCantidadTurnosSinResolver() {
  try {
    const response = await apiClient.obtenerCantidadTurnosSinResolver()
    cantidadTurnosSinResolver.value = response.data?.datos ?? 0
  } catch (error: unknown) {
    console.error('Error al cargar cantidad de turnos sin resolver:', error)
    handleApiError(error, 'No se pudo cargar la cantidad de turnos sin resolver.', { showToast: false })
  }
}

// ==================== FUNCIONES DISPONIBILIDAD ====================

async function cargarDisponibilidad() {
  loadingDisponibilidad.value = true
  try {
    disponibilidades.value = await disponibilidadService.obtenerDisponibilidad()
  } catch (error: unknown) {
    console.error('Error al cargar disponibilidad:', error)
    handleApiError(error, 'No se pudo cargar la disponibilidad.', { showToast: false })
  } finally {
    loadingDisponibilidad.value = false
  }
}

async function cargarHorariosEmpresa() {
  try {
    horariosEmpresa.value = await disponibilidadService.obtenerHorariosEmpresa()
  } catch (error: unknown) {
    console.error('Error al cargar horarios de empresa:', error)
    handleApiError(error, 'No se pudieron cargar los horarios de la empresa.', { showToast: false })
  }
}

async function inicializarDesdeEmpresa() {
  submittingInicializar.value = true
  errorInicializar.value = ''

  try {
    await disponibilidadService.inicializarDesdeEmpresa()
    await cargarDisponibilidad()
    toastStore.showSuccess('Horarios inicializados desde la empresa correctamente')
  } catch (error: unknown) {
    console.error('Error al inicializar desde empresa:', error)
    errorInicializar.value = handleApiError(error, 'Error al inicializar disponibilidad desde la empresa', { showToast: false })
  } finally {
    submittingInicializar.value = false
  }
}

function abrirModalDisponibilidad(disponibilidad: DisponibilidadResponse | null = null) {
  if (disponibilidad) {
    editingDisponibilidad.value = disponibilidad
    formDataDisponibilidad.value = {
      diaSemana: disponibilidad.diaSemana,
      horaInicio: disponibilidad.horaInicio,
      horaFin: disponibilidad.horaFin
    }
  } else {
    editingDisponibilidad.value = null
    formDataDisponibilidad.value = crearFormularioDisponibilidadInicial()
  }
  errorDisponibilidad.value = ''
  showModalDisponibilidad.value = true
}

function cerrarModalDisponibilidad() {
  showModalDisponibilidad.value = false
  editingDisponibilidad.value = null
  formDataDisponibilidad.value = crearFormularioDisponibilidadInicial()
  errorDisponibilidad.value = ''
  disponibilidadConflictoDetalles.value = []
}

async function submitFormDisponibilidad() {
  errorDisponibilidad.value = ''
  submittingDisponibilidad.value = true

  try {
    if (editingDisponibilidad.value && editingDisponibilidad.value.id) {
      await disponibilidadService.actualizarDisponibilidad(
        editingDisponibilidad.value.id, 
        formDataDisponibilidad.value
      )
    } else {
      await disponibilidadService.crearDisponibilidad(formDataDisponibilidad.value)
    }
    
    await cargarDisponibilidad()
    cerrarModalDisponibilidad()
    toastStore.showSuccess('Disponibilidad guardada correctamente')
  } catch (error: unknown) {
    console.error('Error al guardar disponibilidad:', error)
    disponibilidadConflictoDetalles.value = []
    const status = getApiErrorStatus(error)
    const data = getApiErrorData(error)

    if (status === 409) {
      if (data?.turnosAfectados?.length) {
        errorDisponibilidad.value = data.mensaje || 'No se puede modificar la disponibilidad por turnos activos.'
        disponibilidadConflictoDetalles.value = data.turnosAfectados
      } else {
        errorDisponibilidad.value = data?.mensaje || 'No se puede modificar la disponibilidad por conflictos existentes.'
      }
    } else {
      errorDisponibilidad.value = handleApiError(error, 'Error al guardar la disponibilidad', { showToast: false })
    }
  } finally {
    submittingDisponibilidad.value = false
  }
}

function confirmarEliminarDisponibilidad(disponibilidad: DisponibilidadResponse) {
  if (!disponibilidad.id) return
  disponibilidadPendienteEliminar.value = disponibilidad
  showConfirmDeleteDisponibilidad.value = true
}

function cerrarConfirmDeleteDisponibilidad() {
  showConfirmDeleteDisponibilidad.value = false
  disponibilidadPendienteEliminar.value = null
}

async function ejecutarEliminarDisponibilidad() {
  if (!disponibilidadPendienteEliminar.value?.id) return
  eliminandoDisponibilidad.value = true
  try {
    await disponibilidadService.eliminarDisponibilidad(disponibilidadPendienteEliminar.value.id)
    await cargarDisponibilidad()
    cerrarConfirmDeleteDisponibilidad()
    toastStore.showSuccess('Disponibilidad eliminada correctamente')
  } catch (error: unknown) {
    console.error('Error al eliminar disponibilidad:', error)
    cerrarConfirmDeleteDisponibilidad()
    const status = getApiErrorStatus(error)
    const data = getApiErrorData(error)

    if (status === 409) {
      if (data?.turnosAfectados?.length) {
        toastStore.showErrorConDetalles(
          data.mensaje || 'No se puede eliminar la disponibilidad por turnos activos.',
          data.turnosAfectados
        )
      } else {
        toastStore.showError(data?.mensaje || 'No se puede eliminar la disponibilidad por conflictos existentes.')
      }
    } else {
      handleApiError(error, 'Error inesperado al eliminar la disponibilidad. Intente nuevamente.')
    }
  } finally {
    eliminandoDisponibilidad.value = false
  }
}

// ==================== FUNCIONES BLOQUEOS ====================

async function cargarBloqueos() {
  loadingBloqueos.value = true
  try {
    bloqueos.value = await bloqueosService.obtenerBloqueos()
  } catch (error: unknown) {
    console.error('Error al cargar bloqueos:', error)
    handleApiError(error, 'No se pudieron cargar los bloqueos.', { showToast: false })
  } finally {
    loadingBloqueos.value = false
  }
}

function abrirModalBloqueo(bloqueo: BloqueoResponse | null = null) {
  if (bloqueo) {
    editingBloqueo.value = bloqueo
    formDataBloqueo.value = {
      fechaInicio: bloqueo.fechaInicio,
      fechaFin: bloqueo.fechaFin,
      motivo: bloqueo.motivo
    }
  } else {
    editingBloqueo.value = null
    formDataBloqueo.value = {
      fechaInicio: '',
      fechaFin: null,
      motivo: null
    }
  }
  errorBloqueo.value = ''
  showModalBloqueo.value = true
}

function cerrarModalBloqueo() {
  showModalBloqueo.value = false
  editingBloqueo.value = null
  formDataBloqueo.value = {
    fechaInicio: '',
    fechaFin: null,
    motivo: null
  }
  errorBloqueo.value = ''
}

async function submitFormBloqueo() {
  errorBloqueo.value = ''
  submittingBloqueo.value = true

  try {
    if (editingBloqueo.value) {
      // Actualización no verifica conflictos (por ahora)
      await bloqueosService.actualizarBloqueo(editingBloqueo.value.id, formDataBloqueo.value)
      await cargarBloqueos()
      cerrarModalBloqueo()
      toastStore.showSuccess('Bloqueo actualizado correctamente')
    } else {
      // Crear nuevo: verificar conflictos primero
      const conflictos = await bloqueosService.verificarConflictos(formDataBloqueo.value)
      
      if (conflictos.tieneConflictos) {
        // Hay conflictos: mostrar modal de resolución
        bloqueoPendiente.value = { ...formDataBloqueo.value }
        conflictosData.value = conflictos
        showModalBloqueo.value = false
        showModalConflictos.value = true
      } else {
        // Sin conflictos: crear directamente
        await bloqueosService.crearBloqueo(formDataBloqueo.value)
        await cargarBloqueos()
        cerrarModalBloqueo()
        toastStore.showSuccess('Bloqueo creado exitosamente')
      }
    }
  } catch (error: unknown) {
    console.error('Error al guardar bloqueo:', error)
    errorBloqueo.value = handleApiError(error, 'Error al guardar el bloqueo', { showToast: false })
  } finally {
    submittingBloqueo.value = false
  }
}

// ==================== FUNCIONES DE CONFLICTOS ====================

function cerrarModalConflictos() {
  showModalConflictos.value = false
  conflictosData.value = null
  bloqueoPendiente.value = null
  showModalBloqueo.value = true // Volver al modal de bloqueo
}

async function onBloqueoCreado() {
  showModalConflictos.value = false
  showModalBloqueo.value = false
  bloqueoPendiente.value = null
  conflictosData.value = null
  await cargarBloqueos()
  // Toast eliminado: el modal ya muestra el mensaje del backend (fuente única de verdad).
}

// Estado para confirmación de eliminación de bloqueo
const bloqueoAPendienteEliminar = ref<BloqueoResponse | null>(null)
const showConfirmDeleteBloqueo = ref(false)

function confirmarEliminarBloqueo(bloqueo: BloqueoResponse) {
  bloqueoAPendienteEliminar.value = bloqueo
  showConfirmDeleteBloqueo.value = true
}

async function ejecutarEliminarBloqueo() {
  if (!bloqueoAPendienteEliminar.value) return
  showConfirmDeleteBloqueo.value = false
  try {
    await bloqueosService.eliminarBloqueo(bloqueoAPendienteEliminar.value.id)
    await cargarBloqueos()
    toastStore.showSuccess('Bloqueo eliminado correctamente', 4000)
  } catch (error: unknown) {
    console.error('Error al eliminar bloqueo:', error)
    handleApiError(error, 'Error al eliminar el bloqueo')
  } finally {
    bloqueoAPendienteEliminar.value = null
  }
}

function formatearFecha(fecha: string): string {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
  })
}

// ==================== FUNCIONES TURNOS ====================

async function cargarTurnos() {
  try {
    loadingTurnos.value = true
    const params: Record<string, string | number> = {
      page: currentPageTurnos.value,
      size: pageSizeTurnos.value
    }

    if (filtrosTurnos.value.estado) params.estado = filtrosTurnos.value.estado
    if (filtrosTurnos.value.servicioId) params.servicioId = Number(filtrosTurnos.value.servicioId)
    if (filtrosTurnos.value.fechaDesde) params.fechaDesde = filtrosTurnos.value.fechaDesde
    if (filtrosTurnos.value.fechaHasta) params.fechaHasta = filtrosTurnos.value.fechaHasta
    if (filtrosTurnos.value.clienteNombre.trim()) params.clienteNombre = filtrosTurnos.value.clienteNombre.trim()

    const response = await apiClient.obtenerTurnosProfesional(params)
    const pageData = response.data.datos
    const contenido = pageData.content || []
    turnos.value = filtrarReprogramadosSegunVista(contenido)
    
    // Ordenar cronológicamente (Fase 3)
    turnos.value.sort((a, b) => {
      const fechaHoraA = new Date(`${a.fecha}T${a.horaInicio}`).getTime()
      const fechaHoraB = new Date(`${b.fecha}T${b.horaInicio}`).getTime()
      return fechaHoraA - fechaHoraB
    })

    totalPagesTurnos.value = pageData.totalPages ?? 0
    currentPageTurnos.value = pageData.number ?? currentPageTurnos.value
    actualizarOpcionesServicioTurnos(turnos.value)
  } catch (error: unknown) {
    console.error('Error al cargar turnos:', error)
    handleApiError(error, 'Error al cargar turnos.')
  } finally {
    loadingTurnos.value = false
  }
}

function filtrarReprogramadosSegunVista(turnosEntrada: TurnoProfesional[]): TurnoProfesional[] {
  const estadoSeleccionado = filtrosTurnos.value.estado

  if (!estadoSeleccionado) {
    return turnosEntrada
  }

  return turnosEntrada.filter(turno => turno.estado === estadoSeleccionado)
}

function actualizarOpcionesServicioTurnos(turnosPagina: TurnoProfesional[]) {
  const mapaServicios = new Map<number, string>()

  for (const servicio of servicioOpcionesTurnos.value) {
    mapaServicios.set(servicio.id, servicio.nombre)
  }

  for (const turno of turnosPagina) {
    if (turno.servicioId && turno.servicioNombre) {
      mapaServicios.set(turno.servicioId, turno.servicioNombre)
    }
  }

  servicioOpcionesTurnos.value = Array.from(mapaServicios.entries())
    .map(([id, nombre]) => ({ id, nombre }))
    .sort((a, b) => a.nombre.localeCompare(b.nombre))
}

async function cargarServicioOpcionesTurnosGlobal() {
  const mapaServicios = new Map<number, string>()

  try {
    const response = await api.get('/dueno/servicios')
    const responseData = response?.data
    const servicios = Array.isArray(responseData)
      ? responseData
      : isRecord(responseData) && Array.isArray(responseData.datos)
        ? responseData.datos
        : []

    if (Array.isArray(servicios)) {
      for (const servicio of servicios) {
        if (esServicioEmpresa(servicio) && servicio.activo !== false) {
          mapaServicios.set(Number(servicio.id), servicio.nombre)
        }
      }
    }
  } catch (error) {
    console.warn('No se pudieron obtener servicios globales desde /dueno/servicios, se aplica fallback por turnos paginados.')
  }

  if (mapaServicios.size === 0) {
    try {
      const size = 200
      let page = 0
      let totalPages = 1

      while (page < totalPages) {
        const response = await apiClient.obtenerTurnosProfesional({ page, size })
        const pageData = response?.data?.datos
        const contenido = pageData?.content || []

        for (const turno of contenido) {
          if (turno.servicioId && turno.servicioNombre) {
            mapaServicios.set(Number(turno.servicioId), turno.servicioNombre)
          }
        }

        totalPages = pageData?.totalPages ?? 0
        page += 1

        if (totalPages === 0) {
          break
        }
      }
    } catch (error) {
      console.error('No se pudieron cargar opciones globales de servicios:', error)
    }
  }

  if (mapaServicios.size > 0) {
    servicioOpcionesTurnos.value = Array.from(mapaServicios.entries())
      .map(([id, nombre]) => ({ id, nombre }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre))
  }
}

function aplicarFiltrosTurnos() {
  currentPageTurnos.value = 0
  cargarTurnos()
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function calcularRangoSemanaActual() {
  const hoy = new Date()
  const diaSemana = hoy.getDay() // 0 domingo, 1 lunes...
  const diferenciaALunes = diaSemana === 0 ? -6 : 1 - diaSemana

  const inicioSemana = new Date(hoy)
  inicioSemana.setDate(hoy.getDate() + diferenciaALunes)

  const finSemana = new Date(inicioSemana)
  finSemana.setDate(inicioSemana.getDate() + 6)

  return {
    desde: toIsoDate(inicioSemana),
    hasta: toIsoDate(finSemana)
  }
}

function onCambioPeriodo() {
  if (filtrosTurnos.value.periodo === 'todos') {
    filtrosTurnos.value.fechaDesde = ''
    filtrosTurnos.value.fechaHasta = ''
  } else if (filtrosTurnos.value.periodo === 'hoy') {
    const hoy = toIsoDate(new Date())
    filtrosTurnos.value.fechaDesde = hoy
    filtrosTurnos.value.fechaHasta = hoy
  } else if (filtrosTurnos.value.periodo === 'semana') {
    const rango = calcularRangoSemanaActual()
    filtrosTurnos.value.fechaDesde = rango.desde
    filtrosTurnos.value.fechaHasta = rango.hasta
  } else if (filtrosTurnos.value.periodo === 'rango') {
    if (!filtrosTurnos.value.fechaDesde && !filtrosTurnos.value.fechaHasta) {
      const hoy = toIsoDate(new Date())
      filtrosTurnos.value.fechaDesde = hoy
      filtrosTurnos.value.fechaHasta = hoy
    }
  }

  aplicarFiltrosTurnos()
}

function paginaAnteriorTurnos() {
  if (currentPageTurnos.value <= 0) return
  currentPageTurnos.value -= 1
  cargarTurnos()
}

function paginaSiguienteTurnos() {
  if (currentPageTurnos.value >= totalPagesTurnos.value - 1) return
  currentPageTurnos.value += 1
  cargarTurnos()
}

function esEstadoFinalTurno(estado: string): boolean {
  return ['NO_ASISTIO', 'NO_ASISTIDO', 'CANCELADO', 'ATENDIDO', 'REPROGRAMADO'].includes(estado)
}

function estadosDisponibles(turno: TurnoProfesional): EstadoDisponible[] {
  const estados: EstadoDisponible[] = []
  const esPasado = esTurnoPasado(turno)
  
  if (turno.estado === 'PENDIENTE_CONFIRMACION') {
    estados.push({ valor: 'CONFIRMADO', label: 'Confirmar' })
    if (!esPasado) {
      estados.push({ valor: 'CANCELADO', label: 'Cancelar' })
    }
  } else if (turno.estado === 'PENDIENTE_PAGO') {
    if (!esPasado) {
      estados.push({ valor: 'CANCELADO', label: 'Cancelar' })
    }
  } else if (turno.estado === 'CONFIRMADO') {
    estados.push({ valor: 'ATENDIDO', label: 'Atendido' })
    estados.push({ valor: 'NO_ASISTIO', label: 'No Asistió' })
    if (!esPasado) {
      estados.push({ valor: 'CANCELADO', label: 'Cancelar' })
    }
  }
  
  return estados
}

function abrirModalPago(turno: TurnoProfesional) {
  cerrarModalDetalle()
  turnoPagoSeleccionado.value = turno
  metodoPagoManual.value = 'EFECTIVO'
  showModalPago.value = true
}

function abrirModalDetalle(turno: TurnoProfesional) {
  turnoDetalleSeleccionado.value = turno
  showModalDetalleTurno.value = true
}

function cerrarModalDetalle() {
  showModalDetalleTurno.value = false
  turnoDetalleSeleccionado.value = null
}

function cerrarModalPago() {
  showModalPago.value = false
  turnoPagoSeleccionado.value = null
  metodoPagoManual.value = 'EFECTIVO'
}

async function submitPagoManual() {
  if (!turnoPagoSeleccionado.value) return

  try {
    submittingPagoManual.value = true
    await apiClient.confirmarPagoManual(turnoPagoSeleccionado.value.id, {
      metodoPago: metodoPagoManual.value
    })
    cerrarModalPago()
    await cargarTurnos()
    toastStore.showSuccess('Pago registrado y turno confirmado correctamente')
  } catch (error: unknown) {
    console.error('Error al registrar pago manual:', error)
    handleApiError(error, 'Error al registrar pago')
  } finally {
    submittingPagoManual.value = false
  }
}

function abrirModalReprogramacion(turno: TurnoProfesional) {
  cerrarModalDetalle()
  turnoReprogramacionSeleccionado.value = turno
  reprogramacionFecha.value = turno.fecha
  slotReprogramacionSeleccionado.value = null
  slotsDisponiblesReprogramacion.value = []
  errorReprogramacion.value = ''
  showModalReprogramacion.value = true
  cargarSlotsReprogramacion()
}

function cerrarModalReprogramacion() {
  showModalReprogramacion.value = false
  turnoReprogramacionSeleccionado.value = null
  reprogramacionFecha.value = ''
  slotReprogramacionSeleccionado.value = null
  slotsDisponiblesReprogramacion.value = []
  errorReprogramacion.value = ''
}

function extraerHoraHHmm(isoDateTime: string) {
  const fecha = new Date(isoDateTime)
  const horas = String(fecha.getHours()).padStart(2, '0')
  const minutos = String(fecha.getMinutes()).padStart(2, '0')
  return `${horas}:${minutos}`
}

function seleccionarSlotReprogramacion(slot: SlotReprogramacion) {
  slotReprogramacionSeleccionado.value = slot
}

function formatearHoraSlotReprogramacion(isoDateTime: string) {
  return extraerHoraHHmm(isoDateTime)
}

function calcularFinServicioSlotReprogramacion(horaInicioISO: string): string {
  const turno = turnoReprogramacionSeleccionado.value
  if (!turno?.duracionMinutos) return ''

  const base = new Date(horaInicioISO)
  base.setMinutes(base.getMinutes() + turno.duracionMinutos)
  return `${String(base.getHours()).padStart(2, '0')}:${String(base.getMinutes()).padStart(2, '0')}`
}

async function cargarSlotsReprogramacion() {
  if (!turnoReprogramacionSeleccionado.value?.id || !reprogramacionFecha.value) {
    slotsDisponiblesReprogramacion.value = []
    return
  }

  try {
    loadingSlotsReprogramacion.value = true
    errorReprogramacion.value = ''
    slotReprogramacionSeleccionado.value = null

    const response = await apiClient.obtenerSlotsReprogramacionProfesional(
      turnoReprogramacionSeleccionado.value.id,
      reprogramacionFecha.value
    )

    slotsDisponiblesReprogramacion.value = response.data?.datos || []
  } catch (error: unknown) {
    console.error('Error al cargar slots de reprogramación:', error)
    slotsDisponiblesReprogramacion.value = []
    errorReprogramacion.value = handleApiError(error, 'No se pudo obtener la disponibilidad para reprogramar', { showToast: false })
  } finally {
    loadingSlotsReprogramacion.value = false
  }
}

async function submitReprogramacion() {
  if (!turnoReprogramacionSeleccionado.value?.id || !slotReprogramacionSeleccionado.value || !reprogramacionFecha.value) {
    errorReprogramacion.value = 'Debe seleccionar una fecha y un horario disponible'
    return
  }

  let reprogramacionExitosa = false

  try {
    submittingReprogramacion.value = true
    errorReprogramacion.value = ''

    await apiClient.reprogramarTurnoProfesional(
      turnoReprogramacionSeleccionado.value.id,
      {
        fecha: reprogramacionFecha.value,
        horaInicio: extraerHoraHHmm(slotReprogramacionSeleccionado.value.horaInicio)
      }
    )

    reprogramacionExitosa = true
    toastStore.showSuccess('Turno reprogramado correctamente')
    await cargarTurnos()
  } catch (error: unknown) {
    console.error('Error al reprogramar turno:', error)
    errorReprogramacion.value = handleApiError(error, 'No se pudo reprogramar el turno', { showToast: false })
  } finally {
    submittingReprogramacion.value = false
    showModalReprogramacion.value = false
    if (reprogramacionExitosa) {
      cerrarModalReprogramacion()
    }
  }
}

async function cambiarEstado(turno: TurnoProfesional, nuevoEstado: EstadoTurno) {
  if (!nuevoEstado) return
  if (nuevoEstado === 'CANCELADO' && esTurnoPasado(turno)) {
    toastStore.showWarning('No se puede cancelar un turno que ya ha pasado. Debe marcarse como Atendido o No Asistió.')
    return
  }
  
  try {
    await apiClient.cambiarEstadoTurno(turno.id, {
      nuevoEstado,
      observaciones: null
    })
    await cargarTurnos()
    await cargarCantidadTurnosSinResolver()
    
    toastStore.showSuccess('Estado actualizado exitosamente')
    cerrarModalDetalle()
  } catch (error: unknown) {
    console.error('Error al cambiar estado:', error)
    handleApiError(error, 'Error al cambiar estado')
  }
}

function solicitarCambioEstado(turno: TurnoProfesional, nuevoEstado: EstadoTurno, selectEstado?: HTMLSelectElement) {
  if (!nuevoEstado) return

  if (nuevoEstado === 'CANCELADO' && esTurnoPasado(turno)) {
    toastStore.showWarning('No se puede cancelar un turno que ya ha pasado. Debe marcarse como Atendido o No Asistió.')
    if (selectEstado) selectEstado.value = ''
    return
  }

  turnoPendienteCambioEstado.value = turno
  nuevoEstadoPendienteTurno.value = nuevoEstado
  showConfirmCambioEstadoTurno.value = true

  if (selectEstado) selectEstado.value = ''
}

function cerrarConfirmCambioEstadoTurno() {
  showConfirmCambioEstadoTurno.value = false
  turnoPendienteCambioEstado.value = null
  nuevoEstadoPendienteTurno.value = ''
}

async function confirmarCambioEstadoTurno() {
  if (!turnoPendienteCambioEstado.value || !nuevoEstadoPendienteTurno.value) return

  const turno = turnoPendienteCambioEstado.value
  const nuevoEstado = nuevoEstadoPendienteTurno.value as EstadoTurno

  cerrarConfirmCambioEstadoTurno()
  await cambiarEstado(turno, nuevoEstado)
}

function solicitarToggleCliente(turno: TurnoProfesional) {
  if (!turno?.clienteId) return
  turnoPendienteDesactivarCliente.value = turno
  showConfirmDesactivarCliente.value = true
}

function cerrarConfirmDesactivarCliente() {
  showConfirmDesactivarCliente.value = false
  turnoPendienteDesactivarCliente.value = null
}

async function confirmarDesactivarCliente() {
  if (turnoPendienteDesactivarCliente.value?.clienteId == null) return

  const estabaActivo = !!turnoPendienteDesactivarCliente.value.clienteActivo
  const clienteId = turnoPendienteDesactivarCliente.value.clienteId
  const nombreCliente = turnoPendienteDesactivarCliente.value.clienteNombre || 'cliente'
  cerrarConfirmDesactivarCliente()

  try {
    await apiClient.actualizarEstadoCliente(clienteId, !estabaActivo)
    await cargarTurnos()
    toastStore.showSuccess(
      estabaActivo
        ? `Cliente ${nombreCliente} desactivado correctamente`
        : `Cliente ${nombreCliente} reactivado correctamente`
    )
    cerrarModalDetalle()
  } catch (error: unknown) {
    console.error('Error al actualizar estado del cliente:', error)
    handleApiError(error, 'No se pudo actualizar el estado del cliente')
  }
}

function obtenerLabelEstado(estado: string) {
  switch (estado) {
    case 'PENDIENTE_CONFIRMACION': return 'Pendiente de confirmación'
    case 'PENDIENTE_PAGO': return 'Seña pendiente'
    case 'CONFIRMADO': return 'Confirmado'
    case 'ATENDIDO': return 'Atendido'
    case 'NO_ASISTIO': return 'No asistió'
    case 'REPROGRAMADO': return 'Reprogramado'
    case 'CANCELADO': return 'Cancelado'
    default: return estado
  }
}

function abrirModalObservaciones(turno: TurnoProfesional) {
  cerrarModalDetalle()
  turnoSeleccionado.value = turno
  nuevaObservacion.value = ''
  errorObservaciones.value = ''
  showModalObservaciones.value = true
}

function cerrarModalObservaciones() {
  showModalObservaciones.value = false
  turnoSeleccionado.value = null
  nuevaObservacion.value = ''
  errorObservaciones.value = ''
}

async function submitObservaciones() {
  if (!turnoSeleccionado.value || !nuevaObservacion.value.trim()) {
    errorObservaciones.value = 'La observación es obligatoria'
    return
  }

  try {
    submittingObservaciones.value = true
    errorObservaciones.value = ''
    
    const response = await apiClient.agregarObservacionesTurno(
      turnoSeleccionado.value.id,
      { observaciones: nuevaObservacion.value }
    )
    
    // Actualizar turno en la lista
    const index = turnos.value.findIndex(t => t.id === turnoSeleccionado.value!.id)
    if (index !== -1) {
      turnos.value[index] = response.data.datos
    }
    
    cerrarModalObservaciones()
    toastStore.showSuccess('Observación agregada exitosamente')
  } catch (error: unknown) {
    errorObservaciones.value = handleApiError(error, 'Error al agregar observación', { showToast: false })
  } finally {
    submittingObservaciones.value = false
  }
}

function formatearFechaLegible(fecha: string): string {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    weekday: 'short',
    day: '2-digit', 
    month: 'short'
  })
}

function cerrarSesion() {
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
</script>

