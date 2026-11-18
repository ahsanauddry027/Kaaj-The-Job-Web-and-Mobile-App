import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import i18n from '../../i18n'
import type { EmployerProfile, VerificationStatus } from '../../lib/types'
import { VerificationPanel } from './VerificationPanel'

function profile(status: VerificationStatus, overrides: Partial<EmployerProfile> = {}): EmployerProfile {
  return {
    id: '1',
    name: 'Karim Transport',
    kind: 'SME',
    address: 'Tongi',
    latitude: null,
    longitude: null,
    contactPerson: 'Karim',
    contactPhone: '01712345678',
    contactEmail: null,
    hasDocument: status !== 'UNVERIFIED',
    documentName: status !== 'UNVERIFIED' ? 'licence.pdf' : null,
    documentType: status !== 'UNVERIFIED' ? 'TRADE_LICENCE' : null,
    status,
    verificationNote: null,
    submittedAt: status !== 'UNVERIFIED' ? '2026-11-05T09:00:00Z' : null,
    verifiedAt: status === 'VERIFIED' ? '2026-11-06T09:00:00Z' : null,
    createdAt: '2026-11-05T08:00:00Z',
    canPublishJobs: status === 'VERIFIED',
    missing: status === 'UNVERIFIED' ? ['document'] : [],
    ...overrides,
  }
}

describe('verification status banner', () => {
  it.each<VerificationStatus>(['UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED'])('explains the %s state', async (status) => {
    await i18n.changeLanguage('en')
    render(<VerificationPanel profile={profile(status)} />)
    expect(screen.getByTestId('verification-banner')).toHaveTextContent(i18n.t(`verification.banner.${status}`))
  })

  it('shows the rejection reason and lets the employer send it again', async () => {
    await i18n.changeLanguage('en')
    render(<VerificationPanel profile={profile('REJECTED', { verificationNote: 'The licence is not readable' })} onResubmit={() => {}} />)
    expect(screen.getByText('The licence is not readable')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Send for review again' })).toBeInTheDocument()
  })

  it('lists what is still missing, in Bangla by default', async () => {
    await i18n.changeLanguage('bn')
    render(<VerificationPanel profile={profile('UNVERIFIED')} />)
    expect(screen.getByText('যাচাইয়ের নথি')).toBeInTheDocument()
    expect(screen.getByTestId('verification-banner')).toHaveTextContent('যাচাই হয়নি')
  })
})
