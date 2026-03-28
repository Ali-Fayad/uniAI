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

import React, { useState, useRef } from 'react';
import { useOnClickOutside } from '../../../../hooks/useOnClickOutside';
import { FaBriefcase } from 'react-icons/fa';
import type { PersonalInfoExperienceEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { moveItem, normalizeOptionId } from '../personalInfoUtils';
import AnimatedInput from '../../../common/AnimatedInput';

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
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  useOnClickOutside(containerRef, () => setIsDropdownOpen(false));

  return (
    <PersonalInfoSectionCard
      title="Experience"
      icon={<FaBriefcase className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-5 gap-3 items-start">
        <AnimatedInput
          value={positionQuery}
          onFocus={() => setIsDropdownOpen(true)}
          onChange={(e) => {
            setIsDropdownOpen(true);
            setPositionQuery(e.target.value);
            setSelectedPositionId(null);
          }}
          label="Type a position"
          containerClassName="sm:col-span-2"
        >
          {isDropdownOpen && (isPositionsLoading || positionSuggestions.length > 0) && (
            <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-72 overflow-auto">
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
        </AnimatedInput>
        <AnimatedInput
          value={experienceCompany}
          onChange={(e) => setExperienceCompany(e.target.value)}
          label="Company"
          containerClassName="sm:col-span-2"
        />
        <button
          type="button"
          onClick={addExperience}
          className="sm:col-span-1 h-14 rounded-xl bg-[var(--color-primary)] px-4 text-[var(--color-background)] font-medium hover:bg-[var(--color-primaryVariant)] transition-colors"
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

      <div className="space-y-2 max-h-[60vh] overflow-y-auto pr-2 overflow-x-hidden">
        {experience.map((item, index) => (
          <div
            key={`exp-row-${item.id}`}
            className="flex flex-col gap-2 rounded-md border border-[var(--color-border)] p-3"
          >
            {editingExperienceId === item.id ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <AnimatedInput
                  value={editingExperiencePosition}
                  onChange={(e) => setEditingExperiencePosition(e.target.value)}
                  label="Position"
                />
                <AnimatedInput
                  value={editingExperienceCompany}
                  onChange={(e) => setEditingExperienceCompany(e.target.value)}
                  label="Company"
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
