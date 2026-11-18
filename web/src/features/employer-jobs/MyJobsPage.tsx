import { Banknote, Briefcase, CalendarDays, ChevronRight, ClipboardList, Hourglass, MapPin, PlusCircle, Search, Users } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'
import { JobStatusBadge, VerifiedBadge } from '../../components/StatusBadge'
import { Alert, Button, Card, EmptyState, Input, Spinner } from '../../components/ui'
import { cn } from '../../lib/cn'
import { daysLeft, formatDate, formatWage, localDigits } from '../../lib/format'
import type { Job, JobStatus } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useJobCounts, useMyJobs } from './api'
import { JobActionsMenu, useRunAction } from './JobActions'

const TABS: Array<JobStatus | null> = [null, 'DRAFT', 'IN_REVIEW', 'PUBLISHED', 'PAUSED', 'CLOSED']

/** "My jobs" (/employer/jobs): every post with its status, and the manage actions. */
export function MyJobsPage() {
  const { t } = useTranslation()
  const lang = useLang()
  const [status, setStatus] = useState<JobStatus | null>(null)
  const [text, setText] = useState('')
  const [query, setQuery] = useState('')
  const jobs = useMyJobs(status, query)
  const counts = useJobCounts()
  const { run, error, busy } = useRunAction()
  const total = counts.data ? Object.values(counts.data).reduce((a, b) => a + b, 0) : 0
  const items = jobs.data?.pages.flatMap((page) => page.items) ?? []

  return (
    <Card>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="flex items-center gap-2 text-xl font-bold md:text-2xl">
          <ClipboardList className="size-7 text-brand-600" aria-hidden />
          {t('job.myJobs')}
        </h1>
        <Link to="/employer/jobs/new" className="inline-flex min-h-11 items-center gap-2 rounded-xl bg-brand-600 px-4 text-sm font-semibold text-white hover:bg-brand-700">
          <PlusCircle className="size-4" aria-hidden />
          {t('nav.postJob')}
        </Link>
      </div>

      <div className="mt-5 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div className="-mx-1 flex gap-1 overflow-x-auto px-1" role="tablist">
          {TABS.map((tab) => {
            const count = tab ? (counts.data?.[tab] ?? 0) : total
            return (
              <button
                key={tab ?? 'ALL'}
                role="tab"
                aria-selected={status === tab}
                onClick={() => setStatus(tab)}
                className={cn(
                  'rounded-lg px-3 py-2 text-sm font-semibold whitespace-nowrap',
                  status === tab ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-100',
                )}
              >
                {tab ? t(`job.status.${tab}`) : t('job.all')} ({localDigits(count, lang)})
              </button>
            )
          })}
        </div>
        <form
          className="relative lg:w-72"
          onSubmit={(e) => {
            e.preventDefault()
            setQuery(text.trim())
          }}
        >
          <Search className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400" aria-hidden />
          <Input className="pl-9" placeholder={t('job.searchPlaceholder')} value={text} onChange={(e) => setText(e.target.value)} onBlur={() => setQuery(text.trim())} aria-label={t('common.search')} />
        </form>
      </div>

      {error && (
        <div className="mt-4">
          <Alert tone="error">{error}</Alert>
        </div>
      )}

      <div className="mt-5 flex flex-col divide-y divide-slate-200">
        {jobs.isLoading && <Spinner label={t('common.loading')} />}
        {jobs.error && <Alert tone="error">{jobs.error.message}</Alert>}
        {jobs.isSuccess && items.length === 0 && (
          <EmptyState icon={<Briefcase className="size-10" />} text={t('job.empty')} action={<Link to="/employer/jobs/new" className="font-semibold text-brand-700 underline">{t('nav.postJob')}</Link>} />
        )}
        {items.map((job) => (
          <JobRow key={job.id} job={job} busy={busy} onRun={run} />
        ))}
      </div>

      {jobs.hasNextPage && (
        <div className="mt-4 flex justify-center">
          <Button variant="secondary" loading={jobs.isFetchingNextPage} onClick={() => jobs.fetchNextPage()}>
            {t('common.loadMore')}
          </Button>
        </div>
      )}
    </Card>
  )
}

