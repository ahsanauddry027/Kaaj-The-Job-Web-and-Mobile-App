// Scratch mock of the Kaaj API (same JSON shapes as the Spring Boot code) so the React app can be
// exercised and screenshotted here, where Maven can't download Spring Boot. Not part of the delivery.
import http from 'node:http'
import { randomUUID } from 'node:crypto'
import fs from 'node:fs'

const now = () => new Date().toISOString()
const days = (n) => new Date(Date.now() + n * 86400000).toISOString()

const users = {
  'karim@demo.kaaj': { id: 'u-karim', email: 'karim@demo.kaaj', role: 'EMPLOYER', locale: 'bn' },
  'tongigarments@demo.kaaj': { id: 'u-tongi', email: 'tongigarments@demo.kaaj', role: 'EMPLOYER', locale: 'bn' },
  'admin@kaaj.local': { id: 'u-admin', email: 'admin@kaaj.local', role: 'ADMIN', locale: 'bn' },
  'rahim@demo.kaaj': { id: 'u-rahim', email: 'rahim@demo.kaaj', role: 'SEEKER', locale: 'bn' },
  'quickjobs@demo.kaaj': { id: 'u-quick', email: 'quickjobs@demo.kaaj', role: 'EMPLOYER', locale: 'en' },
}
const sessions = new Map()

const occupations = [
  { id: 'o1', nameBn: 'ড্রাইভার (কার, পিকআপ, মাইক্রোবাস)', nameEn: 'Driver (car, pickup, microbus)' },
  { id: 'o2', nameBn: 'নিরাপত্তা প্রহরী', nameEn: 'Security guard' },
  { id: 'o3', nameBn: 'লোডার', nameEn: 'Loader' },
  { id: 'o4', nameBn: 'সেলাই মেশিন অপারেটর', nameEn: 'Sewing machine operator' },
]
const skills = [
  { id: 's1', nameBn: 'হালকা যানবাহন চালানো', nameEn: 'Light vehicle driving' },
  { id: 's2', nameBn: 'ঢাকার রাস্তা চেনা', nameEn: 'Knowing Dhaka roads' },
  { id: 's3', nameBn: 'নিরাপত্তা টহল', nameEn: 'Security patrol' },
  { id: 's4', nameBn: 'গাড়ির রক্ষণাবেক্ষণ', nameEn: 'Vehicle maintenance' },
]

const employers = {
  'u-karim': {
    id: 'e-karim', ownerId: 'u-karim', name: 'করিম ট্রান্সপোর্ট', kind: 'SME', address: 'স্টেশন রোড, টঙ্গী, গাজীপুর',
    latitude: 23.8916, longitude: 90.4023, contactPerson: 'করিম উদ্দিন', contactPhone: '01712345678',
    contactEmail: 'info@karimtransport.example', documentKey: 'document-karim.png', documentName: 'trade-licence.png',
    documentType: 'TRADE_LICENCE', status: 'VERIFIED', verificationNote: 'সব তথ্য সঠিক আছে। ধন্যবাদ!',
    submittedAt: days(-14), verifiedAt: days(-13), createdAt: days(-15),
  },
  'u-tongi': {
    id: 'e-tongi', ownerId: 'u-tongi', name: 'Tongi Garments Ltd.', kind: 'COMPANY', address: 'Cherag Ali, Tongi, Gazipur',
    latitude: 23.905, longitude: 90.4, contactPerson: 'Nasrin Akter', contactPhone: '01812345678', contactEmail: null,
    documentKey: 'document-tongi.png', documentName: 'tongi-licence.png', documentType: 'TRADE_LICENCE', status: 'PENDING',
    verificationNote: null, submittedAt: days(-1), verifiedAt: null, createdAt: days(-2),
  },
  'u-quick': {
    id: 'e3', ownerId: 'u-quick', name: 'Quick Jobs & Overseas', kind: 'SME', address: 'Sector 7, Uttara, Dhaka',
    latitude: 23.869, longitude: 90.399, contactPerson: 'Rafiq', contactPhone: '01912345678', contactEmail: null,
    documentKey: 'document-quick.png', documentName: 'licence.png', documentType: 'TRADE_LICENCE', status: 'VERIFIED',
    verificationNote: null, submittedAt: days(-10), verifiedAt: days(-9), createdAt: days(-11),
  },
}

