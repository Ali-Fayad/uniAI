/**
 * ExperienceSection
 *
 * Responsibility:
 * - Render experience inputs (position + company), position suggestions, and the experience entry list.
 *
 * Does NOT:
 * - Fetch suggestion data (provided via props)
 * - Persist personal info
 */

import React from 'react';
import { FaBriefcase } from 'react-icons/fa';
import type { PersonalInfoExperienceEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { moveItem, normalizeOptionId } from '../personalInfoUtils';

export interface ExperienceSectionProps {
  experience: PersonalInfoExperienceEntryDto[];
  setExperience: React.Dispatch<React.SetStateAction<PersonalInfoExperienceEntryDto[]>>;

  positionQuery: string;
  setPositionQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedPositionId: React.Dispatch<React.SetStateAction<number | null>>;

  experienceCompany: string;
  setExperienceCompany: React.Dispatch<React.SetStateAction<string>>;

  isPositionsLoading: boolean;
  positionSuggestions: Array<{ id: number; name: string }>;

  addExperience: () => void;

  editingExperienceId: string | null;
  setEditingExperienceId: React.Dispatch<React.SetStateAction<string | null>>;
  editingExperiencePosition: string;
  setEditingExperiencePosition: React.Dispatch<React.SetStateAction<string>>;
  editingExperienceCompany: string;
  setEditingExperienceCompany: React.Dispatch<React.SetStateAction<string>>;
}

const ExperienceSection: React.FC<ExperienceSectionProps> = ({
  experience,
  setExperience,
  positionQuery,
  setPositionQuery,
  setSelectedPositionId,
  experienceCompany,
  setExperienceCompany,
  isPositionsLoading,
  positionSuggestions,
  addExperience,
  editingExperienceId,
  setEditingExperienceId,
  editingExperiencePosition,
  setEditingExperiencePosition,
  editingExperienceCompany,
  setEditingExperienceCompany,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Experience"
      icon={<FaBriefcase className="h-5 w-5" aria-hidden="true" />}
      className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
        <div className="sm:col-span-2 relative">
          <input
            value={positionQuery}
            onChange={(e) => {
              setPositionQuery(e.target.value);
              setSelectedPositionId(null);
            }}
            placeholder="Type a position"
            className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
          />
          {(isPositionsLoading || positionSuggestions.length > 0) && (
            <div className="absolute top-11 left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
              {isPositionsLoading ? (
                <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
              ) : (
                positionSuggestions.map((position) => (
                  <button
                    key={position.id}
                    type="button"
                    onClick={() => {
                      setPositionQuery(position.name);
                      setSelectedPositionId(position.id);
                    }}
                    className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                  >
                    {position.name}
                  </button>
                ))
              )}
            </div>
          )}
        </div>
        <input
          value={experienceCompany}
          onChange={(e) => setExperienceCompany(e.target.value)}
          placeholder="Company"
          className="sm:col-span-2 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <button
          type="button"
          onClick={addExperience}
          className="sm:col-span-1 rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
        >
          Add
        </button>
      </div>

      <div className="flex flex-wrap gap-2">
        {experience.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.position}{item.company ? ` · ${item.company}` : ''}
            <button
              type="button"
              onClick={() => setExperience((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      <div className="space-y-2">
        {experience.map((item, index) => (
          <div
            key={`exp-row-${item.id}`}
            className="flex flex-col gap-2 rounded-md border border-[var(--color-border)] p-3"
          >
            {editingExperienceId === item.id ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <input
                  value={editingExperiencePosition}
                  onChange={(e) => setEditingExperiencePosition(e.target.value)}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
                <input
                  value={editingExperienceCompany}
                  onChange={(e) => setEditingExperienceCompany(e.target.value)}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
              </div>
            ) : (
              <span className="text-[var(--color-textPrimary)]">
                {item.position}{item.company ? ` · ${item.company}` : ''}
              </span>
            )}

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setExperience((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < experience.length - 1 && setExperience((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingExperienceId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    const nextPosition = editingExperiencePosition.trim();
                    if (nextPosition) {
                      setExperience((prev) =>
                        prev.map((entry) =>
                          entry.id === item.id
                            ? {
                                ...entry,
                                position: nextPosition,
                                company: editingExperienceCompany.trim(),
                                positionId: normalizeOptionId('position', nextPosition),
                              }
                            : entry,
                        ),
                      );
                    }
                    setEditingExperienceId(null);
                    setEditingExperiencePosition('');
                    setEditingExperienceCompany('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingExperienceId(item.id);
                    setEditingExperiencePosition(item.position);
                    setEditingExperienceCompany(item.company);
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setExperience((prev) => prev.filter((entry) => entry.id !== item.id))}
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

export default ExperienceSection;
