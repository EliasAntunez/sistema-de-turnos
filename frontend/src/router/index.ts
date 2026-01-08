import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import AdminView from '../views/AdminView.vue'
import DuenoView from '../views/DuenoView.vue'

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
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Guard de navegación
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // Cargar usuario del localStorage si existe
  if (!authStore.autenticado) {
    authStore.cargarUsuario()
  }

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
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
