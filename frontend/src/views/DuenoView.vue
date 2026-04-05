<template>
  <div class="min-h-screen bg-slate-50 pb-24 md:pb-8 md:pl-64">
    <!-- Navegación Desktop: Sidebar -->
    <aside class="fixed inset-y-0 left-0 z-40 hidden w-64 border-r border-slate-200 bg-white md:flex md:flex-col">
      <div class="border-b border-slate-100 px-4 py-5">
        <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">Panel Dueño</p>
        <h2 class="mt-1 truncate text-base font-bold text-slate-900">{{ nombreEmpresa }}</h2>
      </div>
      <nav class="flex-1 space-y-1 p-3">
        <button
          @click="activeTab = 'profesionales'"
          :class="[
            'flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors',
            activeTab === 'profesionales' ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5V4H2v16h5m10 0v-2a3 3 0 10-6 0v2m6 0H7m5-10a3 3 0 110 6 3 3 0 010-6z" />
          </svg>
          <span>Profesionales</span>
        </button>
        <button
          @click="activeTab = 'servicios'"
          :class="[
            'flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors',
            activeTab === 'servicios' ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 7.5l-9-4-9 4m18 0v9l-9 4m9-13.5l-9 4m0 0l-9-4m9 4v9" />
          </svg>
          <span>Servicios</span>
        </button>
        <button
          @click="activeTab = 'horarios'"
          :class="[
            'flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors',
            activeTab === 'horarios' ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 9h10m-12 9h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>Horarios</span>
        </button>
        <!--
        <button
          @click="activeTab = 'politicas'"
          :class="[
            'flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors',
            activeTab === 'politicas' ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Políticas</span>
        </button>
        -->
        <button
          @click="activeTab = 'configuracion'"
          :class="[
            'flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left text-sm font-semibold transition-colors',
            activeTab === 'configuracion' ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-100'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11.25 3h1.5a.75.75 0 01.75.75v1.02a7.969 7.969 0 012.08.86l.72-.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 010 1.06l-.72.72c.37.64.66 1.34.86 2.08h1.02a.75.75 0 01.75.75v1.5a.75.75 0 01-.75.75h-1.02a7.969 7.969 0 01-.86 2.08l.72.72a.75.75 0 010 1.06l-1.06 1.06a.75.75 0 01-1.06 0l-.72-.72a7.969 7.969 0 01-2.08.86v1.02a.75.75 0 01-.75.75h-1.5a.75.75 0 01-.75-.75v-1.02a7.969 7.969 0 01-2.08-.86l-.72.72a.75.75 0 01-1.06 0l-1.06-1.06a.75.75 0 010-1.06l.72-.72a7.969 7.969 0 01-.86-2.08H3.75a.75.75 0 01-.75-.75v-1.5a.75.75 0 01.75-.75h1.02c.2-.74.49-1.44.86-2.08l-.72-.72a.75.75 0 010-1.06l1.06-1.06a.75.75 0 011.06 0l.72.72c.64-.37 1.34-.66 2.08-.86V3.75a.75.75 0 01.75-.75z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span>Configuración</span>
        </button>
      </nav>
    </aside>

    <!-- Header -->
    <header class="sticky top-0 z-30 border-b border-slate-700 bg-slate-900 text-white">
      <div class="flex items-center justify-between gap-3 px-4 py-3 md:px-6">
        <h1 class="min-w-0 truncate text-base font-semibold sm:text-lg">{{ nombreEmpresa }}</h1>
        <div class="relative flex items-center gap-2" ref="userMenuRef">
        <button
          v-if="authStore.isProfesional"
          @click="router.push('/profesional')"
          class="inline-flex items-center gap-2 rounded-md border border-amber-400/50 bg-amber-400 px-3 py-2 text-xs font-semibold text-slate-900 transition hover:bg-amber-300 sm:text-sm"
          title="Ir a mi agenda de profesional"
        >
          <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 9h10m-12 9h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span class="hidden sm:inline">Mi Agenda</span>
          <span class="sm:hidden">Agenda</span>
        </button>

        <button
          ref="userMenuButtonRef"
          class="inline-flex max-w-[11rem] items-center gap-2 rounded-md border border-slate-600 bg-slate-800 px-3 py-2 text-xs font-semibold text-white transition hover:bg-slate-700 sm:max-w-none sm:text-sm"
          @click.stop="toggleUserMenu"
        >
          <span class="truncate">{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
          <svg class="h-4 w-4 shrink-0" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 11.168l3.71-3.939a.75.75 0 111.08 1.04l-4.25 4.511a.75.75 0 01-1.08 0L5.21 8.269a.75.75 0 01.02-1.06z" clip-rule="evenodd" />
          </svg>
        </button>

        <div
          v-if="showUserMenu"
          class="absolute right-0 top-full z-50 mt-2 w-56 max-w-[calc(100vw-1rem)] overflow-hidden rounded-lg border border-slate-200 bg-white shadow-xl"
          @click.stop
        >
          <button class="block w-full px-3 py-2 text-left text-sm font-medium text-slate-700 transition hover:bg-slate-50" @click="abrirModalCambiarContrasena">Cambiar Contraseña</button>
          <button class="block w-full px-3 py-2 text-left text-sm font-medium text-slate-700 transition hover:bg-slate-50" @click="abrirModalEditarEmpresa">Editar Empresa</button>
          <div class="mx-3 my-1 h-px bg-slate-200"></div>
          <button class="flex w-full items-center gap-2 px-3 py-2 text-left text-sm font-medium text-red-600 transition hover:bg-red-50" @click="handleLogout">
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </div>
      </div>
    </header>

    <!-- Navegación Móvil: Bottom Bar -->
    <nav class="fixed bottom-0 left-0 z-50 w-full border-t border-slate-200 bg-white md:hidden">
      <div class="grid grid-cols-4">
        <button
          @click="activeTab = 'profesionales'"
          :class="[
            'flex min-h-[60px] flex-col items-center justify-center gap-1 px-1 text-[11px] font-semibold transition-colors',
            activeTab === 'profesionales' ? 'text-amber-500' : 'text-slate-500'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5V4H2v16h5m10 0v-2a3 3 0 10-6 0v2m6 0H7m5-10a3 3 0 110 6 3 3 0 010-6z" />
          </svg>
          <span>Profes</span>
        </button>
        <button
          @click="activeTab = 'servicios'"
          :class="[
            'flex min-h-[60px] flex-col items-center justify-center gap-1 px-1 text-[11px] font-semibold transition-colors',
            activeTab === 'servicios' ? 'text-amber-500' : 'text-slate-500'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 7.5l-9-4-9 4m18 0v9l-9 4m9-13.5l-9 4m0 0l-9-4m9 4v9" />
          </svg>
          <span>Servicios</span>
        </button>
        <button
          @click="activeTab = 'horarios'"
          :class="[
            'flex min-h-[60px] flex-col items-center justify-center gap-1 px-1 text-[11px] font-semibold transition-colors',
            activeTab === 'horarios' ? 'text-amber-500' : 'text-slate-500'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 9h10m-12 9h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>Horarios</span>
        </button>
        <!--
        Tab móvil de políticas oculto temporalmente para versión inicial.
        <button
          @click="activeTab = 'politicas'"
          :class="[
            'flex min-h-[60px] flex-col items-center justify-center gap-1 px-1 text-[11px] font-semibold transition-colors',
            activeTab === 'politicas' ? 'text-amber-500' : 'text-slate-500'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span>Políticas</span>
        </button>
        -->
        <button
          @click="activeTab = 'configuracion'"
          :class="[
            'flex min-h-[60px] flex-col items-center justify-center gap-1 px-1 text-[11px] font-semibold transition-colors',
            activeTab === 'configuracion' ? 'text-amber-500' : 'text-slate-500'
          ]"
        >
          <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11.25 3h1.5a.75.75 0 01.75.75v1.02a7.969 7.969 0 012.08.86l.72-.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 010 1.06l-.72.72c.37.64.66 1.34.86 2.08h1.02a.75.75 0 01.75.75v1.5a.75.75 0 01-.75.75h-1.02a7.969 7.969 0 01-.86 2.08l.72.72a.75.75 0 010 1.06l-1.06 1.06a.75.75 0 01-1.06 0l-.72-.72a7.969 7.969 0 01-2.08.86v1.02a.75.75 0 01-.75.75h-1.5a.75.75 0 01-.75-.75v-1.02a7.969 7.969 0 01-2.08-.86l-.72.72a.75.75 0 01-1.06 0l-1.06-1.06a.75.75 0 010-1.06l.72-.72a7.969 7.969 0 01-.86-2.08H3.75a.75.75 0 01-.75-.75v-1.5a.75.75 0 01.75-.75h1.02c.2-.74.49-1.44.86-2.08l-.72-.72a.75.75 0 010-1.06l1.06-1.06a.75.75 0 011.06 0l.72.72c.64-.37 1.34-.66 2.08-.86V3.75a.75.75 0 01.75-.75z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span>Config.</span>
        </button>
      </div>
    </nav>

    <!-- Tab: Profesionales -->
    <main v-if="activeTab === 'profesionales'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <div class="mb-6 flex items-center justify-between gap-3">
        <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
          <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.742-.479 3 3 0 0 0-4.682-2.72m.94 3.198.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0 1 12 21c-2.17 0-4.203-.576-5.964-1.584A6.062 6.062 0 0 1 6 18.719m12 0a5.971 5.971 0 0 0-.941-3.197m0 0A5.995 5.995 0 0 0 12 12.75a5.995 5.995 0 0 0-5.058 2.772m0 0a5.971 5.971 0 0 0-.94 3.197m0 0A9.094 9.094 0 0 1 2.25 18.24a3 3 0 0 1 4.682-2.72m.94 3.198a5.971 5.971 0 0 1 .94-3.197M15 9.75a3 3 0 1 1-6 0 3 3 0 0 1 6 0Zm6 3a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Zm-13.5 0a2.25 2.25 0 1 1-4.5 0 2.25 2.25 0 0 1 4.5 0Z" /></svg>
          Profesionales de la Empresa
        </h2>
        <button @click="openModal()" class="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800">+ Agregar Profesional</button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm">Cargando...</div>

      <!-- Profesionales Grid -->
      <div v-else-if="profesionales.length > 0" class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 sm:gap-6">
        <div v-for="profesional in profesionales" :key="profesional.id" class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
          <div class="mb-4 flex items-start justify-between gap-3 border-b border-slate-100 pb-3">
            <h3 class="text-base font-semibold text-slate-900">{{ profesional.nombre }} {{ profesional.apellido }}</h3>
            <span :class="['inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset', profesional.activo ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-rose-50 text-rose-700 ring-rose-600/20']">
              {{ profesional.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="space-y-2 text-sm text-slate-600">
            <div>
              <strong class="text-slate-800">Email:</strong> {{ profesional.email }}
            </div>
            <div>
              <strong class="text-slate-800">Teléfono:</strong> {{ profesional.telefono || 'No especificado' }}
            </div>
            <div v-if="profesional.descripcion">
              <strong class="text-slate-800">Descripción:</strong> {{ profesional.descripcion }}
            </div>
          </div>
          <div class="mt-5 grid grid-cols-2 gap-2">
            <button @click="openModal(profesional)" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50">Editar</button>
            <button 
              @click="confirmarToggleProfesional(profesional)" 
              :class="[
                'rounded-lg px-3 py-2 text-sm font-semibold transition',
                profesional.activo
                  ? 'bg-white text-rose-600 border border-rose-200 hover:bg-rose-50'
                  : 'bg-emerald-600 text-white hover:bg-emerald-700'
              ]"
            >
              {{ profesional.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="rounded-xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <p class="text-base font-medium text-slate-700">No hay profesionales registrados</p>
        <p class="mt-1 text-sm text-slate-500">Haz clic en "Agregar Profesional" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Servicios -->
    <main v-if="activeTab === 'servicios'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <div class="mb-6 flex items-center justify-between gap-3">
        <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
          <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M21 7.5v9m0 0-9 4.5m9-4.5-9-4.5m9 4.5V7.5m-9 13.5V12m0 9-9-4.5m9 4.5V12m0 0L3 7.5m0 0l9-4.5 9 4.5M3 7.5v9m0 0 9 4.5" /></svg>
          Servicios de la Empresa
        </h2>
        <button @click="openModalServicio()" class="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800">+ Agregar Servicio</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingServicios" class="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm">Cargando...</div>

      <!-- Servicios Grid -->
      <div v-else-if="servicios.length > 0" class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 sm:gap-6">
        <div v-for="servicio in servicios" :key="servicio.id" class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
          <div class="mb-4 flex items-start justify-between gap-3 border-b border-slate-100 pb-3">
            <h3 class="text-base font-semibold text-slate-900">{{ servicio.nombre }}</h3>
            <span :class="['inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset', servicio.activo ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-rose-50 text-rose-700 ring-rose-600/20']">
              {{ servicio.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="space-y-2 text-sm text-slate-600">
            <div v-if="servicio.descripcion">
              <strong class="text-slate-800">Descripción:</strong> {{ servicio.descripcion }}
            </div>
            <div>
              <strong class="text-slate-800">Duración:</strong> {{ servicio.duracionMinutos }} minutos
            </div>
            <div>
              <strong class="text-slate-800">Precio:</strong> {{ formatearMonedaARS(servicio.precio) }}
            </div>
            <div>
              <strong class="text-slate-800">Requiere seña:</strong> {{ servicio.requiereSena ? 'Sí' : 'No' }}
            </div>
            <div v-if="servicio.requiereSena && servicio.montoSena !== null">
              <strong class="text-slate-800">Monto seña:</strong> {{ formatearMonedaARS(servicio.montoSena) }}
            </div>
          </div>
          <div class="mt-5 grid grid-cols-2 gap-2">
            <button @click="openModalServicio(servicio)" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50">Editar</button>
            <button 
              @click="confirmarToggleServicioActivo(servicio)" 
              :class="[
                'rounded-lg px-3 py-2 text-sm font-semibold transition',
                servicio.activo
                  ? 'bg-white text-rose-600 border border-rose-200 hover:bg-rose-50'
                  : 'bg-emerald-600 text-white hover:bg-emerald-700'
              ]"
            >
              {{ servicio.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="rounded-xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <p class="text-base font-medium text-slate-700">No hay servicios registrados</p>
        <p class="mt-1 text-sm text-slate-500">Haz clic en "Agregar Servicio" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Horarios de la Empresa -->
    <main v-if="activeTab === 'horarios'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <div class="mb-6 flex items-center justify-between gap-3">
        <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
          <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25" /></svg>
          Horarios de Atención
        </h2>
        <button @click="openModalHorario()" class="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800">+ Agregar Horario</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingHorarios" class="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm">Cargando...</div>

      <!-- Horarios por Día -->
      <div v-else-if="horariosAgrupados && Object.keys(horariosAgrupados).length > 0" class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="(dia, index) in diasSemana" :key="dia" class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div class="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-4 py-3">
            <div class="flex items-center gap-2">
              <h3 class="text-sm font-semibold text-slate-800">{{ nombresDias[dia] }}</h3>
              <span v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0" class="inline-flex rounded-full bg-slate-200 px-2 py-0.5 text-xs font-medium text-slate-700">
                {{ horariosAgrupados[dia].length }} {{ horariosAgrupados[dia].length === 1 ? 'horario' : 'horarios' }}
              </span>
            </div>
            <button 
              v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0"
              @click="abrirModalCopiar(dia)" 
              class="inline-flex items-center gap-1.5 rounded-md border border-slate-300 px-2.5 py-1 text-xs font-medium text-slate-600 transition hover:bg-slate-100 hover:text-teal-700"
              title="Copiar a otros días">
              <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 17.25v3.375c0 .621-.504 1.125-1.125 1.125h-9.75a1.125 1.125 0 01-1.125-1.125V7.125c0-.621.504-1.125 1.125-1.125h3.972m0 0l4.992 4.992m-4.992-4.992v4.992h4.992M18.75 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
              </svg>
              Copiar
            </button>
          </div>
          <div>
            <div v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0">
              <div v-for="horario in horariosAgrupados[dia]" :key="horario.id" class="flex items-center justify-between border-b border-slate-100 px-4 py-3 last:border-0">
                <div>
                  <span class="text-sm font-semibold text-slate-800">{{ horario.horaInicio }} - {{ horario.horaFin }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <button @click="openModalHorario(horario)" class="rounded-md p-1.5 text-slate-600 transition hover:bg-slate-100" title="Editar horario">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 112.652 2.652L6.832 19.82a4.5 4.5 0 01-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 011.13-1.897L16.863 4.487zm0 0L19.5 7.125" /></svg>
                  </button>
                  <button @click="confirmarEliminarHorario(horario)" class="rounded-md p-1.5 text-rose-600 transition hover:bg-rose-50" title="Eliminar horario">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" /></svg>
                  </button>
                </div>
              </div>
            </div>
            <div v-else class="px-4 py-6 text-center text-sm text-slate-500">
              <span>Sin horarios configurados</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="rounded-xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <p class="text-base font-medium text-slate-700">No hay horarios configurados</p>
        <p class="mt-1 text-sm text-slate-500">Configura los horarios de atención de tu empresa por día de la semana</p>
      </div>
    </main>

    <!-- Tab: Políticas de Cancelación Comentada indefinidamente 
    <main v-if="activeTab === 'politicas'" class="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
      <div class="mb-6 flex items-center justify-between gap-3">
        <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
          <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75 11.25 15 15 9.75m6 2.25a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" /></svg>
          Políticas de Cancelación e Inasistencias
        </h2>
        <button class="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800" @click="openModalPolitica()">+ Nueva Política</button>
      </div>

      <div v-if="loadingPoliticas" class="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm">Cargando...</div>

      <div v-else-if="politicas.length > 0" class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 sm:gap-6">
        <div v-for="politica in politicas.filter(p => p.empresaId === empresaId)" :key="politica.id" class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
          <div class="mb-4 flex items-start justify-between gap-3 border-b border-slate-100 pb-3">
            <h3 class="text-base font-semibold text-slate-900">{{ politica.tipo === 'CANCELACION' ? 'Cancelación' : (politica.tipo === 'INASISTENCIA' ? 'Inasistencia' : 'Ambos') }}</h3>
            <span :class="['inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset', politica.activa ? 'bg-emerald-50 text-emerald-700 ring-emerald-600/20' : 'bg-rose-50 text-rose-700 ring-rose-600/20']">
              {{ politica.activa ? 'Activa' : 'Inactiva' }}
            </span>
          </div>
          <div class="space-y-2 text-sm text-slate-600">
            <div class="card-descripcion" :title="politica.descripcion">
              <strong class="text-slate-800">Descripción:</strong>
              <span class="truncate-text">{{ politica.descripcion }}</span>
            </div>
            <div>
              <strong class="text-slate-800">Horas límite:</strong> {{ politica.horasLimiteCancelacion }} horas
            </div>
            <div v-if="politica.fechaCreacion">
              <strong class="text-slate-800">Creada:</strong> {{ formatearFecha(politica.fechaCreacion) }}
            </div>
          </div>
          <div class="mt-5 grid grid-cols-2 gap-2">
            <button 
              @click="openModalPolitica(politica)" 
              class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            >
              Editar
            </button>
            <button 
              @click="confirmarTogglePoliticaActiva(politica)" 
              :class="[
                'rounded-lg px-3 py-2 text-sm font-semibold transition',
                politica.activa
                  ? 'bg-white text-rose-600 border border-rose-200 hover:bg-rose-50'
                  : 'bg-emerald-600 text-white hover:bg-emerald-700'
              ]"
            >
              {{ politica.activa ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <div v-else class="rounded-xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <p class="text-base font-medium text-slate-700">No hay políticas registradas</p>
        <p class="mt-1 text-sm text-slate-500">Haz clic en "Nueva Política" para comenzar</p>
      </div>
    </main>
    -->
    
    <!-- Tab: Configuración -->
    <main v-if="activeTab === 'configuracion'" class="mx-auto w-full max-w-4xl px-4 py-6">
      <div class="mb-6 flex items-center justify-between gap-3">
        <h2 class="flex items-center text-xl font-semibold text-slate-900 sm:text-2xl">
          <svg class="mr-2 h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 6h9.75M10.5 6a1.5 1.5 0 11-3 0m3 0a1.5 1.5 0 10-3 0M3.75 6H7.5m3 12h9.75m-9.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-3.75 0H7.5m9-6h3.75m-3.75 0a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m-9.75 0h9.75" />
          </svg>
          Configuración de la Empresa
        </h2>
      </div>

      <!-- Loading State -->
      <div v-if="loadingConfiguracion" class="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600 shadow-sm">Cargando configuración...</div>

      <!-- Formulario de Configuración -->
      <div v-else>
        <form @submit.prevent="submitConfiguracion" class="space-y-6">
          
          <!-- Sección: Configuración Operativa -->
          <section class="mb-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-100 bg-slate-50 px-6 py-4">
              <svg class="h-5 w-5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.25 3h1.5a.75.75 0 01.75.75v1.02a7.969 7.969 0 012.08.86l.72-.72a.75.75 0 011.06 0l1.06 1.06a.75.75 0 010 1.06l-.72.72c.37.64.66 1.34.86 2.08h1.02a.75.75 0 01.75.75v1.5a.75.75 0 01-.75.75h-1.02a7.969 7.969 0 01-.86 2.08l.72.72a.75.75 0 010 1.06l-1.06 1.06a.75.75 0 01-1.06 0l-.72-.72a7.969 7.969 0 01-2.08.86v1.02a.75.75 0 01-.75.75h-1.5a.75.75 0 01-.75-.75v-1.02a7.969 7.969 0 01-2.08-.86l-.72.72a.75.75 0 01-1.06 0l-1.06-1.06a.75.75 0 010-1.06l.72-.72a7.969 7.969 0 01-.86-2.08H3.75a.75.75 0 01-.75-.75v-1.5a.75.75 0 01.75-.75h1.02c.2-.74.49-1.44.86-2.08l-.72-.72a.75.75 0 010-1.06l1.06-1.06a.75.75 0 011.06 0l.72.72c.64-.37 1.34-.66 2.08-.86V3.75a.75.75 0 01.75-.75z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <h3 class="font-semibold text-slate-800">Configuración Operativa</h3>
            </div>
            <div class="space-y-6 p-6">
              <div class="flex flex-col gap-0">
                <div class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center" :class="{ 'has-error': fieldErrorsConfiguracion.bufferPorDefecto }">
                  <div class="md:w-1/3">
                    <label class="text-sm font-semibold text-slate-800">Buffer por defecto (minutos)</label>
                    <p class="mt-1 text-xs text-slate-500">Tiempo de descanso entre turnos consecutivos</p>
                  </div>
                  <div class="flex flex-col md:w-2/3">
                    <input 
                      v-model.number="formDataConfiguracion.bufferPorDefecto" 
                      type="number" 
                      min="0"
                      max="120"
                      required
                      placeholder="5"
                      class="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 sm:text-sm"
                    />
                    <span v-if="fieldErrorsConfiguracion.bufferPorDefecto" class="field-error">
                      {{ fieldErrorsConfiguracion.bufferPorDefecto }}
                    </span>
                  </div>
                </div>

                <div class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center" :class="{ 'has-error': fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos }">
                  <div class="md:w-1/3">
                    <label class="text-sm font-semibold text-slate-800">Anticipación mínima (minutos)</label>
                    <p class="mt-1 text-xs text-slate-500">Tiempo mínimo para reservar antes del turno</p>
                  </div>
                  <div class="flex flex-col md:w-2/3">
                    <input 
                      v-model.number="formDataConfiguracion.tiempoMinimoAnticipacionMinutos" 
                      type="number" 
                      min="0"
                      max="1440"
                      required
                      placeholder="30"
                      class="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 sm:text-sm"
                    />
                    <span v-if="fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos" class="field-error">
                      {{ fieldErrorsConfiguracion.tiempoMinimoAnticipacionMinutos }}
                    </span>
                  </div>
                </div>

                <div class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center" :class="{ 'has-error': fieldErrorsConfiguracion.diasMaximosReserva }">
                  <div class="md:w-1/3">
                    <label class="text-sm font-semibold text-slate-800">Días máximos de reserva</label>
                    <p class="mt-1 text-xs text-slate-500">Hasta cuántos días adelante pueden reservar</p>
                  </div>
                  <div class="flex flex-col md:w-2/3">
                    <input 
                      v-model.number="formDataConfiguracion.diasMaximosReserva" 
                      type="number" 
                      min="1"
                      max="365"
                      required
                      placeholder="30"
                      class="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 sm:text-sm"
                    />
                    <span v-if="fieldErrorsConfiguracion.diasMaximosReserva" class="field-error">
                      {{ fieldErrorsConfiguracion.diasMaximosReserva }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Sección: Recordatorios -->
          <section class="mb-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-100 bg-slate-50 px-6 py-4">
              <svg class="h-5 w-5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 7.5v9a2.25 2.25 0 01-2.25 2.25h-15A2.25 2.25 0 012.25 16.5v-9m19.5 0A2.25 2.25 0 0019.5 5.25h-15A2.25 2.25 0 002.25 7.5m19.5 0v.243a2.25 2.25 0 01-.965 1.852l-7.5 5.25a2.25 2.25 0 01-2.57 0l-7.5-5.25A2.25 2.25 0 012.25 7.743V7.5" />
              </svg>
              <h3 class="font-semibold text-slate-800">Recordatorios por Email</h3>
            </div>
            <div class="space-y-6 p-6">
              <div class="flex flex-col gap-0">
                <div class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center">
                  <div class="md:w-1/3">
                    <label class="text-sm font-semibold text-slate-800">Estado de recordatorios</label>
                    <p class="mt-1 text-xs text-slate-500">Habilita o deshabilita el envío de recordatorios automáticos</p>
                  </div>
                  <div class="flex flex-col md:w-2/3">
                    <label class="inline-flex items-center gap-3 self-start">
                      <input 
                        v-model="formDataConfiguracion.enviarRecordatorios" 
                        type="checkbox"
                        class="peer sr-only"
                      />
                      <span class="relative h-6 w-11 rounded-full bg-slate-300 transition-colors after:absolute after:left-0.5 after:top-0.5 after:h-5 after:w-5 after:rounded-full after:bg-white after:shadow-sm after:transition-all peer-checked:bg-emerald-600 peer-checked:after:translate-x-5"></span>
                      <span class="text-sm font-medium text-slate-700">
                        {{ formDataConfiguracion.enviarRecordatorios ? 'Recordatorios Activos' : 'Recordatorios Desactivados' }}
                      </span>
                    </label>
                  </div>
                </div>

                <div v-if="formDataConfiguracion.enviarRecordatorios" class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center" :class="{ 'has-error': fieldErrorsConfiguracion.horasAntesRecordatorio }">
                  <div class="md:w-1/3">
                    <label class="text-sm font-semibold text-slate-800">Enviar recordatorio con (horas de anticipación)</label>
                    <p class="mt-1 text-xs text-slate-500">1 hora mínimo, 168 horas (7 días) máximo</p>
                  </div>
                  <div class="flex flex-col md:w-2/3">
                    <input 
                      v-model.number="formDataConfiguracion.horasAntesRecordatorio" 
                      type="number" 
                      min="1"
                      max="168"
                      required
                      placeholder="24"
                      class="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 sm:text-sm"
                    />
                    <span v-if="fieldErrorsConfiguracion.horasAntesRecordatorio" class="field-error">
                      {{ fieldErrorsConfiguracion.horasAntesRecordatorio }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section class="mb-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
            <div class="flex items-center gap-2 border-b border-slate-100 bg-slate-50 px-6 py-4">
              <svg class="h-5 w-5 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 8.25h19.5m-18 7.5h4.5m-4.5 3h3m-1.5-15h15a1.5 1.5 0 011.5 1.5v13.5a1.5 1.5 0 01-1.5 1.5h-15a1.5 1.5 0 01-1.5-1.5V5.25a1.5 1.5 0 011.5-1.5z" />
              </svg>
              <h3 class="font-semibold text-slate-800">Datos para Señas por Transferencia</h3>
            </div>
            <div class="space-y-6 p-6">
              <div class="flex flex-col gap-4 border-b border-slate-100 py-5 last:border-0 md:flex-row md:items-center">
                <div class="md:w-1/3">
                  <label class="text-sm font-semibold text-slate-800">Datos bancarios (opcional)</label>
                  <p class="mt-1 text-xs text-slate-500">Este texto se mostrará al cliente en Mis Turnos cuando tenga una reserva en pendiente de pago.</p>
                </div>
                <div class="flex flex-col md:w-2/3">
                  <textarea
                    v-model="formDataConfiguracion.datosBancarios"
                    rows="4"
                    placeholder="Ej: Alias: mi.negocio.cobros\nCBU: 0000003100012345678901\nTitular: Mi Empresa SRL"
                    class="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 shadow-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 sm:text-sm"
                  ></textarea>
                </div>
              </div>
            </div>
          </section>

          <!-- Error Message -->
          <div v-if="errorConfiguracion" class="rounded-md border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">{{ errorConfiguracion }}</div>

          <!-- Botones de Acción -->
          <div class="flex justify-end border-t border-slate-200 bg-slate-50 px-6 py-4">
            <button 
              type="submit" 
              class="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60" 
              :disabled="!canSubmitConfiguracion"
            >
              <svg class="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M9 3.75H6.375a1.125 1.125 0 0 0-1.125 1.125v14.25c0 .621.504 1.125 1.125 1.125h11.25c.621 0 1.125-.504 1.125-1.125V7.5L15 3.75H9Z" /><path stroke-linecap="round" stroke-linejoin="round" d="M9 3.75V7.5h6V3.75M9.75 12h4.5m-4.5 3h4.5" /></svg>
              {{ submittingConfiguracion ? 'Guardando...' : 'Guardar Configuración' }}
            </button>
          </div>
        </form>
      </div>
    </main>

        <!-- Modal Form Empresa -->
        <div v-if="showModalEmpresa" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalEditarEmpresa">
          <div class="w-full max-w-lg max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
            <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
              <h2 class="text-lg font-semibold text-slate-800">Editar Empresa</h2>
              <button @click="cerrarModalEditarEmpresa" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
                <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
                </svg>
              </button>
            </div>
            <form @submit.prevent="submitFormEmpresa" class="space-y-4 p-6">
              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">Nombre *</label>
                <input
                  v-model="formDataEmpresa.nombre"
                  type="text"
                  required
                  class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                  placeholder="Nombre de la empresa"
                />
                <p v-if="fieldErrorsEmpresa.nombre" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.nombre }}</p>
              </div>

              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">Email</label>
                <input
                  v-model="formDataEmpresa.email"
                  type="email"
                  class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                  placeholder="contacto@empresa.com"
                />
                <p v-if="fieldErrorsEmpresa.email" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.email }}</p>
              </div>

              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">Teléfono</label>
                <input
                  v-model="formDataEmpresa.telefono"
                  type="tel"
                  class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                  placeholder="Solo números (10-15 dígitos)"
                />
                <p v-if="fieldErrorsEmpresa.telefono" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.telefono }}</p>
              </div>

              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">Dirección</label>
                <input
                  v-model="formDataEmpresa.direccion"
                  type="text"
                  class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                  placeholder="Dirección comercial"
                />
                <p v-if="fieldErrorsEmpresa.direccion" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.direccion }}</p>
              </div>

              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-700">Ciudad</label>
                  <input
                    v-model="formDataEmpresa.ciudad"
                    type="text"
                    class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                    placeholder="Ciudad"
                  />
                  <p v-if="fieldErrorsEmpresa.ciudad" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.ciudad }}</p>
                </div>

                <div>
                  <label class="mb-1 block text-sm font-medium text-slate-700">Provincia</label>
                  <input
                    v-model="formDataEmpresa.provincia"
                    type="text"
                    class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                    placeholder="Provincia"
                  />
                  <p v-if="fieldErrorsEmpresa.provincia" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.provincia }}</p>
                </div>
              </div>

              <div>
                <label class="mb-1 block text-sm font-medium text-slate-700">Descripción</label>
                <textarea
                  v-model="formDataEmpresa.descripcion"
                  rows="3"
                  class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                  placeholder="Descripción breve de la empresa"
                ></textarea>
                <p v-if="fieldErrorsEmpresa.descripcion" class="mt-1 text-xs text-rose-500">{{ fieldErrorsEmpresa.descripcion }}</p>
              </div>

              <div v-if="errorEmpresa" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorEmpresa }}</div>

              <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
                <button type="button" @click="cerrarModalEditarEmpresa" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
                <button type="submit" class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" :disabled="!canSubmitEmpresa">
                  {{ submittingEmpresa ? 'Guardando...' : 'Guardar' }}
                </button>
              </div>
            </form>
          </div>
        </div>

