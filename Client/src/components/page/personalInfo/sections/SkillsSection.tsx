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

import React, { useState, useRef } from 'react';
import { useOnClickOutside } from '../../../../hooks/useOnClickOutside';
import { FaCode } from 'react-icons/fa';
import type { PersonalInfoSkillEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { createClientId } from '../personalInfoUtils';
import AnimatedInput from '../../../common/AnimatedInput';
import FormButton from '../../../settings/FormButton';

export interface SkillsSectionProps {
  skills: PersonalInfoSkillEntryDto[];
  setSkills: React.Dispatch<React.SetStateAction<PersonalInfoSkillEntryDto[]>>;

  skillQuery: string;
  setSkillQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedSkillId: React.Dispatch<React.SetStateAction<number | null>>;

  isSkillsLoading: boolean;
  skillSuggestions: Array<{ id: number; name: string }>;

  addSkill: () => void;

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
}) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  useOnClickOutside(containerRef, () => setIsDropdownOpen(false));

  return (
    <PersonalInfoSectionCard
      title="Skills"
      icon={<FaCode className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div ref={containerRef} className="relative flex flex-col sm:flex-row gap-3 items-start">
        <AnimatedInput
          value={skillQuery}
          onFocus={() => setIsDropdownOpen(true)}
          onChange={(e) => {
            setIsDropdownOpen(true);
            setSkillQuery(e.target.value);
            setSelectedSkillId(null);
          }}
          label="Type a skill"
          containerClassName="flex-1"
        >
        {isDropdownOpen && (isSkillsLoading || skillSuggestions.length > 0) && (
          <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-72 overflow-auto">
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
        <FormButton
          variant="primary"
          type="button"
          onClick={addSkill}
          className="h-14 px-6 shrink-0"
        >
          Add
        </FormButton>
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

    </PersonalInfoSectionCard>
  );
};

export default SkillsSection;
