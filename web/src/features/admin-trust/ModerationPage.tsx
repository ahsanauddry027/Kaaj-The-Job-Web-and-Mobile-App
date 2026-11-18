import { Ban, Bot, Building2, CheckCircle2, Flag, Gavel, Mail, ShieldAlert, UserX } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { FindingList } from '../../components/FindingList'
import { JobStatusBadge, VerifiedBadge } from '../../components/StatusBadge'
import { useToast } from '../../components/Toast'
import { Alert, Button, Card, EmptyState, PageHeader, Spinner, Textarea } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { cn } from '../../lib/cn'
import { formatDate, formatWage, localDigits } from '../../lib/format'
import { decisionNoteError, highlightParts } from '../../lib/trust'
import type { CaseDecision, CaseDetail, CaseStatus } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useCase, useCaseCounts, useCaseDecision, useCases } from './api'

const TABS: CaseStatus[] = ['OPEN', 'REMOVED', 'DISMISSED']

/**
 * Admin: the moderation queue (scam screening and moderation, Module 2). Posts flagged or blocked by
 * screening and posts reported by users, most reported and oldest first; each case shows the post with the
 * red flags highlighted, the rule findings, the optional AI check and the reports, and three decisions:
 * remove the post, suspend the employer, or dismiss.
 */
export function ModerationPage() {
  const { t } = useTranslation()
  const lang = useLang()
  const [status, setStatus] = useState<CaseStatus>('OPEN')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const queue = useCases(status)
  const counts = useCaseCounts()
  const items = queue.data?.pages.flatMap((page) => page.items) ?? []

  return (
    <div>
      <PageHeader icon={<ShieldAlert className="size-6" aria-hidden />} title={t('moderation.title')} subtitle={t('moderation.subtitle')} />
      <div className="grid gap-6 lg:grid-cols-[360px_minmax(0,1fr)]">
        <Card className="p-3 md:p-4">
          <div className="flex gap-1" role="tablist">
            {TABS.map((tab) => (
              <button
                key={tab}
                role="tab"
                aria-selected={status === tab}
                onClick={() => {
                  setStatus(tab)
                  setSelectedId(null)
                }}
                className={cn('flex-1 rounded-lg px-2 py-2 text-xs font-semibold whitespace-nowrap sm:text-sm', status === tab ? 'bg-brand-600 text-white' : 'text-slate-600 hover:bg-slate-100')}
              >
                {t(`moderation.tabs.${tab}`)} ({localDigits(counts.data?.[tab] ?? 0, lang)})
              </button>
            ))}
          </div>
          <ul className="mt-3 flex flex-col gap-2">
            {queue.isLoading && <Spinner label={t('common.loading')} />}
            {queue.error && <Alert tone="error">{queue.error.message}</Alert>}
            {queue.isSuccess && items.length === 0 && <EmptyState text={t('moderation.empty')} />}
            {items.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  onClick={() => setSelectedId(item.id)}
                  className={cn('w-full rounded-xl border p-3 text-left hover:border-brand-300', selectedId === item.id ? 'border-brand-600 bg-brand-50' : 'border-slate-200 bg-white')}
                >
                  <div className="flex items-start justify-between gap-2">
                    <p className="font-semibold">{(lang === 'bn' ? item.jobTitleBn : item.jobTitleEn) || item.jobTitleBn || '—'}</p>
                    {item.jobStatus && <JobStatusBadge status={item.jobStatus} />}
                  </div>
                  <p className="mt-0.5 text-xs text-slate-500">{item.employerName}</p>
                  <div className="mt-2 flex flex-wrap gap-1.5 text-xs">
                    <span className={cn('rounded-full px-2 py-0.5 font-semibold', item.screening === 'BLOCK' ? 'bg-red-100 text-red-800' : item.source === 'REPORT' ? 'bg-orange-100 text-orange-800' : 'bg-amber-100 text-amber-900')}>
                      {item.source === 'REPORT' ? t('moderation.source.REPORT') : t(`moderation.screening.${item.screening ?? 'REVIEW'}`)}
                    </span>
                    {item.reportCount > 0 && (
                      <span className="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-0.5 font-semibold text-slate-700">
                        <Flag className="size-3" aria-hidden /> {localDigits(item.reportCount, lang)}
                      </span>
                    )}
                    {item.codes.slice(0, 2).map((code) => (
                      <span key={code} className="rounded-full bg-slate-100 px-2 py-0.5 text-slate-700">
                        {t(`finding.short.${code}`, { defaultValue: code })}
                      </span>
                    ))}
                  </div>
                  <p className="mt-1 text-xs text-slate-400">{formatDate(item.createdAt, lang)}</p>
                </button>
              </li>
            ))}
            {queue.hasNextPage && (
              <Button variant="ghost" onClick={() => void queue.fetchNextPage()} loading={queue.isFetchingNextPage}>
                {t('common.loadMore')}
              </Button>
            )}
          </ul>
        </Card>

        {selectedId ? <CasePanel key={selectedId} id={selectedId} /> : <Card><EmptyState icon={<Gavel className="size-8" aria-hidden />} text={t('moderation.select')} /></Card>}
      </div>
    </div>
  )
}