<!-- Modal Form Políticas -->
<div v-if="showModalPolitica" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="closeModalPolitica">
  <div class="w-full max-w-lg max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
    <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
      <h2 class="text-lg font-semibold text-slate-800">Nueva Política de Cancelación</h2>
      <button @click="closeModalPolitica" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
        <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
        </svg>
      </button>
    </div>
    <form @submit.prevent="submitFormPolitica" class="space-y-4 p-6">
      <div>
        <label class="mb-1 block text-sm font-medium text-slate-700">Descripción *</label>
        <textarea 
          v-model="formDataPolitica.descripcion" 
          required 
          rows="3"
          class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
          placeholder="Descripción de la política de cancelación"
        ></textarea>
        <p v-if="fieldErrorsPolitica.descripcion" class="mt-1 text-xs text-rose-500">{{ fieldErrorsPolitica.descripcion }}</p>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div
          v-if="formDataPolitica.tipo !== 'INASISTENCIA'"
          class=""
        >
          <label class="mb-1 block text-sm font-medium text-slate-700">Horas límite para cancelar *</label>
          <input 
            v-model.number="formDataPolitica.horasLimiteCancelacion" 
            type="number" 
            min="1" 
            class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
            placeholder="24"
          />
          <p v-if="fieldErrorsPolitica.horasLimiteCancelacion" class="mt-1 text-xs text-rose-500">{{ fieldErrorsPolitica.horasLimiteCancelacion }}</p>
        </div>

        <div>
          <label class="mb-1 block text-sm font-medium text-slate-700">Tipo de política *</label>
          <select v-model="formDataPolitica.tipo" required class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500">
            <option value="CANCELACION">Cancelación</option>
            <option value="INASISTENCIA">Inasistencia</option>
            <option value="AMBOS">Ambos</option>
          </select>
          <p v-if="fieldErrorsPolitica.tipo" class="mt-1 text-xs text-rose-500">{{ fieldErrorsPolitica.tipo }}</p>
        </div>
      </div>

      <div v-if="errorPolitica" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorPolitica }}</div>

      <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
        <button type="button" @click="closeModalPolitica" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
        <button type="submit" class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" :disabled="!canSubmitPolitica">
          {{ submittingPolitica ? 'Guardando...' : (editingPolitica ? 'Actualizar' : 'Crear') }}
        </button>
      </div>
    </form>
  </div>
