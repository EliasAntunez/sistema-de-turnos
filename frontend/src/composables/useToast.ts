import { reactive, readonly } from 'vue'

export type ToastType = 'default' | 'success' | 'error'

export type Toast = {
  id: number
  message: string
  type: ToastType
  timeout?: number
  detalles?: string[]
}

const state = reactive({ toasts: [] as Toast[] })
let nextId = 1

export function useToastStore() {
  function show(message: string, timeout = 4000, type: ToastType = 'default') {
    const id = nextId++
    state.toasts.push({ id, message, type, timeout })
    if (timeout > 0) {
      setTimeout(() => dismiss(id), timeout)
    }
    return id
  }

  function showSuccess(message: string, timeout = 4000) {
    return show(message, timeout, 'success')
  }

  function showError(message: string, timeout = 5000) {
    return show(message, timeout, 'error')
  }

  function showErrorConDetalles(message: string, detalles: string[]) {
    const id = nextId++
    // autoClose desactivado: timeout = 0
    state.toasts.push({ id, message, type: 'error', timeout: 0, detalles })
    return id
  }

  function dismiss(id: number) {
    const idx = state.toasts.findIndex(t => t.id === id)
    if (idx >= 0) state.toasts.splice(idx, 1)
  }

  return {
    toasts: readonly(state.toasts) as unknown as Toast[],
    show,
    showSuccess,
    showError,
    showErrorConDetalles,
    dismiss
  }
}

// Export a convenient global helper
export default useToastStore()
