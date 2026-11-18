import { Building2, CheckCircle2, ExternalLink, FileText, Mail, MapPin, Phone, ShieldCheck, User, XCircle } from 'lucide-react'
import type { ReactNode } from 'react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { MapPicker } from '../../components/MapPicker'
import { VerificationBadge } from '../../components/StatusBadge'
import { useToast } from '../../components/Toast'
import { Alert, Button, Card, EmptyState, PageHeader, Spinner, Textarea } from '../../components/ui'
import { cn } from '../../lib/cn'
import { formatDate, localDigits } from '../../lib/format'
import type { AdminEmployer } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useDecision, useVerificationCounts, useVerificationQueue, type QueueStatus } from './api'

const TABS: QueueStatus[] = ['PENDING', 'VERIFIED', 'REJECTED']

/** Admin: the employer verification queue (oldest first) and the decision panel. */
export function AdminVerificationPage() {
  const { t } = useTranslation()
  const lang = useLang()
  const [status, setStatus] = useState<QueueStatus>('PENDING')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const queue = useVerificationQueue(status)
  const counts = useVerificationCounts()
  const items = queue.data?.pages.flatMap((page) => page.items) ?? []
  const selected = items.find((item) => item.id === selectedId) ?? null

  return (
    <div>
      <PageHeader icon={<ShieldCheck className="size-6" aria-hidden />} title={t('admin.title')} subtitle={t('admin.subtitle')} />
      <div className="grid gap-6 lg:grid-cols-[340px_minmax(0,1fr)]">
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
                {t(`admin.tabs.${tab}`)} ({localDigits(counts.data?.[tab] ?? 0, lang)})
              </button>
            ))}
          </div>
          <ul className="mt-3 flex flex-col gap-2">
            {queue.isLoading && <Spinner label={t('common.loading')} />}
            {queue.error && <Alert tone="error">{queue.error.message}</Alert>}
            {queue.isSuccess && items.length === 0 && <EmptyState text={t('admin.empty')} />}
            {items.map((employer) => (
              <li key={employer.id}>
                <button
                  type="button"
                  onClick={() => setSelectedId(employer.id)}
                  className={cn(
                    'w-full rounded-xl border p-3 text-left hover:border-brand-300',
                    selectedId === employer.id ? 'border-brand-600 bg-brand-50' : 'border-slate-200 bg-white',
                  )}
                >
                  <span className="flex items-center justify-between gap-2">
                    <span className="truncate font-semibold">{employer.name}</span>
                    <VerificationBadge status={employer.status} />
                  </span>
                  <span className="mt-1 block text-xs text-slate-500">
                    {t(`kind.${employer.kind}`)} · {t('admin.submitted')}: {formatDate(employer.submittedAt, lang)}
                  </span>
                </button>
              </li>
            ))}
          </ul>
          {queue.hasNextPage && (
            <Button variant="ghost" className="mt-2 w-full" loading={queue.isFetchingNextPage} onClick={() => queue.fetchNextPage()}>
              {t('common.loadMore')}
            </Button>
          )}
        </Card>

        {selected ? <Decision key={selected.id} employer={selected} onDone={() => setSelectedId(null)} /> : <EmptyState icon={<Building2 className="size-10" />} text={t('admin.select')} />}
      </div>
    </div>
  )
}

