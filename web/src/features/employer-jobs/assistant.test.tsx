import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { FindingList } from '../../components/FindingList'
import i18n from '../../i18n'
import type { JobDraft } from '../../lib/types'
import { draftToForm, emptyJobForm } from './schema'

const karim: JobDraft = {
  source: 'TEMPLATE',
  titleBn: 'পিকআপ ড্রাইভার',
  titleEn: 'Pickup driver',
  descriptionBn: 'নিয়োগ: পিকআপ ড্রাইভার।',
  descriptionEn: 'We are hiring: Pickup driver.',
  occupation: { id: 'o1', nameBn: 'ড্রাইভার', nameEn: 'Driver' },
  jobType: 'FULL_TIME',
  shift: 'DAY',
  openings: 2,
  wageMin: 16000,
  wageMax: 20000,
  wagePeriod: 'MONTH',
  hours: '08:00-18:00',
  areaName: 'Tongi',
  skills: [],
  requiredDocuments: [],
  benefits: [],
  questions: ['Is there a weekly holiday?'],
  findings: [{ kind: 'QUESTION', code: 'long-day', quote: '08:00-18:00' }],
  blocked: false,
}

describe('the job-post assistant', () => {
  it('fills the form from a draft and keeps what the draft does not know', () => {
    const form = draftToForm(karim, { ...emptyJobForm, latitude: 23.89, longitude: 90.4 })
    expect(form.titleBn).toBe('পিকআপ ড্রাইভার')
    expect(form.openings).toBe('2')
    expect(form.wageMin).toBe('16000')
    expect(form.wageMax).toBe('20000')
    expect(form.startTime).toBe('08:00')
    expect(form.endTime).toBe('18:00')
    expect(form.areaName).toBe('Tongi')
    expect(form.latitude).toBe(23.89) // the map pin is never guessed
    expect(form.occupation?.id).toBe('o1')
  })

  it('explains each finding in plain words, blocks first', async () => {
    await i18n.changeLanguage('en')
    render(
      <FindingList
        findings={[
          { kind: 'QUESTION', code: 'long-day', quote: '08:00-18:00' },
          { kind: 'BLOCK', code: 'money-request', quote: 'registration fee' },
        ]}
      />,
    )
    const items = screen.getAllByRole('listitem')
    expect(items[0]).toHaveTextContent('Blocked: asks job seekers for money')
    expect(items[0]).toHaveTextContent('“registration fee”')
    expect(items[1]).toHaveTextContent('Question: more than 8 working hours a day')
  })
})