function JobRow({ job, onRun, busy }: { job: Job; onRun: ReturnType<typeof useRunAction>['run']; busy: boolean }) {
  const { t } = useTranslation()
  const lang = useLang()
  const title = (lang === 'bn' ? job.titleBn : job.titleEn) || job.titleBn
  const wage = formatWage(job.wageMin, job.wageMax, job.wagePeriod ? t(`job.period.${job.wagePeriod}`) : null, lang)
  const facts = [
    job.remote ? t('job.remoteLabel') : job.areaName,
    t(`job.type.${job.jobType}`),
    job.minExperienceYears ? t('job.experience', { years: localDigits(job.minExperienceYears, lang) }) : null,
  ].filter(Boolean)

  return (
    <div className={cn('grid gap-4 py-5 md:grid-cols-[minmax(0,1.6fr)_minmax(0,1fr)_auto] md:items-center', busy && 'opacity-70')}>
      <div className="flex items-start gap-3">
        <div className="grid size-14 shrink-0 place-items-center rounded-2xl bg-brand-50 text-brand-600">
          <Briefcase className="size-7" aria-hidden />
        </div>
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <Link to={`/employer/jobs/${job.id}`} className="truncate text-lg font-bold text-slate-900 hover:underline">
              {title}
            </Link>
            <JobStatusBadge status={job.status} />
            {job.status === 'PUBLISHED' && <VerifiedBadge verified={!!job.employer?.verified} />}
          </div>
          <p className="mt-1 flex items-start gap-1.5 text-sm text-slate-600">
            <MapPin className="mt-0.5 size-4 shrink-0 text-slate-400" aria-hidden />
            <span>{facts.join(' • ')}</span>
          </p>
          <p className="mt-1 flex items-start gap-1.5 text-sm text-slate-700">
            <Banknote className="mt-0.5 size-4 shrink-0 text-slate-400" aria-hidden />
            <span>
              {wage ?? '—'} • {t('job.people', { n: localDigits(job.openings, lang) })}
            </span>
          </p>
          {job.awaitingVerification && <p className="mt-1 text-xs font-semibold text-amber-700">{t('job.statusReason.AWAITING_VERIFICATION')}</p>}
        </div>
      </div>

      <div className="flex flex-col gap-1 text-sm text-slate-600">
        {job.publishedAt && (
          <span className="flex items-center gap-2">
            <CalendarDays className="size-4" aria-hidden />
            {t('job.publishedOn', { date: formatDate(job.publishedAt, lang) })}
          </span>
        )}
        {job.expiresAt && (job.status === 'PUBLISHED' || job.status === 'PAUSED') && (
          <span className="flex items-center gap-2">
            <Hourglass className="size-4" aria-hidden />
            {t('job.expiresOn', { date: formatDate(job.expiresAt, lang), days: localDigits(daysLeft(job.expiresAt), lang) })}
          </span>
        )}
        {job.closedAt && (
          <span className="flex items-center gap-2">
            <CalendarDays className="size-4" aria-hidden />
            {t('job.closedOn', { date: formatDate(job.closedAt, lang) })}
          </span>
        )}
        {!job.publishedAt && !job.closedAt && (
          <span className="flex items-center gap-2">
            <CalendarDays className="size-4" aria-hidden />
            {t('job.createdOn', { date: formatDate(job.createdAt, lang) })}
          </span>
        )}
        <span className="flex items-center gap-2 text-slate-500" title={t('job.applicantsLater')}>
          <Users className="size-4" aria-hidden />
          {t('job.applicants')}: —
        </span>
      </div>

      <div className="flex items-center gap-2 md:justify-end">
        <Link to={`/employer/jobs/${job.id}`} className="inline-flex min-h-11 items-center gap-1 rounded-xl border border-brand-200 px-4 text-sm font-semibold text-brand-700 hover:bg-brand-50">
          {t('common.viewDetails')}
          <ChevronRight className="size-4" aria-hidden />
        </Link>
        <JobActionsMenu job={job} onRun={onRun} />
      </div>
    </div>
  )
}
