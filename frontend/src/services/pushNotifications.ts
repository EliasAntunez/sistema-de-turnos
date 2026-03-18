import { solicitarPermisoYObtenerToken } from '@/firebase'
import { notificacionesService } from '@/services/notificaciones'

export async function registrarTokenPushFCM(): Promise<void> {
  const token = await solicitarPermisoYObtenerToken()

  if (!token) {
    return
  }

  await notificacionesService.registrarTokenPush(token, navigator.userAgent)
}