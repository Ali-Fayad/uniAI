/**
 * ProjectsSection
 *
 * Responsibility:
 * - Render project entry inputs and the project list editor.
 *
 * Does NOT:
 * - Persist personal info
 * - Own controller state (provided via props)
 */

import React from 'react';
import { FaFolderOpen } from 'react-icons/fa';
import type { PersonalInfoProjectEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';
import AnimatedTextarea from '../../../common/AnimatedTextarea';

export interface ProjectsSectionProps {
  projectName: string;
  setProjectName: React.Dispatch<React.SetStateAction<string>>;
  projectDescription: string;
  setProjectDescription: React.Dispatch<React.SetStateAction<string>>;
  projectRepositoryUrl: string;
  setProjectRepositoryUrl: React.Dispatch<React.SetStateAction<string>>;
  projectLiveUrl: string;
  setProjectLiveUrl: React.Dispatch<React.SetStateAction<string>>;
  addProject: () => void;

  projects: PersonalInfoProjectEntryDto[];
  setProjects: React.Dispatch<React.SetStateAction<PersonalInfoProjectEntryDto[]>>;

}

const ProjectsSection: React.FC<ProjectsSectionProps> = ({
  projectName,
  setProjectName,
  projectDescription,
  setProjectDescription,
  projectRepositoryUrl,
  setProjectRepositoryUrl,
  projectLiveUrl,
  setProjectLiveUrl,
  addProject,
  projects,
  setProjects,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Projects"
      icon={<FaFolderOpen className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-start">
        <AnimatedInput
          value={projectName}
          onChange={(e) => setProjectName(e.target.value)}
          label="Project name"
        />
        <AnimatedInput
          value={projectRepositoryUrl}
          onChange={(e) => setProjectRepositoryUrl(e.target.value)}
          label="Repository URL"
        />
        <AnimatedInput
          value={projectLiveUrl}
          onChange={(e) => setProjectLiveUrl(e.target.value)}
          label="Live URL"
        />
        <button
          type="button"
          onClick={addProject}
          className="h-14 rounded-xl bg-[var(--color-primary)] px-4 text-[var(--color-background)] font-medium hover:bg-[var(--color-primaryVariant)] transition-colors"
        >
          Add Project
        </button>
      </div>
      <AnimatedTextarea
        value={projectDescription}
        onChange={(e) => setProjectDescription(e.target.value)}
        label="Project description"
        rows={3}
      />

      <div className="flex flex-wrap gap-2 mt-4">
        {projects.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}
            <button
              type="button"
              onClick={() => setProjects((prev) => prev.filter((entry) => entry.id !== item.id))}
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

export default ProjectsSection;