</div>
    
    <!-- Modal Form Profesionales -->
    <div v-if="showModal" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="closeModal">
      <div class="w-full max-w-lg max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ editingProfesional ? 'Editar' : 'Nuevo' }} Profesional</h2>
          <button @click="closeModal" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
            </svg>
          </button>
        </div>
        <form @submit.prevent="submitForm" class="space-y-4 p-6">
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Nombre *</label>
              <input 
                v-model="formData.nombre" 
                type="text" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                placeholder="Nombre"
              />
              <p v-if="fieldErrors.nombre" class="mt-1 text-xs text-rose-500">{{ fieldErrors.nombre }}</p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Apellido *</label>
              <input 
                v-model="formData.apellido" 
                type="text" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                placeholder="Apellido"
              />
              <p v-if="fieldErrors.apellido" class="mt-1 text-xs text-rose-500">{{ fieldErrors.apellido }}</p>
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Email *</label>
            <input 
              v-model="formData.email" 
              type="email" 
              required
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="email@ejemplo.com"
            />
            <p v-if="fieldErrors.email" class="mt-1 text-xs text-rose-500">{{ fieldErrors.email }}</p>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">{{ editingProfesional ? 'Nueva Contraseña (opcional)' : 'Contraseña *' }}</label>
            <input 
              v-model="formData.contrasena" 
              type="password" 
              :required="!editingProfesional"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="Contraseña (mínimo 8 caracteres)"
            />
            <p v-if="fieldErrors.contrasena" class="mt-1 text-xs text-rose-500">{{ fieldErrors.contrasena }}</p>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Teléfono</label>
              <input 
                v-model="formData.telefono" 
                type="tel" 
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                placeholder="Solo números (10-15 dígitos)"
              />
              <p v-if="fieldErrors.telefono" class="mt-1 text-xs text-rose-500">{{ fieldErrors.telefono }}</p>
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Descripción</label>
            <textarea 
              v-model="formData.descripcion" 
              rows="3"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="Descripción adicional del profesional"
            ></textarea>
            <p v-if="fieldErrors.descripcion" class="mt-1 text-xs text-rose-500">{{ fieldErrors.descripcion }}</p>
          </div>

          <!-- Gestión de Servicios (solo al editar) -->
          <div v-if="editingProfesional" class="space-y-3">
            <label class="mb-1 block text-sm font-medium text-slate-700">Servicios Disponibles</label>
            <button 
              type="button" 
              @click="cargarServiciosProfesional" 
              class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:opacity-50"
              :disabled="cargandoServicios"
            >
              {{ cargandoServicios ? 'Cargando...' : 'Gestionar Servicios' }}
            </button>
            
            <div v-if="serviciosProfesional.length > 0" class="max-h-[300px] overflow-y-auto rounded-lg border border-slate-200">
              <div 
                v-for="servicio in serviciosProfesional" 
                :key="servicio.servicioId" 
                class="flex items-center justify-between border-b border-slate-200 px-4 py-3 last:border-0"
                :class="!servicio.disponible ? 'bg-rose-50/50 opacity-70' : 'bg-white'"
              >
                <div class="flex flex-1 flex-col gap-1">
                  <strong class="text-sm text-slate-800">{{ servicio.nombre }}</strong>
                  <span class="text-xs text-slate-500">{{ servicio.duracionMinutos }} min - {{ formatearMonedaARS(servicio.precio) }}</span>
                </div>
                <button 
                  type="button"
                  @click="confirmarToggleServicioProfesional(servicio)"
                  :class="[
                    'rounded-lg px-3 py-2 text-xs font-semibold text-white transition disabled:opacity-50',
                    servicio.disponible ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'
                  ]"
                  :disabled="submittingToggle"
                >
                  {{ servicio.disponible 
                      ? (submittingToggle && servicioProfesionalPendienteToggle?.servicioId === servicio.servicioId ? 'Guardando...' : 'Desactivar') 
                      : (submittingToggle && servicioProfesionalPendienteToggle?.servicioId === servicio.servicioId ? 'Guardando...' : 'Activar') 
                  }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="error" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ error }}</div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
            <button type="button" @click="closeModal" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" :disabled="!canSubmitProfesional">
              {{ submitting ? 'Guardando...' : (editingProfesional ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Servicios -->
    <div v-if="showModalServicio" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="closeModalServicio">
      <div class="w-full max-w-lg max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ editingServicio ? 'Editar' : 'Nuevo' }} Servicio</h2>
          <button @click="closeModalServicio" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
            </svg>
          </button>
        </div>
        <form @submit.prevent="submitFormServicio" class="space-y-4 p-6">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Nombre del Servicio *</label>
            <input 
              v-model="formDataServicio.nombre" 
              type="text" 
              required
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="Ej: Corte de cabello"
            />
            <p v-if="fieldErrorsServicio.nombre" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.nombre }}</p>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Descripción</label>
            <textarea 
              v-model="formDataServicio.descripcion" 
              rows="3"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="Descripción del servicio"
            ></textarea>
            <p v-if="fieldErrorsServicio.descripcion" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.descripcion }}</p>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Duración (minutos) *</label>
              <input 
                v-model.number="formDataServicio.duracionMinutos" 
                type="number" 
                required
                min="1"
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                placeholder="45"
              />
              <p v-if="fieldErrorsServicio.duracionMinutos" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.duracionMinutos }}</p>
            </div>

            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Precio ($) *</label>
              <input 
                v-model.number="formDataServicio.precio" 
                type="number" 
                required
                min="0"
                step="0.01"
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
                placeholder="5000"
              />
              <p v-if="fieldErrorsServicio.precio" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.precio }}</p>
            </div>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Tiempo entre turnos (minutos)</label>
            <input 
              v-model.number="formDataServicio.bufferMinutos" 
              type="number" 
              min="0"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              placeholder="Tiempo de preparación después del servicio (opcional)"
            />
            <small class="mt-1 block text-xs text-slate-500">Si no se especifica, se usará el buffer del profesional o de la empresa</small>
            <p v-if="fieldErrorsServicio.bufferMinutos" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.bufferMinutos }}</p>
          </div>

          <div>
            <label class="inline-flex items-center gap-2 text-sm font-medium text-slate-700">
              <input
                v-model="formDataServicio.requiereSena"
                type="checkbox"
                class="h-4 w-4 rounded border-slate-300 text-slate-900 focus:ring-slate-500"
              />
              <span>Requiere seña para confirmar la reserva</span>
            </label>
          </div>

          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Monto de seña ($)</label>
            <input
              v-model.number="formDataServicio.montoSena"
              type="number"
              min="0"
              step="0.01"
              :disabled="!formDataServicio.requiereSena"
              class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500 disabled:bg-slate-100 disabled:text-slate-500"
              placeholder="Ej: 3000"
            />
            <small class="mt-1 block text-xs text-slate-500">Solo se habilita si el servicio requiere seña</small>
            <p v-if="fieldErrorsServicio.montoSena" class="mt-1 text-xs text-rose-500">{{ fieldErrorsServicio.montoSena }}</p>
          </div>

          <div v-if="errorServicio" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorServicio }}</div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
            <button type="button" @click="closeModalServicio" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" :disabled="!canSubmitServicio">
              {{ submittingServicio ? 'Guardando...' : (editingServicio ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Horarios -->
    <div v-if="showModalHorario" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="closeModalHorario">
      <div class="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">{{ editingHorario ? 'Editar' : 'Nuevo' }} Horario</h2>
          <button @click="closeModalHorario" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
            </svg>
          </button>
        </div>
        <form @submit.prevent="submitFormHorario" class="space-y-4 p-6">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Día de la Semana *</label>
            <select v-model="formDataHorario.diaSemana" required class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500">
              <option value="">Seleccione un día</option>
              <option v-for="dia in diasSemana" :key="dia" :value="dia">
                {{ nombresDias[dia] }}
              </option>
            </select>
            <p v-if="fieldErrorsHorario.diaSemana" class="mt-1 text-xs text-rose-500">{{ fieldErrorsHorario.diaSemana }}</p>
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Hora Inicio *</label>
              <input 
                v-model="formDataHorario.horaInicio" 
                type="time" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              />
              <p v-if="fieldErrorsHorario.horaInicio" class="mt-1 text-xs text-rose-500">{{ fieldErrorsHorario.horaInicio }}</p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">Hora Fin *</label>
              <input 
                v-model="formDataHorario.horaFin" 
                type="time" 
                required
                class="block w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-900 focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
              />
              <p v-if="fieldErrorsHorario.horaFin" class="mt-1 text-xs text-rose-500">{{ fieldErrorsHorario.horaFin }}</p>
            </div>
          </div>

          <div v-if="errorHorario" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">
            <p>{{ errorHorario }}</p>
            <ul v-if="horarioConflictoDetalles.length" class="mt-2 list-disc pl-5 text-sm text-rose-700">
              <li v-for="item in horarioConflictoDetalles" :key="item">{{ item }}</li>
            </ul>
          </div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
            <button type="button" @click="closeModalHorario" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
            <button type="submit" class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" :disabled="!canSubmitHorario">
              {{ submittingHorario ? 'Guardando...' : (editingHorario ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Copiar Horarios -->
    <div v-if="showModalCopiar" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm sm:p-0" @click="cerrarModalCopiar">
      <div class="w-full max-w-md max-h-[85vh] overflow-y-auto rounded-2xl bg-white shadow-2xl overscroll-contain" @click.stop>
        <div class="flex items-center justify-between border-b border-slate-100 bg-white px-6 py-4">
          <h2 class="text-lg font-semibold text-slate-800">Copiar Horarios de {{ nombresDias[diaFuenteCopia] }}</h2>
          <button @click="cerrarModalCopiar" class="rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" type="button" aria-label="Cerrar modal">
            <svg class="h-5 w-5" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6l8 8M14 6l-8 8" />
            </svg>
          </button>
        </div>
        <div class="space-y-4 p-6">
          <div>
            <label class="mb-1 block text-sm font-medium text-slate-700">Seleccione los días destino:</label>
            <div class="mt-2 flex flex-col gap-2">
              <label v-for="dia in diasSemana" :key="dia" class="flex items-center gap-2 rounded-md px-2 py-1.5 transition hover:bg-slate-50">
                <input 
                  type="checkbox" 
                  :value="dia"
                  v-model="diasDestinoSeleccionados"
                  :disabled="dia === diaFuenteCopia"
                  class="h-4 w-4 rounded border-slate-300 text-slate-900 focus:ring-slate-500 disabled:opacity-50"
                />
                <span :class="dia === diaFuenteCopia ? 'text-slate-400' : 'text-slate-700'" class="text-sm">
                  {{ nombresDias[dia] }}
                  <span v-if="dia === diaFuenteCopia" class="ml-1 text-xs italic text-slate-500">(fuente)</span>
                </span>
              </label>
            </div>
          </div>

          <div v-if="diasConConflicto.length > 0" class="rounded-lg bg-amber-50 p-4 text-sm text-amber-800">
            <div class="flex items-start gap-2">
              <svg class="h-5 w-5 shrink-0 text-amber-600" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>Los siguientes días ya tienen horarios configurados y serán reemplazados: <strong>{{ diasConConflicto.map(d => nombresDias[d]).join(', ') }}</strong></span>
            </div>
          </div>

          <div v-if="errorCopiar" class="rounded-lg bg-rose-50 p-4 text-sm text-rose-700">{{ errorCopiar }}</div>

          <div class="flex justify-end gap-3 border-t border-slate-100 bg-slate-50 px-6 py-4 -mx-6 -mb-6">
            <button type="button" @click="cerrarModalCopiar" class="rounded-lg bg-white px-4 py-2 text-sm font-medium text-slate-700 shadow-sm ring-1 ring-inset ring-slate-300 hover:bg-slate-50">Cancelar</button>
            <button 
              type="button" 
              @click="confirmarCopiarHorarios" 
              class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-800 disabled:opacity-50" 
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
    colorBoton="bg-rose-600 hover:bg-rose-700"
    @confirm="ejecutarEliminarHorario"
    @cancel="cerrarConfirmDeleteHorario"
  />

  <ConfirmModal
    :show="showConfirmToggleProfesional"
    :titulo="`${profesionalPendienteToggle?.activo ? 'Desactivar' : 'Activar'} Profesional`"
    :mensaje="`¿Estás seguro de ${profesionalPendienteToggle?.activo ? 'desactivar' : 'activar'} a ${profesionalPendienteToggle?.nombre} ${profesionalPendienteToggle?.apellido}?`"
    :textoConfirmar="profesionalPendienteToggle?.activo ? 'Desactivar' : 'Activar'"
    :colorBoton="profesionalPendienteToggle?.activo ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'"
    @confirm="ejecutarToggleProfesional"
    @cancel="cerrarConfirmToggleProfesional"
  />

  <ConfirmModal
    :show="showConfirmDeletePolitica"
    titulo="Eliminar Política"
    mensaje="¿Estás seguro de eliminar esta política? Esta acción no se puede deshacer."
    textoConfirmar="Eliminar"
    colorBoton="bg-rose-600 hover:bg-rose-700"
    @confirm="ejecutarEliminarPolitica"
    @cancel="cerrarConfirmDeletePolitica"
  />

  <ConfirmModal
    :show="showConfirmToggleServicio"
    :titulo="`${servicioPendienteToggle?.activo ? 'Desactivar' : 'Activar'} Servicio`"
    :mensaje="servicioPendienteToggle?.activo ? `¿Estás seguro de que deseas desactivar el servicio ${servicioPendienteToggle?.nombre}? Dejará de estar disponible para nuevas reservas.` : `¿Estás seguro de que deseas activar el servicio ${servicioPendienteToggle?.nombre}?`"
    :textoConfirmar="submittingToggleServicio ? 'Guardando...' : (servicioPendienteToggle?.activo ? 'Desactivar' : 'Activar')"
    :colorBoton="servicioPendienteToggle?.activo ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'"
    @confirm="ejecutarToggleServicioActivo"
    @cancel="cerrarConfirmToggleServicio"
  />

  <ConfirmModal
    :show="showConfirmTogglePolitica"
    :titulo="`${politicaPendienteToggle?.activa ? 'Desactivar' : 'Activar'} Política`"
    :mensaje="politicaPendienteToggle?.activa ? '¿Estás seguro de que deseas desactivar esta política de cancelación? Dejará de aplicarse a los nuevos turnos.' : '¿Estás seguro de que deseas activar esta política de cancelación? Comenzará a aplicarse a los nuevos turnos.'"
    :textoConfirmar="submittingTogglePolitica ? 'Guardando...' : (politicaPendienteToggle?.activa ? 'Desactivar' : 'Activar')"
    :colorBoton="politicaPendienteToggle?.activa ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'"
    @confirm="ejecutarTogglePoliticaActiva"
    @cancel="cerrarConfirmTogglePolitica"
  />

  <ConfirmModal
    :show="showConfirmToggleServicioProfesional"
    :titulo="`${servicioProfesionalPendienteToggle?.disponible ? 'Desactivar' : 'Activar'} Servicio en Agenda`"
    :mensaje="`¿Confirmas ${servicioProfesionalPendienteToggle?.disponible ? 'desactivar' : 'activar'} ${servicioProfesionalPendienteToggle?.nombre} para ${editingProfesional?.nombre} ${editingProfesional?.apellido}?`"
    :textoConfirmar="submittingToggle ? 'Guardando...' : (servicioProfesionalPendienteToggle?.disponible ? 'Desactivar' : 'Activar')"
    :colorBoton="servicioProfesionalPendienteToggle?.disponible ? 'bg-rose-600 hover:bg-rose-700' : 'bg-emerald-600 hover:bg-emerald-700'"
    @confirm="ejecutarToggleServicioProfesional"
    @cancel="cerrarConfirmToggleServicioProfesional"
  />

  <CambiarContrasenaModal
    :show="showChangePasswordModal"
    :logoutOnSuccess="true"
    @close="cerrarModalCambiarContrasena"
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
import CambiarContrasenaModal from '../components/CambiarContrasenaModal.vue'
import { formatCurrencyARS as formatearMonedaARS } from '../utils/currency'

type FieldErrors = Record<string, string>

interface Profesional {
  id: number
  nombre: string
  apellido: string
  email: string
  telefono?: string | null
  descripcion?: string | null
  activo: boolean
}

interface ProfesionalForm {
  nombre: string
  apellido: string
  email: string
  contrasena: string
  telefono: string
  descripcion: string
}

interface ServicioProfesionalItem {
  servicioId: number
  nombre: string
  duracionMinutos: number
  precio: number
  disponible: boolean
}

interface HorarioEmpresa {
  id: number
  diaSemana: string
  horaInicio: string
  horaFin: string
}

interface HorarioForm {
  diaSemana: string
  horaInicio: string
  horaFin: string
}

interface ApiErrorLike {
  response?: {
    status?: number
    data?: {
      errores?: FieldErrors
      mensaje?: string
      message?: string
      error?: string
      turnosAfectados?: string[]
      profesionalesAfectados?: string[]
    }
  }
  message?: string
}

const router = useRouter()
const authStore = useAuthStore()
const toast = useToastStore()

const nombreEmpresa = ref('')
const empresaId = ref<number | null>(null)
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)
const userMenuButtonRef = ref<HTMLButtonElement | null>(null)
const showChangePasswordModal = ref(false)

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

// Estado para Políticas de Cancelación
const politicas = ref<PoliticaCancelacionResponse[]>([])
const loadingPoliticas = ref(false)
const showModalPolitica = ref(false)
const errorPolitica = ref('')
const submittingPolitica = ref(false)
const submittingTogglePolitica = ref(false)
const fieldErrorsPolitica = ref<FieldErrors>({})

const formDataPolitica = ref<PoliticaCancelacionRequest>({
  tipo: 'CANCELACION',
  descripcion: '',
  horasLimiteCancelacion: 24,
  penalizacion: 'ADVERTENCIA',
  activa: true
})

const politicaOriginal = ref<PoliticaCancelacionRequest>({
  tipo: 'CANCELACION',
  descripcion: '',
  horasLimiteCancelacion: 24,
  penalizacion: 'ADVERTENCIA',
  activa: true
})

// Estado para Profesionales
const profesionales = ref<Profesional[]>([])
const serviciosProfesional = ref<ServicioProfesionalItem[]>([])
const cargandoServicios = ref(false)
const submittingToggle = ref(false)
const loading = ref(false)
const showModal = ref(false)
const editingProfesional = ref<Profesional | null>(null)
const error = ref('')
const submitting = ref(false)
const fieldErrors = ref<FieldErrors>({})

const formData = ref<ProfesionalForm>({
  nombre: '',
  apellido: '',
  email: '',
  contrasena: '',
  telefono: '',
  descripcion: ''
})

const profesionalOriginal = ref({
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
const fieldErrorsServicio = ref<FieldErrors>({})

const formDataServicio = ref<ServicioRequest>({
  nombre: '',
  descripcion: '',
  duracionMinutos: 0,
  bufferMinutos: undefined,
  precio: 0,
  requiereSena: false,
  montoSena: null
})

const servicioOriginal = ref<ServicioRequest>({
  nombre: '',
  descripcion: '',
  duracionMinutos: 0,
  bufferMinutos: undefined,
  precio: 0,
  requiereSena: false,
  montoSena: null
})

// Estado para Horarios
const horarios = ref<HorarioEmpresa[]>([])
const loadingHorarios = ref(false)
const showModalHorario = ref(false)
const editingHorario = ref<HorarioEmpresa | null>(null)
const errorHorario = ref('')
const submittingHorario = ref(false)
const fieldErrorsHorario = ref<FieldErrors>({})

// Estado para modal de confirmación de eliminación de horario
const showConfirmDeleteHorario = ref(false)
const horarioPendienteEliminar = ref<HorarioEmpresa>({
  id: 0,
  diaSemana: 'LUNES',
  horaInicio: '',
  horaFin: ''
})

const showConfirmTogglePolitica = ref(false)
const politicaPendienteToggle = ref<PoliticaCancelacionResponse | null>(null)

const showConfirmToggleServicioProfesional = ref(false)
const servicioProfesionalPendienteToggle = ref<ServicioProfesionalItem | null>(null)

// Detalles de conflicto 409 en el formulario de horario
const horarioConflictoDetalles = ref<string[]>([])

const formDataHorario = ref<HorarioForm>({
  diaSemana: '',
  horaInicio: '',
  horaFin: ''
})

const horarioOriginal = ref({
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
const horariosAgrupados = ref<Record<string, HorarioEmpresa[]>>({})

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
const fieldErrorsConfiguracion = ref<FieldErrors>({})

const formDataConfiguracion = ref({
  bufferPorDefecto: 5,
  tiempoMinimoAnticipacionMinutos: 30,
  diasMaximosReserva: 30,
  horasAntesRecordatorio: 24,
  enviarRecordatorios: true,
  timezone: 'America/Argentina/Buenos_Aires',
  datosBancarios: '' as string | null
})

const configuracionOriginal = ref({
  bufferPorDefecto: 5,
  tiempoMinimoAnticipacionMinutos: 30,
  diasMaximosReserva: 30,
  horasAntesRecordatorio: 24,
  enviarRecordatorios: true,
  timezone: 'America/Argentina/Buenos_Aires',
  datosBancarios: '' as string | null
})

function agruparHorariosPorDia() {
  const agrupados: Record<string, HorarioEmpresa[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = horarios.value.filter(h => h.diaSemana === dia)
  })
  horariosAgrupados.value = agrupados
}

function extractApiErrorInfo(err: unknown, fallbackMessage: string): { fieldErrors?: FieldErrors; message: string } {
  const apiError = err as ApiErrorLike
  const data = apiError?.response?.data
  if (data?.errores) {
    return { fieldErrors: data.errores, message: 'Por favor corrija los errores en el formulario.' }
  }
  return {
    message: data?.mensaje || data?.message || data?.error || apiError?.message || fallbackMessage
  }
}

function extractApiErrorMessage(err: unknown, fallbackMessage: string): string {
  return extractApiErrorInfo(err, fallbackMessage).message
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutsideUserMenu)
  document.addEventListener('keydown', handleEscapeKey)
  await cargarNombreEmpresa()
  await Promise.all([
    cargarProfesionales(),
    cargarServicios(),
    cargarHorarios(),
    // Políticas ocultas temporalmente: evitamos cargar datos innecesarios en esta versión
    // cargarPoliticasCancelacion(),
    cargarConfiguracion()
  ])
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

// --- 1. Profesional ---
const formProfesionalDirty = computed(() => JSON.stringify(formData.value) !== JSON.stringify(profesionalOriginal.value))
const formProfesionalValido = computed(() => {
  const nombre = (formData.value.nombre ?? '').trim()
  const apellido = (formData.value.apellido ?? '').trim()
  const email = (formData.value.email ?? '').trim()
  const contrasena = (formData.value.contrasena ?? '').trim()
  const baseValido = nombre.length > 0 && apellido.length > 0 && EMAIL_REGEX.test(email)
  return editingProfesional.value ? baseValido : (baseValido && contrasena.length > 0)
})
const canSubmitProfesional = computed(() => formProfesionalDirty.value && formProfesionalValido.value && !submitting.value)

// --- 2. Servicio ---
const formServicioDirty = computed(() => JSON.stringify(formDataServicio.value) !== JSON.stringify(servicioOriginal.value))
const formServicioValido = computed(() => {
  const nombre = (formDataServicio.value.nombre ?? '').trim()
  const duracionValida = Number(formDataServicio.value.duracionMinutos) > 0
  const precioValido = Number(formDataServicio.value.precio) >= 0
  if (formDataServicio.value.requiereSena) {
    return nombre.length > 0 && duracionValida && precioValido && Number(formDataServicio.value.montoSena) > 0
  }
  return nombre.length > 0 && duracionValida && precioValido
})
const canSubmitServicio = computed(() => formServicioDirty.value && formServicioValido.value && !submittingServicio.value)

// --- 3. Horario ---
const formHorarioDirty = computed(() => JSON.stringify(formDataHorario.value) !== JSON.stringify(horarioOriginal.value))
const formHorarioValido = computed(() => {
  return (formDataHorario.value.diaSemana ?? '').trim().length > 0
    && (formDataHorario.value.horaInicio ?? '').trim().length > 0
    && (formDataHorario.value.horaFin ?? '').trim().length > 0
})
const canSubmitHorario = computed(() => formHorarioDirty.value && formHorarioValido.value && !submittingHorario.value)

// --- 4. Política ---
const formPoliticaDirty = computed(() => JSON.stringify(formDataPolitica.value) !== JSON.stringify(politicaOriginal.value))
const formPoliticaValida = computed(() => {
  const descripcion = (formDataPolitica.value.descripcion ?? '').trim()
  if (formDataPolitica.value.tipo === 'INASISTENCIA') {
    return descripcion.length > 0
  }
  return descripcion.length > 0 && Number(formDataPolitica.value.horasLimiteCancelacion) >= 1
})
const canSubmitPolitica = computed(() => formPoliticaDirty.value && formPoliticaValida.value && !submittingPolitica.value)

// --- 5. Configuración ---
const formConfiguracionDirty = computed(() => JSON.stringify(formDataConfiguracion.value) !== JSON.stringify(configuracionOriginal.value))
const formConfiguracionValida = computed(() => {
  const bufferValido = Number(formDataConfiguracion.value.bufferPorDefecto) >= 0 && Number(formDataConfiguracion.value.bufferPorDefecto) <= 120
  const anticipacionValida = Number(formDataConfiguracion.value.tiempoMinimoAnticipacionMinutos) >= 0 && Number(formDataConfiguracion.value.tiempoMinimoAnticipacionMinutos) <= 1440
  const diasValido = Number(formDataConfiguracion.value.diasMaximosReserva) >= 1 && Number(formDataConfiguracion.value.diasMaximosReserva) <= 365
  if (!formDataConfiguracion.value.enviarRecordatorios) {
    return bufferValido && anticipacionValida && diasValido
  }
  const recordatorioValido = Number(formDataConfiguracion.value.horasAntesRecordatorio) >= 1 && Number(formDataConfiguracion.value.horasAntesRecordatorio) <= 168
  return bufferValido && anticipacionValida && diasValido && recordatorioValido
})
const canSubmitConfiguracion = computed(() => formConfiguracionDirty.value && formConfiguracionValida.value && !submittingConfiguracion.value)

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

function mapearEmpresaAFormulario(data: unknown) {
  const empresa = (data && typeof data === 'object') ? (data as Record<string, unknown>) : {}
  const mapped = {
    nombre: (empresa.nombre as string | undefined) ?? '',
    descripcion: (empresa.descripcion as string | undefined) ?? '',
    direccion: (empresa.direccion as string | undefined) ?? '',
    ciudad: (empresa.ciudad as string | undefined) ?? '',
    provincia: (empresa.provincia as string | undefined) ?? '',
    telefono: (empresa.telefono as string | undefined) ?? '',
    email: (empresa.email as string | undefined) ?? ''
  }

  empresaOriginal.value = { ...mapped }
  formDataEmpresa.value = { ...mapped }
}

function abrirModalEditarEmpresa() {
  closeUserMenu(false)
  errorEmpresa.value = ''
  formDataEmpresa.value = { ...empresaOriginal.value }
  fieldErrorsEmpresa.value = validarFormularioEmpresa()
  showModalEmpresa.value = true
}

function abrirModalCambiarContrasena() {
  closeUserMenu(false)
  showChangePasswordModal.value = true
}

function cerrarModalCambiarContrasena() {
  showChangePasswordModal.value = false
  setTimeout(() => userMenuButtonRef.value?.focus(), 0)
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
  const errores: FieldErrors = {}
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
  } catch (err: unknown) {
    const { fieldErrors, message } = extractApiErrorInfo(err, 'Error al actualizar la empresa. Intente nuevamente.')
    if (fieldErrors) {
      fieldErrorsEmpresa.value = fieldErrors
    }
    errorEmpresa.value = message
  } finally {
    submittingEmpresa.value = false
  }
}

// ====Funcion para cargar Nombre de la empresa====
async function cargarNombreEmpresa() {
  try {
    const response = await api.get('/dueno/empresa')
    let data: unknown = response.data
    if (typeof data === 'string') {
      try {
        data = JSON.parse(data)
      } catch (parseError: unknown) {
        console.error('[FRONTEND] Error al parsear data:', parseError)
        data = {}
      }
    }
    const empresa = (data && typeof data === 'object') ? (data as Record<string, unknown>) : {}
    nombreEmpresa.value = (empresa.nombre as string | undefined) || 'Mi Empresa'
    empresaId.value = (empresa.id as number | undefined) || null
    mapearEmpresaAFormulario(data)
  } catch (err: unknown) {
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
    if (!empresaId.value) {
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
      } catch (parseError: unknown) {
        console.error('[FRONTEND] Error al parsear políticas:', parseError)
        politicasData = []
      }
    }
    politicas.value = Array.isArray(politicasData) ? politicasData : []
  } catch (err: unknown) {
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
  politicaOriginal.value = { ...formDataPolitica.value }
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
  } catch (err: unknown) {
    console.error('Error al guardar política:', err)
    const { fieldErrors, message } = extractApiErrorInfo(err, 'Error al crear la política.')
    if (fieldErrors) {
      fieldErrorsPolitica.value = fieldErrors
    }
    errorPolitica.value = message
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
  } catch (err: unknown) {
    console.error('Error al cambiar estado de política:', err)
    toast.showError(extractApiErrorMessage(err, 'Error al cambiar el estado de la política.'))
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
  } catch (err: unknown) {
    console.error('Error al eliminar política:', err)
    toast.showError(extractApiErrorMessage(err, 'Error al eliminar la política.'))
  } finally {
    politicaPendienteEliminar.value = null
  }
}

// ==================== FUNCIONES AUXILIARES ====================

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
  } catch (err: unknown) {
    console.error('Error al cargar profesionales:', err)
    profesionales.value = [] // Limpiar en caso de error
    error.value = 'Error al cargar la lista de profesionales'
  } finally {
    loading.value = false
  }
}



function openModal(profesional: Profesional | null = null) {
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
  profesionalOriginal.value = { ...formData.value }
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
  } catch (err: unknown) {
    console.error('Error al cargar servicios:', err)
    toast.showError('Error al cargar servicios del profesional')
  } finally {
    cargandoServicios.value = false
  }
}

async function toggleServicio(servicio: ServicioProfesionalItem) {
  if (!editingProfesional.value) return
  
  submittingToggle.value = true
  try {
    await api.patch(`/dueno/profesionales/${editingProfesional.value.id}/servicios/toggle`, {
      servicioId: servicio.servicioId,
      disponible: !servicio.disponible
    })
    
    // Actualizar localmente
    servicio.disponible = !servicio.disponible
  } catch (err: unknown) {
    console.error('Error al cambiar servicio:', err)
    toast.showError(extractApiErrorMessage(err, 'Error al cambiar el estado del servicio'))
  } finally {
    submittingToggle.value = false
  }
}

function confirmarToggleServicioProfesional(servicio: ServicioProfesionalItem) {
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
  } catch (err: unknown) {
    console.error('Error completo:', err)
    const { fieldErrors: apiFieldErrors, message } = extractApiErrorInfo(err, 'Error al guardar el profesional. Por favor intente nuevamente.')
    if (apiFieldErrors) {
      fieldErrors.value = apiFieldErrors
    }
    error.value = message
  } finally {
    submitting.value = false
  }
}

const profesionalPendienteToggle = ref<Profesional | null>(null)
const showConfirmToggleProfesional = ref(false)

function confirmarToggleProfesional(profesional: Profesional) {
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
  } catch (err: unknown) {
    console.error(`Error al ${accion} profesional:`, err)
    toast.showError(extractApiErrorMessage(err, `Error al ${accion} el profesional`))
  } finally {
    profesionalPendienteToggle.value = null
  }
}

function handleLogout() {
  closeUserMenu(false)
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
  } catch (err: unknown) {
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
  servicioOriginal.value = { ...formDataServicio.value }
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
  } catch (err: unknown) {
    console.error('Error completo:', err)
    const { fieldErrors, message } = extractApiErrorInfo(err, 'Error al guardar el servicio. Por favor intente nuevamente.')
    if (fieldErrors) {
      fieldErrorsServicio.value = fieldErrors
    }
    errorServicio.value = message
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
  } catch (err: unknown) {
    console.error('Error al cambiar estado del servicio:', err)
    toast.showError(extractApiErrorMessage(err, 'Error al cambiar el estado del servicio'))
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
  } catch (err: unknown) {
    console.error('Error al cargar horarios:', err)
    horarios.value = []
    agruparHorariosPorDia()
    toast.showError('Error al cargar los horarios de la empresa')
  } finally {
    loadingHorarios.value = false
  }
}