function job(o) {
  return {
    id: randomUUID(), employerOwner: 'u-karim', status: 'DRAFT', statusReason: null, titleBn: '', titleEn: '',
    descriptionBn: null, descriptionEn: null, occupation: null, jobType: 'FULL_TIME', shift: 'DAY', hours: null,
    openings: 1, wageMin: null, wageMax: null, wagePeriod: null, latitude: null, longitude: null, areaName: null,
    remote: false, minExperienceYears: null, skills: [], requiredDocuments: [], publishedAt: null, expiresAt: null,
    closedAt: null, createdAt: now(), updatedAt: now(), repostedFrom: null, ...o,
  }
}
const jobs = [
  job({ titleBn: 'পিকআপ ড্রাইভার', titleEn: 'Pickup driver', status: 'PUBLISHED', occupation: occupations[0],
    descriptionBn: 'টঙ্গী ও গাজীপুরে ডেলিভারি রুটে পিকআপ চালাতে হবে। সপ্তাহে একদিন ছুটি।', hours: '08:00-18:00', openings: 2,
    wageMin: 16000, wageMax: 20000, wagePeriod: 'MONTH', latitude: 23.8916, longitude: 90.4023, areaName: 'টঙ্গী, গাজীপুর',
    minExperienceYears: 1, skills: [skills[0], skills[1]], requiredDocuments: ['DRIVING_PRO_LIGHT'],
    publishedAt: days(-3), expiresAt: days(27), createdAt: days(-3) }),
  job({ titleBn: 'নিরাপত্তা প্রহরী', titleEn: 'Night security guard', status: 'PAUSED', occupation: occupations[1], shift: 'NIGHT',
    descriptionBn: 'গুদামের রাতের পাহারা।', hours: '20:00-06:00', wageMin: 12000, wageMax: 15000, wagePeriod: 'MONTH',
    latitude: 23.895, longitude: 90.41, areaName: 'টঙ্গী, গাজীপুর', minExperienceYears: 1, skills: [skills[2]],
    publishedAt: days(-20), expiresAt: days(10), createdAt: days(-20) }),
  job({ titleBn: 'মাইক্রোবাস ড্রাইভার', titleEn: 'Microbus driver', status: 'CLOSED', statusReason: 'EXPIRED', occupation: occupations[0],
    descriptionBn: 'অফিস স্টাফ আনা-নেওয়া।', hours: '07:00-17:00', wageMin: 18000, wageMax: 22000, wagePeriod: 'MONTH',
    latitude: 23.874, longitude: 90.398, areaName: 'উত্তরা, ঢাকা', minExperienceYears: 2, publishedAt: days(-45),
    expiresAt: days(-15), closedAt: days(-15), createdAt: days(-45) }),
  job({ titleBn: 'লোডার', titleEn: 'Loader', status: 'DRAFT', occupation: occupations[2], jobType: 'DAILY', openings: 3,
    wageMin: 600, wageMax: 800, wagePeriod: 'DAY', createdAt: days(-1) }),
]

const allowed = { DRAFT: ['EDIT', 'SUBMIT', 'DELETE'], PUBLISHED: ['EDIT', 'PAUSE', 'CLOSE'], PAUSED: ['EDIT', 'RESUME', 'CLOSE'], CLOSED: ['REPOST'], IN_REVIEW: [], BLOCKED: [] }
const missing = (e) => ['name', 'kind', 'address', 'contactPerson', 'contactPhone'].filter((f) => !e[f]).concat(e.documentKey ? [] : ['document'])
const profileView = (e) => ({ ...e, hasDocument: !!e.documentKey, canPublishJobs: e.status === 'VERIFIED', missing: missing(e) })
const adminView = (e) => ({ ...e, ownerEmail: Object.values(users).find((u) => u.id === e.ownerId)?.email, documentUrl: e.documentKey ? `/api/v1/files/${e.documentKey}` : null })
const jobView = (j) => {
  const e = employers[j.employerOwner]
  return { ...j, awaitingVerification: j.statusReason === 'AWAITING_VERIFICATION', wageMonthlyMax: j.wageMax, employer: { id: e.id, name: e.name, verified: e.status === 'VERIFIED' }, allowedActions: allowed[j.status] }
}

