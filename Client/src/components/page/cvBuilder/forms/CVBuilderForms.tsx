/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState, useRef } from "react";
import { useSkills } from "../../../../hooks/useSkills";
import { useLanguages } from "../../../../hooks/useLanguages";
import { useUniversities } from "../../../../hooks/useUniversities";
import { usePositions } from "../../../../hooks/usePositions";
import { useOnClickOutside } from "../../../../hooks/useOnClickOutside";

const InputField = ({
  label,
  value,
  onChange,
  placeholder,
  type = "text",
}: any) => (
  <div className="flex flex-col gap-1">
    <label className="text-sm font-medium text-[var(--color-textPrimary)]">
      {label}
    </label>
    <input
      type={type}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)]"
    />
  </div>
);

const AutocompleteField = ({
  label,
  value,
  onChange,
  placeholder,
  hookResult,
  onSelect,
  getDisplayValue,
}: any) => {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef(null);
  useOnClickOutside(containerRef, () => setIsOpen(false));

  return (
    <div ref={containerRef} className="flex flex-col gap-1 relative z-10">
      <label className="text-sm font-medium text-[var(--color-textPrimary)]">
        {label}
      </label>
      <input
        type="text"
        value={value}
        onFocus={() => setIsOpen(true)}
        onChange={(e) => {
          onChange(e);
          setIsOpen(true);
        }}
        placeholder={placeholder}
        className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)]"
      />
      {isOpen && (hookResult.isLoading || hookResult.items.length > 0) && (
        <div className="absolute top-[100%] left-0 right-0 mt-1 max-h-48 overflow-y-auto rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg">
          {hookResult.isLoading ? (
            <p className="p-2 text-sm text-[var(--color-textSecondary)]">
              Loading...
            </p>
          ) : (
            hookResult.items.map((item: any) => (
              <button
                key={item.id}
                type="button"
                className="w-full text-left p-2 text-sm hover:bg-[var(--color-background)]"
                onClick={() => {
                  onSelect(item);
                  setIsOpen(false);
                }}
              >
                {getDisplayValue(item)}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export const SkillForm = ({ formData, setFormData }: any) => {
  const [query, setQuery] = useState(formData.name || "");
  const hookResult = useSkills(query);

  return (
    <div className="space-y-4">
      <AutocompleteField
        label="Skill Name"
        value={query}
        onChange={(e: any) => {
          setQuery(e.target.value);
          setFormData({
            ...formData,
            name: e.target.value,
            skillId: undefined,
          });
        }}
        placeholder="e.g. JavaScript"
        hookResult={hookResult}
        onSelect={(item: any) => {
          setQuery(item.name);
          setFormData({
            ...formData,
            name: item.name,
            skillId: String(item.id),
          });
        }}
        getDisplayValue={(item: any) => item.name}
      />
      <InputField
        label="Proficiency Level (Optional)"
        value={formData.level || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, level: e.target.value })
        }
        placeholder="e.g. Expert, Beginner"
      />
    </div>
  );
};

export const LanguageForm = ({ formData, setFormData }: any) => {
  const [query, setQuery] = useState(formData.name || "");
  const hookResult = useLanguages(query);

  return (
    <div className="space-y-4">
      <AutocompleteField
        label="Language Name"
        value={query}
        onChange={(e: any) => {
          setQuery(e.target.value);
          setFormData({
            ...formData,
            name: e.target.value,
            languageId: undefined,
          });
        }}
        placeholder="e.g. English"
        hookResult={hookResult}
        onSelect={(item: any) => {
          setQuery(item.name);
          setFormData({
            ...formData,
            name: item.name,
            languageId: String(item.id),
          });
        }}
        getDisplayValue={(item: any) => item.name}
      />
      <InputField
        label="Proficiency (Optional)"
        value={formData.proficiency || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, proficiency: e.target.value })
        }
        placeholder="e.g. Fluent, Native"
      />
    </div>
  );
};

export const EducationForm = ({ formData, setFormData }: any) => {
  const [query, setQuery] = useState(formData.universityName || "");
  const hookResult = useUniversities(query);

  return (
    <div className="space-y-4">
      <AutocompleteField
        label="University"
        value={query}
        onChange={(e: any) => {
          setQuery(e.target.value);
          setFormData({
            ...formData,
            universityName: e.target.value,
            universityId: undefined,
          });
        }}
        placeholder="e.g. Stanford University"
        hookResult={hookResult}
        onSelect={(item: any) => {
          setQuery(item.name);
          setFormData({
            ...formData,
            universityName: item.name,
            universityId: item.id,
          });
        }}
        getDisplayValue={(item: any) => item.name}
      />
      <InputField
        label="Degree"
        value={formData.degree || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, degree: e.target.value })
        }
        placeholder="e.g. Bachelor's"
      />
      <InputField
        label="Field of Study"
        value={formData.fieldOfStudy || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, fieldOfStudy: e.target.value })
        }
        placeholder="e.g. Computer Science"
      />
      <div className="grid grid-cols-2 gap-4">
        <InputField
          type="date"
          label="Start Date"
          value={formData.startDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, startDate: e.target.value })
          }
        />
        <InputField
          type="date"
          label="End Date (Optional)"
          value={formData.endDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, endDate: e.target.value })
          }
        />
      </div>
      <InputField
        label="Grade (Optional)"
        value={formData.grade || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, grade: e.target.value })
        }
        placeholder="e.g. 4.0 GPA"
      />
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium text-[var(--color-textPrimary)]">
          Description (Optional)
        </label>
        <textarea
          value={formData.description || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, description: e.target.value })
          }
          placeholder="What did you do?"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)] min-h-[80px]"
        />
      </div>
    </div>
  );
};

