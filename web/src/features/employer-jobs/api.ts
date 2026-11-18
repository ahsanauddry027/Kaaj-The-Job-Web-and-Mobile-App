import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { CursorPage, Job, JobAction, JobCounts, JobDraft, JobInput, JobStatus, ScreeningView } from '../../lib/types'

export function useMyJobs(status: JobStatus | null, q: string, enabled = true) {
  return useInfiniteQuery({
    enabled,
    queryKey: ['jobs', 'list', status, q],
    initialPageParam: null as string | null,
    queryFn: ({ pageParam }) => {
      const params = new URLSearchParams({ size: '10' })
      if (status) params.set('status', status)
      if (q) params.set('q', q)
      if (pageParam) params.set('cursor', pageParam)
      return api.get<CursorPage<Job>>(`/employer/jobs?${params}`)
    },
    getNextPageParam: (last) => last.nextCursor,
  })
}

export function useJobCounts(enabled = true) {
  return useQuery({ queryKey: ['jobs', 'counts'], queryFn: () => api.get<JobCounts>('/employer/jobs/summary'), enabled })
}

export function useJob(id: string | undefined) {
  return useQuery({ queryKey: ['jobs', 'one', id], queryFn: () => api.get<Job>(`/employer/jobs/${id}`), enabled: !!id })
}

function useInvalidateJobs() {
  const client = useQueryClient()
  return () => client.invalidateQueries({ queryKey: ['jobs'] })
}

export function useCreateJob() {
  const invalidate = useInvalidateJobs()
  return useMutation({
    mutationFn: ({ input, submit, key }: { input: JobInput; submit: boolean; key: string }) =>
      api.post<Job>(`/employer/jobs?submit=${submit}`, input, { 'Idempotency-Key': key }),
    onSuccess: invalidate,
  })
}

export function useUpdateJob(id: string) {
  const invalidate = useInvalidateJobs()
  return useMutation({
    mutationFn: ({ input, submit }: { input: JobInput; submit: boolean }) => api.put<Job>(`/employer/jobs/${id}?submit=${submit}`, input),
    onSuccess: invalidate,
  })
}

export function useJobAction() {
  const invalidate = useInvalidateJobs()
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: JobAction }) => api.patch<Job>(`/employer/jobs/${id}`, { action }),
    onSuccess: invalidate,
  })
}

export function useDeleteDraft() {
  const invalidate = useInvalidateJobs()
  return useMutation({ mutationFn: (id: string) => api.delete<void>(`/employer/jobs/${id}`), onSuccess: invalidate })
}

/** The AI job-post assistant (Module 3): one sentence → a draft for the form. Nothing is saved. */
export function useDraftJob() {
  return useMutation({ mutationFn: (text: string) => api.post<JobDraft>('/employer/jobs/drafts', { text }) })
}

/** Why a post is in review or blocked (scam screening, Module 2). */
export function useScreening(id: string | undefined, enabled: boolean) {
  return useQuery({ queryKey: ['jobs', 'screening', id], queryFn: () => api.get<ScreeningView>(`/employer/jobs/${id}/screening`), enabled: !!id && enabled })
}