// ---- Scam screening and moderation (Module 2) and the job-post assistant (Module 3)
const quickJob = (o) => job({ employerOwner: 'u-quick', occupation: occupations[2], jobType: 'FULL_TIME', shift: 'DAY', openings: 4, wagePeriod: 'MONTH', areaName: 'উত্তরা, ঢাকা', latitude: 23.869, longitude: 90.399, createdAt: days(-2), ...o })
jobs.push(
  quickJob({ id: 'b1', titleBn: 'মালয়েশিয়ায় কারখানার চাকরি', titleEn: 'Malaysia factory job', status: 'BLOCKED', statusReason: 'SCREENING_BLOCKED', wageMin: 45000, wageMax: 60000, hours: '08:00-17:00', openings: 20,
    descriptionBn: 'মালয়েশিয়ায় কারখানার কাজ। প্রসেসিং ফি ৫০,০০০ টাকা বিকাশে পাঠান। ৩০ দিনে ভিসা।', descriptionEn: 'Malaysia factory job. Processing fee Tk 50,000 by bKash. Visa in 30 days.' }),
  quickJob({ id: 'b2', titleBn: 'অনলাইন ডেটা এন্ট্রি', titleEn: 'Online data entry', status: 'IN_REVIEW', statusReason: 'SCREENING_REVIEW', wageMin: 2000, wageMax: 2000, wagePeriod: 'DAY', hours: '10:00-14:00', remote: true, openings: 50,
    descriptionBn: 'ঘরে বসে আয় করুন। দৈনিক ২০০০ টাকা। যোগাযোগ হোয়াটসঅ্যাপে।', descriptionEn: 'Work from home and earn Tk 2,000 daily. Contact us on WhatsApp.' }),
  quickJob({ id: 'b3', titleBn: 'গুদাম সহকারী', titleEn: 'Warehouse helper', status: 'PUBLISHED', wageMin: 11000, wageMax: 13000, hours: '09:00-17:00', publishedAt: days(-5), expiresAt: days(25),
    descriptionBn: 'উত্তরার গুদামে মালামাল ওঠানো-নামানো ও প্যাকিং।', descriptionEn: 'Loading, unloading and packing at our Uttara warehouse.' }),
  job({ id: 'k5', titleBn: 'ডেলিভারি হেল্পার', titleEn: 'Delivery helper', status: 'IN_REVIEW', statusReason: 'SCREENING_REVIEW', occupation: occupations[2], wageMin: 12000, wageMax: 14000, wagePeriod: 'MONTH', hours: '09:00-18:00', openings: 2, areaName: 'টঙ্গী, গাজীপুর', latitude: 23.89, longitude: 90.40, createdAt: days(0),
    descriptionBn: 'পিকআপে মালামাল ওঠানো-নামানো। বিস্তারিত জানতে ফোন করুন ০১৭১২-৩৪৫৬৭৮।', descriptionEn: 'Load and unload goods on our pickup. Call 01712-345678 for details.' }),
)
const cases = [
  { id: 'c3', subjectId: 'b3', source: 'REPORT', screening: null, reportCount: 2, status: 'OPEN', decision: null, note: null, createdAt: days(-3), decidedAt: null, findings: [], aiAssessment: null,
    reports: [{ reason: 'ASKED_FOR_MONEY', details: 'At the interview they asked for Tk 1,500 for the uniform and the ID card.', createdAt: days(-3) }, { reason: 'FAKE_JOB', details: 'The phone number on the gate is switched off and there is no warehouse at this address.', createdAt: days(-1) }] },
  { id: 'c1', subjectId: 'b1', source: 'SCREENING', screening: 'BLOCK', reportCount: 0, status: 'OPEN', decision: null, note: null, createdAt: days(-2), decidedAt: null, reports: [], aiAssessment: null,
    findings: [{ kind: 'BLOCK', code: 'money-request', quote: 'প্রসেসিং ফি' }, { kind: 'BLOCK', code: 'money-request', quote: 'Processing fee' }, { kind: 'BLOCK', code: 'money-request', quote: 'fee Tk 50,000' }, { kind: 'REVIEW', code: 'overseas-no-licence', quote: 'মালয়েশিয়া' }] },
  { id: 'c2', subjectId: 'b2', source: 'SCREENING', screening: 'REVIEW', reportCount: 0, status: 'OPEN', decision: null, note: null, createdAt: days(-1), decidedAt: null, reports: [],
    aiAssessment: null,
    findings: [{ kind: 'REVIEW', code: 'contact-details', quote: 'হোয়াটসঅ্যাপ' }, { kind: 'REVIEW', code: 'contact-details', quote: 'WhatsApp' }, { kind: 'REVIEW', code: 'easy-money', quote: 'ঘরে বসে' }, { kind: 'REVIEW', code: 'easy-money', quote: 'Work from home' }] },
  { id: 'c4', subjectId: 'k5', source: 'SCREENING', screening: 'REVIEW', reportCount: 0, status: 'OPEN', decision: null, note: null, createdAt: days(0), decidedAt: null, reports: [], aiAssessment: null,
    findings: [{ kind: 'REVIEW', code: 'contact-details', quote: '01712-345678' }, { kind: 'QUESTION', code: 'long-day', quote: '09:00-18:00' }] },
]
const suspended = new Set()
const employerOf = (j) => j.employerOwner === 'u-quick' ? { id: 'e3', name: 'Quick Jobs & Overseas', verified: true, ownerEmail: 'quickjobs@demo.kaaj', ownerActive: !suspended.has('u-quick') }
  : { id: 'e1', name: 'করিম ট্রান্সপোর্ট', verified: true, ownerEmail: 'karim@demo.kaaj', ownerActive: !suspended.has('u-karim') }
