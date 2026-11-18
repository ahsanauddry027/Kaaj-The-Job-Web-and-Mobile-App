import { AlertTriangle, CheckCircle2, Info, LoaderCircle, XCircle } from 'lucide-react'
import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'
import { forwardRef, useId } from 'react'
import { cn } from '../lib/cn'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'success'

const variants: Record<Variant, string> = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700 disabled:bg-brand-600/50',
  secondary: 'border border-brand-200 bg-white text-brand-700 hover:bg-brand-50 disabled:opacity-50',
  ghost: 'text-slate-700 hover:bg-slate-100 disabled:opacity-50',
  danger: 'border border-red-200 bg-white text-red-700 hover:bg-red-50 disabled:opacity-50',
  success: 'bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50',
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  loading?: boolean
  icon?: ReactNode
}

export function Button({ variant = 'primary', loading, icon, className, children, disabled, ...rest }: ButtonProps) {
  return (
    <button
      className={cn(
        'inline-flex min-h-11 items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-colors',
        'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 disabled:cursor-not-allowed',
        variants[variant],
        className,
      )}
      disabled={disabled || loading}
      {...rest}
    >
      {loading ? <LoaderCircle className="size-4 animate-spin" aria-hidden /> : icon}
      {children}
    </button>
  )
}

export function Card({ className, children }: { className?: string; children: ReactNode }) {
  return <section className={cn('rounded-2xl border border-slate-200 bg-white p-5 shadow-sm md:p-6', className)}>{children}</section>
}

export function PageHeader({ icon, title, subtitle, actions }: { icon?: ReactNode; title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-5 flex flex-wrap items-start justify-between gap-4">
      <div className="flex items-start gap-3">
        {icon && <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-brand-600 text-white">{icon}</div>}
        <div>
          <h1 className="text-xl font-bold text-slate-900 md:text-2xl">{title}</h1>
          {subtitle && <p className="mt-1 text-sm text-slate-600">{subtitle}</p>}
        </div>
      </div>
      {actions}
    </div>
  )
}

interface FieldProps {
  label: string
  error?: string
  hint?: string
  required?: boolean
  optionalLabel?: string
  children: (id: string, describedBy: string | undefined) => ReactNode
  className?: string
}

/** A label, the input, a hint and the error message, linked for screen readers. */
export function Field({ label, error, hint, required, optionalLabel, children, className }: FieldProps) {
  const id = useId()
  const describedBy = error ? `${id}-error` : hint ? `${id}-hint` : undefined
  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      <label htmlFor={id} className="text-sm font-semibold text-slate-800">
        {label}
        {required && <span className="text-red-600"> *</span>}
        {optionalLabel && <span className="font-normal text-slate-500"> ({optionalLabel})</span>}
      </label>
      {children(id, describedBy)}
      {hint && !error && (
        <p id={`${id}-hint`} className="text-xs text-slate-500">
          {hint}
        </p>
      )}
      {error && (
        <p id={`${id}-error`} role="alert" className="text-xs font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  )
}

const inputClass =
  'w-full rounded-xl border border-slate-300 bg-white px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 ' +
  'focus:border-brand-600 focus:outline-none focus:ring-2 focus:ring-brand-100 aria-[invalid=true]:border-red-500 disabled:bg-slate-50'

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(function Input({ className, ...rest }, ref) {
  return <input ref={ref} className={cn(inputClass, className)} {...rest} />
})

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(function Textarea(
  { className, ...rest },
  ref,
) {
  return <textarea ref={ref} className={cn(inputClass, 'min-h-28', className)} {...rest} />
})

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(function Select({ className, ...rest }, ref) {
  return <select ref={ref} className={cn(inputClass, 'pr-8', className)} {...rest} />
})

type Tone = 'info' | 'success' | 'warning' | 'error'
const tones: Record<Tone, { box: string; icon: ReactNode }> = {
  info: { box: 'border-brand-100 bg-brand-50 text-brand-700', icon: <Info className="size-5 shrink-0" aria-hidden /> },
  success: { box: 'border-emerald-200 bg-emerald-50 text-emerald-800', icon: <CheckCircle2 className="size-5 shrink-0" aria-hidden /> },
  warning: { box: 'border-amber-200 bg-amber-50 text-amber-800', icon: <AlertTriangle className="size-5 shrink-0" aria-hidden /> },
  error: { box: 'border-red-200 bg-red-50 text-red-700', icon: <XCircle className="size-5 shrink-0" aria-hidden /> },
}

export function Alert({ tone = 'info', title, children, action }: { tone?: Tone; title?: string; children?: ReactNode; action?: ReactNode }) {
  return (
    <div role={tone === 'error' ? 'alert' : 'status'} className={cn('flex gap-3 rounded-xl border p-4 text-sm', tones[tone].box)}>
      {tones[tone].icon}
      <div className="flex-1">
        {title && <p className="font-semibold">{title}</p>}
        {children && <div className={cn(title && 'mt-1')}>{children}</div>}
        {action && <div className="mt-3">{action}</div>}
      </div>
    </div>
  )
}

export function Spinner({ label }: { label: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-slate-500" role="status">
      <LoaderCircle className="size-5 animate-spin" aria-hidden />
      <span className="text-sm">{label}</span>
    </div>
  )
}

export function EmptyState({ icon, text, action }: { icon?: ReactNode; text: string; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-12 text-center">
      {icon && <div className="text-slate-400">{icon}</div>}
      <p className="text-sm text-slate-600">{text}</p>
      {action}
    </div>
  )
}
