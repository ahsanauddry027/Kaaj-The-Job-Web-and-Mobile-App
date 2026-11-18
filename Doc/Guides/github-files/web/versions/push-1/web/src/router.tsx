import { lazy, Suspense, type ReactNode } from 'react'
import { createBrowserRouter } from 'react-router'
import { AppLayout, PublicLayout } from './components/AppLayout'
import { Spinner } from './components/ui'
import { LoginPage, RegisterPage } from './features/auth/AuthPages'
import { RequireRole } from './features/auth/RequireRole'
import { HomePage, NotFoundPage, SeekerHomePage } from './features/home/HomePage'
import i18n from './i18n'

// Employer and admin pages load only when needed, so a job seeker's first load stays light (context file §18).
const DashboardPage = lazy(() => import('./features/employer-jobs/DashboardPage').then((m) => ({ default: m.DashboardPage })))
const EmployerProfilePage = lazy(() => import('./features/employer-profile/EmployerProfilePage').then((m) => ({ default: m.EmployerProfilePage })))
const MyJobsPage = lazy(() => import('./features/employer-jobs/MyJobsPage').then((m) => ({ default: m.MyJobsPage })))
const JobFormPage = lazy(() => import('./features/employer-jobs/JobFormPage').then((m) => ({ default: m.JobFormPage })))
const JobDetailPage = lazy(() => import('./features/employer-jobs/JobDetailPage').then((m) => ({ default: m.JobDetailPage })))
const AdminVerificationPage = lazy(() => import('./features/admin-trust/AdminVerificationPage').then((m) => ({ default: m.AdminVerificationPage })))
const PublicJobPage = lazy(() => import('./features/jobs/PublicJobPage').then((m) => ({ default: m.PublicJobPage })))

const page = (node: ReactNode) => <Suspense fallback={<Spinner label={i18n.t('common.loading')} />}>{node}</Suspense>
const employer = (node: ReactNode) => <RequireRole role="EMPLOYER">{page(node)}</RequireRole>

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/jobs/:id', element: page(<PublicJobPage />) },
      { path: '/seeker', element: <RequireRole role="SEEKER"><SeekerHomePage /></RequireRole> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
  {
    element: <AppLayout />,
    children: [
      { path: '/employer', element: employer(<DashboardPage />) },
      { path: '/employer/profile', element: employer(<EmployerProfilePage />) },
      { path: '/employer/jobs', element: employer(<MyJobsPage />) },
      { path: '/employer/jobs/new', element: employer(<JobFormPage />) },
      { path: '/employer/jobs/:id', element: employer(<JobDetailPage />) },
      { path: '/employer/jobs/:id/edit', element: employer(<JobFormPage />) },
      { path: '/admin/verification', element: <RequireRole role="ADMIN">{page(<AdminVerificationPage />)}</RequireRole> },
    ],
  },
])
