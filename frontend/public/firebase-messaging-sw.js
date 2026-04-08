/* eslint-disable no-undef */
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js')
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js')

self.addEventListener('install', function () {
  self.skipWaiting()
})

self.addEventListener('activate', function (event) {
  event.waitUntil(self.clients.claim())
})

function getPushEmoji(titulo) {
  var tituloLower = (titulo || '').toLowerCase()

  if (
    tituloLower.indexOf('nuevo') !== -1 ||
    tituloLower.indexOf('reserva') !== -1 ||
    tituloLower.indexOf('pago') !== -1 ||
    tituloLower.indexOf('seña') !== -1 ||
    tituloLower.indexOf('sena') !== -1
  ) {
    return '✅'
  }

  if (
    tituloLower.indexOf('cancelado') !== -1 ||
    tituloLower.indexOf('rechazado') !== -1 ||
    tituloLower.indexOf('inasistencia') !== -1
  ) {
    return '❌'
  }

  if (tituloLower.indexOf('reprogramado') !== -1 || tituloLower.indexOf('cambio') !== -1) {
    return '🔄'
  }

  return '🔔'
}

try {
  firebase.initializeApp({
    apiKey: 'AIzaSyC89GMthkgbe-cx5MHEbpHqMxnaD0vd1MA',
    authDomain: 'ansa---sistema-de-turnos.firebaseapp.com',
    projectId: 'ansa---sistema-de-turnos',
    storageBucket: 'ansa---sistema-de-turnos.firebasestorage.app',
    messagingSenderId: '385605466272',
    appId: '1:385605466272:web:4c69b13eb5d593660071c5'
  })

  var messaging = firebase.messaging()

  messaging.onBackgroundMessage(function (payload) {
    var notificationPayload = payload && payload.notification ? payload.notification : {}
    var dataPayload = payload && payload.data ? payload.data : {}
    var tieneNotificationNativa = Boolean(notificationPayload.title || notificationPayload.body)

    if (tieneNotificationNativa) {
      return
    }

    var titleBase = notificationPayload.title || dataPayload.title || 'Sistema de Turnos'
    var title = getPushEmoji(titleBase) + ' ' + titleBase
    var options = {
      body: notificationPayload.body || dataPayload.body || 'Tienes una nueva notificación',
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      data: dataPayload
    }

    self.registration.showNotification(title, options)
  })
} catch (error) {
  console.error('Error critico en FCM SW:', error)
}
