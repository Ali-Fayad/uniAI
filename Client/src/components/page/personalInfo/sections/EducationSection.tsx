/**
 * EducationSection
 *
 * Responsibility:
 * - Render education inputs, suggestions, and the education entry list.
 *
 * Does NOT:
 * - Fetch suggestion data (provided via props)
 * - Persist personal info
 */

import React from 'react';
import { FaGraduationCap } from 'react-icons/fa';
import type { PersonalInfoEducationEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { createClientId, moveItem } from '../personalInfoUtils';
import AnimatedInput from '../../../common/AnimatedInput';

export interface EducationSectionProps {
  education: PersonalInfoEducationEntryDto[];
  setEducation: React.Dispatch<React.SetStateAction<PersonalInfoEducationEntryDto[]>>;

  universityQuery: string;
  setUniversityQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedUniversityId: number | null;
  setSelectedUniversityId: React.Dispatch<React.SetStateAction<number | null>>;

  isUniversitiesLoading: boolean;
  universitySuggestions: Array<{ id: number; name: string; acronym?: string | null }>;

  addEducation: () => void;
  setError: React.Dispatch<React.SetStateAction<string | null>>;

  editingEducationId: string | null;
  setEditingEducationId: React.Dispatch<React.SetStateAction<string | null>>;
  editingEducationValue: string;
  setEditingEducationValue: React.Dispatch<React.SetStateAction<string>>;
}

const EducationSection: React.FC<EducationSectionProps> = ({
  education,
  setEducation,
  universityQuery,
  setUniversityQuery,
  setSelectedUniversityId,
  isUniversitiesLoading,
  universitySuggestions,
  addEducation,
  setError,
  editingEducationId,
  setEditingEducationId,
  editingEducationValue,
  setEditingEducationValue,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Education"
      icon={<FaGraduationCap className="h-5 w-5" aria-hidden="true" />}
      className="rounded-3xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="relative flex flex-col sm:flex-row gap-3 items-start">
        <AnimatedInput
          value={universityQuery}
          onChange={(e) => {
            setUniversityQuery(e.target.value);
            setSelectedUniversityId(null);
          }}
          label="Type university"
          containerClassName="flex-1"
        >
          {(isUniversitiesLoading || universitySuggestions.length > 0) && (
            <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
              {isUniversitiesLoading ? (
                <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
              ) : (
                universitySuggestions.map((uni) => (
                  <button
                    key={uni.id}
                    type="button"
                    onClick={() => {
                      const id = `uni-${uni.id}-${createClientId()}`;
                      setEducation((prev) => [
                        ...prev,
                        { id, universityId: uni.id, universityName: uni.name },
                      ]);
                      setUniversityQuery('');
                      setSelectedUniversityId(null);
                      setError(null);
                    }}
                    className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                  >
                    {uni.name}{uni.acronym ? ` (${uni.acronym})` : ''}
                  </button>
                ))
              )}
            </div>
          )}
        </AnimatedInput>
        <button
          type="button"
          onClick={addEducation}
          className="h-14 rounded-xl bg-[var(--color-primary)] px-6 text-[var(--color-background)] font-medium hover:bg-[var(--color-primaryVariant)] transition-colors"
        >
          Add
        </button>
      </div>

      <div className="flex flex-wrap gap-2">
        {education.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.universityName}
            <button
              type="button"
              onClick={() => setEducation((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      <div className="space-y-2">
        {education.map((item, index) => (
          <div
            key={`row-${item.id}`}
            className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3"
          >
            {editingEducationId === item.id ? (
              <AnimatedInput
                value={editingEducationValue}
                onChange={(e) => setEditingEducationValue(e.target.value)}
                label="University"
                containerClassName="flex-1"
              />
            ) : (
              <span className="flex-1 text-[var(--color-textPrimary)]">{item.universityName}</span>
            )}

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setEducation((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < education.length - 1 && setEducation((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingEducationId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    setEducation((prev) =>
                      prev.map((entry) =>
                        entry.id === item.id
                          ? {
                              ...entry,
                              universityName: editingEducationValue.trim() || entry.universityName,
                              universityId: null,
                            }
                          : entry,
                      ),
                    );
                    setEditingEducationId(null);
                    setEditingEducationValue('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingEducationId(item.id);
                    setEditingEducationValue(item.universityName);
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setEducation((prev) => prev.filter((entry) => entry.id !== item.id))}
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

export default EducationSection;
