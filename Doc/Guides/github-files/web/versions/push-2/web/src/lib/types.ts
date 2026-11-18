// The JSON shapes of the Kaaj API (Spring Boot records in app.kaaj.*). Keep them in step with the backend;
// later the team can generate them from /v3/api-docs with openapi-typescript.

export type Role = 'SEEKER' | 'EMPLOYER' | 'ADMIN'

export interface Me {
  id: string
  email: string
  role: Role
  locale: string
}

export interface Term {
  id: string
  nameBn: string
  nameEn: string
}

export interface CursorPage<T> {
  items: T[]
  nextCursor: string | null
}

// ---- Feature 3: employer profile and verification

export type EmployerKind = 'HOUSEHOLD' | 'SME' | 'COMPANY' | 'AGENCY' | 'NGO'
export const EMPLOYER_KINDS: EmployerKind[] = ['HOUSEHOLD', 'SME', 'COMPANY', 'AGENCY', 'NGO']

export type VerificationStatus = 'UNVERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED'
export type DocumentType = 'TRADE_LICENCE' | 'NATIONAL_ID'

export interface EmployerProfileInput {
  name: string
  kind: EmployerKind
  address: string
  latitude: number | null
  longitude: number | null
  contactPerson: string
  contactPhone: string
  contactEmail: string | null
}

export interface EmployerProfile extends EmployerProfileInput {
  id: string
  hasDocument: boolean
  documentName: string | null
  documentType: DocumentType | null
  status: VerificationStatus
  verificationNote: string | null
  submittedAt: string | null
  verifiedAt: string | null
  createdAt: string
  canPublishJobs: boolean
  missing: string[]
}

export interface AdminEmployer extends EmployerProfileInput {
  id: string
  ownerEmail: string | null
  documentName: string | null
  documentType: DocumentType | null
  documentUrl: string | null
  status: VerificationStatus
  verificationNote: string | null
  submittedAt: string | null
  verifiedAt: string | null
  createdAt: string
}

// ---- Feature 4: post and manage jobs

export type JobStatus = 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'PAUSED' | 'CLOSED' | 'BLOCKED'
export type JobType = 'FULL_TIME' | 'PART_TIME' | 'DAILY'
export type Shift = 'DAY' | 'NIGHT' | 'ROTATING' | 'FLEXIBLE'
export type WagePeriod = 'HOUR' | 'DAY' | 'WEEK' | 'MONTH' | 'PIECE'
export type JobAction = 'SUBMIT' | 'PAUSE' | 'RESUME' | 'CLOSE' | 'REPOST'
export type AllowedAction = JobAction | 'EDIT' | 'DELETE'

export const JOB_TYPES: JobType[] = ['FULL_TIME', 'PART_TIME', 'DAILY']
export const SHIFTS: Shift[] = ['DAY', 'NIGHT', 'ROTATING', 'FLEXIBLE']
export const WAGE_PERIODS: WagePeriod[] = ['MONTH', 'DAY', 'WEEK', 'HOUR', 'PIECE']
export const DRIVING_CODES = [
  'DRIVING_MOTORCYCLE',
  'DRIVING_LIGHT',
  'DRIVING_PRO_LIGHT',
  'DRIVING_PRO_MEDIUM',
  'DRIVING_PRO_HEAVY',
] as const

export interface JobInput {
  titleBn: string | null
  titleEn: string | null
  descriptionBn: string | null
  descriptionEn: string | null
  occupationId: string | null
  jobType: JobType
  shift: Shift
  hours: string | null
  openings: number | null
  wageMin: number | null
  wageMax: number | null
  wagePeriod: WagePeriod | null
  latitude: number | null
  longitude: number | null
  areaName: string | null
  remote: boolean
  minExperienceYears: number | null
  skillIds: string[]
  requiredDocuments: string[]
}

export interface EmployerBadge {
  id: string
  name: string
  verified: boolean
}

