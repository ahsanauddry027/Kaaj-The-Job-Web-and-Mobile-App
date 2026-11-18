import { useQuery } from '@tanstack/react-query'
import { api } from '../../lib/api'
import type { Term } from '../../lib/types'

/**
 * Searches occupations or skills in the shared taxonomy: Bangla, Banglish and English names, spelling-tolerant
 * on the server (GET /taxonomy/occupations?q=, /taxonomy/skills?q=). Runs only while `enabled`.
 */
export function useTaxonomySearch(kind: 'occupations' | 'skills', query: string, enabled: boolean) {
  return useQuery({
    queryKey: ['taxonomy', kind, query],
    queryFn: () => api.get<Term[]>(`/taxonomy/${kind}?q=${encodeURIComponent(query)}&limit=12`),
    enabled,
    staleTime: 5 * 60_000,
  })
}
