import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, useLocation } from 'react-router'
import { Alert, Spinner } from '../../components/ui'
import type { Role } from '../../lib/types'
import { useCurrentUser } from './useCurrentUser'

/** Shows the page only to signed-in users with this role; guests go to the sign-in page. */
export function RequireRole({ role, children }: { role: Role; children: ReactNode }) {
  const { t } = useTranslation()
  const location = useLocation()
  const { data: me, isLoading, error } = useCurrentUser()
  if (isLoading) return <Spinner label={t('common.loading')} />
  if (error) return <div className="p-6"><Alert tone="error">{t('common.networkError')}</Alert></div>
  if (!me) return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />
  if (me.role !== role) {
    return (
      <div className="p-6">
        <Alert tone="warning" title={t('common.notAllowed')} action={<Link className="font-semibold underline" to="/">{t('common.goHome')}</Link>} />
      </div>
    )
  }
  return <>{children}</>
}
