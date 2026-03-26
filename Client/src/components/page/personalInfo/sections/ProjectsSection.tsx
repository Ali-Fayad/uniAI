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
import type { PersonalInfoProjectEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { moveItem } from '../personalInfoUtils';

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

  editingProjectId: string | null;
  setEditingProjectId: React.Dispatch<React.SetStateAction<string | null>>;
  editingProjectName: string;
  setEditingProjectName: React.Dispatch<React.SetStateAction<string>>;
  editingProjectDescription: string;
  setEditingProjectDescription: React.Dispatch<React.SetStateAction<string>>;
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
  editingProjectId,
  setEditingProjectId,
  editingProjectName,
  setEditingProjectName,
  editingProjectDescription,
  setEditingProjectDescription,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Projects"
      className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <input
          value={projectName}
          onChange={(e) => setProjectName(e.target.value)}
          placeholder="Project name"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={projectRepositoryUrl}
          onChange={(e) => setProjectRepositoryUrl(e.target.value)}
          placeholder="Repository URL"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={projectLiveUrl}
          onChange={(e) => setProjectLiveUrl(e.target.value)}
          placeholder="Live URL"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <button
          type="button"
          onClick={addProject}
          className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
        >
          Add Project
        </button>
      </div>
      <textarea
        value={projectDescription}
        onChange={(e) => setProjectDescription(e.target.value)}
        placeholder="Project description"
        rows={3}
        className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
      />

      <div className="space-y-2">
        {projects.map((item, index) => (
          <div key={item.id} className="rounded-md border border-[var(--color-border)] p-3 space-y-2">
            {editingProjectId === item.id ? (
              <>
                <input
                  value={editingProjectName}
                  onChange={(e) => setEditingProjectName(e.target.value)}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
                <textarea
                  value={editingProjectDescription}
                  onChange={(e) => setEditingProjectDescription(e.target.value)}
                  rows={2}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
              </>
            ) : (
              <>
                <p className="text-[var(--color-textPrimary)] font-medium">{item.name}</p>
                {item.description && <p className="text-sm text-[var(--color-textSecondary)]">{item.description}</p>}
              </>
            )}
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setProjects((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < projects.length - 1 && setProjects((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingProjectId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    const nextName = editingProjectName.trim();
                    if (!nextName) {
                      return;
                    }
                    setProjects((prev) =>
                      prev.map((entry) =>
                        entry.id === item.id
                          ? {
                              ...entry,
                              name: nextName,
                              description: editingProjectDescription.trim(),
                            }
                          : entry,
                      ),
                    );
                    setEditingProjectId(null);
                    setEditingProjectName('');
                    setEditingProjectDescription('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingProjectId(item.id);
                    setEditingProjectName(item.name);
                    setEditingProjectDescription(item.description ?? '');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setProjects((prev) => prev.filter((entry) => entry.id !== item.id))}
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

export default ProjectsSection;
