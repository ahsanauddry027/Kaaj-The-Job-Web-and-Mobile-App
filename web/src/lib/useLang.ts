import { useTranslation } from 'react-i18next'
import type { Lang } from '../i18n'

/** The current language, re-rendering the component when the user switches it. */
export function useLang(): Lang {
  const { i18n } = useTranslation()
  return i18n.language === 'en' ? 'en' : 'bn'
}
