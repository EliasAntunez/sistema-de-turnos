import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useClienteStore } from '../stores/cliente'
import api from '../services/api'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'
import DuenoView from '../views/DuenoView.vue'
import ProfesionalView from '../views/ProfesionalView.vue'
import ReservarView from '../views/ReservarView.vue'
import RegistroClienteView from '../views/RegistroClienteView.vue'
import LoginClienteView from '../views/LoginClienteView.vue'
import MisTurnosView from '../views/MisTurnosView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/reservar/:empresaSlug',
    name: 'Reservar',
    component: ReservarView
  },
  {
    path: '/empresa/:slug',
    redirect: (to: any) => ({ name: 'Reservar', params: { empresaSlug: to.params.slug } })
  },
  {
    path: '/reservar/:slug',
    redirect: (to: any) => `/empresa/${to.params.slug}`
  },
  {
    path: '/empresa/:empresaSlug/registro-cliente',
    name: 'RegistroCliente',
    component: RegistroClienteView
  },
  {
    path: '/empresa/:empresaSlug/login-cliente',
    name: 'LoginCliente',
    component: LoginClienteView
  },
  {
    path: '/empresa/:empresaSlug/mis-turnos',
    name: 'MisTurnos',
    component: MisTurnosView,
    meta: { requiresClienteAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: AdminView,
    meta: { requiresAuth: true, role: 'SUPER_ADMIN' }
  },
  {
    path: '/dueno',
    name: 'Dueno',
    component: DuenoView,
    meta: { requiresAuth: true, role: 'DUENO' }
  },
  {
    path: '/profesional',
    name: 'Profesional',
    component: ProfesionalView,
    meta: { requiresAuth: true, role: 'PROFESIONAL' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Guard de navegación asíncrono
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  const clienteStore = useClienteStore()
  
  // Cargar usuario del localStorage si existe
  if (!authStore.autenticado) {
    authStore.cargarUsuario()
  }

  // Rutas protegidas de cliente
  if (to.meta.requiresClienteAuth) {
    // Si no hay cliente en store, intentar recuperar sesión del backend
    if (!clienteStore.isAuthenticated) {
      try {
        const response = await api.obtenerPerfilCliente()
        if (response.data.exito) {
          clienteStore.setCliente(response.data.datos)
          next() // Sesión válida, permitir navegación
        } else {
          // Sin sesión válida, redirigir a login
          const empresaSlug = to.params.empresaSlug as string
          next({
            path: `/empresa/${empresaSlug}/login-cliente`,
            query: { redirect: to.fullPath }
          })
        }
      } catch {
        // Error al verificar sesión, redirigir a login
        const empresaSlug = to.params.empresaSlug as string
        next({
          path: `/empresa/${empresaSlug}/login-cliente`,
          query: { redirect: to.fullPath }
        })
      }
    } else {
      next() // Cliente ya autenticado en store
    }
    return
  }

  // Rutas protegidas de usuario (admin, dueño, profesional)
  if (to.meta.requiresAuth && !authStore.autenticado) {
    next('/login')
  } else if (to.meta.role && authStore.usuario?.rol !== to.meta.role) {
    next('/login')
  } else if (to.path === '/login' && authStore.autenticado) {
    // Redirigir según el rol del usuario
    if (authStore.usuario?.rol === 'SUPER_ADMIN') {
      next('/admin')
    } else if (authStore.usuario?.rol === 'DUENO') {
      next('/dueno')
    } else if (authStore.usuario?.rol === 'PROFESIONAL') {
      next('/profesional')
    } else {
      // Si el rol no es reconocido, cerrar sesión y permitir acceso a login
      authStore.logout()
      next()
    }
  } else {
    next()
  }
})

export default router
