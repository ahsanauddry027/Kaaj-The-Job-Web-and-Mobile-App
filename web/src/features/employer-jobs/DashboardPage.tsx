import { Building2, ChevronRight, MapPin, Phone, PlusCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { JobStatusBadge, VerificationBadge } from '../../components/StatusBadge'
import { Alert, Card, Spinner } from '../../components/ui'
import { formatDate, localDigits } from '../../lib/format'
import type { JobStatus } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useEmployerProfile } from '../employer-profile/api'
import { useJobCounts, useMyJobs } from './api'

const STATS: JobStatus[] = ['PUBLISHED', 'DRAFT', 'PAUSED', 'CLOSED']

/** The employer's home (/employer): the business card, the verification state and recent posts. */
export function DashboardPage() {
  const { t } = useTranslation()
  const lang = useLang()
  const profile = useEmployerProfile()
  const hasProfile = !!profile.data
  const counts = useJobCounts(hasProfile)
  const recent = useMyJobs(null, '', hasProfile)

  if (profile.isLoading) return <Spinner label={t('common.loading')} />
  if (profile.error) return <Alert tone="error">{profile.error.message}</Alert>

  const p = profile.data
  const items = recent.data?.pages[0]?.items.slice(0, 3) ?? []

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold">{t('dashboard.title')}</h1>
        <p className="mt-1 text-slate-600">
          {t('dashboard.welcome')} {t('dashboard.subtitle')}
        </p>
      </div>

      {!p ? (
        <Alert
          tone="info"
          title={t('dashboard.noProfileTitle')}
          action={
            <Link to="/employer/profile" className="inline-flex items-center gap-1 font-semibold underline">
              {t('nav.business')} <ChevronRight className="size-4" aria-hidden />
            </Link>
          }
        >
          {t('dashboard.noProfile')}
        </Alert>
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          <Card className="flex items-start gap-4">
            <div className="grid size-16 shrink-0 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Building2 className="size-8" aria-hidden />
            </div>
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-xl font-bold">{p.name}</h2>
                <VerificationBadge status={p.status} />
              </div>
              <p className="text-sm text-slate-600">{t(`kind.${p.kind}`)}</p>
              <p className="mt-2 flex items-center gap-2 text-sm text-slate-700">
                <MapPin className="size-4 text-slate-400" aria-hidden />
                {p.address}
              </p>
              <p className="mt-1 flex items-center gap-2 text-sm text-slate-700">
                <Phone className="size-4 text-slate-400" aria-hidden />
                {localDigits(p.contactPhone, lang)}
              </p>
              <Link to="/employer/profile" className="mt-3 inline-flex items-center gap-1 rounded-xl bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700">
                {t('dashboard.viewProfile')} <ChevronRight className="size-4" aria-hidden />
              </Link>
            </div>
          </Card>
          <Alert tone={p.status === 'VERIFIED' ? 'success' : p.status === 'REJECTED' ? 'error' : 'warning'} title={t(`verification.status.${p.status}`)}>
            {t(`verification.banner.${p.status}`)}
          </Alert>
        </div>
      )}

      {hasProfile && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          {STATS.map((status) => (
            <Card key={status} className="p-4 md:p-5">
              <p className="text-sm text-slate-600">{t(`dashboard.stats.${status}`)}</p>
              <p className="mt-1 text-3xl font-bold text-slate-900">{localDigits(counts.data?.[status] ?? 0, lang)}</p>
            </Card>
          ))}
        </div>
      )}

      {hasProfile && (
        <Card>
          <div className="flex items-center justify-between">
            <h2 className="font-bold">{t('dashboard.recent')}</h2>
            <Link to="/employer/jobs" className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700">
              {t('dashboard.seeAll')} <ChevronRight className="size-4" aria-hidden />
            </Link>
          </div>
          <ul className="mt-3 divide-y divide-slate-100">
            {items.map((job) => (
              <li key={job.id} className="flex items-center justify-between gap-3 py-3">
                <Link to={`/employer/jobs/${job.id}`} className="min-w-0 truncate font-semibold hover:underline">
                  {(lang === 'bn' ? job.titleBn : job.titleEn) || job.titleBn}
                </Link>
                <span className="flex shrink-0 items-center gap-3 text-xs text-slate-500">
                  <JobStatusBadge status={job.status} />
                  <span className="hidden sm:inline">{formatDate(job.updatedAt, lang)}</span>
                </span>
              </li>
            ))}
          </ul>
          <Link to="/employer/jobs/new" className="mt-4 inline-flex items-center gap-2 text-sm font-semibold text-brand-700">
            <PlusCircle className="size-4" aria-hidden />
            {t('nav.postJob')}
          </Link>
        </Card>
      )}
    </div>
  )
}
