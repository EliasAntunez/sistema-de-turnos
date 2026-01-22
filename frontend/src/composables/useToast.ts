import { reactive, readonly } from 'vue'

type Toast = { id: number; message: string; timeout?: number }

const state = reactive({ toasts: [] as Toast[] })
let nextId = 1

export function useToastStore() {
  function show(message: string, timeout = 4000) {
    const id = nextId++
    state.toasts.push({ id, message, timeout })
    if (timeout > 0) {
      setTimeout(() => dismiss(id), timeout)
    }
    return id
  }

  function dismiss(id: number) {
    const idx = state.toasts.findIndex(t => t.id === id)
    if (idx >= 0) state.toasts.splice(idx, 1)
  }

  return {
    toasts: readonly(state.toasts) as unknown as Toast[],
    show,
    dismiss
  }
}

// Export a convenient global helper
export default useToastStore()
