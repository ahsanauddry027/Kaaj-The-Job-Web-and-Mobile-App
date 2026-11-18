import { BadgeCheck, Banknote, ShieldCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate } from 'react-router'
import { Spinner } from '../../components/ui'
import { homeFor, useCurrentUser } from '../auth/useCurrentUser'

export function HomePage() {
  const { t } = useTranslation()
  const { data: me, isLoading } = useCurrentUser()
  if (isLoading) return <Spinner label={t('common.loading')} />
  if (me) return <Navigate to={homeFor(me.role)} replace />
  return (
    <div className="mx-auto flex max-w-3xl flex-col items-center gap-6 px-4 py-14 text-center">
      <h1 className="text-3xl leading-tight font-bold text-slate-900 md:text-4xl">{t('home.headline')}</h1>
      <p className="text-lg text-slate-600">{t('app.tagline')}</p>
      <div className="flex flex-wrap justify-center gap-3">
        <Link to="/register" className="rounded-xl bg-brand-600 px-6 py-3 font-semibold text-white hover:bg-brand-700">{t('home.start')}</Link>
        <Link to="/login" className="rounded-xl border border-brand-200 bg-white px-6 py-3 font-semibold text-brand-700 hover:bg-brand-50">{t('nav.signIn')}</Link>
      </div>
      <ul className="mt-6 grid w-full gap-4 text-left sm:grid-cols-3">
        <li className="rounded-2xl bg-white p-4 shadow-sm"><BadgeCheck className="size-6 text-emerald-600" aria-hidden /><p className="mt-2 text-sm">{t('verification.badgeExplain')}</p></li>
        <li className="rounded-2xl bg-white p-4 shadow-sm"><Banknote className="size-6 text-emerald-600" aria-hidden /><p className="mt-2 text-sm">{t('job.wageHint')}</p></li>
        <li className="rounded-2xl bg-white p-4 shadow-sm"><ShieldCheck className="size-6 text-brand-600" aria-hidden /><p className="mt-2 text-sm">{t('home.employers')}</p></li>
      </ul>
      <p className="text-xs text-slate-500">{t('home.member3')}</p>
    </div>
  )
}

export function SeekerHomePage() {
  const { t } = useTranslation()
  return (
    <div className="mx-auto max-w-xl p-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-6 text-slate-700">{t('auth.seekerHome')}</div>
    </div>
  )
}

export function NotFoundPage() {
  const { t } = useTranslation()
  return (
    <div className="mx-auto max-w-xl p-10 text-center">
      <p className="text-lg font-semibold">{t('common.notFound')}</p>
      <Link to="/" className="mt-3 inline-block text-brand-700 underline">{t('common.goHome')}</Link>
    </div>
  )
}
