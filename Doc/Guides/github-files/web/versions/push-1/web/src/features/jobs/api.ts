import { useQuery } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { PublicJob } from '../../lib/types'

/** A published post as anyone sees it (GET /jobs/{id}), with the employer's "Verified" badge. */
export function usePublicJob(id: string | undefined) {
  return useQuery({ queryKey: ['public-job', id], queryFn: () => api.get<PublicJob>(`/jobs/${id}`), enabled: !!id })
}