function Decision({ employer, onDone }: { employer: AdminEmployer; onDone: () => void }) {
  const { t } = useTranslation()
  const lang = useLang()
  const toast = useToast()
  const decide = useDecision()
  const [reason, setReason] = useState('')
  const [reasonError, setReasonError] = useState<string | null>(null)
  const isPdf = employer.documentName?.toLowerCase().endsWith('.pdf') || employer.documentUrl?.endsWith('.pdf')
  const quickReasons = t('admin.quickReasons', { returnObjects: true }) as string[]

  function approve() {
    decide.mutate(
      { id: employer.id, decision: 'APPROVE', reason: reason.trim() || null },
      { onSuccess: () => { toast(t('admin.approved')); onDone() } },
    )
  }

  function reject() {
    if (!reason.trim()) {
      setReasonError(t('admin.reasonRequired'))
      return
    }
    decide.mutate({ id: employer.id, decision: 'REJECT', reason: reason.trim() }, { onSuccess: () => { toast(t('admin.rejected')); onDone() } })
  }

  return (
    <Card className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold">{employer.name}</h2>
          <p className="text-sm text-slate-600">{t(`kind.${employer.kind}`)}</p>
        </div>
        <VerificationBadge status={employer.status} />
      </div>

      <dl className="grid gap-3 text-sm sm:grid-cols-2">
        <Info icon={<MapPin className="size-4" />} label={t('profile.address')} value={employer.address} />
        <Info icon={<User className="size-4" />} label={t('profile.contactPerson')} value={employer.contactPerson} />
        <Info icon={<Phone className="size-4" />} label={t('profile.contactPhone')} value={localDigits(employer.contactPhone, lang)} />
        <Info icon={<Mail className="size-4" />} label={t('admin.owner')} value={employer.ownerEmail ?? '—'} />
        {employer.contactEmail && <Info icon={<Mail className="size-4" />} label={t('profile.contactEmail')} value={employer.contactEmail} />}
        <Info icon={<FileText className="size-4" />} label={t('profile.documentType')} value={employer.documentType ? t(`docType.${employer.documentType}`) : '—'} />
      </dl>

      {employer.latitude != null && employer.longitude != null && <MapPicker readOnly value={{ lat: employer.latitude, lng: employer.longitude }} height={180} />}

      <div>
        <div className="mb-2 flex items-center justify-between">
          <p className="font-semibold">{t('verification.document')}</p>
          {employer.documentUrl && (
            <a href={employer.documentUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-sm font-semibold text-brand-700">
              {t('admin.openDocument')} <ExternalLink className="size-4" aria-hidden />
            </a>
          )}
        </div>
        {employer.documentUrl ? (
          isPdf ? (
            <iframe title={employer.documentName ?? 'document'} src={employer.documentUrl} className="h-96 w-full rounded-xl border border-slate-200" />
          ) : (
            <img src={employer.documentUrl} alt={employer.documentName ?? ''} className="max-h-96 w-full rounded-xl border border-slate-200 object-contain" />
          )
        ) : (
          <p className="text-sm text-slate-500">{t('admin.noDocument')}</p>
        )}
      </div>

      {employer.verificationNote && (
        <Alert tone={employer.status === 'REJECTED' ? 'error' : 'info'} title={t('verification.note')}>
          {employer.verificationNote}
        </Alert>
      )}

      {employer.status === 'PENDING' && (
        <div className="flex flex-col gap-3 border-t border-slate-200 pt-4">
          <div className="flex flex-wrap gap-2">
            {quickReasons.map((quick) => (
              <button key={quick} type="button" onClick={() => { setReason(quick); setReasonError(null) }} className="rounded-full border border-slate-300 px-3 py-1 text-xs hover:bg-slate-50">
                {quick}
              </button>
            ))}
          </div>
          <label className="text-sm font-semibold" htmlFor="decision-reason">
            {t('admin.reasonLabel')} / {t('admin.note')}
          </label>
          <Textarea id="decision-reason" value={reason} onChange={(e) => { setReason(e.target.value); setReasonError(null) }} className="min-h-20" aria-invalid={!!reasonError} />
          {reasonError && <p role="alert" className="text-xs font-medium text-red-600">{reasonError}</p>}
          {decide.error && <Alert tone="error">{decide.error.message}</Alert>}
          <div className="flex flex-wrap gap-3">
            <Button variant="success" icon={<CheckCircle2 className="size-4" />} loading={decide.isPending} onClick={approve}>
              {t('admin.approve')}
            </Button>
            <Button variant="danger" icon={<XCircle className="size-4" />} disabled={decide.isPending} onClick={reject}>
              {t('admin.reject')}
            </Button>
          </div>
        </div>
      )}
    </Card>
  )
}

function Info({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-start gap-2">
      <span className="mt-0.5 text-slate-400" aria-hidden>{icon}</span>
      <div>
        <dt className="text-xs text-slate-500">{label}</dt>
        <dd className="font-medium break-words">{value}</dd>
      </div>
    </div>
  )
}
