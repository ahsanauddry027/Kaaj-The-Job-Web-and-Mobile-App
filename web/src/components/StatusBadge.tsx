import { BadgeCheck } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { cn } from '../lib/cn'
import type { JobStatus, VerificationStatus } from '../lib/types'

const jobColours: Record<JobStatus, string> = {
  DRAFT: 'bg-amber-100 text-amber-800 ring-amber-200',
  IN_REVIEW: 'bg-sky-100 text-sky-800 ring-sky-200',
  PUBLISHED: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  PAUSED: 'bg-orange-100 text-orange-800 ring-orange-200',
  CLOSED: 'bg-slate-200 text-slate-700 ring-slate-300',
  BLOCKED: 'bg-red-100 text-red-700 ring-red-200',
}

const verificationColours: Record<VerificationStatus, string> = {
  UNVERIFIED: 'bg-slate-100 text-slate-700 ring-slate-200',
  PENDING: 'bg-amber-100 text-amber-800 ring-amber-200',
  VERIFIED: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  REJECTED: 'bg-red-100 text-red-700 ring-red-200',
}

const pill = 'inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ring-1 ring-inset whitespace-nowrap'

export function JobStatusBadge({ status }: { status: JobStatus }) {
  const { t } = useTranslation()
  return <span className={cn(pill, jobColours[status])}>{t(`job.status.${status}`)}</span>
}

export function VerificationBadge({ status }: { status: VerificationStatus }) {
  const { t } = useTranslation()
  return <span className={cn(pill, verificationColours[status])}>{t(`verification.status.${status}`)}</span>
}

/**
 * The "Verified" badge on job cards and job pages (Feature 3). Member-1's search results reuse it.
 * Tapping it explains what it means.
 */
export function VerifiedBadge({ verified }: { verified: boolean }) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  if (!verified) return null
  return (
    <span className="relative inline-flex">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={cn(pill, 'bg-emerald-600 text-white ring-emerald-600')}
        aria-expanded={open}
      >
        <BadgeCheck className="size-3.5" aria-hidden />
        {t('verification.badge')}
      </button>
      {open && (
        <span role="tooltip" className="absolute top-full left-0 z-20 mt-1 w-64 rounded-xl bg-slate-900 p-3 text-xs text-white shadow-lg">
          {t('verification.badgeExplain')}
        </span>
      )}
    </span>
  )
}
