import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router'
import { MapPicker } from '../../components/MapPicker'
import { ReportButton } from '../../components/ReportButton'
import { Alert, Spinner } from '../../components/ui'
import { useLang } from '../../lib/useLang'
import { JobCard } from '../employer-jobs/JobCard'
import { usePublicJob } from './api'

/**
 * A published post as anyone sees it (/jobs/:id), with the employer's "Verified" badge.
 * Member-1's search (M2-1) links its results here.
 */
export function PublicJobPage() {
  const { id } = useParams()
  const { t } = useTranslation()
  const lang = useLang()
  const { data: job, isLoading, error } = usePublicJob(id)

  if (isLoading) return <Spinner label={t('common.loading')} />
  if (error || !job) return <div className="mx-auto max-w-2xl p-6"><Alert tone="warning">{t('publicJob.notFound')}</Alert></div>

  const description = (lang === 'bn' ? job.descriptionBn : job.descriptionEn) || job.descriptionBn
  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-4 px-4 py-8">
      <JobCard
        job={{ ...job, employerName: job.employer?.name ?? null, employerVerified: !!job.employer?.verified }}
      />
      {description && <p className="rounded-2xl border border-slate-200 bg-white p-5 text-sm leading-7 whitespace-pre-line">{description}</p>}
      {!job.remote && job.latitude != null && job.longitude != null && <MapPicker readOnly value={{ lat: job.latitude, lng: job.longitude }} height={220} />}
      <p className="rounded-xl bg-slate-50 p-3 text-xs text-slate-600">{t('report.neverPay')}</p>
      <ReportButton jobId={job.id} />
    </div>
  )
}
