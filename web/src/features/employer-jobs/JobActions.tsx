import { CirclePause, CirclePlay, CircleX, Ellipsis, Pencil, RotateCcw, Send, Trash2 } from 'lucide-react'
import type { ReactNode } from 'react'
import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router'
import { useToast } from '../../components/Toast'
import { Button } from '../../components/ui'
import { ApiError } from '../../lib/api'
import { cn } from '../../lib/cn'
import type { AllowedAction, Job } from '../../lib/types'
import { useDeleteDraft, useJobAction } from './api'

const icons: Record<AllowedAction, ReactNode> = {
  EDIT: <Pencil className="size-4" aria-hidden />,
  SUBMIT: <Send className="size-4" aria-hidden />,
  PAUSE: <CirclePause className="size-4" aria-hidden />,
  RESUME: <CirclePlay className="size-4" aria-hidden />,
  CLOSE: <CircleX className="size-4" aria-hidden />,
  REPOST: <RotateCcw className="size-4" aria-hidden />,
  DELETE: <Trash2 className="size-4" aria-hidden />,
}

/** Runs one action on a post (edit, publish, pause, resume, close, repost, delete), with confirmations. */
export function useRunAction() {
  const { t } = useTranslation()
  const toast = useToast()
  const navigate = useNavigate()
  const act = useJobAction()
  const remove = useDeleteDraft()
  const [error, setError] = useState<string | null>(null)

  async function run(job: Job, action: AllowedAction) {
    setError(null)
    if (action === 'EDIT') {
      navigate(`/employer/jobs/${job.id}/edit`)
      return
    }
    if (action === 'CLOSE' && !window.confirm(t('job.confirmClose'))) return
    if (action === 'DELETE' && !window.confirm(t('job.confirmDelete'))) return
    try {
      if (action === 'DELETE') {
        await remove.mutateAsync(job.id)
        toast(t('job.actionDone.DELETE'))
        return
      }
      const result = await act.mutateAsync({ id: job.id, action })
      if (action === 'SUBMIT') {
        toast(result.status === 'PUBLISHED' ? t('job.published') : result.awaitingVerification ? t('job.waiting') : t('job.actionDone.SUBMIT'))
      } else {
        toast(t(`job.actionDone.${action}`))
      }
      if (action === 'REPOST') navigate(`/employer/jobs/${result.id}/edit`)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : t('common.somethingWrong'))
    }
  }

  return { run, error, busy: act.isPending || remove.isPending }
}

/** The "⋯" menu on each row of "My jobs". */
export function JobActionsMenu({ job, onRun }: { job: Job; onRun: (job: Job, action: AllowedAction) => void }) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const box = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    const close = (event: MouseEvent) => !box.current?.contains(event.target as Node) && setOpen(false)
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [open])

  if (job.allowedActions.length === 0) return null
  return (
    <div className="relative" ref={box}>
      <Button variant="secondary" className="px-3" aria-haspopup="menu" aria-expanded={open} aria-label={t('job.manage')} onClick={() => setOpen((v) => !v)}>
        <Ellipsis className="size-5" aria-hidden />
      </Button>
      {open && (
        <ul role="menu" className="absolute right-0 z-30 mt-2 w-56 rounded-xl border border-slate-200 bg-white py-1 shadow-lg">
          {job.allowedActions.map((action) => (
            <li key={action} role="none">
              <button
                role="menuitem"
                type="button"
                className={cn('flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm hover:bg-slate-50', (action === 'CLOSE' || action === 'DELETE') && 'text-red-700')}
                onClick={() => {
                  setOpen(false)
                  onRun(job, action)
                }}
              >
                {icons[action]}
                {t(`job.action.${action}`)}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

/** The action buttons in the "Actions" panel of the job page. */
export function JobActionButtons({ job, onRun, busy }: { job: Job; onRun: (job: Job, action: AllowedAction) => void; busy: boolean }) {
  const { t } = useTranslation()
  return (
    <div className="flex flex-col gap-2">
      {job.allowedActions.map((action, index) => (
        <Button
          key={action}
          variant={index === 0 ? 'primary' : action === 'CLOSE' || action === 'DELETE' ? 'danger' : 'secondary'}
          icon={icons[action]}
          disabled={busy}
          onClick={() => onRun(job, action)}
        >
          {t(`job.action.${action}`)}
        </Button>
      ))}
    </div>
  )
}
