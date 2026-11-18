import { useMutation, useQuery } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { PublicJob, ReportReason } from '../../lib/types'

/** A published post as anyone sees it (GET /jobs/{id}), with the employer's "Verified" badge. */
export function usePublicJob(id: string | undefined) {
  return useQuery({ queryKey: ['public-job', id], queryFn: () => api.get<PublicJob>(`/jobs/${id}`), enabled: !!id })
}

/** Sends a report about a post to the moderators (POST /reports; scam screening and moderation, Module 2). */
export function useReportPost(jobId: string) {
  return useMutation({
    mutationFn: (report: { reason: ReportReason; details: string | null }) =>
      api.post<{ received: boolean }>('/reports', { subjectType: 'JOB', subjectId: jobId, ...report }),
  })
}
