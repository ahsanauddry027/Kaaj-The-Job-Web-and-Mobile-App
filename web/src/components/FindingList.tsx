import { AlertTriangle, Ban, Eraser, HelpCircle, Lightbulb } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { cn } from '../lib/cn'
import { sortFindings } from '../lib/trust'
import type { Finding, FindingKind } from '../lib/types'

const STYLE: Record<FindingKind, { icon: ReactNode; box: string }> = {
  BLOCK: { icon: <Ban className="size-4" aria-hidden />, box: 'border-red-200 bg-red-50 text-red-800' },
  REVIEW: { icon: <AlertTriangle className="size-4" aria-hidden />, box: 'border-amber-200 bg-amber-50 text-amber-900' },
  QUESTION: { icon: <HelpCircle className="size-4" aria-hidden />, box: 'border-sky-200 bg-sky-50 text-sky-900' },
  NUDGE: { icon: <Lightbulb className="size-4" aria-hidden />, box: 'border-violet-200 bg-violet-50 text-violet-900' },
  REMOVED: { icon: <Eraser className="size-4" aria-hidden />, box: 'border-slate-200 bg-slate-50 text-slate-700' },
}

/**
 * The rule findings in plain words (context file §10.1: "every warning or block says why"), most serious
 * first, each with the words from the post it is about.
 */
export function FindingList({ findings, compact = false }: { findings: Finding[]; compact?: boolean }) {
  const { t } = useTranslation()
  const items = sortFindings(findings)
  if (items.length === 0) return null
  return (
    <ul className={cn('flex flex-col', compact ? 'gap-1.5' : 'gap-2')}>
      {items.map((finding) => (
        <li key={`${finding.kind}-${finding.code}-${finding.quote ?? ''}`} className={cn('flex gap-2 rounded-xl border px-3 py-2 text-sm', STYLE[finding.kind].box)}>
          <span className="mt-0.5 shrink-0">{STYLE[finding.kind].icon}</span>
          <span>
            <span className="font-semibold">{t(`finding.kind.${finding.kind}`)}: </span>
            {t(`finding.code.${finding.code}`, { defaultValue: finding.code })}
            {finding.quote && <span className="ms-1 inline-block rounded bg-white/70 px-1 text-xs font-medium whitespace-nowrap">“{finding.quote}”</span>}
          </span>
        </li>
      ))}
    </ul>
  )
}
