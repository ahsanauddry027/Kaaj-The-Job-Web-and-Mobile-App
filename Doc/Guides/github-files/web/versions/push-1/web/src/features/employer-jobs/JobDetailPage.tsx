import { ArrowLeft, Banknote, Briefcase, CalendarClock, Clock, FileCheck, GraduationCap, MapPin, Users } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router'
import { MapPicker } from '../../components/MapPicker'
import { JobStatusBadge, VerifiedBadge } from '../../components/StatusBadge'
import { Alert, Card, Spinner } from '../../components/ui'
import { daysLeft, formatDate, formatWage, localDigits, termName } from '../../lib/format'
import { useLang } from '../../lib/useLang'
import { useJob } from './api'
import { JobActionButtons, useRunAction } from './JobActions'

/** One post with its details and the manage actions (/employer/jobs/:id). */
export function JobDetailPage() {
  const { id } = useParams()
  const { t } = useTranslation()
  const lang = useLang()
  const { data: job, isLoading, error } = useJob(id)
  const { run, error: actionError, busy } = useRunAction()

  if (isLoading) return <Spinner label={t('common.loading')} />
  if (error || !job) return <Alert tone="error">{error?.message ?? t('common.notFound')}</Alert>

  const title = (lang === 'bn' ? job.titleBn : job.titleEn) || job.titleBn
  const description = (lang === 'bn' ? job.descriptionBn : job.descriptionEn) || job.descriptionBn
  const wage = formatWage(job.wageMin, job.wageMax, job.wagePeriod ? t(`job.period.${job.wagePeriod}`) : null, lang)

  return (
    <div className="flex flex-col gap-4">
      <Link to="/employer/jobs" className="inline-flex items-center gap-2 text-sm font-semibold text-brand-700 hover:underline">
        <ArrowLeft className="size-4" aria-hidden />
        {t('job.backToJobs')}
      </Link>
      <h1 className="text-xl font-bold md:text-2xl">{t('job.detailTitle')}</h1>

      {job.awaitingVerification && (
        <Alert tone="warning" title={t('job.notVerifiedTitle')} action={<Link to="/employer/profile" className="font-semibold underline">{t('job.goVerify')}</Link>}>
          {t('job.notVerified')}
        </Alert>
      )}
      {actionError && <Alert tone="error">{actionError}</Alert>}

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
        <Card>
          <div className="flex items-start gap-4">
            <div className="grid size-16 shrink-0 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Briefcase className="size-8" aria-hidden />
            </div>
            <div className="min-w-0">
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-2xl font-bold">{title}</h2>
                <JobStatusBadge status={job.status} />
              </div>
              <p className="mt-1 flex flex-wrap items-center gap-2 text-sm text-slate-600">
                {job.employer?.name}
                <VerifiedBadge verified={!!job.employer?.verified} />
                <span className="inline-flex items-center gap-1">
                  <MapPin className="size-4" aria-hidden /> {job.remote ? t('job.remoteLabel') : job.areaName || '—'}
                </span>
              </p>
              {job.statusReason && job.statusReason !== 'AWAITING_VERIFICATION' && (
                <p className="mt-1 text-xs text-slate-500">{t(`job.statusReason.${job.statusReason}`, { defaultValue: job.statusReason })}</p>
              )}
            </div>
          </div>

          <dl className="mt-6 grid gap-x-6 gap-y-4 sm:grid-cols-2">
            <Fact icon={<Banknote className="size-5" />} label={t('job.salary')} value={wage ?? '—'} />
            <Fact icon={<Users className="size-5" />} label={t('job.openings')} value={t('job.people', { n: localDigits(job.openings, lang) })} />
            <Fact icon={<Briefcase className="size-5" />} label={t('job.jobType')} value={t(`job.type.${job.jobType}`)} />
            <Fact icon={<Clock className="size-5" />} label={t('job.shiftLabel')} value={`${t(`job.shift.${job.shift}`)}${job.hours ? ` · ${localDigits(job.hours, lang)}` : ''}`} />
            {job.expiresAt && (
              <Fact
                icon={<CalendarClock className="size-5" />}
                label={t('job.expiry')}
                value={`${formatDate(job.expiresAt, lang)}${job.status === 'PUBLISHED' || job.status === 'PAUSED' ? ` (${t('job.daysLeft', { days: localDigits(daysLeft(job.expiresAt), lang) })})` : ''}`}
              />
            )}
            <Fact
              icon={<GraduationCap className="size-5" />}
              label={t('job.minExperience')}
              value={job.minExperienceYears ? t('job.experience', { years: localDigits(job.minExperienceYears, lang) }) : t('job.noExperience')}
            />
            {job.occupation && <Fact icon={<Briefcase className="size-5" />} label={t('job.category')} value={termName(job.occupation, lang)} />}
            {job.requiredDocuments.length > 0 && (
              <Fact icon={<FileCheck className="size-5" />} label={t('job.documents')} value={job.requiredDocuments.map((code) => t(`documentCode.${code}`, { defaultValue: code })).join(', ')} />
            )}
          </dl>

          {job.skills.length > 0 && (
            <div className="mt-5">
              <p className="text-sm font-semibold">{t('job.skills')}</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {job.skills.map((skill) => (
                  <span key={skill.id} className="rounded-full bg-slate-100 px-3 py-1 text-sm">
                    {termName(skill, lang)}
                  </span>
                ))}
              </div>
            </div>
          )}

          {description && <p className="mt-5 text-sm leading-7 whitespace-pre-line text-slate-700">{description}</p>}

          {!job.remote && job.latitude != null && job.longitude != null && (
            <div className="mt-5">
              <MapPicker readOnly value={{ lat: job.latitude, lng: job.longitude }} height={200} />
            </div>
          )}

          {(job.status === 'PUBLISHED' || job.status === 'PAUSED') && (
            <div className="mt-5">
              <Alert tone="success">{t('job.autoClose')}</Alert>
            </div>
          )}
          {job.repostedFrom && <p className="mt-3 text-xs text-slate-500">{t('job.repostedFrom')}</p>}
        </Card>

        <Card className="h-fit">
          <h3 className="mb-3 font-bold">{t('job.manage')}</h3>
          <JobActionButtons job={job} onRun={run} busy={busy} />
          {job.status === 'PUBLISHED' && (
            <Link to={`/jobs/${job.id}`} className="mt-4 block text-center text-sm font-semibold text-brand-700 underline">
              {t('job.preview')}
            </Link>
          )}
        </Card>
      </div>
    </div>
  )
}

function Fact({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-start gap-3">
      <span className="mt-0.5 text-slate-400" aria-hidden>
        {icon}
      </span>
      <div>
        <dt className="text-xs font-semibold text-slate-500">{label}</dt>
        <dd className="text-sm font-semibold text-slate-900">{value}</dd>
      </div>
    </div>
  )
}
