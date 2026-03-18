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

  messaging.onBackgroundMessage((payload) => {
    const tieneNotificationNativa = Boolean(payload?.notification?.title || payload?.notification?.body)
    if (tieneNotificationNativa) {
      return
    }

    const notification = payload?.notification || {}
    const title = notification.title || 'Sistema de Turnos'
    const options = {
      body: notification.body || 'Tienes una nueva notificación',
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      data: payload?.data || {}
    }

    self.registration.showNotification(title, options)
  })
} catch (error) {
  console.error('🔥 Error crítico en FCM SW:', error)
}