function openModalHorario(horario: HorarioEmpresa | null = null) {
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
  horarioOriginal.value = { ...formDataHorario.value }
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
  } catch (err: unknown) {
    console.error('Error al guardar horario:', err)
    horarioConflictoDetalles.value = []
    const apiError = err as ApiErrorLike
    const status = apiError.response?.status
    const data = apiError.response?.data
    
    if (status === 409) {
      // Conflicto de disponibilidad de profesionales o turnos activos
      if (data?.turnosAfectados?.length) {
        errorHorario.value = data.mensaje || 'No se puede guardar el horario: hay turnos afectados.'
        horarioConflictoDetalles.value = data.turnosAfectados as string[]
      } else if (data?.profesionalesAfectados?.length) {
        errorHorario.value = data.mensaje || 'No se puede guardar el horario: hay profesionales afectados.'
        horarioConflictoDetalles.value = data.profesionalesAfectados as string[]
      } else {
        errorHorario.value = data?.mensaje || 'El horario no puede modificarse porque hay conflictos activos.'
      }
    } else if (status === 400) {
      const mensaje = data?.mensaje || data?.message
      errorHorario.value = mensaje || 'Error al validar los datos del horario'
    } else {
      errorHorario.value = 'Error al guardar el horario'
    }
  } finally {
    submittingHorario.value = false
  }
}

