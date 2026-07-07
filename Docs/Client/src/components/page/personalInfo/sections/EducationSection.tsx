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

import React, { useState, useRef } from 'react';
import { useOnClickOutside } from '../../../../hooks/useOnClickOutside';
import { FaGraduationCap } from 'react-icons/fa';
import type { PersonalInfoEducationEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';
import AnimatedTextarea from '../../../common/AnimatedTextarea';
import FormButton from '../../../settings/FormButton';
import { formatEducationLabel } from '../personalInfoStateHelpers';

export interface EducationSectionProps {
  education: PersonalInfoEducationEntryDto[];
  setEducation: React.Dispatch<React.SetStateAction<PersonalInfoEducationEntryDto[]>>;

  universityQuery: string;
  setUniversityQuery: React.Dispatch<React.SetStateAction<string>>;
  selectedUniversityId: number | null;
  setSelectedUniversityId: React.Dispatch<React.SetStateAction<number | null>>;
  educationDegree: string;
  setEducationDegree: React.Dispatch<React.SetStateAction<string>>;
  educationFieldOfStudy: string;
  setEducationFieldOfStudy: React.Dispatch<React.SetStateAction<string>>;
  educationStartDate: string;
  setEducationStartDate: React.Dispatch<React.SetStateAction<string>>;
  educationEndDate: string;
  setEducationEndDate: React.Dispatch<React.SetStateAction<string>>;
  educationGrade: string;
  setEducationGrade: React.Dispatch<React.SetStateAction<string>>;
  educationDescription: string;
  setEducationDescription: React.Dispatch<React.SetStateAction<string>>;

  isUniversitiesLoading: boolean;
  universitySuggestions: Array<{ id: number; name: string; acronym?: string | null }>;

  addEducation: () => void;
  setError: React.Dispatch<React.SetStateAction<string | null>>;

}

const EducationSection: React.FC<EducationSectionProps> = ({
  education,
  setEducation,
  universityQuery,
  setUniversityQuery,
  selectedUniversityId,
  setSelectedUniversityId,
  educationDegree,
  setEducationDegree,
  educationFieldOfStudy,
  setEducationFieldOfStudy,
  educationStartDate,
  setEducationStartDate,
  educationEndDate,
  setEducationEndDate,
  educationGrade,
  setEducationGrade,
  educationDescription,
  setEducationDescription,
  isUniversitiesLoading,
  universitySuggestions,
  addEducation,
  setError,
}) => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  useOnClickOutside(containerRef, () => setIsDropdownOpen(false));

  return (
    <PersonalInfoSectionCard
      title="Education"
      icon={<FaGraduationCap className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div ref={containerRef} className="relative flex flex-col sm:flex-row gap-3 items-start">
        <div className="relative flex-1 w-full">
          <AnimatedInput
            value={universityQuery}
            onFocus={() => setIsDropdownOpen(true)}
            onChange={(e) => {
              setIsDropdownOpen(true);
              setUniversityQuery(e.target.value);
              setSelectedUniversityId(null);
            }}
            label="University"
            containerClassName="w-full"
          >
            {isDropdownOpen && (isUniversitiesLoading || universitySuggestions.length > 0) && (
              <div className="absolute top-[calc(100%+4px)] left-0 right-0 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-72 overflow-auto">
                {isUniversitiesLoading ? (
                  <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
                ) : (
                  universitySuggestions.map((uni) => (
                    <button
                      key={uni.id}
                      type="button"
                      onClick={() => {
                        setUniversityQuery(uni.name);
                        setSelectedUniversityId(uni.id);
                        setError(null);
                        setIsDropdownOpen(false);
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
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <AnimatedInput
          value={educationDegree}
          onChange={(e) => setEducationDegree(e.target.value)}
          label="Degree"
          containerClassName="w-full"
        />
        <AnimatedInput
          value={educationFieldOfStudy}
          onChange={(e) => setEducationFieldOfStudy(e.target.value)}
          label="Field Of Study"
          containerClassName="w-full"
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <AnimatedInput
          type="date"
          value={educationStartDate}
          onChange={(e) => setEducationStartDate(e.target.value)}
          label="Start Date"
          containerClassName="w-full"
        />
        <AnimatedInput
          type="date"
          value={educationEndDate}
          onChange={(e) => setEducationEndDate(e.target.value)}
          label="End Date"
          containerClassName="w-full"
        />
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <AnimatedInput
          value={educationGrade}
          onChange={(e) => setEducationGrade(e.target.value)}
          label="Grade"
          containerClassName="w-full"
        />
        <FormButton
          variant="primary"
          type="button"
          onClick={addEducation}
          className="h-14 px-6 shrink-0 sm:self-end"
          disabled={
            !universityQuery.trim() ||
            selectedUniversityId === null ||
            !educationDegree.trim() ||
            !educationFieldOfStudy.trim() ||
            !educationStartDate.trim()
          }
        >
          Add
        </FormButton>
      </div>

      <AnimatedTextarea
        value={educationDescription}
        onChange={(e) => setEducationDescription(e.target.value)}
        label="Description"
        containerClassName="w-full"
      />

      <div className="flex flex-wrap gap-2">
        {education.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {formatEducationLabel(item)}
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

    </PersonalInfoSectionCard>
  );
};

export default EducationSection;