function caseSummary(c) {
  const j = jobs.find((x) => x.id === c.subjectId)
  return { id: c.id, subjectType: 'JOB', subjectId: c.subjectId, source: c.source, screening: c.screening, reportCount: c.reportCount, status: c.status, decision: c.decision,
    jobTitleBn: j.titleBn, jobTitleEn: j.titleEn, employerName: employerOf(j).name, jobStatus: j.status,
    codes: [...new Set(c.findings.filter((f) => f.kind === 'BLOCK' || f.kind === 'REVIEW').map((f) => f.code))], createdAt: c.createdAt, decidedAt: c.decidedAt }
}
function caseDetail(c) {
  const j = jobs.find((x) => x.id === c.subjectId)
  return { ...caseSummary(c), note: c.note, findings: c.findings, aiAssessment: c.aiAssessment, reports: c.reports, employer: employerOf(j),
    job: { id: j.id, titleBn: j.titleBn, titleEn: j.titleEn, descriptionBn: j.descriptionBn, descriptionEn: j.descriptionEn, wageMin: j.wageMin, wageMax: j.wageMax, wagePeriod: j.wagePeriod, hours: j.hours, areaName: j.areaName, status: j.status, statusReason: j.statusReason } }
}
const KARIM_DRAFT = {
  source: 'TEMPLATE', titleBn: 'পিকআপ ড্রাইভার', titleEn: 'Pickup driver',
  descriptionBn: 'নিয়োগ: পিকআপ ড্রাইভার।\nপদ: ২ জন।\nকাজের জায়গা: টঙ্গী।\nবেতন: মাসে ৳১৬,০০০–২০,০০০।\nকাজের সময়: ০৮:০০–১৮:০০।',
  descriptionEn: 'We are hiring: Pickup driver.\nOpenings: 2.\nPlace of work: Tongi.\nWage: Tk 16,000–20,000 a month.\nWorking hours: 08:00–18:00.',
  occupation: occupations[0], jobType: 'FULL_TIME', shift: 'DAY', openings: 2, wageMin: 16000, wageMax: 20000, wagePeriod: 'MONTH', hours: '08:00-18:00', areaName: 'Tongi',
  skills: [], requiredDocuments: [], benefits: [], blocked: false,
  findings: [{ kind: 'QUESTION', code: 'long-day', quote: '08:00-18:00' }],
}
const QUESTIONS = {
  bn: ['দিনটি ৮ ঘণ্টার বেশি। ওভারটাইম কীভাবে দেওয়া হবে?', 'সাপ্তাহিক ছুটি আছে কি? কোন দিন?', 'কোন ড্রাইভিং লাইসেন্স লাগবে: হালকা, মাঝারি না ভারী গাড়ির?', 'প্রতিদিন কী কী কাজ করতে হবে? বিবরণে কাজগুলো লিখুন।'],
  en: ['The day is longer than 8 hours. How is overtime paid?', 'Is there a weekly holiday? Which day?', 'Which driving licence is needed: light, medium or heavy vehicles?', 'What will the person do each day? Add the duties to the description.'],
}

