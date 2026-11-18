import { Ban, HelpCircle, Sparkles, Wand2 } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { FindingList } from '../../components/FindingList'
import { Alert, Button, Textarea } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { formatWage, localDigits, termName } from '../../lib/format'
import type { JobDraft } from '../../lib/types'
import { useLang } from '../../lib/useLang'
import { useDraftJob } from './api'

const EXAMPLES = [
  '2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta',
  'টঙ্গীতে ৩ জন নিরাপত্তা প্রহরী দরকার, বেতন ১২০০০ টাকা, রাত ১০টা থেকে সকাল ৬টা',
  'Need two delivery riders in Mirpur, Tk 600 per day, 9am to 5pm',
]

/**
 * The AI job-post assistant (Member-3, Module 3) at the top of "Post a job": the employer describes the job
 * in one sentence, sees the draft in both languages with questions and warnings, and "Use this draft" fills
 * the form below, where they check and edit everything before publishing (context file §10.4).
 */
export function AssistantPanel({ onUse }: { onUse: (draft: JobDraft) => void }) {
  const { t } = useTranslation()
  const lang = useLang()
  const [text, setText] = useState('')
  const draft = useDraftJob()
  const result = draft.data

  function ask() {
    if (text.trim().length < 3) return
    draft.mutate(text.trim())
  }

  const error = draft.error instanceof ApiError ? (draft.error.fieldErrors.text ?? draft.error.message) : draft.error?.message

  return (
    <section className="rounded-2xl border border-violet-200 bg-gradient-to-br from-violet-50 to-white p-4 md:p-5" aria-labelledby="assistant-title">
      <div className="flex items-start gap-3">
        <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-violet-600 text-white">
          <Sparkles className="size-5" aria-hidden />
        </span>
        <div>
          <h2 id="assistant-title" className="font-bold text-violet-900">{t('assistant.title')}</h2>
          <p className="text-sm text-slate-600">{t('assistant.subtitle')}</p>
        </div>
      </div>

      <label htmlFor="assistant-text" className="sr-only">{t('assistant.label')}</label>
      <Textarea
        id="assistant-text"
        className="mt-3 min-h-20"
        maxLength={500}
        placeholder={t('assistant.placeholder')}
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) ask()
        }}
      />
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <span className="text-xs font-semibold text-slate-500">{t('assistant.examples')}</span>
        {EXAMPLES.map((example) => (
          <button
            key={example}
            type="button"
            onClick={() => setText(example)}
            className="max-w-full truncate rounded-full border border-violet-200 bg-white px-3 py-1 text-xs text-violet-800 hover:bg-violet-50"
          >
            {example}
          </button>
        ))}
      </div>
      <div className="mt-3 flex flex-wrap items-center gap-3">
        <Button type="button" icon={<Wand2 className="size-4" />} loading={draft.isPending} disabled={text.trim().length < 3} onClick={ask}>
          {draft.isPending ? t('assistant.working') : t('assistant.draft')}
        </Button>
        <span className="text-xs text-slate-500">{t('assistant.privacy')}</span>
      </div>

      {error && (
        <div className="mt-3">
          <Alert tone="error">{error}</Alert>
        </div>
      )}

      {result && (
        <div className="mt-4 flex flex-col gap-4 rounded-2xl border border-slate-200 bg-white p-4" aria-live="polite">
          {result.blocked && (
            <Alert tone="error" title={t('assistant.blockedTitle')}>
              {t('assistant.blocked')}
            </Alert>
          )}
          <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-slate-500">
            <span className="rounded-full bg-slate-100 px-2 py-0.5">{t(`assistant.source.${result.source}`)}</span>
            {result.occupation && <span>{termName(result.occupation, lang)}</span>}
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <DraftSide lang="bn" title={result.titleBn} description={result.descriptionBn} label={t('assistant.bangla')} />
            <DraftSide lang="en" title={result.titleEn} description={result.descriptionEn} label={t('assistant.english')} />
          </div>
          <DraftFacts draft={result} />
          {result.questions.length > 0 && (
            <div>
              <p className="flex items-center gap-2 text-sm font-semibold">
                <HelpCircle className="size-4 text-sky-600" aria-hidden />
                {t('assistant.questions')}
              </p>
              <ul className="mt-2 list-disc ps-6 text-sm text-slate-700">
                {result.questions.map((question) => (
                  <li key={question}>{question}</li>
                ))}
              </ul>
            </div>
          )}
          {result.findings.length > 0 && (
            <div>
              <p className="mb-2 text-sm font-semibold">{t('assistant.findings')}</p>
              <FindingList findings={result.findings} compact />
            </div>
          )}
          <div className="flex flex-wrap items-center gap-3 border-t border-slate-200 pt-3">
            <Button type="button" variant={result.blocked ? 'secondary' : 'primary'} icon={result.blocked ? <Ban className="size-4" /> : <Sparkles className="size-4" />} onClick={() => onUse(result)}>
              {t('assistant.use')}
            </Button>
            <span className="text-xs text-slate-500">{t('assistant.useHint')}</span>
          </div>
        </div>
      )}
    </section>
  )
}

function DraftSide({ lang, title, description, label }: { lang: 'bn' | 'en'; title: string | null; description: string | null; label: string }) {
  return (
    <div lang={lang} className="rounded-xl bg-slate-50 p-3">
      <p className="text-xs font-semibold tracking-wide text-slate-500 uppercase">{label}</p>
      <p className="mt-1 font-bold">{title || '—'}</p>
      {description && <p className="mt-1 text-sm leading-6 whitespace-pre-line text-slate-700">{description}</p>}
    </div>
  )
}

function DraftFacts({ draft }: { draft: JobDraft }) {
  const { t } = useTranslation()
  const lang = useLang()
  const wage = formatWage(draft.wageMin, draft.wageMax, draft.wagePeriod ? t(`job.period.${draft.wagePeriod}`) : null, lang)
  const facts: Array<[string, string]> = [
    [t('job.openings'), draft.openings == null ? '—' : t('job.people', { n: localDigits(draft.openings, lang) })],
    [t('job.salary'), wage ?? '—'],
    [t('job.workingHours'), draft.hours ? localDigits(draft.hours, lang) : '—'],
    [t('job.areaName'), draft.areaName ?? '—'],
  ]
  return (
    <dl className="grid gap-2 text-sm sm:grid-cols-4">
      {facts.map(([label, value]) => (
        <div key={label} className="rounded-lg border border-slate-200 px-3 py-2">
          <dt className="text-xs text-slate-500">{label}</dt>
          <dd className="font-semibold">{value}</dd>
        </div>
      ))}
    </dl>
  )
}
