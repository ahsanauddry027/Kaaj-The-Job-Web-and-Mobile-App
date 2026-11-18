import { describe, expect, it } from 'vitest'
import { decisionNoteError, highlightParts, sortFindings } from './trust'
import type { Finding } from './types'

describe('highlightParts', () => {
  it('marks every quoted red flag, ignoring case', () => {
    const parts = highlightParts('Malaysia factory job. Processing fee Tk 50,000 by bKash.', ['processing fee', 'malaysia'])
    expect(parts.filter((p) => p.hit).map((p) => p.text)).toEqual(['Malaysia', 'Processing fee'])
    expect(parts.map((p) => p.text).join('')).toBe('Malaysia factory job. Processing fee Tk 50,000 by bKash.')
  })

  it('matches English digits in a quote against Bangla digits in the post', () => {
    const parts = highlightParts('যোগাযোগ: ০১৭১২৩৪৫৬৭৮', ['01712345678'])
    expect(parts.filter((p) => p.hit).map((p) => p.text)).toEqual(['০১৭১২৩৪৫৬৭৮'])
  })

  it('keeps a Bangla vowel sign with the highlighted word', () => {
    const parts = highlightParts('যোগাযোগ হোয়াটসঅ্যাপে।', ['হোয়াটসঅ্যাপ'])
    expect(parts).toEqual([
      { text: 'যোগাযোগ ', hit: false },
      { text: 'হোয়াটসঅ্যাপে', hit: true },
      { text: '।', hit: false },
    ])
  })

  it('leaves the text alone without quotes', () => {
    expect(highlightParts('Drive a pickup', [null, ''])).toEqual([{ text: 'Drive a pickup', hit: false }])
    expect(highlightParts('', ['x'])).toEqual([])
  })
})

describe('decisionNoteError', () => {
  it('needs a note to remove a post or suspend an employer, but not to dismiss', () => {
    expect(decisionNoteError('REMOVE_POST', '')).toBe('moderation.noteRequired')
    expect(decisionNoteError('SUSPEND_EMPLOYER', ' a ')).toBe('moderation.noteRequired')
    expect(decisionNoteError('REMOVE_POST', 'Asks for a processing fee')).toBeNull()
    expect(decisionNoteError('DISMISS', '')).toBeNull()
    expect(decisionNoteError('REMOVE_POST', 'x'.repeat(501))).toBe('moderation.noteTooLong')
  })
})

describe('sortFindings', () => {
  it('puts blocks first and removes duplicates', () => {
    const findings: Finding[] = [
      { kind: 'QUESTION', code: 'long-day', quote: '08:00-18:00' },
      { kind: 'BLOCK', code: 'money-request', quote: 'processing fee' },
      { kind: 'REVIEW', code: 'contact-details', quote: '01712345678' },
      { kind: 'BLOCK', code: 'money-request', quote: 'processing fee' },
    ]
    expect(sortFindings(findings).map((f) => f.code)).toEqual(['money-request', 'contact-details', 'long-day'])
  })
})
