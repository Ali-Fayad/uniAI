/**
 * ExperienceSection
 *
 * Responsibility:
 * - Render experience inputs, position suggestions, and the experience entry list.
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
import AnimatedInput from '../../../common/AnimatedInput';
import AnimatedTextarea from '../../../common/AnimatedTextarea';
import FormButton from '../../../settings/FormButton';

export interface ExperienceSectionProps {
  experience: PersonalInfoExperienceEntryDto[];
  setExperience: React.Dispatch<React.SetStateAction<PersonalInfoExperienceEntryDto[]>>;

  positionQuery: string;
  setPositionQuery: React.Dispatch<React.SetStateAction<string>>;
  setSelectedPositionId: React.Dispatch<React.SetStateAction<number | null>>;

  experienceCompany: string;
  setExperienceCompany: React.Dispatch<React.SetStateAction<string>>;
  experienceLocation: string;
  setExperienceLocation: React.Dispatch<React.SetStateAction<string>>;
  experienceStartDate: string;
  setExperienceStartDate: React.Dispatch<React.SetStateAction<string>>;
  experienceEndDate: string;
  setExperienceEndDate: React.Dispatch<React.SetStateAction<string>>;
  experienceCurrentlyWorking: boolean;
  setExperienceCurrentlyWorking: React.Dispatch<React.SetStateAction<boolean>>;
  experienceDescription: string;
  setExperienceDescription: React.Dispatch<React.SetStateAction<string>>;

  isPositionsLoading: boolean;
  positionSuggestions: Array<{ id: number; name: string }>;

  addExperience: () => void;

}

const ExperienceSection: React.FC<ExperienceSectionProps> = ({
  experience,
  setExperience,
  positionQuery,
  setPositionQuery,
  setSelectedPositionId,
  experienceCompany,
  setExperienceCompany,
  experienceLocation,
  setExperienceLocation,
  experienceStartDate,
  setExperienceStartDate,
  experienceEndDate,
  setExperienceEndDate,
  experienceCurrentlyWorking,
  setExperienceCurrentlyWorking,
  experienceDescription,
  setExperienceDescription,
  isPositionsLoading,
  positionSuggestions,
  addExperience,
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
      <div ref={containerRef} className="grid grid-cols-1 sm:grid-cols-5 gap-3 items-start relative">
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
                      setIsDropdownOpen(false);
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
        <FormButton
          variant="primary"
          type="button"
          onClick={addExperience}
          className="sm:col-span-1 h-14 px-4 shrink-0"
        >
          Add
        </FormButton>
      </div>

      <div className="grid grid-cols-1 gap-3">
        <AnimatedInput
          value={experienceLocation}
          onChange={(e) => setExperienceLocation(e.target.value)}
          label="Location"
        />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <AnimatedInput
          type="date"
          value={experienceStartDate}
          onChange={(e) => setExperienceStartDate(e.target.value)}
          label="Start Date"
        />
        <AnimatedInput
          type="date"
          value={experienceEndDate}
          onChange={(e) => setExperienceEndDate(e.target.value)}
          label="End Date"
          disabled={experienceCurrentlyWorking}
        />
      </div>

      <label className="inline-flex items-center gap-2 text-sm text-[var(--color-textPrimary)]">
        <input
          type="checkbox"
          checked={experienceCurrentlyWorking}
          onChange={(e) => {
            const next = e.target.checked;
            setExperienceCurrentlyWorking(next);
            if (next) {
              setExperienceEndDate('');
            }
          }}
          className="h-4 w-4 accent-[var(--color-primary)]"
        />
        I currently work here
      </label>

      <AnimatedTextarea
        value={experienceDescription}
        onChange={(e) => setExperienceDescription(e.target.value)}
        label="Description"
        containerClassName="w-full"
      />

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

    </PersonalInfoSectionCard>
  );
};

export default ExperienceSection;
