import { BadgeCheck, CalendarDays, Clock3, FileText, MessageSquare, ShieldAlert, ShieldCheck, ShieldQuestion } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { VerificationBadge } from '../../components/StatusBadge'
import { Button, Card } from '../../components/ui'
import { cn } from '../../lib/cn'
import { formatDate } from '../../lib/format'
import type { EmployerProfile, VerificationStatus } from '../../lib/types'
import { useLang } from '../../lib/useLang'

const look: Record<VerificationStatus, { box: string; icon: ReactNode }> = {
  UNVERIFIED: { box: 'border-slate-200 bg-slate-50 text-slate-800', icon: <ShieldQuestion className="size-10 text-slate-500" aria-hidden /> },
  PENDING: { box: 'border-amber-200 bg-amber-50 text-amber-900', icon: <Clock3 className="size-10 text-amber-600" aria-hidden /> },
  VERIFIED: { box: 'border-emerald-200 bg-emerald-50 text-emerald-900', icon: <BadgeCheck className="size-10 text-emerald-600" aria-hidden /> },
  REJECTED: { box: 'border-red-200 bg-red-50 text-red-900', icon: <ShieldAlert className="size-10 text-red-600" aria-hidden /> },
}

interface Props {
  profile: EmployerProfile | null
  onResubmit?: () => void
  resubmitting?: boolean
}

/** The right-hand "Verification status" panel: one banner per state, then the details. */
export function VerificationPanel({ profile, onResubmit, resubmitting }: Props) {
  const { t } = useTranslation()
  const lang = useLang()
  const status: VerificationStatus = profile?.status ?? 'UNVERIFIED'
  const missing = (profile?.missing ?? []).filter((field) => field !== 'document' || status !== 'PENDING')

  return (
    <Card className="flex flex-col gap-5">
      <h2 className="flex items-center gap-2 text-lg font-bold">
        <ShieldCheck className="size-6 text-emerald-600" aria-hidden />
        {t('verification.title')}
      </h2>

      <div data-testid="verification-banner" className={cn('flex items-start gap-3 rounded-2xl border p-4', look[status].box)}>
        {look[status].icon}
        <div>
          <p className="text-base font-bold">{t(`verification.status.${status}`)}</p>
          <p className="mt-1 text-sm">{t(`verification.banner.${status}`)}</p>
        </div>
      </div>

      {status === 'REJECTED' && profile?.verificationNote && (
        <div className="rounded-xl border border-red-200 bg-white p-3 text-sm">
          <p className="font-semibold text-red-700">{t('verification.reason')}</p>
          <p className="mt-1 text-slate-800">{profile.verificationNote}</p>
          {onResubmit && profile.hasDocument && (
            <Button className="mt-3" variant="secondary" loading={resubmitting} onClick={onResubmit}>
              {t('verification.resubmit')}
            </Button>
          )}
        </div>
      )}

      {profile && missing.length > 0 && status !== 'VERIFIED' && (
        <div className="text-sm">
          <p className="font-semibold">{t('verification.missing')}</p>
          <ul className="mt-1 list-disc pl-5 text-slate-700">
            {missing.map((field) => (
              <li key={field}>{t(`profile.missing.${field}`)}</li>
            ))}
          </ul>
        </div>
      )}

      {profile && (
        <div className="border-t border-slate-200 pt-4">
          <h3 className="font-bold">{t('verification.details')}</h3>
          <dl className="mt-3 grid grid-cols-[auto_1fr] items-center gap-x-3 gap-y-3 text-sm">
            <Row icon={<CalendarDays className="size-4" />} label={t('verification.submittedAt')}>
              {profile.submittedAt ? formatDate(profile.submittedAt, lang) : t('common.notSet')}
            </Row>
            <Row icon={<ShieldCheck className="size-4" />} label={t('verification.title')}>
              <VerificationBadge status={status} />
            </Row>
            {status === 'VERIFIED' && profile.verifiedAt && (
              <Row icon={<BadgeCheck className="size-4" />} label={t('verification.verifiedAt')}>
                {formatDate(profile.verifiedAt, lang)}
              </Row>
            )}
            {status === 'VERIFIED' && profile.verificationNote && (
              <Row icon={<MessageSquare className="size-4" />} label={t('verification.note')}>
                {profile.verificationNote}
              </Row>
            )}
            <Row icon={<FileText className="size-4" />} label={t('verification.document')}>
              {profile.hasDocument && profile.documentType
                ? `${t(`docType.${profile.documentType}`)} · ${profile.documentName ?? ''}`
                : t('verification.noDocument')}
            </Row>
          </dl>
        </div>
      )}

      {status === 'VERIFIED' && <p className="rounded-xl bg-brand-50 p-3 text-xs text-brand-700">{t('verification.changeWarning')}</p>}
    </Card>
  )
}

function Row({ icon, label, children }: { icon: ReactNode; label: string; children: ReactNode }) {
  return (
    <>
      <dt className="flex items-center gap-2 text-slate-600">
        <span aria-hidden>{icon}</span>
        {label}
      </dt>
      <dd className="font-medium text-slate-900">{children}</dd>
    </>
  )
}
