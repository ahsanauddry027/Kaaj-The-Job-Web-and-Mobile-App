import { toAsciiDigits } from './format'
import type { CaseDecision, Finding, FindingKind } from './types'

/** A piece of text, and whether it is one of the quoted red flags. */
export interface TextPart {
  text: string
  hit: boolean
}

/**
 * Splits a post's text so the quoted red flags can be highlighted for the moderator. Matching ignores
 * letter case and Bangla vs. English digits ("০১৭১২…" matches the quote "01712…").
 */
const COMBINING = /\p{M}/u

export function highlightParts(text: string, quotes: Array<string | null | undefined>): TextPart[] {
  if (!text) return []
  const haystack = toAsciiDigits(text).toLowerCase()
  const marks = new Array<boolean>(text.length).fill(false)
  for (const quote of quotes) {
    const needle = quote ? toAsciiDigits(quote).toLowerCase().replace(/…$/, '').trim() : ''
    if (needle.length < 2) continue
    let from = 0
    for (;;) {
      const at = haystack.indexOf(needle, from)
      if (at < 0) break
      // Never split a Bangla letter from its vowel sign: "হোয়াটসঅ্যাপ" inside "হোয়াটসঅ্যাপে"
      // would leave "ে" on its own, which shows as a dotted circle.
      let end = at + needle.length
      while (end < text.length && COMBINING.test(text[end])) end++
      marks.fill(true, at, end)
      from = end
    }
  }
  const parts: TextPart[] = []
  for (let i = 0; i < text.length; i++) {
    const last = parts[parts.length - 1]
    if (last && last.hit === marks[i]) last.text += text[i]
    else parts.push({ text: text[i], hit: marks[i] })
  }
  return parts
}

/** The moderator must explain a removal or a suspension; a dismissal needs no note. */
export function decisionNoteError(decision: CaseDecision, note: string): string | null {
  if (decision === 'DISMISS') return null
  if (note.trim().length < 3) return 'moderation.noteRequired'
  if (note.trim().length > 500) return 'moderation.noteTooLong'
  return null
}

/** Most serious first: what blocks, then what sends to review, then questions, nudges and removals. */
const ORDER: FindingKind[] = ['BLOCK', 'REVIEW', 'QUESTION', 'NUDGE', 'REMOVED']

export function sortFindings(findings: Finding[]): Finding[] {
  const seen = new Set<string>()
  return [...findings]
    .sort((a, b) => ORDER.indexOf(a.kind) - ORDER.indexOf(b.kind))
    .filter((finding) => {
      const key = `${finding.kind}|${finding.code}|${finding.quote ?? ''}`
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
}
