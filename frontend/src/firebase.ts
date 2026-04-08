import { initializeApp } from 'firebase/app'
import { getMessaging, getToken, onMessage, isSupported, type Messaging } from 'firebase/messaging'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
}

const vapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY

export const firebaseApp = initializeApp(firebaseConfig)

function getPushEmoji(titulo: string): string {
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

function tieneEmojiInicial(titulo: string): boolean {
  return /^(✅|❌|🔄|🔔)\s/.test((titulo || '').trim())
}

async function obtenerMessaging(): Promise<Messaging | null> {
  const supported = await isSupported()
  if (!supported) {
    return null
  }

  return getMessaging(firebaseApp)
}

async function registrarServiceWorkerPush(): Promise<ServiceWorkerRegistration | null> {
  if (typeof window === 'undefined' || !('serviceWorker' in navigator)) {
    return null
  }

  try {
    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js', {
      scope: '/'
    })

    // Fuerza verificación de updates para evitar workers obsoletos en CDN/cache.
    await registration.update()

    const worker = registration.active || registration.waiting || registration.installing

    if (worker && worker.state !== 'activated') {
      await new Promise<void>((resolve, reject) => {
        const timeoutId = window.setTimeout(() => {
          reject(new Error('Timeout esperando activación del Service Worker de Firebase'))
        }, 8000)

        const onStateChange = () => {
          if (worker.state === 'activated') {
            worker.removeEventListener('statechange', onStateChange)
            window.clearTimeout(timeoutId)
            resolve()
          }
        }

        worker.addEventListener('statechange', onStateChange)
      })
    }

    return registration.active ? registration : await navigator.serviceWorker.ready
  } catch (error) {
    console.error('🔥 Error registrando firebase-messaging-sw.js:', error)
    return null
  }
}

export async function solicitarPermisoYObtenerToken(): Promise<string | null> {
  if (typeof window === 'undefined' || !('Notification' in window) || !('serviceWorker' in navigator)) {
    return null
  }

  let permission = Notification.permission
  if (permission === 'default') {
    permission = await Notification.requestPermission()
  }

  if (permission !== 'granted') {
    return null
  }

  const messaging = await obtenerMessaging()
  if (!messaging) {
    return null
  }

  try {
    const swRegistration = await registrarServiceWorkerPush()
    if (!swRegistration) {
      return null
    }

    const tokenPromise = getToken(messaging, {
      vapidKey,
      serviceWorkerRegistration: swRegistration
    })
    const timeoutPromise = new Promise<never>((_, reject) =>
      setTimeout(() => reject(new Error('Timeout de 5s esperando a FCM')), 5000)
    )
    const token = await Promise.race([tokenPromise, timeoutPromise])

    return token || null
  } catch (error) {
    console.error('🔥 Error crítico en FCM:', error)
    return null
  }
}

export async function escucharNotificacionesForeground(): Promise<(() => void) | null> {
  const messaging = await obtenerMessaging()
  if (!messaging) {
    return null
  }

  const unsubscribe = onMessage(messaging, (payload) => {
    const titulo = payload.notification?.title || 'Nueva notificación'
    const cuerpo = payload.notification?.body || ''
    const tituloConEmoji = tieneEmojiInicial(titulo) ? titulo : `${getPushEmoji(titulo)} ${titulo}`

    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted') {
      new Notification(tituloConEmoji, { body: cuerpo })
    }
  })

  return unsubscribe
}