function send(res, status, body, headers = {}) {
  if (status === 204) { res.writeHead(204, headers); return res.end() }
  res.writeHead(status, { 'Content-Type': status >= 400 ? 'application/problem+json' : 'application/json', ...headers })
  res.end(JSON.stringify(body))
}
const problem = (res, status, detail, errors) => send(res, status, { status, title: 'Error', detail, code: 'mock', errors })

async function readBody(req) {
  const chunks = []
  for await (const c of req) chunks.push(c)
  const raw = Buffer.concat(chunks)
  const type = req.headers['content-type'] ?? ''
  if (type.includes('json')) return JSON.parse(raw.toString() || '{}')
  if (type.includes('multipart')) {
    const text = raw.toString('latin1')
    const name = /filename="([^"]+)"/.exec(text)?.[1]
    const docType = /name="documentType"\r\n\r\n(\w+)/.exec(text)?.[1]
    return { fileName: name, documentType: docType }
  }
  return {}
}

const fields = (input, j) => {
  const occ = occupations.find((o) => o.id === input.occupationId) ?? null
  Object.assign(j, {
    titleBn: input.titleBn ?? input.titleEn, titleEn: input.titleEn ?? input.titleBn, descriptionBn: input.descriptionBn ?? input.descriptionEn,
    descriptionEn: input.descriptionEn ?? input.descriptionBn, occupation: occ, jobType: input.jobType, shift: input.shift, hours: input.hours,
    openings: input.openings ?? 1, wageMin: input.wageMin, wageMax: input.wageMax, wagePeriod: input.wagePeriod, latitude: input.latitude,
    longitude: input.longitude, areaName: input.areaName, remote: input.remote, minExperienceYears: input.minExperienceYears,
    skills: skills.filter((s) => input.skillIds.includes(s.id)), requiredDocuments: input.requiredDocuments, updatedAt: now(),
  })
}
function submit(j, owner) {
  const errors = {}
  if (!j.wageMin) errors.wageMin = 'বেতন অবশ্যই দিতে হবে। কাজ সবসময় বেতন দেখায়।'
  if (Object.keys(errors).length) return errors
  if (employers[owner].status !== 'VERIFIED') { j.statusReason = 'AWAITING_VERIFICATION'; return null }
  j.status = 'PUBLISHED'; j.statusReason = null; j.publishedAt ??= now(); j.expiresAt ??= days(30)
  return null
}

