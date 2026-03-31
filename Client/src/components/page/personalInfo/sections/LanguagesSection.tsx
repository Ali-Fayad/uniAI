/**
 * LanguagesSection
 *
 * Responsibility:
 * - Render languages inputs, suggestions, and the languages entry list.
 *
 * Does NOT:
 * - Fetch suggestion data (provided via props)
 * - Persist personal info
 */

import React, { useState, useRef } from 'react';
import { useOnClickOutside } from '../../../../hooks/useOnClickOutside';
import { FaLanguage } from 'react-icons/fa';
import type { PersonalInfoLanguageEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { createClientId } from '../personalInfoUtils';
import AnimatedInput from '../../../common/AnimatedInput';
import FormButton from '../../../settings/FormButton';

export interface LanguagesSectionProps {
  languages: PersonalInfoLanguageEntryDto[];
  setLanguages: React.Dispatch<React.SetStateAction<PersonalInfoLanguageEntryDto[]>>;

  languageQuery: string;
  setLanguageQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedLanguageId: React.Dispatch<React.SetStateAction<number | null>>;

  isLanguagesLoading: boolean;
  languageSuggestions: Array<{ id: number; name: string }>;

  addLanguage: () => void;

}

const LanguagesSection: React.FC<LanguagesSectionProps> = ({
  languages,
  setLanguages,
  languageQuery,
  setLanguageQuery,
  setSelectedLanguageId,
  isLanguagesLoading,
  languageSuggestions,
  addLanguage,
}) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  useOnClickOutside(containerRef, () => setIsDropdownOpen(false));

  return (
    <PersonalInfoSectionCard
      title="Languages"
      icon={<FaLanguage className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div ref={containerRef} className="relative flex flex-col sm:flex-row gap-3 items-start">
        <AnimatedInput
          value={languageQuery}
          onFocus={() => setIsDropdownOpen(true)}
          onChange={(e) => {
            setIsDropdownOpen(true);
            setLanguageQuery(e.target.value);
            setSelectedLanguageId(null);
          }}
          label="Type a language"
          containerClassName="flex-1"
        >
        {isDropdownOpen && (isLanguagesLoading || languageSuggestions.length > 0) && (
          <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-72 overflow-auto sm:right-24">
            {isLanguagesLoading ? (
              <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
            ) : (
              languageSuggestions.map((language) => (
                <button
                  key={language.id}
                  type="button"
                  onClick={() => {
                    const languageId = String(language.id);
                    const id = `${languageId}-${createClientId()}`;
                    setLanguages((prev) => [...prev, { id, languageId, name: language.name }]);
                    setLanguageQuery('');
                    setSelectedLanguageId(null);
                  }}
                  className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                >
                  {language.name}
                </button>
              ))
            )}
          </div>
        )}
        </AnimatedInput>
        <FormButton
          variant="primary"
          type="button"
          onClick={addLanguage}
          className="h-14 px-6 shrink-0"
        >
          Add
        </FormButton>
      </div>

      <div className="flex flex-wrap gap-2">
        {languages.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}
            <button
              type="button"
              onClick={() => setLanguages((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>

    </PersonalInfoSectionCard>
  );
};

export default LanguagesSection;