export interface Job {
  id: string
  status: JobStatus
  statusReason: string | null
  awaitingVerification: boolean
  titleBn: string
  titleEn: string
  descriptionBn: string | null
  descriptionEn: string | null
  occupation: Term | null
  jobType: JobType
  shift: Shift
  hours: string | null
  openings: number
  wageMin: number | null
  wageMax: number | null
  wagePeriod: WagePeriod | null
  wageMonthlyMax: number | null
  latitude: number | null
  longitude: number | null
  areaName: string | null
  remote: boolean
  minExperienceYears: number | null
  skills: Term[]
  requiredDocuments: string[]
  publishedAt: string | null
  expiresAt: string | null
  closedAt: string | null
  createdAt: string
  updatedAt: string
  repostedFrom: string | null
  employer: EmployerBadge | null
  allowedActions: AllowedAction[]
}

export type PublicJob = Omit<
  Job,
  'status' | 'statusReason' | 'awaitingVerification' | 'wageMonthlyMax' | 'closedAt' | 'createdAt' | 'updatedAt' | 'repostedFrom' | 'allowedActions'
>

export type JobCounts = Record<JobStatus, number>

// ---- Rule checks (shared by the job-post assistant and scam screening)

export type FindingKind = 'BLOCK' | 'REVIEW' | 'QUESTION' | 'NUDGE' | 'REMOVED'

/** One result of the server's JobRuleChecks: what happens, a stable code, and the words it is about. */
export interface Finding {
  kind: FindingKind
  code: string
  quote: string | null
}

// ---- Module 2: scam screening and moderation

export type ReportReason = 'ASKED_FOR_MONEY' | 'FAKE_JOB' | 'HARASSMENT' | 'OTHER'
export const REPORT_REASONS: ReportReason[] = ['ASKED_FOR_MONEY', 'FAKE_JOB', 'HARASSMENT', 'OTHER']

export type CaseStatus = 'OPEN' | 'REMOVED' | 'DISMISSED'
export type CaseDecision = 'REMOVE_POST' | 'SUSPEND_EMPLOYER' | 'DISMISS'

/** GET /employer/jobs/{id}/screening: why the employer's own post is in review or blocked. */
export interface ScreeningView {
  caseStatus: CaseStatus | null
  screening: 'REVIEW' | 'BLOCK' | null
  findings: Finding[]
  decision: CaseDecision | null
  note: string | null
  reported: boolean
}

export interface ScamAssessment {
  risk: 'LOW' | 'MEDIUM' | 'HIGH'
  asksApplicantsForMoney: boolean | null
  movesContactOffPlatform: boolean | null
  overseasJob: boolean | null
  licenceNumber: string | null
  redFlags: string[] | null
  reasonForModerator: string | null
}

export interface CaseSummary {
  id: string
  subjectType: 'JOB' | 'MESSAGE' | 'EMPLOYER'
  subjectId: string
  source: 'SCREENING' | 'REPORT'
  screening: 'REVIEW' | 'BLOCK' | null
  reportCount: number
  status: CaseStatus
  decision: CaseDecision | null
  jobTitleBn: string | null
  jobTitleEn: string | null
  employerName: string | null
  jobStatus: JobStatus | null
  codes: string[]
  createdAt: string
  decidedAt: string | null
}

export interface CaseDetail {
  id: string
  subjectType: 'JOB' | 'MESSAGE' | 'EMPLOYER'
  subjectId: string
  source: 'SCREENING' | 'REPORT'
  screening: 'REVIEW' | 'BLOCK' | null
  reportCount: number
  status: CaseStatus
  decision: CaseDecision | null
  note: string | null
  createdAt: string
  decidedAt: string | null
  findings: Finding[]
  aiAssessment: ScamAssessment | null
  job: {
    id: string
    titleBn: string | null
    titleEn: string | null
    descriptionBn: string | null
    descriptionEn: string | null
    wageMin: number | null
    wageMax: number | null
    wagePeriod: WagePeriod | null
    hours: string | null
    areaName: string | null
    status: JobStatus
    statusReason: string | null
  } | null
  employer: { id: string; name: string; verified: boolean; ownerEmail: string | null; ownerActive: boolean } | null
  reports: Array<{ reason: ReportReason; details: string | null; createdAt: string }>
}
