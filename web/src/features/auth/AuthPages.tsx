import { zodResolver } from '@hookform/resolvers/zod'
import { Briefcase, Search } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { z } from 'zod'
import { Alert, Button, Card, Field, Input } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { cn } from '../../lib/cn'
import type { Role } from '../../lib/types'
import { homeFor, useLogin, useRegister } from './useCurrentUser'

const loginSchema = z.object({
  email: z.email('v.email'),
  password: z.string().min(1, 'v.required'),
})

const registerSchema = z.object({
  email: z.email('v.email'),
  password: z.string().min(8, 'v.passwordLength'),
  role: z.enum(['SEEKER', 'EMPLOYER']),
})

function AuthShell({ title, children }: { title: string; children: React.ReactNode }) {
  const { t } = useTranslation()
  return (
    <div className="mx-auto flex w-full max-w-md flex-col gap-4 px-4 py-10">
      <Card>
        <h1 className="text-xl font-bold">{title}</h1>
        <div className="mt-5">{children}</div>
      </Card>
      <Alert tone="info">{t('auth.standIn')}</Alert>
    </div>
  )
}

export function LoginPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const login = useLogin()
  const form = useForm<z.infer<typeof loginSchema>>({ resolver: zodResolver(loginSchema), defaultValues: { email: '', password: '' } })
  const errors = form.formState.errors

  const onSubmit = form.handleSubmit((values) =>
    login.mutate(values, { onSuccess: (me) => navigate(params.get('next') ?? homeFor(me.role), { replace: true }) }),
  )

  return (
    <AuthShell title={t('auth.signInTitle')}>
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <Field label={t('auth.email')} error={errors.email && t(errors.email.message!)} required>
          {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.email} type="email" autoComplete="email" {...form.register('email')} />}
        </Field>
        <Field label={t('auth.password')} error={errors.password && t(errors.password.message!)} required>
          {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.password} type="password" autoComplete="current-password" {...form.register('password')} />}
        </Field>
        {login.error && <Alert tone="error">{login.error.message}</Alert>}
        <Button type="submit" loading={login.isPending}>{t('auth.signIn')}</Button>
        <p className="text-center text-sm text-slate-600">
          {t('auth.noAccount')} <Link to="/register" className="font-semibold text-brand-700 underline">{t('auth.register')}</Link>
        </p>
        <details className="rounded-xl bg-slate-50 p-3 text-xs text-slate-600">
          <summary className="cursor-pointer font-semibold">{t('auth.demoTitle')}</summary>
          <ul className="mt-2 list-disc pl-5 font-mono">
            <li>karim@demo.kaaj</li>
            <li>tongigarments@demo.kaaj</li>
            <li>rahim@demo.kaaj</li>
          </ul>
          <p className="mt-2">{t('auth.demoHint')}</p>
        </details>
      </form>
    </AuthShell>
  )
}

export function RegisterPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const register = useRegister()
  const form = useForm<z.infer<typeof registerSchema>>({
    resolver: zodResolver(registerSchema),
    defaultValues: { email: '', password: '', role: 'EMPLOYER' },
  })
  const errors = form.formState.errors
  const role = form.watch('role')

  const onSubmit = form.handleSubmit((values) =>
    register.mutate(values, {
      onSuccess: (me) => navigate(homeFor(me.role), { replace: true }),
      onError: (error) => {
        if (error instanceof ApiError) {
          for (const [field, message] of Object.entries(error.fieldErrors)) {
            if (field === 'email' || field === 'password') form.setError(field, { message })
          }
        }
      },
    }),
  )

  const roles: Array<{ value: Role; label: string; icon: React.ReactNode }> = [
    { value: 'SEEKER', label: t('auth.findJob'), icon: <Search className="size-5" aria-hidden /> },
    { value: 'EMPLOYER', label: t('auth.hire'), icon: <Briefcase className="size-5" aria-hidden /> },
  ]

  return (
    <AuthShell title={t('auth.registerTitle')}>
      <form onSubmit={onSubmit} className="flex flex-col gap-4" noValidate>
        <fieldset>
          <legend className="mb-2 text-sm font-semibold">{t('auth.iWantTo')}</legend>
          <div className="grid grid-cols-2 gap-2">
            {roles.map((option) => (
              <label
                key={option.value}
                className={cn(
                  'flex cursor-pointer items-center gap-2 rounded-xl border p-3 text-sm font-semibold',
                  role === option.value ? 'border-brand-600 bg-brand-50 text-brand-700' : 'border-slate-300',
                )}
              >
                <input type="radio" value={option.value} className="sr-only" {...form.register('role')} />
                {option.icon}
                {option.label}
              </label>
            ))}
          </div>
        </fieldset>
        <Field label={t('auth.email')} error={errors.email && t(errors.email.message!)} required>
          {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.email} type="email" autoComplete="email" {...form.register('email')} />}
        </Field>
        <Field label={t('auth.password')} error={errors.password && t(errors.password.message!)} hint={t('v.passwordLength')} required>
          {(id, d) => <Input id={id} aria-describedby={d} aria-invalid={!!errors.password} type="password" autoComplete="new-password" {...form.register('password')} />}
        </Field>
        {register.error && <Alert tone="error">{register.error.message}</Alert>}
        <Button type="submit" loading={register.isPending}>{t('auth.register')}</Button>
        <p className="text-center text-sm text-slate-600">
          {t('auth.haveAccount')} <Link to="/login" className="font-semibold text-brand-700 underline">{t('auth.signIn')}</Link>
        </p>
      </form>
    </AuthShell>
  )
}