http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://x')
  const path = url.pathname.replace('/api/v1', '')
  const sid = /SESSION=([\w-]+)/.exec(req.headers.cookie ?? '')?.[1]
  const me = sid ? sessions.get(sid) : null
  const m = (re) => re.exec(path)
  try {
    if (path === '/auth/csrf') return send(res, 204, null, { 'Set-Cookie': 'XSRF-TOKEN=mock; Path=/' })
    if (path === '/auth/login' && req.method === 'POST') {
      const body = await readBody(req); const u = users[body.email]
      if (!u) return problem(res, 401, 'ইমেইল বা পাসওয়ার্ড ভুল।')
      const id = randomUUID(); sessions.set(id, u)
      return send(res, 200, u, { 'Set-Cookie': `SESSION=${id}; Path=/; HttpOnly` })
    }
    if (path === '/auth/logout') return send(res, 204, null, { 'Set-Cookie': 'SESSION=; Path=/; Max-Age=0' })
    if (path === '/me') return me ? send(res, 200, me) : problem(res, 401, 'Please sign in.')
    if (path.startsWith('/taxonomy/')) {
      const list = path.endsWith('occupations') ? occupations : skills
      const q = (url.searchParams.get('q') ?? '').toLowerCase()
      return send(res, 200, list.filter((t) => !q || t.nameEn.toLowerCase().includes(q) || t.nameBn.includes(q)))
    }
    if (m(/^\/files\/(.+)$/)) {
      return res.writeHead(200, { 'Content-Type': 'image/png' }), res.end(fs.readFileSync(process.env.SAMPLE_DOC))
    }
    if (m(/^\/jobs\/([\w-]+)$/)) {
      const j = jobs.find((x) => x.id === m(/^\/jobs\/([\w-]+)$/)[1] && x.status === 'PUBLISHED')
      return j ? send(res, 200, jobView(j)) : problem(res, 404, 'Not found')
    }
    if (!me) return problem(res, 401, 'Please sign in.')

    if (path === '/employer/profile') {
      if (req.method === 'PUT') {
        const body = await readBody(req)
        employers[me.id] = { ...(employers[me.id] ?? { id: randomUUID(), ownerId: me.id, status: 'UNVERIFIED', createdAt: now() }), ...body }
      }
      return employers[me.id] ? send(res, 200, profileView(employers[me.id])) : problem(res, 404, 'Create your business profile first.')
    }
    if (path === '/employer/profile/document') {
      const body = await readBody(req); const e = employers[me.id]
      Object.assign(e, { documentKey: 'document-' + randomUUID() + '.png', documentName: body.fileName, documentType: body.documentType, status: 'PENDING', submittedAt: now(), verificationNote: null })
      return send(res, 200, profileView(e))
    }
    if (path === '/employer/jobs/drafts' && req.method === 'POST') {
      const { text } = await readBody(req)
      if (!text || text.trim().length < 3) return problem(res, 400, 'Some fields need fixing.', { text: 'Describe the job in one sentence (3 to 500 characters).' })
      const lang = (req.headers['accept-language'] ?? 'bn').startsWith('en') ? 'en' : 'bn'
      await new Promise((r) => setTimeout(r, 400))
      if (/fee|ফি/i.test(text)) {
        return send(res, 200, { ...KARIM_DRAFT, titleBn: 'নিরাপত্তা প্রহরী', titleEn: 'Security guard', occupation: occupations[1], openings: null, wageMin: 12000, wageMax: 12000, hours: null,
          descriptionBn: 'নিয়োগ: নিরাপত্তা প্রহরী।\nকাজের জায়গা: টঙ্গী।\nবেতন: মাসে ৳১২,০০০।', descriptionEn: 'We are hiring: Security guard.\nPlace of work: Tongi.\nWage: Tk 12,000 a month.',
          blocked: true, questions: QUESTIONS[lang].slice(1, 2),
          findings: [{ kind: 'BLOCK', code: 'money-request', quote: 'registration fee' }, { kind: 'REMOVED', code: 'contact-details', quote: '01712345678' }] })
      }
      return send(res, 200, { ...KARIM_DRAFT, questions: QUESTIONS[lang] })
    }
    const sm = m(/^\/employer\/jobs\/([\w-]+)\/screening$/)
    if (sm) {
      const c = cases.find((x) => x.subjectId === sm[1])
      return send(res, 200, c ? { caseStatus: c.status, screening: c.screening, findings: c.findings, decision: c.decision, note: c.status === 'REMOVED' ? c.note : null, reported: c.reportCount > 0 }
        : { caseStatus: null, screening: null, findings: [], decision: null, note: null, reported: false })
    }
    if (path === '/reports' && req.method === 'POST') {
      const body = await readBody(req)
      const c = cases.find((x) => x.subjectId === body.subjectId && x.status === 'OPEN')
      if (c) { c.reportCount++; c.reports.unshift({ reason: body.reason, details: body.details, createdAt: now() }) }
      return send(res, 201, { received: true })
    }
    if (path === '/admin/cases/counts') {
      const c = { OPEN: 0, REMOVED: 0, DISMISSED: 0 }; cases.forEach((x) => c[x.status]++)
      return send(res, 200, c)
    }
    if (path === '/admin/cases') {
      const status = url.searchParams.get('status') ?? 'OPEN'
      const items = cases.filter((x) => x.status === status).sort((a, b) => b.reportCount - a.reportCount || a.createdAt.localeCompare(b.createdAt))
      return send(res, 200, { items: items.map(caseSummary), nextCursor: null })
    }
    const cm = m(/^\/admin\/cases\/([\w-]+)(\/decision)?$/)
    if (cm) {
      const c = cases.find((x) => x.id === cm[1]); if (!c) return problem(res, 404, 'Case not found.')
      if (cm[2]) {
        const body = await readBody(req); const j = jobs.find((x) => x.id === c.subjectId)
        if (body.decision !== 'DISMISS' && (!body.note || body.note.trim().length < 3)) return problem(res, 400, 'Some fields need fixing.', { note: 'Write a short note.' })
        c.decision = body.decision; c.note = body.note; c.decidedAt = now(); c.status = body.decision === 'DISMISS' ? 'DISMISSED' : 'REMOVED'
        if (body.decision === 'DISMISS') { j.status = 'PUBLISHED'; j.statusReason = null } else { j.status = 'BLOCKED'; j.statusReason = 'REMOVED' }
        if (body.decision === 'SUSPEND_EMPLOYER') suspended.add(j.employerOwner)
      }
      return send(res, 200, caseDetail(c))
    }
    if (path === '/employer/jobs/summary') {
      const c = { DRAFT: 0, IN_REVIEW: 0, PUBLISHED: 0, PAUSED: 0, CLOSED: 0, BLOCKED: 0 }
      jobs.filter((j) => j.employerOwner === me.id).forEach((j) => c[j.status]++)
      return send(res, 200, c)
    }
    if (path === '/employer/jobs') {
      if (req.method === 'POST') {
        const input = await readBody(req); const j = job({ employerOwner: me.id }); fields(input, j); jobs.unshift(j)
        if (url.searchParams.get('submit') === 'true') { const errs = submit(j, me.id); if (errs) { jobs.shift(); return problem(res, 400, 'Some fields need fixing.', errs) } }
        return send(res, 201, jobView(j))
      }
      const status = url.searchParams.get('status'); const q = (url.searchParams.get('q') ?? '').toLowerCase()
      const items = jobs.filter((j) => j.employerOwner === me.id && (!status || j.status === status) && (!q || j.titleEn.toLowerCase().includes(q) || j.titleBn.includes(q)))
      return send(res, 200, { items: items.map(jobView), nextCursor: null })
    }
    const jm = m(/^\/employer\/jobs\/([\w-]+)$/)
    if (jm) {
      const j = jobs.find((x) => x.id === jm[1] && x.employerOwner === me.id)
      if (!j) return problem(res, 404, 'Job post not found.')
      if (req.method === 'PUT') { fields(await readBody(req), j); if (url.searchParams.get('submit') === 'true') submit(j, me.id) }
      if (req.method === 'DELETE') { jobs.splice(jobs.indexOf(j), 1); return send(res, 204) }
      if (req.method === 'PATCH') {
        const { action } = await readBody(req)
        if (action === 'PAUSE') j.status = 'PAUSED'
        if (action === 'RESUME') j.status = 'PUBLISHED'
        if (action === 'CLOSE') { j.status = 'CLOSED'; j.closedAt = now(); j.statusReason = 'CLOSED_BY_EMPLOYER' }
        if (action === 'SUBMIT') submit(j, me.id)
        if (action === 'REPOST') { const copy = job({ ...j, id: randomUUID(), status: 'DRAFT', statusReason: null, publishedAt: null, expiresAt: null, closedAt: null, repostedFrom: j.id, createdAt: now() }); jobs.unshift(copy); return send(res, 200, jobView(copy)) }
      }
      return send(res, 200, jobView(j))
    }
    if (path === '/admin/employers/counts') {
      const c = { UNVERIFIED: 0, PENDING: 0, VERIFIED: 0, REJECTED: 0 }; Object.values(employers).forEach((e) => c[e.status]++)
      return send(res, 200, c)
    }
    if (path === '/admin/employers') {
      const status = url.searchParams.get('status') ?? 'PENDING'
      return send(res, 200, { items: Object.values(employers).filter((e) => e.status === status).map(adminView), nextCursor: null })
    }
    const vm = m(/^\/admin\/employers\/([\w-]+)\/verification$/)
    if (vm) {
      const e = Object.values(employers).find((x) => x.id === vm[1]); const body = await readBody(req)
      if (body.decision === 'APPROVE') { e.status = 'VERIFIED'; e.verifiedAt = now(); e.verificationNote = body.reason }
      else { e.status = 'REJECTED'; e.verificationNote = body.reason }
      return send(res, 200, adminView(e))
    }
    return problem(res, 404, 'Not found: ' + path)
  } catch (error) {
    console.error(error); return problem(res, 500, String(error))
  }
}).listen(1027, () => console.log('mock API on :1027'))
