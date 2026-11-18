import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import bn from './bn.json'
import en from './en.json'

export type Lang = 'bn' | 'en'

const STORAGE_KEY = 'kaaj.lang'

function savedLanguage(): Lang {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY)
    return saved === 'en' ? 'en' : 'bn'
  } catch {
    return 'bn' // storage can be blocked; Bangla is the default
  }
}

void i18n.use(initReactI18next).init({
  resources: { bn: { translation: bn }, en: { translation: en } },
  lng: savedLanguage(),
  fallbackLng: 'bn',
  interpolation: { escapeValue: false }, // React already escapes text
  returnObjects: true,
})

document.documentElement.lang = i18n.language

export function setLanguage(lang: Lang) {
  void i18n.changeLanguage(lang)
  document.documentElement.lang = lang
  try {
    window.localStorage.setItem(STORAGE_KEY, lang)
  } catch {
    // ignore: the choice just won't be remembered
  }
}

export function currentLang(): Lang {
  return i18n.language === 'en' ? 'en' : 'bn'
}

export default i18n
