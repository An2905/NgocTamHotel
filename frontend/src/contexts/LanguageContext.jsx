import { createContext, useContext, useState, useCallback } from 'react';
import vi from '../i18n/vi.js';
import en from '../i18n/en.js';

const dictionaries = { vi, en };
const LanguageContext = createContext();

export function LanguageProvider({ children }) {
  const [lang, setLang] = useState(() => {
    return localStorage.getItem('nt-lang') || 'vi';
  });

  function switchLang(newLang) {
    setLang(newLang);
    localStorage.setItem('nt-lang', newLang);
  }

  const t = useCallback((key, replacements) => {
    let text = dictionaries[lang]?.[key] || dictionaries.vi[key] || key;
    if (replacements) {
      Object.entries(replacements).forEach(([k, v]) => {
        text = text.replace(`{${k}}`, v);
      });
    }
    return text;
  }, [lang]);

  return (
    <LanguageContext.Provider value={{ lang, switchLang, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  return useContext(LanguageContext);
}