function CasePanel({ id }: { id: string }) {
  const { t } = useTranslation()
  const lang = useLang()
  const { data, isLoading, error } = useCase(id)
  if (isLoading) return <Card><Spinner label={t('common.loading')} /></Card>
  if (error || !data) return <Alert tone="error">{error?.message ?? t('common.notFound')}</Alert>

  const quotes = [...data.findings.map((f) => f.quote), ...(data.aiAssessment?.redFlags ?? [])]
  const job = data.job
  return (
    <Card className="flex flex-col gap-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold">{job ? (lang === 'bn' ? job.titleBn : job.titleEn) || job.titleBn : '—'}</h2>
          <p className="mt-1 text-sm text-slate-600">
            {t(`moderation.source.${data.source}`)} · {formatDate(data.createdAt, lang)}
            {data.reportCount > 0 && ` · ${t('moderation.reports', { n: localDigits(data.reportCount, lang) })}`}
          </p>
        </div>
        {job && <JobStatusBadge status={job.status} />}
      </div>

      {job && (
        <section>
          <h3 className="mb-2 text-sm font-semibold">{t('moderation.post')}</h3>
          <div className="grid gap-3 md:grid-cols-2">
            {[
              ['bn', job.titleBn, job.descriptionBn],
              ['en', job.titleEn, job.descriptionEn],
            ].map(([language, title, description]) => (
              <div key={language} lang={language ?? undefined} className="rounded-xl bg-slate-50 p-3 text-sm leading-6">
                <p className="font-semibold">
                  <Highlighted text={title ?? ''} quotes={quotes} />
                </p>
                <p className="mt-1 whitespace-pre-line text-slate-700">
                  <Highlighted text={description ?? ''} quotes={quotes} />
                </p>
              </div>
            ))}
          </div>
          <p className="mt-2 text-sm text-slate-600">
            {formatWage(job.wageMin, job.wageMax, job.wagePeriod ? t(`job.period.${job.wagePeriod}`) : null, lang) ?? t('moderation.noWage')}
            {job.hours && ` · ${localDigits(job.hours, lang)}`}
            {job.areaName && ` · ${job.areaName}`}
          </p>
        </section>
      )}

      {data.findings.length > 0 && (
        <section>
          <h3 className="mb-2 text-sm font-semibold">{t('moderation.findings')}</h3>
          <FindingList findings={data.findings} compact />
        </section>
      )}

      {data.aiAssessment && (
        <section className="rounded-xl border border-violet-200 bg-violet-50 p-3 text-sm">
          <h3 className="flex items-center gap-2 font-semibold text-violet-900">
            <Bot className="size-4" aria-hidden />
            {t('moderation.ai', { risk: t(`moderation.risk.${data.aiAssessment.risk}`) })}
          </h3>
          {data.aiAssessment.reasonForModerator && <p className="mt-1 text-violet-900">{data.aiAssessment.reasonForModerator}</p>}
        </section>
      )}

      {data.reports.length > 0 && (
        <section>
          <h3 className="mb-2 text-sm font-semibold">{t('moderation.reportList')}</h3>
          <ul className="flex flex-col gap-2">
            {data.reports.map((report, index) => (
              <li key={index} className="rounded-xl border border-orange-200 bg-orange-50 p-3 text-sm">
                <p className="font-semibold text-orange-900">
                  {t(`report.reasons.${report.reason}`)} <span className="font-normal text-orange-800">· {formatDate(report.createdAt, lang)}</span>
                </p>
                {report.details && <p className="mt-1 text-orange-900">{report.details}</p>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {data.employer && (
        <section className="flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 p-3 text-sm">
          <Building2 className="size-4 text-slate-500" aria-hidden />
          <span className="font-semibold">{data.employer.name}</span>
          <VerifiedBadge verified={data.employer.verified} />
          {data.employer.ownerEmail && (
            <span className="inline-flex items-center gap-1 text-slate-600">
              <Mail className="size-4" aria-hidden /> {data.employer.ownerEmail}
            </span>
          )}
          {!data.employer.ownerActive && <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-800">{t('moderation.suspended')}</span>}
        </section>
      )}

      {data.status === 'OPEN' ? <DecisionForm data={data} /> : <Outcome data={data} />}
    </Card>
  )
}

/** The post's text with the quoted red flags highlighted. */
export function Highlighted({ text, quotes }: { text: string; quotes: Array<string | null | undefined> }) {
  return (
    <>
      {highlightParts(text, quotes).map((part, index) =>
        part.hit ? (
          <mark key={index} className="rounded bg-red-200 px-0.5 text-red-950">
            {part.text}
          </mark>
        ) : (
          <span key={index}>{part.text}</span>
        ),
      )}
    </>
  )
}

function DecisionForm({ data }: { data: CaseDetail }) {
  const { t } = useTranslation()
  const toast = useToast()
  const decide = useCaseDecision()
  const [note, setNote] = useState('')
  const [noteError, setNoteError] = useState<string | null>(null)
  const [confirmSuspend, setConfirmSuspend] = useState(false)

  function submit(decision: CaseDecision) {
    const problem = decisionNoteError(decision, note)
    setNoteError(problem ? t(problem) : null)
    if (problem) return
    if (decision === 'SUSPEND_EMPLOYER' && !confirmSuspend) {
      setConfirmSuspend(true)
      return
    }
    decide.mutate(
      { id: data.id, decision, note: note.trim() || null },
      { onSuccess: () => toast(t(`moderation.done.${decision}`)) },
    )
  }

  const error = decide.error instanceof ApiError ? decide.error.message : decide.error?.message
  return (
    <section className="flex flex-col gap-3 border-t border-slate-200 pt-4">
      <h3 className="text-sm font-semibold">{t('moderation.decide')}</h3>
      <label htmlFor="moderation-note" className="text-sm">
        {t('moderation.note')}
      </label>
      <Textarea id="moderation-note" maxLength={500} value={note} onChange={(e) => setNote(e.target.value)} aria-invalid={!!noteError} placeholder={t('moderation.notePlaceholder')} />
      {noteError && <p className="text-sm font-semibold text-red-700">{noteError}</p>}
      {confirmSuspend && <Alert tone="warning">{t('moderation.confirmSuspend')}</Alert>}
      {error && <Alert tone="error">{error}</Alert>}
      <div className="flex flex-wrap gap-2">
        <Button variant="danger" icon={<Ban className="size-4" />} loading={decide.isPending && decide.variables?.decision === 'REMOVE_POST'} disabled={decide.isPending} onClick={() => submit('REMOVE_POST')}>
          {t('moderation.removePost')}
        </Button>
        <Button variant="danger" icon={<UserX className="size-4" />} loading={decide.isPending && decide.variables?.decision === 'SUSPEND_EMPLOYER'} disabled={decide.isPending} onClick={() => submit('SUSPEND_EMPLOYER')}>
          {confirmSuspend ? t('moderation.suspendConfirm') : t('moderation.suspend')}
        </Button>
        <Button variant="success" icon={<CheckCircle2 className="size-4" />} loading={decide.isPending && decide.variables?.decision === 'DISMISS'} disabled={decide.isPending} onClick={() => submit('DISMISS')}>
          {t('moderation.dismiss')}
        </Button>
      </div>
      <p className="text-xs text-slate-500">{t('moderation.auditHint')}</p>
    </section>
  )
}

function Outcome({ data }: { data: CaseDetail }) {
  const { t } = useTranslation()
  const lang = useLang()
  return (
    <Alert tone={data.status === 'DISMISSED' ? 'success' : 'error'} title={t(`moderation.outcome.${data.decision ?? 'DISMISS'}`)}>
      {data.note && <p>{data.note}</p>}
      {data.decidedAt && <p className="mt-1 text-xs">{formatDate(data.decidedAt, lang)}</p>}
    </Alert>
  )
}
