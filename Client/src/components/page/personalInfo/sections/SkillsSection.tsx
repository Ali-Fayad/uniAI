/**
 * SkillsSection
 *
 * Responsibility:
 * - Render skills inputs, suggestions, and the skills entry list.
 *
 * Does NOT:
 * - Fetch suggestion data (provided via props)
 * - Persist personal info
 */

import React from 'react';
import { FaCode } from 'react-icons/fa';
import type { PersonalInfoSkillEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { createClientId, moveItem, normalizeOptionId } from '../personalInfoUtils';
import AnimatedInput from '../../../common/AnimatedInput';

export interface SkillsSectionProps {
  skills: PersonalInfoSkillEntryDto[];
  setSkills: React.Dispatch<React.SetStateAction<PersonalInfoSkillEntryDto[]>>;

  skillQuery: string;
  setSkillQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedSkillId: React.Dispatch<React.SetStateAction<number | null>>;

  isSkillsLoading: boolean;
  skillSuggestions: Array<{ id: number; name: string }>;

  addSkill: () => void;

  editingSkillId: string | null;
  setEditingSkillId: React.Dispatch<React.SetStateAction<string | null>>;
  editingSkillValue: string;
  setEditingSkillValue: React.Dispatch<React.SetStateAction<string>>;
}

const SkillsSection: React.FC<SkillsSectionProps> = ({
  skills,
  setSkills,
  skillQuery,
  setSkillQuery,
  setSelectedSkillId,
  isSkillsLoading,
  skillSuggestions,
  addSkill,
  editingSkillId,
  setEditingSkillId,
  editingSkillValue,
  setEditingSkillValue,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Skills"
      icon={<FaCode className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm overflow-hidden p-5 sm:p-6 space-y-4"
    >
      <div className="relative flex flex-col sm:flex-row gap-3 items-start">
        <AnimatedInput
          value={skillQuery}
          onChange={(e) => {
            setSkillQuery(e.target.value);
            setSelectedSkillId(null);
          }}
          label="Type a skill"
          containerClassName="flex-1"
        >
        {(isSkillsLoading || skillSuggestions.length > 0) && (
          <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
            {isSkillsLoading ? (
              <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
            ) : (
              skillSuggestions.map((skill) => (
                <button
                  key={skill.id}
                  type="button"
                  onClick={() => {
                    const skillId = String(skill.id);
                    const id = `${skillId}-${createClientId()}`;
                    setSkills((prev) => [...prev, { id, skillId, name: skill.name }]);
                    setSkillQuery('');
                    setSelectedSkillId(null);
                  }}
                  className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                >
                  {skill.name}
                </button>
              ))
            )}
          </div>
        )}
        </AnimatedInput>
        <button
          type="button"
          onClick={addSkill}
          className="h-14 rounded-xl bg-[var(--color-primary)] px-6 text-[var(--color-background)] font-medium hover:bg-[var(--color-primaryVariant)] transition-colors"
        >
          Add
        </button>
      </div>

      <div className="flex flex-wrap gap-2">
        {skills.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}
            <button
              type="button"
              onClick={() => setSkills((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      <div className="space-y-2">
        {skills.map((item, index) => (
          <div
            key={`skill-row-${item.id}`}
            className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3"
          >
            {editingSkillId === item.id ? (
              <AnimatedInput
                value={editingSkillValue}
                onChange={(e) => setEditingSkillValue(e.target.value)}
                label="Skill name"
                containerClassName="flex-1"
              />
            ) : (
              <span className="flex-1 text-[var(--color-textPrimary)]">{item.name}</span>
            )}

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setSkills((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < skills.length - 1 && setSkills((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingSkillId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    const nextValue = editingSkillValue.trim();
                    if (nextValue) {
                      setSkills((prev) =>
                        prev.map((entry) =>
                          entry.id === item.id
                            ? {
                                ...entry,
                                name: nextValue,
                                skillId: normalizeOptionId('skill', nextValue),
                              }
                            : entry,
                        ),
                      );
                    }
                    setEditingSkillId(null);
                    setEditingSkillValue('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingSkillId(item.id);
                    setEditingSkillValue(item.name);
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setSkills((prev) => prev.filter((entry) => entry.id !== item.id))}
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

export default SkillsSection;