function confirmarEliminarHorario(horario: HorarioEmpresa) {
  horarioPendienteEliminar.value = horario
  showConfirmDeleteHorario.value = true
}

function cerrarConfirmDeleteHorario() {
  showConfirmDeleteHorario.value = false
  horarioPendienteEliminar.value = {
    id: 0,
    diaSemana: 'LUNES',
    horaInicio: '',
    horaFin: ''
  }
}

async function ejecutarEliminarHorario() {
  if (!horarioPendienteEliminar.value.id) return
  try {
    await api.eliminarHorarioEmpresa(horarioPendienteEliminar.value.id)
    await cargarHorarios()
    cerrarConfirmDeleteHorario()
    toast.showSuccess('Horario eliminado correctamente')
  } catch (err: unknown) {
    console.error('Error al eliminar horario:', err)
    cerrarConfirmDeleteHorario()
    const apiError = err as ApiErrorLike
    if (apiError.response?.status === 409) {
      const data = apiError.response?.data
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
  } catch (err: unknown) {
    console.error('Error al copiar horarios:', err)
    const apiError = err as ApiErrorLike
    
    if (apiError.response?.status === 400) {
      const mensaje = apiError.response.data?.mensaje || apiError.response.data?.message
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
    configuracionOriginal.value = { ...formDataConfiguracion.value }
  } catch (err: unknown) {
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
    await api.actualizarConfiguracion(formDataConfiguracion.value)
    
    // Mostrar mensaje de éxito
    toast.showSuccess('Configuración guardada exitosamente')
    
    // Recargar configuración para reflejar cambios
    await cargarConfiguracion()
  } catch (err: unknown) {
    console.error('Error al guardar configuración:', err)
    const { fieldErrors, message } = extractApiErrorInfo(err, 'Error al guardar la configuración. Por favor intente nuevamente.')
    if (fieldErrors) {
      fieldErrorsConfiguracion.value = fieldErrors
    }
    errorConfiguracion.value = message
  } finally {
    submittingConfiguracion.value = false
  }
}

</script>



