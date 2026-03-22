/* eslint-disable no-undef */
importScripts('https://www.gstatic.com/firebasejs/11.0.1/firebase-app-compat.js')
importScripts('https://www.gstatic.com/firebasejs/11.0.1/firebase-messaging-compat.js')

try {
  firebase.initializeApp({
    apiKey: 'AIzaSyC89GMthkgbe-cx5MHEbpHqMxnaD0vd1MA',
    authDomain: 'ansa---sistema-de-turnos.firebaseapp.com',
    projectId: 'ansa---sistema-de-turnos',
    storageBucket: 'ansa---sistema-de-turnos.firebasestorage.app',
    messagingSenderId: '385605466272',
    appId: '1:385605466272:web:4c69b13eb5d593660071c5'
  })

  const messaging = firebase.messaging()

  function getPushEmoji(titulo) {
    const tituloLower = (titulo || '').toLowerCase()

    if (tituloLower.includes('nuevo') || tituloLower.includes('reserva') || tituloLower.includes('pago') || tituloLower.includes('seña') || tituloLower.includes('sena')) {
      return '✅'
    }

    if (tituloLower.includes('cancelado') || tituloLower.includes('rechazado') || tituloLower.includes('inasistencia')) {
      return '❌'
    }

    if (tituloLower.includes('reprogramado') || tituloLower.includes('cambio')) {
      return '🔄'
    }

    return '🔔'
  }

  messaging.onBackgroundMessage((payload) => {
    const tieneNotificationNativa = Boolean(payload?.notification?.title || payload?.notification?.body)
    if (tieneNotificationNativa) {
      return
    }

    const notification = payload?.notification || {}
    const titleBase = notification.title || payload?.data?.title || 'Sistema de Turnos'
    const title = `${getPushEmoji(titleBase)} ${titleBase}`
    const options = {
      body: notification.body || payload?.data?.body || 'Tienes una nueva notificación',
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      data: payload?.data || {}
    }

    self.registration.showNotification(title, options)
  })
} catch (error) {
  console.error('🔥 Error crítico en FCM SW:', error)
}
