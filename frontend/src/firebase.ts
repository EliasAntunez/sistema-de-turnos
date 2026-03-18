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

async function obtenerMessaging(): Promise<Messaging | null> {
  const supported = await isSupported()
  if (!supported) {
    return null
  }

  return getMessaging(firebaseApp)
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
    await navigator.serviceWorker.register('/firebase-messaging-sw.js')
    const swRegistration = await navigator.serviceWorker.ready

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

    if (typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted') {
      new Notification(titulo, { body: cuerpo })
    }
  })

  return unsubscribe
}