export const ExperienceForm = ({ formData, setFormData }: any) => {
  const [query, setQuery] = useState(formData.position || "");
  const hookResult = usePositions(query);

  return (
    <div className="space-y-4">
      <AutocompleteField
        label="Position"
        value={query}
        onChange={(e: any) => {
          setQuery(e.target.value);
          setFormData({
            ...formData,
            position: e.target.value,
            positionId: undefined,
          });
        }}
        placeholder="e.g. Software Engineer"
        hookResult={hookResult}
        onSelect={(item: any) => {
          setQuery(item.name);
          setFormData({
            ...formData,
            position: item.name,
            positionId: String(item.id),
          });
        }}
        getDisplayValue={(item: any) => item.name}
      />
      <InputField
        label="Company Name"
        value={formData.company || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, company: e.target.value })
        }
        placeholder="e.g. Acme Corp"
      />
      <InputField
        label="Location (Optional)"
        value={formData.location || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, location: e.target.value })
        }
        placeholder="e.g. Remote, San Francisco"
      />
      <div className="grid grid-cols-2 gap-4">
        <InputField
          type="date"
          label="Start Date"
          value={formData.startDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, startDate: e.target.value })
          }
        />
        <InputField
          type="date"
          label="End Date (Optional)"
          value={formData.endDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, endDate: e.target.value })
          }
          disabled={formData.currentlyWorking}
        />
      </div>
      <label className="flex items-center gap-2 text-sm text-[var(--color-textPrimary)]">
        <input
          type="checkbox"
          checked={formData.currentlyWorking || false}
          onChange={(e) =>
            setFormData({ ...formData, currentlyWorking: e.target.checked })
          }
        />
        I currently work here
      </label>
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium text-[var(--color-textPrimary)]">
          Description (Optional)
        </label>
        <textarea
          value={formData.description || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, description: e.target.value })
          }
          placeholder="What did you do?"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)] min-h-[80px]"
        />
      </div>
    </div>
  );
};

export const ProjectForm = ({ formData, setFormData }: any) => {
  return (
    <div className="space-y-4">
      <InputField
        label="Project Name"
        value={formData.name || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, name: e.target.value })
        }
        placeholder="e.g. E-Commerce Platform"
      />
      <InputField
        label="Github URL (Optional)"
        value={formData.repositoryUrl || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, repositoryUrl: e.target.value })
        }
        placeholder="https://github.com/..."
      />
      <InputField
        label="Live URL (Optional)"
        value={formData.liveUrl || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, liveUrl: e.target.value })
        }
        placeholder="https://..."
      />
      <div className="grid grid-cols-2 gap-4">
        <InputField
          type="date"
          label="Start Date (Optional)"
          value={formData.startDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, startDate: e.target.value })
          }
        />
        <InputField
          type="date"
          label="End Date (Optional)"
          value={formData.endDate || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, endDate: e.target.value })
          }
        />
      </div>
      <InputField
        label="Technologies (comma-separated) (Optional)"
        value={formData.technologies ? formData.technologies.join(", ") : ""}
        onChange={(e: any) =>
          setFormData({
            ...formData,
            technologies: e.target.value
              .split(",")
              .map((t: string) => t.trim()),
          })
        }
        placeholder="React, Node.js, ..."
      />
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium text-[var(--color-textPrimary)]">
          Description (Optional)
        </label>
        <textarea
          value={formData.description || ""}
          onChange={(e: any) =>
            setFormData({ ...formData, description: e.target.value })
          }
          placeholder="Describe your project"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)] min-h-[80px]"
        />
      </div>
    </div>
  );
};

export const CertificateForm = ({ formData, setFormData }: any) => {
  return (
    <div className="space-y-4">
      <InputField
        label="Certificate Name"
        value={formData.name || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, name: e.target.value })
        }
        placeholder="e.g. AWS Solutions Architect"
      />
      <InputField
        label="Issuer (Optional)"
        value={formData.issuer || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, issuer: e.target.value })
        }
        placeholder="e.g. Amazon Web Services"
      />
      <InputField
        type="date"
        label="Date (Optional)"
        value={formData.date || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, date: e.target.value })
        }
      />
      <InputField
        label="Credential URL (Optional)"
        value={formData.credentialUrl || ""}
        onChange={(e: any) =>
          setFormData({ ...formData, credentialUrl: e.target.value })
        }
        placeholder="https://..."
      />
    </div>
  );
};
