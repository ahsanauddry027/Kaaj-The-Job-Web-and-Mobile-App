import { Banknote, Briefcase, Clock, MapPin, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { VerifiedBadge } from '../../components/StatusBadge'
import { formatWage, localDigits, termName } from '../../lib/format'
import type { JobType, Shift, Term, WagePeriod } from '../../lib/types'
import { useLang } from '../../lib/useLang'

export interface JobCardData {
  titleBn: string
  titleEn: string
  occupation: Term | null
  jobType: JobType
  shift: Shift
  hours: string | null
  openings: number | null
  wageMin: number | null
  wageMax: number | null
  wagePeriod: WagePeriod | null
  areaName: string | null
  remote: boolean
  employerName: string | null
  employerVerified: boolean
}

/**
 * A job card as job seekers will see it: the wage, the area and the "Verified" badge are always visible
 * (context file §18). Member-1's search results can reuse this card.
 */
export function JobCard({ job }: { job: JobCardData }) {
  const { t } = useTranslation()
  const lang = useLang()
  const title = (lang === 'bn' ? job.titleBn : job.titleEn) || job.titleBn || job.titleEn || '—'
  const wage = formatWage(job.wageMin, job.wageMax, job.wagePeriod ? t(`job.period.${job.wagePeriod}`) : null, lang)
  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start gap-3">
        <div className="grid size-12 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-600">
          <Briefcase className="size-6" aria-hidden />
        </div>
        <div className="min-w-0 flex-1">
          <h3 className="truncate text-lg font-bold text-slate-900">{title}</h3>
          <div className="mt-1 flex flex-wrap items-center gap-2 text-sm text-slate-600">
            {job.employerName && <span className="font-medium">{job.employerName}</span>}
            <VerifiedBadge verified={job.employerVerified} />
          </div>
        </div>
      </div>
      <ul className="mt-4 grid gap-2 text-sm text-slate-700 sm:grid-cols-2">
        <li className="flex items-center gap-2 font-semibold text-emerald-700">
          <Banknote className="size-4" aria-hidden />
          {wage ?? '—'}
        </li>
        <li className="flex items-center gap-2">
          <MapPin className="size-4 text-slate-400" aria-hidden />
          {job.remote ? t('job.remoteLabel') : job.areaName || '—'}
        </li>
        <li className="flex items-center gap-2">
          <Clock className="size-4 text-slate-400" aria-hidden />
          {t(`job.shift.${job.shift}`)} · {t(`job.type.${job.jobType}`)}
          {job.hours && ` · ${localDigits(job.hours, lang)}`}
        </li>
        <li className="flex items-center gap-2">
          <Users className="size-4 text-slate-400" aria-hidden />
          {t('job.people', { n: localDigits(job.openings ?? 1, lang) })}
          {job.occupation && ` · ${termName(job.occupation, lang)}`}
        </li>
      </ul>
    </article>
  )
}
