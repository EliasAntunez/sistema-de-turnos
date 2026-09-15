<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const WA_LINK =
  'https://wa.me/5493755831137?text=Hola,%20me%20interesa%20probar%20AnSa%20Turnos%20para%20mi%20negocio'

// ─────────────────────────────────────────────────────────────────────────────
// MICROSOFT CLARITY — Rastreo solo para esta vista pública (Landing Page)
//
// Instrucciones de inyección:
//
//   OPCIÓN A (recomendada — index.html):
//   En /frontend/index.html, dentro del <head>, agregá el siguiente bloque.
//   La condición evita que Clarity se cargue en rutas del sistema interno.
//
//   <script>
//     if (!window.location.pathname.startsWith('/login') &&
//         !window.location.pathname.startsWith('/admin') &&
//         !window.location.pathname.startsWith('/dueno') &&
//         !window.location.pathname.startsWith('/profesional')) {
//       (function(c,l,a,r,i,t,y){
//         c[a]=c[a]||function(){(c[a].q=c[a].q||[]).push(arguments)};
//         t=l.createElement(r);t.async=1;t.src="https://www.clarity.ms/tag/"+i;
//         y=l.getElementsByTagName(r)[0];y.parentNode.insertBefore(t,y);
//       })(window, document, "clarity", "script", "yivid5mply");
//     }
//   <\/script>
//
//   OPCIÓN B (granular — solo esta vista):
//   Descomentar el bloque onMounted de abajo. Inyecta Clarity dinámicamente
//   SOLO cuando el usuario visita la Landing. No se carga en ninguna otra ruta.
//
// import { onMounted } from 'vue'
// onMounted(() => {
//   if (document.getElementById('clarity-script')) return // evitar duplicado
//   ;(function (c: any, l: any, a: string, r: string, i: string) {
//     c[a] = c[a] || function () { (c[a].q = c[a].q || []).push(arguments) }
//     const t = l.createElement(r) as HTMLScriptElement
//     t.id = 'clarity-script'
//     t.async = true
//     t.src = 'https://www.clarity.ms/tag/' + i
//     const y = l.getElementsByTagName(r)[0]
//     y.parentNode?.insertBefore(t, y)
//   })(window, document, 'clarity', 'script', 'yivid5mply')
// })
// ─────────────────────────────────────────────────────────────────────────────
</script>

