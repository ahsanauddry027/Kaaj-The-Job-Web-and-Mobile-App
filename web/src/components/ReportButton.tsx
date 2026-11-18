import { CheckCircle2, Flag } from 'lucide-react'
import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useLocation } from 'react-router'
import { useCurrentUser } from '../features/auth/useCurrentUser'
import { useReportPost } from '../features/jobs/api'
import { ApiError } from '../lib/api'
import { REPORT_REASONS, type ReportReason } from '../lib/types'
import { Alert, Button, Textarea } from './ui'

/**
 * "Report this post" (scam screening and moderation, Module 2). Signed-in users pick a reason; the post goes
 * to the admin queue, and three reports take it out of search until an admin decides. Member-2's chat
 * (Module 3) can reuse this for messages.
 */
export function ReportButton({ jobId }: { jobId: string }) {
  const { t } = useTranslation()
  const location = useLocation()
  const { data: me } = useCurrentUser()
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState<ReportReason | null>(null)
  const [details, setDetails] = useState('')
  const titleId = useId()
  const send = useReportPost(jobId)

  if (send.isSuccess) {
    return (
      <p className="flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm text-emerald-800" role="status">
        <CheckCircle2 className="size-4" aria-hidden />
        {t('report.thanks')}
      </p>
    )
  }
  if (!me) {
    return (
      <Link to={`/login?next=${encodeURIComponent(location.pathname)}`} className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 underline">
        <Flag className="size-4" aria-hidden />
        {t('report.signInToReport')}
      </Link>
    )
  }
  if (!open) {
    return (
      <button type="button" onClick={() => setOpen(true)} className="inline-flex items-center gap-2 self-start text-sm font-semibold text-red-700 hover:underline">
        <Flag className="size-4" aria-hidden />
        {t('report.button')}
      </button>
    )
  }
  const error = send.error instanceof ApiError ? send.error.message : send.error ? t('common.somethingWrong') : null
  return (
    <section className="rounded-2xl border border-red-200 bg-white p-4" aria-labelledby={titleId}>
      <h2 id={titleId} className="flex items-center gap-2 font-bold text-red-800">
        <Flag className="size-4" aria-hidden />
        {t('report.title')}
      </h2>
      <fieldset className="mt-3">
        <legend className="mb-2 text-sm font-semibold">{t('report.reason')}</legend>
        <div className="flex flex-col gap-2">
          {REPORT_REASONS.map((value) => (
            <label key={value} className="inline-flex items-center gap-2 text-sm">
              <input type="radio" name="report-reason" value={value} checked={reason === value} onChange={() => setReason(value)} className="size-4 accent-red-600" />
              {t(`report.reasons.${value}`)}
            </label>
          ))}
        </div>
      </fieldset>
      <label className="mt-3 block text-sm font-semibold" htmlFor={`${titleId}-details`}>
        {t('report.details')} <span className="font-normal text-slate-500">({t('common.optional')})</span>
      </label>
      <Textarea id={`${titleId}-details`} className="mt-1" maxLength={500} value={details} onChange={(e) => setDetails(e.target.value)} placeholder={t('report.detailsPlaceholder')} />
      {error && (
        <div className="mt-3">
          <Alert tone="error">{error}</Alert>
        </div>
      )}
      <div className="mt-3 flex gap-2">
        <Button type="button" variant="danger" disabled={!reason} loading={send.isPending} onClick={() => reason && send.mutate({ reason, details: details.trim() || null })}>
          {t('report.send')}
        </Button>
        <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
          {t('common.cancel')}
        </Button>
      </div>
      <p className="mt-2 text-xs text-slate-500">{t('report.privacy')}</p>
    </section>
  )
}
