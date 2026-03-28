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

import React from 'react';
import { FaLanguage } from 'react-icons/fa';
import type { PersonalInfoLanguageEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { createClientId, moveItem, normalizeOptionId } from '../personalInfoUtils';

export interface LanguagesSectionProps {
  languages: PersonalInfoLanguageEntryDto[];
  setLanguages: React.Dispatch<React.SetStateAction<PersonalInfoLanguageEntryDto[]>>;

  languageQuery: string;
  setLanguageQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedLanguageId: React.Dispatch<React.SetStateAction<number | null>>;

  isLanguagesLoading: boolean;
  languageSuggestions: Array<{ id: number; name: string }>;

  addLanguage: () => void;

  editingLanguageId: string | null;
  setEditingLanguageId: React.Dispatch<React.SetStateAction<string | null>>;
  editingLanguageValue: string;
  setEditingLanguageValue: React.Dispatch<React.SetStateAction<string>>;
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
  editingLanguageId,
  setEditingLanguageId,
  editingLanguageValue,
  setEditingLanguageValue,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Languages"
      icon={<FaLanguage className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm overflow-hidden p-5 sm:p-6 space-y-4"
    >
      <div className="relative flex flex-col sm:flex-row gap-3">
        <input
          value={languageQuery}
          onChange={(e) => {
            setLanguageQuery(e.target.value);
            setSelectedLanguageId(null);
          }}
          placeholder="Type a language"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <button
          type="button"
          onClick={addLanguage}
          className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
        >
          Add
        </button>
        {(isLanguagesLoading || languageSuggestions.length > 0) && (
          <div className="absolute top-11 left-0 right-0 sm:right-24 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
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

      <div className="space-y-2">
        {languages.map((item, index) => (
          <div
            key={`language-row-${item.id}`}
            className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3"
          >
            {editingLanguageId === item.id ? (
              <input
                value={editingLanguageValue}
                onChange={(e) => setEditingLanguageValue(e.target.value)}
                className="flex-1 rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
              />
            ) : (
              <span className="flex-1 text-[var(--color-textPrimary)]">{item.name}</span>
            )}

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setLanguages((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < languages.length - 1 && setLanguages((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingLanguageId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    const nextValue = editingLanguageValue.trim();
                    if (nextValue) {
                      setLanguages((prev) =>
                        prev.map((entry) =>
                          entry.id === item.id
                            ? {
                                ...entry,
                                name: nextValue,
                                languageId: normalizeOptionId('language', nextValue),
                              }
                            : entry,
                        ),
                      );
                    }
                    setEditingLanguageId(null);
                    setEditingLanguageValue('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingLanguageId(item.id);
                    setEditingLanguageValue(item.name);
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setLanguages((prev) => prev.filter((entry) => entry.id !== item.id))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </PersonalInfoSectionCard>
  );
};

export default LanguagesSection;