<template>
  <!-- LANDING PAGE: AnSa Turnos -->
  <div class="min-h-screen bg-white font-sans antialiased">

    <!-- ═══════════════════════════════════════════════════════════
         HEADER / NAVBAR
    ════════════════════════════════════════════════════════════ -->
    <header class="sticky top-0 z-40 bg-white/95 backdrop-blur-sm border-b border-slate-100 shadow-sm">
      <nav class="max-w-6xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">

        <!-- Logo / Nombre -->
        <div class="flex items-center gap-2">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-600 to-indigo-600 flex items-center justify-center shadow-md">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="white" class="w-5 h-5">
              <path d="M12.75 12.75a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM7.5 15.75a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5ZM8.25 17.25a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM9.75 15.75a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5ZM10.5 17.25a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM12 15.75a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5ZM12.75 17.25a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM14.25 15.75a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5ZM15 17.25a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM16.5 15.75a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5ZM15 12.75a.75.75 0 1 1-1.5 0 .75.75 0 0 1 1.5 0ZM16.5 13.5a.75.75 0 1 0 0-1.5.75.75 0 0 0 0 1.5Z" />
              <path fill-rule="evenodd" d="M6.75 2.25A.75.75 0 0 1 7.5 3v1.5h9V3A.75.75 0 0 1 18 3v1.5h.75a3 3 0 0 1 3 3v11.25a3 3 0 0 1-3 3H5.25a3 3 0 0 1-3-3V7.5a3 3 0 0 1 3-3H6V3a.75.75 0 0 1 .75-.75Zm13.5 9a1.5 1.5 0 0 0-1.5-1.5H5.25a1.5 1.5 0 0 0-1.5 1.5v7.5a1.5 1.5 0 0 0 1.5 1.5h13.5a1.5 1.5 0 0 0 1.5-1.5v-7.5Z" clip-rule="evenodd" />
            </svg>
          </div>
          <span class="text-lg font-bold tracking-tight text-slate-800">
            AnSa <span class="text-violet-600">Turnos</span>
          </span>
        </div>

        <!-- CTA Navbar -->
        <button
          id="btn-navbar-login"
          @click="router.push('/login')"
          class="inline-flex items-center gap-2 bg-violet-600 hover:bg-violet-700 active:scale-95 text-white text-sm font-semibold px-4 py-2 rounded-xl shadow-md shadow-violet-200 transition-all duration-200"
        >
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-4 h-4">
            <path fill-rule="evenodd" d="M3 4.25A2.25 2.25 0 0 1 5.25 2h5.5A2.25 2.25 0 0 1 13 4.25v2a.75.75 0 0 1-1.5 0v-2a.75.75 0 0 0-.75-.75h-5.5a.75.75 0 0 0-.75.75v11.5c0 .414.336.75.75.75h5.5a.75.75 0 0 0 .75-.75v-2a.75.75 0 0 1 1.5 0v2A2.25 2.25 0 0 1 10.75 18h-5.5A2.25 2.25 0 0 1 3 15.75V4.25Z" clip-rule="evenodd" />
            <path fill-rule="evenodd" d="M6 10a.75.75 0 0 1 .75-.75h9.546l-1.048-.943a.75.75 0 1 1 1.004-1.114l2.5 2.25a.75.75 0 0 1 0 1.114l-2.5 2.25a.75.75 0 1 1-1.004-1.114l1.048-.943H6.75A.75.75 0 0 1 6 10Z" clip-rule="evenodd" />
          </svg>
          Iniciar Sesión
        </button>
      </nav>
    </header>

    <main>

      <!-- ═══════════════════════════════════════════════════════════
           HERO SECTION
      ════════════════════════════════════════════════════════════ -->
      <section
        id="hero"
        class="relative overflow-hidden bg-gradient-to-br from-slate-900 via-violet-950 to-indigo-950 text-white"
      >
        <!-- Fondo decorativo: puntos -->
        <div
          class="absolute inset-0 opacity-20"
          style="background-image: radial-gradient(circle, #a78bfa 1px, transparent 1px); background-size: 32px 32px;"
        ></div>
        <!-- Glow blob superior izquierdo -->
        <div class="absolute -top-24 -left-24 w-72 h-72 bg-violet-600 rounded-full filter blur-3xl opacity-25 pointer-events-none"></div>
        <!-- Glow blob inferior derecho -->
        <div class="absolute -bottom-24 -right-24 w-96 h-96 bg-indigo-600 rounded-full filter blur-3xl opacity-20 pointer-events-none"></div>

        <div class="relative max-w-5xl mx-auto px-4 sm:px-6 py-20 sm:py-28 flex flex-col items-center text-center gap-6">

          <!-- Badge -->
          <span class="inline-flex items-center gap-2 bg-violet-500/20 border border-violet-400/30 text-violet-300 text-xs font-semibold px-4 py-1.5 rounded-full tracking-wider uppercase">
            <span class="w-2 h-2 rounded-full bg-violet-400 animate-pulse"></span>
            Asistente virtual 24/7
          </span>

          <!-- Título principal -->
          <h1 class="text-3xl sm:text-5xl font-extrabold leading-tight tracking-tight max-w-3xl">
            Automatizá los turnos de tu&nbsp;negocio
            <span class="bg-gradient-to-r from-violet-400 to-indigo-400 bg-clip-text text-transparent">
              por WhatsApp.
            </span>
          </h1>

          <!-- Subtítulo -->
          <p class="text-base sm:text-xl text-slate-300 max-w-2xl leading-relaxed">
            Tus manos están ocupadas. Dejá que nuestro asistente virtual atienda a tus clientes
            <strong class="text-white font-semibold">24/7</strong> y organice tu agenda sin superposiciones.
          </p>

          <!-- CTA principal -->
          <a
            id="btn-hero-cta"
            :href="WA_LINK"
            target="_blank"
            rel="noopener noreferrer"
            class="group mt-2 inline-flex items-center gap-3 bg-gradient-to-r from-emerald-500 to-green-500 hover:from-emerald-400 hover:to-green-400 active:scale-95 text-white font-bold text-base sm:text-lg px-7 py-4 rounded-2xl shadow-xl shadow-green-900/40 transition-all duration-200"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-6 h-6 group-hover:scale-110 transition-transform duration-200">
              <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 0 1-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 0 1-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 0 1 2.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0 0 12.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 0 0 5.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 0 0-3.48-8.413Z"/>
            </svg>
            Quiero probarlo gratis
          </a>

          <!-- Social proof inline -->
          <p class="text-slate-400 text-sm mt-1">Sin tarjeta de crédito · Activación en minutos</p>

          <!-- Mockup visual de conversación WhatsApp -->
          <div class="mt-8 w-full max-w-sm sm:max-w-md bg-white/5 border border-white/10 rounded-2xl p-4 text-left backdrop-blur-sm shadow-2xl">
            <div class="flex items-center gap-2 mb-3">
              <div class="w-3 h-3 rounded-full bg-red-400"></div>
              <div class="w-3 h-3 rounded-full bg-yellow-400"></div>
              <div class="w-3 h-3 rounded-full bg-green-400"></div>
              <span class="ml-2 text-xs text-slate-400 font-mono">WhatsApp — Asistente AnSa</span>
            </div>
            <div class="space-y-2">
              <div class="flex justify-end">
                <span class="bg-emerald-600/80 text-white text-xs px-3 py-2 rounded-2xl rounded-tr-sm max-w-xs">
                  Hola! Quiero turno para el viernes a las 10
                </span>
              </div>
              <div class="flex justify-start">
                <span class="bg-white/10 text-slate-200 text-xs px-3 py-2 rounded-2xl rounded-tl-sm max-w-xs">
                  ¡Hola! 👋 El viernes a las 10:00 está disponible. ¿Lo reservamos a nombre de quién?
                </span>
              </div>
              <div class="flex justify-end">
                <span class="bg-emerald-600/80 text-white text-xs px-3 py-2 rounded-2xl rounded-tr-sm max-w-xs">
                  Sí, a nombre de María
                </span>
              </div>
              <div class="flex justify-start">
                <span class="bg-white/10 text-slate-200 text-xs px-3 py-2 rounded-2xl rounded-tl-sm max-w-xs">
                  ✅ ¡Listo, María! Tu turno quedó confirmado para el viernes a las 10:00. Te esperamos 💜
                </span>
              </div>
            </div>
          </div>

        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════════
           SECCIÓN DE BENEFICIOS
      ════════════════════════════════════════════════════════════ -->
      <section id="beneficios" class="py-16 sm:py-24 bg-slate-50">
        <div class="max-w-5xl mx-auto px-4 sm:px-6">

          <div class="text-center mb-12">
            <p class="text-violet-600 font-semibold text-sm uppercase tracking-wider mb-2">¿Por qué AnSa Turnos?</p>
            <h2 class="text-2xl sm:text-3xl font-extrabold text-slate-800 leading-tight">
              Todo lo que necesitás, sin complicaciones
            </h2>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">

            <!-- Beneficio 1 -->
            <div
              id="beneficio-mensajes"
              class="group bg-white rounded-2xl p-6 shadow-sm border border-slate-100 hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
            >
              <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center mb-4 group-hover:bg-violet-600 transition-colors duration-300">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"
                  class="w-6 h-6 text-violet-600 group-hover:text-white transition-colors duration-300">
                  <path fill-rule="evenodd" d="M4.848 2.771A49.144 49.144 0 0 1 12 2.25c2.43 0 4.817.178 7.152.52 1.978.292 3.348 2.024 3.348 3.97v6.02c0 1.946-1.37 3.678-3.348 3.97a48.901 48.901 0 0 1-3.476.383.39.39 0 0 0-.297.17l-2.755 4.133a.75.75 0 0 1-1.248 0l-2.755-4.133a.39.39 0 0 0-.297-.17 48.9 48.9 0 0 1-3.476-.384c-1.978-.29-3.348-2.024-3.348-3.97V6.741c0-1.946 1.37-3.68 3.348-3.97Z" clip-rule="evenodd" />
                </svg>
              </div>
              <h3 class="text-lg font-bold text-slate-800 mb-2">Adiós a los mensajes sin leer</h3>
              <p class="text-slate-500 text-sm leading-relaxed">
                Respuestas instantáneas para que no pierdas ningún cliente, aunque estés en plena sesión.
              </p>
            </div>

            <!-- Beneficio 2 -->
            <div
              id="beneficio-agenda"
              class="group bg-white rounded-2xl p-6 shadow-sm border border-slate-100 hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
            >
              <div class="w-12 h-12 rounded-xl bg-indigo-100 flex items-center justify-center mb-4 group-hover:bg-indigo-600 transition-colors duration-300">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"
                  class="w-6 h-6 text-indigo-600 group-hover:text-white transition-colors duration-300">
                  <path fill-rule="evenodd" d="M6.75 2.25A.75.75 0 0 1 7.5 3v1.5h9V3A.75.75 0 0 1 18 3v1.5h.75a3 3 0 0 1 3 3v11.25a3 3 0 0 1-3 3H5.25a3 3 0 0 1-3-3V7.5a3 3 0 0 1 3-3H6V3a.75.75 0 0 1 .75-.75Zm13.5 9a1.5 1.5 0 0 0-1.5-1.5H5.25a1.5 1.5 0 0 0-1.5 1.5v7.5a1.5 1.5 0 0 0 1.5 1.5h13.5a1.5 1.5 0 0 0 1.5-1.5v-7.5Z" clip-rule="evenodd" />
                </svg>
              </div>
              <h3 class="text-lg font-bold text-slate-800 mb-2">Agenda siempre organizada</h3>
              <p class="text-slate-500 text-sm leading-relaxed">
                Visualizá tu día de un vistazo, sin turnos superpuestos ni conflictos de horario.
              </p>
            </div>

            <!-- Beneficio 3 -->
            <div
              id="beneficio-facil"
              class="group bg-white rounded-2xl p-6 shadow-sm border border-slate-100 hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
            >
              <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center mb-4 group-hover:bg-emerald-600 transition-colors duration-300">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"
                  class="w-6 h-6 text-emerald-600 group-hover:text-white transition-colors duration-300">
                  <path d="M10.5 18.75a.75.75 0 0 0 0 1.5h3a.75.75 0 0 0 0-1.5h-3Z" />
                  <path fill-rule="evenodd" d="M8.625.75A3.375 3.375 0 0 0 5.25 4.125v15.75a3.375 3.375 0 0 0 3.375 3.375h6.75a3.375 3.375 0 0 0 3.375-3.375V4.125A3.375 3.375 0 0 0 15.375.75h-6.75ZM7.5 4.125C7.5 3.504 8.004 3 8.625 3H9.75v.375c0 .621.504 1.125 1.125 1.125h2.25c.621 0 1.125-.504 1.125-1.125V3h1.125c.621 0 1.125.504 1.125 1.125v15.75c0 .621-.504 1.125-1.125 1.125h-6.75A1.125 1.125 0 0 1 7.5 19.875V4.125Z" clip-rule="evenodd" />
                </svg>
              </div>
              <h3 class="text-lg font-bold text-slate-800 mb-2">Fácil para vos y tus clientes</h3>
              <p class="text-slate-500 text-sm leading-relaxed">
                Sin instalar apps raras, todo desde WhatsApp. Tus clientes ya saben cómo usarlo.
              </p>
            </div>

          </div>
        </div>
      </section>

      <!-- ═══════════════════════════════════════════════════════════
           PRUEBA SOCIAL
      ════════════════════════════════════════════════════════════ -->
      <section
        id="prueba-social"
        class="py-14 sm:py-20 bg-gradient-to-br from-violet-600 to-indigo-700 text-white"
      >
        <div class="max-w-3xl mx-auto px-4 sm:px-6 text-center">
          <div class="flex justify-center gap-1 mb-4">
            <svg v-for="n in 5" :key="n" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" class="w-6 h-6 text-yellow-400">
              <path fill-rule="evenodd" d="M10.868 2.884c-.321-.772-1.415-.772-1.736 0l-1.83 4.401-4.753.381c-.833.067-1.171 1.107-.536 1.651l3.62 3.102-1.106 4.637c-.194.813.691 1.456 1.405 1.02L10 15.591l4.069 2.485c.713.436 1.598-.207 1.404-1.02l-1.106-4.637 3.62-3.102c.635-.544.297-1.584-.536-1.65l-4.752-.382-1.831-4.401Z" clip-rule="evenodd" />
            </svg>
          </div>
          <blockquote class="text-xl sm:text-2xl font-bold leading-snug mb-4">
            "Gestión 100% desatendida probada por profesionales de la estética."
          </blockquote>
          <p class="text-violet-200 text-sm">
            Estéticas, manicuras y salones de belleza ya confían en AnSa Turnos.
          </p>

          <a
            id="btn-social-cta"
            :href="WA_LINK"
            target="_blank"
            rel="noopener noreferrer"
            class="mt-8 inline-flex items-center gap-2 bg-white text-violet-700 font-bold text-sm sm:text-base px-6 py-3 rounded-xl shadow-lg hover:bg-violet-50 active:scale-95 transition-all duration-200"
          >
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-5 h-5 text-green-600">
              <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 0 1-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 0 1-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 0 1 2.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0 0 12.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 0 0 5.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 0 0-3.48-8.413Z"/>
            </svg>
            Hablar con un asesor por WhatsApp
          </a>
        </div>
      </section>

    </main>

    <!-- ═══════════════════════════════════════════════════════════
         FOOTER
    ════════════════════════════════════════════════════════════ -->
    <footer class="bg-slate-900 text-slate-400 py-8">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-sm">
        <span class="font-semibold text-white">AnSa <span class="text-violet-400">Turnos</span></span>
        <span>© {{ new Date().getFullYear() }} · Todos los derechos reservados</span>
      </div>
    </footer>

    <!-- ═══════════════════════════════════════════════════════════
         BOTÓN FLOTANTE DE WHATSAPP
    ════════════════════════════════════════════════════════════ -->
    <a
      id="btn-whatsapp-flotante"
      :href="WA_LINK"
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Contactar por WhatsApp"
      class="fixed bottom-6 right-5 z-50 w-14 h-14 bg-green-500 hover:bg-green-400 active:scale-90 rounded-full shadow-2xl shadow-green-900/50 flex items-center justify-center transition-all duration-200 hover:scale-110"
    >
      <!-- Efecto ping de atención -->
      <span class="absolute -top-1 -right-1 w-4 h-4 bg-green-400 rounded-full animate-ping opacity-75"></span>
      <!-- WhatsApp SVG oficial -->
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="white" class="w-7 h-7">
        <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 0 1-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 0 1-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 0 1 2.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0 0 12.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 0 0 5.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 0 0-3.48-8.413Z"/>
      </svg>
    </a>

  </div>
</template>
