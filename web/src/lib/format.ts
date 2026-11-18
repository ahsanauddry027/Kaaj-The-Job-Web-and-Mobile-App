import type { Lang } from '../i18n'
import type { Term, WagePeriod } from './types'

const BANGLA_DIGITS = '০১২৩৪৫৬৭৮৯'

/** "১৮০০০" → "18000": people may type Bangla digits in number and phone fields. */
export function toAsciiDigits(text: string): string {
  return text.replace(/[০-৯]/g, (d) => String(BANGLA_DIGITS.indexOf(d)))
}

/** Digits in the reader's language: 2026 → ২০২৬ in Bangla. */
export function localDigits(value: string | number, lang: Lang): string {
  const text = String(value)
  return lang === 'bn' ? text.replace(/[0-9]/g, (d) => BANGLA_DIGITS[Number(d)]) : text
}

const locale = (lang: Lang) => (lang === 'bn' ? 'bn-BD' : 'en-BD')

export function formatNumber(n: number, lang: Lang): string {
  return new Intl.NumberFormat(locale(lang), { maximumFractionDigits: 0 }).format(n)
}

/** ৳১৮,০০০ in Bangla, ৳18,000 in English. */
export function formatTaka(n: number, lang: Lang): string {
  return '৳' + formatNumber(n, lang)
}

/** "৳১৬,০০০ – ২০,০০০ / মাস". {@code periodName} is the translated period ("month"). */
export function formatWage(
  min: number | null | undefined,
  max: number | null | undefined,
  periodName: string | null,
  lang: Lang,
): string | null {
  if (min == null && max == null) return null
  const range =
    min != null && max != null && min !== max
      ? `${formatTaka(min, lang)} – ${formatNumber(max, lang)}`
      : formatTaka((min ?? max) as number, lang)
  return periodName ? `${range} / ${periodName}` : range
}

export function formatDate(iso: string | null | undefined, lang: Lang): string {
  if (!iso) return ''
  return new Intl.DateTimeFormat(locale(lang), { day: 'numeric', month: 'long', year: 'numeric' }).format(new Date(iso))
}

/** Whole days from now until {@code iso}, never below 0. */
export function daysLeft(iso: string | null | undefined, now: Date = new Date()): number {
  if (!iso) return 0
  const ms = new Date(iso).getTime() - now.getTime()
  return Math.max(0, Math.ceil(ms / 86_400_000))
}

export function termName(term: Term | null | undefined, lang: Lang): string {
  if (!term) return ''
  return lang === 'bn' ? term.nameBn : term.nameEn
}

export type { WagePeriod }
