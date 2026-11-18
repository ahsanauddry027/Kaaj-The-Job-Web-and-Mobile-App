import { CheckCircle2, X } from 'lucide-react'
import type { ReactNode } from 'react'
import { createContext, useCallback, useContext, useState } from 'react'

interface ToastItem {
  id: number
  text: string
}

const ToastContext = createContext<(text: string) => void>(() => {})

/** Short success messages ("Post paused.") at the bottom of the screen. */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])
  const show = useCallback((text: string) => {
    const id = Date.now() + Math.random()
    setItems((list) => [...list, { id, text }])
    setTimeout(() => setItems((list) => list.filter((item) => item.id !== id)), 4500)
  }, [])
  return (
    <ToastContext.Provider value={show}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 bottom-4 z-50 flex flex-col items-center gap-2 px-4" aria-live="polite">
        {items.map((item) => (
          <div key={item.id} className="pointer-events-auto flex max-w-md items-center gap-2 rounded-xl bg-slate-900 px-4 py-3 text-sm text-white shadow-lg">
            <CheckCircle2 className="size-4 shrink-0 text-emerald-400" aria-hidden />
            <span className="flex-1">{item.text}</span>
            <button type="button" onClick={() => setItems((list) => list.filter((i) => i.id !== item.id))} aria-label="×">
              <X className="size-4" aria-hidden />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  return useContext(ToastContext)
}
