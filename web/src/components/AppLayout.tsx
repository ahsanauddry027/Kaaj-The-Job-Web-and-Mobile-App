import { Briefcase, Building2, ClipboardList, Globe, LayoutDashboard, LogOut, PlusCircle, ShieldAlert, ShieldCheck } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { useCurrentUser, useLogout } from '../features/auth/useCurrentUser'
import { setLanguage } from '../i18n'
import { cn } from '../lib/cn'
import type { Role } from '../lib/types'
import { useLang } from '../lib/useLang'

interface NavItem {
  to: string
  label: string
  icon: ReactNode
  end?: boolean
}

export function Logo() {
  const { t } = useTranslation()
  return (
    <Link to="/" className="flex items-center gap-2" aria-label="Kaaj – The Job">
      <span className="grid size-10 place-items-center rounded-xl bg-brand-600 text-white">
        <Briefcase className="size-5" aria-hidden />
      </span>
      <span className="leading-tight">
        <span className="block text-xl font-bold text-brand-700">{t('app.name')}</span>
        <span className="block text-xs font-semibold text-slate-500">{t('app.subtitle')}</span>
      </span>
    </Link>
  )
}

export function LanguageSwitch() {
  const { t } = useTranslation()
  const lang = useLang()
  return (
    <label className="inline-flex items-center gap-2 text-sm text-slate-700">
      <Globe className="size-4" aria-hidden />
      <span className="sr-only">{t('lang.switch')}</span>
      <select
        value={lang}
        onChange={(e) => setLanguage(e.target.value === 'en' ? 'en' : 'bn')}
        className="rounded-lg border border-slate-300 bg-white px-2 py-1 text-sm"
      >
        <option value="bn">{t('lang.bn')}</option>
        <option value="en">{t('lang.en')}</option>
      </select>
    </label>
  )
}

function navFor(role: Role | undefined, t: (key: string) => string): NavItem[] {
  if (role === 'EMPLOYER') {
    return [
      { to: '/employer', label: t('nav.dashboard'), icon: <LayoutDashboard className="size-5" aria-hidden />, end: true },
      { to: '/employer/profile', label: t('nav.business'), icon: <Building2 className="size-5" aria-hidden /> },
      { to: '/employer/jobs/new', label: t('nav.postJob'), icon: <PlusCircle className="size-5" aria-hidden /> },
      { to: '/employer/jobs', label: t('nav.myJobs'), icon: <ClipboardList className="size-5" aria-hidden />, end: true },
    ]
  }
  if (role === 'ADMIN') {
    return [
      { to: '/admin/verification', label: t('nav.verification'), icon: <ShieldCheck className="size-5" aria-hidden /> },
      { to: '/admin/cases', label: t('nav.moderation'), icon: <ShieldAlert className="size-5" aria-hidden /> },
    ]
  }
  return []
}

/** The signed-in layout from the Figma screens: a side menu on wide screens, a scrolling tab bar on phones. */
export function AppLayout() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { data: me } = useCurrentUser()
  const logout = useLogout()
  const items = navFor(me?.role, t)

  const signOut = () => logout.mutate(undefined, { onSettled: () => navigate('/login', { replace: true }) })

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    cn(
      'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold whitespace-nowrap transition-colors',
      isActive ? 'bg-brand-100 text-brand-700' : 'text-slate-700 hover:bg-slate-100',
    )

  return (
    <div className="min-h-screen md:flex">
      <aside className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white p-4 md:flex">
        <Logo />
        <nav className="mt-8 flex flex-col gap-1" aria-label={t('nav.menu')}>
          {items.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className={linkClass}>
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto flex flex-col gap-3 border-t border-slate-200 pt-4">
          <LanguageSwitch />
          <button type="button" onClick={signOut} className="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-100">
            <LogOut className="size-5" aria-hidden />
            {t('nav.logout')}
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 py-3 md:px-8">
          <div className="md:hidden">
            <Logo />
          </div>
          <div className="hidden text-sm text-slate-500 md:block">{me && t(`role.${me.role}`)}</div>
          <div className="flex items-center gap-3">
            <span className="hidden max-w-56 truncate text-sm font-semibold text-slate-700 sm:inline">{me?.email}</span>
            <span className="md:hidden">
              <LanguageSwitch />
            </span>
            <button type="button" onClick={signOut} className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 md:hidden" aria-label={t('nav.logout')}>
              <LogOut className="size-5" aria-hidden />
            </button>
          </div>
        </header>
        {items.length > 0 && (
          <nav className="flex gap-1 overflow-x-auto border-b border-slate-200 bg-white px-2 py-2 md:hidden" aria-label={t('nav.menu')}>
            {items.map((item) => (
              <NavLink key={item.to} to={item.to} end={item.end} className={linkClass}>
                {item.icon}
                {item.label}
              </NavLink>
            ))}
          </nav>
        )}
        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 md:px-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

/** The layout for pages anyone can open: home, sign-in, public job pages. */
export function PublicLayout() {
  const { t } = useTranslation()
  const { data: me } = useCurrentUser()
  return (
    <div className="min-h-screen">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 md:px-8">
        <Logo />
        <div className="flex items-center gap-3">
          <LanguageSwitch />
          {!me && (
            <Link to="/login" className="rounded-xl bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700">
              {t('nav.signIn')}
            </Link>
          )}
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
