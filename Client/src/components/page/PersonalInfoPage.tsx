import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { cvService } from '../../services/cv';
import { ROUTES } from '../../router';
import type {
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoResponseDto,
  PersonalInfoSkillEntryDto,
} from '../../types/dto';
import { useLanguages } from '../../hooks/useLanguages';
import { usePositions } from '../../hooks/usePositions';
import { useSkills } from '../../hooks/useSkills';
import { useUniversities } from '../../hooks/useUniversities';
import LoadingSpinner from '../common/LoadingSpinner';

type PersonalInfoLocationState = {
  fromOnboarding?: boolean;
};

type BasicFormState = {
  phone: string;
  address: string;
  jobTitle: string;
  company: string;
  linkedin: string;
  github: string;
  portfolio: string;
  summary: string;
};

const createClientId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

const normalizeOptionId = (prefix: string, label: string) =>
  `${prefix}-${label.trim().toLowerCase().replace(/\s+/g, '-')}`;

const moveItem = <T,>(list: T[], from: number, to: number): T[] => {
  const next = [...list];
  const [item] = next.splice(from, 1);
  next.splice(to, 0, item);
  return next;
};

const PersonalInfoPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const fromOnboarding = Boolean((location.state as PersonalInfoLocationState | null)?.fromOnboarding);

  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveToast, setSaveToast] = useState<string | null>(null);

  const [form, setForm] = useState<BasicFormState>({
    phone: '',
    address: '',
    jobTitle: '',
    company: '',
    linkedin: '',
    github: '',
    portfolio: '',
    summary: '',
  });

  const [education, setEducation] = useState<PersonalInfoEducationEntryDto[]>([]);
  const [skills, setSkills] = useState<PersonalInfoSkillEntryDto[]>([]);
  const [languages, setLanguages] = useState<PersonalInfoLanguageEntryDto[]>([]);
  const [experience, setExperience] = useState<PersonalInfoExperienceEntryDto[]>([]);
  const [projects, setProjects] = useState<PersonalInfoProjectEntryDto[]>([]);
  const [certificates, setCertificates] = useState<PersonalInfoCertificateEntryDto[]>([]);

  const [initialSnapshot, setInitialSnapshot] = useState('');

  const [universityQuery, setUniversityQuery] = useState('');
  const [selectedUniversityId, setSelectedUniversityId] = useState<number | null>(null);

  const [skillQuery, setSkillQuery] = useState('');
  const [selectedSkillId, setSelectedSkillId] = useState<number | null>(null);

  const [languageQuery, setLanguageQuery] = useState('');
  const [selectedLanguageId, setSelectedLanguageId] = useState<number | null>(null);

  const [positionQuery, setPositionQuery] = useState('');
  const [selectedPositionId, setSelectedPositionId] = useState<number | null>(null);
  const [experienceCompany, setExperienceCompany] = useState('');

  const [projectName, setProjectName] = useState('');
  const [projectDescription, setProjectDescription] = useState('');
  const [projectRepositoryUrl, setProjectRepositoryUrl] = useState('');
  const [projectLiveUrl, setProjectLiveUrl] = useState('');

  const [certificateName, setCertificateName] = useState('');
  const [certificateIssuer, setCertificateIssuer] = useState('');
  const [certificateCredentialUrl, setCertificateCredentialUrl] = useState('');

  const [editingEducationId, setEditingEducationId] = useState<string | null>(null);
  const [editingEducationValue, setEditingEducationValue] = useState('');

  const [editingSkillId, setEditingSkillId] = useState<string | null>(null);
  const [editingSkillValue, setEditingSkillValue] = useState('');

  const [editingLanguageId, setEditingLanguageId] = useState<string | null>(null);
  const [editingLanguageValue, setEditingLanguageValue] = useState('');

  const [editingExperienceId, setEditingExperienceId] = useState<string | null>(null);
  const [editingExperiencePosition, setEditingExperiencePosition] = useState('');
  const [editingExperienceCompany, setEditingExperienceCompany] = useState('');

  const [editingProjectId, setEditingProjectId] = useState<string | null>(null);
  const [editingProjectName, setEditingProjectName] = useState('');
  const [editingProjectDescription, setEditingProjectDescription] = useState('');

  const [editingCertificateId, setEditingCertificateId] = useState<string | null>(null);
  const [editingCertificateName, setEditingCertificateName] = useState('');
  const [editingCertificateIssuer, setEditingCertificateIssuer] = useState('');

  const { items: universitySuggestions, isLoading: isUniversitiesLoading } = useUniversities(universityQuery);
  const { items: skillSuggestions, isLoading: isSkillsLoading } = useSkills(skillQuery);
  const { items: languageSuggestions, isLoading: isLanguagesLoading } = useLanguages(languageQuery);
  const { items: positionSuggestions, isLoading: isPositionsLoading } = usePositions(positionQuery);

  const snapshot = useMemo(
    () =>
      JSON.stringify({
        form,
        education,
        skills,
        languages,
        experience,
        projects,
        certificates,
      }),
    [certificates, education, experience, form, languages, projects, skills]
  );

  const isDirty = initialSnapshot.length > 0 && snapshot !== initialSnapshot;

  const applyResponseToState = (data: PersonalInfoResponseDto) => {
    const mappedEducation = (data.education ?? []).map((item) => ({
      id: item.id || createClientId(),
      universityId: item.universityId ?? null,
      universityName: item.universityName ?? '',
    }));

    const mappedSkills = (data.skills ?? []).map((item) => ({
      id: item.id || createClientId(),
      skillId: item.skillId || normalizeOptionId('skill', item.name),
      name: item.name || '',
    }));

    const mappedLanguages = (data.languages ?? []).map((item) => ({
      id: item.id || createClientId(),
      languageId: item.languageId || normalizeOptionId('language', item.name),
      name: item.name || '',
    }));

    const mappedExperience = (data.experience ?? []).map((item) => ({
      id: item.id || createClientId(),
      positionId: item.positionId || createClientId(),
      position: item.position || '',
      company: item.company || '',
    }));

    const mappedProjects = (data.projects ?? []).map((item) => ({
      id: item.id || createClientId(),
      name: item.name || '',
      description: item.description || '',
      repositoryUrl: item.repositoryUrl || '',
      liveUrl: item.liveUrl || '',
    }));

    const mappedCertificates = (data.certificates ?? []).map((item) => ({
      id: item.id || createClientId(),
      name: item.name || '',
      issuer: item.issuer || '',
      credentialUrl: item.credentialUrl || '',
    }));

    const nextForm: BasicFormState = {
      phone: data.phone ?? '',
      address: data.address ?? '',
      jobTitle: data.jobTitle ?? '',
      company: data.company ?? '',
      linkedin: data.linkedin ?? '',
      github: data.github ?? '',
      portfolio: data.portfolio ?? '',
      summary: data.summary ?? '',
    };

    setForm(nextForm);
    setEducation(mappedEducation);
    setSkills(mappedSkills);
    setLanguages(mappedLanguages);
    setExperience(mappedExperience);
    setProjects(mappedProjects);
    setCertificates(mappedCertificates);

    setInitialSnapshot(
      JSON.stringify({
        form: nextForm,
        education: mappedEducation,
        skills: mappedSkills,
        languages: mappedLanguages,
        experience: mappedExperience,
        projects: mappedProjects,
        certificates: mappedCertificates,
      })
    );
  };

  useEffect(() => {
    const loadPersonalInfo = async () => {
      setIsPageLoading(true);
      setError(null);
      try {
        const response = await cvService.getPersonalInfo();
        applyResponseToState(response);
      } catch {
        setError('Failed to load your personal information. Please try again.');
      } finally {
        setIsPageLoading(false);
      }
    };

    void loadPersonalInfo();
  }, []);

  useEffect(() => {
    if (!saveToast) {
      return;
    }

    const timeout = setTimeout(() => setSaveToast(null), 3000);
    return () => clearTimeout(timeout);
  }, [saveToast]);

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!isDirty) {
        return;
      }
      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [isDirty]);

  const setField = (field: keyof BasicFormState, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const confirmNavigationIfDirty = (to: string) => {
    if (isDirty && !window.confirm('You have unsaved changes. Leave this page?')) {
      return;
    }
    navigate(to);
  };

  const addEducation = () => {
    const value = universityQuery.trim();
    if (!value || selectedUniversityId === null) {
      setError('Please pick a university from the list before adding.');
      return;
    }

    const id = `uni-${selectedUniversityId}-${createClientId()}`;

    setEducation((prev) => [
      ...prev,
      {
        id,
        universityId: selectedUniversityId,
        universityName: value,
      },
    ]);

    setUniversityQuery('');
    setSelectedUniversityId(null);
    setError(null);
  };

  const addSkill = () => {
    const value = skillQuery.trim();
    if (!value || selectedSkillId === null) {
      setError('Please pick a skill from the catalog list before adding.');
      return;
    }
    const skillId = String(selectedSkillId);
    setSkills((prev) => [
      ...prev,
      {
        id: `${skillId}-${createClientId()}`,
        skillId,
        name: value,
      },
    ]);

    setSkillQuery('');
    setSelectedSkillId(null);
    setError(null);
  };

  const addLanguage = () => {
    const value = languageQuery.trim();
    if (!value || selectedLanguageId === null) {
      setError('Please pick a language from the catalog list before adding.');
      return;
    }
    const languageId = String(selectedLanguageId);
    setLanguages((prev) => [
      ...prev,
      {
        id: `${languageId}-${createClientId()}`,
        languageId,
        name: value,
      },
    ]);

    setLanguageQuery('');
    setSelectedLanguageId(null);
    setError(null);
  };

  const addExperience = () => {
    const position = positionQuery.trim();
    const company = experienceCompany.trim();
    if (!position || selectedPositionId === null) {
      setError('Please pick a position from the catalog list before adding.');
      return;
    }

    const positionId = String(selectedPositionId);
    setExperience((prev) => [
      ...prev,
      {
        id: `${positionId}-${createClientId()}`,
        positionId,
        position,
        company,
      },
    ]);

    setPositionQuery('');
    setExperienceCompany('');
    setSelectedPositionId(null);
    setError(null);
  };

  const addProject = () => {
    const name = projectName.trim();
    if (!name) {
      setError('Project name is required.');
      return;
    }

    setProjects((prev) => [
      ...prev,
      {
        id: createClientId(),
        name,
        description: projectDescription.trim(),
        repositoryUrl: projectRepositoryUrl.trim(),
        liveUrl: projectLiveUrl.trim(),
      },
    ]);

    setProjectName('');
    setProjectDescription('');
    setProjectRepositoryUrl('');
    setProjectLiveUrl('');
    setError(null);
  };

  const addCertificate = () => {
    const name = certificateName.trim();
    if (!name) {
      setError('Certificate name is required.');
      return;
    }

    setCertificates((prev) => [
      ...prev,
      {
        id: createClientId(),
        name,
        issuer: certificateIssuer.trim(),
        credentialUrl: certificateCredentialUrl.trim(),
      },
    ]);

    setCertificateName('');
    setCertificateIssuer('');
    setCertificateCredentialUrl('');
    setError(null);
  };

  const validateBeforeSave = () => {
    const invalidEducation = education.some((item) => !item.universityName.trim() || !item.universityId);
    if (invalidEducation) {
      return 'Education entries must include a selected university.';
    }

    const invalidSkills = skills.some((item) => !item.name.trim() || !item.skillId);
    if (invalidSkills) {
      return 'Skills entries must include a selected skill.';
    }

    const invalidLanguages = languages.some((item) => !item.name.trim() || !item.languageId);
    if (invalidLanguages) {
      return 'Language entries must include a selected language.';
    }

    const invalidExperience = experience.some((item) => !item.position.trim() || !item.positionId);
    if (invalidExperience) {
      return 'Experience entries must include a selected position.';
    }

    const invalidProjects = projects.some((item) => !item.name.trim());
    if (invalidProjects) {
      return 'Project name is required for every project entry.';
    }

    const invalidCertificates = certificates.some((item) => !item.name.trim());
    if (invalidCertificates) {
      return 'Certificate name is required for every certificate entry.';
    }

    return null;
  };

  const saveChanges = async () => {
    const validationError = validateBeforeSave();
    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      const response = await cvService.updatePersonalInfo({
        ...form,
        education,
        skills,
        languages,
        experience,
        projects,
        certificates,
      });

      applyResponseToState(response);

      if (fromOnboarding) {
        navigate(ROUTES.CHAT, { replace: true });
        return;
      }

      setSaveToast('Personal info updated successfully.');
    } catch {
      setError('Failed to save your personal information. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isPageLoading) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading personal info..." />
      </div>
    );
  }

  return (
    <main className="flex-grow py-8 px-4 sm:px-6 lg:px-8 bg-[var(--color-background)]">
      {saveToast && (
        <div className="fixed right-4 top-20 z-50 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3 text-sm text-[var(--color-textPrimary)] shadow-lg">
          {saveToast}
        </div>
      )}

      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl sm:text-4xl font-bold text-[var(--color-textPrimary)]">Personal Info</h1>
          <p className="text-[var(--color-textSecondary)]">
            Add your profile details to personalize recommendations and CV support.
          </p>
        </div>

        {error && (
          <div className="rounded-md border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]">
            {error}
          </div>
        )}

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Basic Information</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <input
              value={form.phone}
              onChange={(e) => setField('phone', e.target.value)}
              placeholder="Phone"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={form.address}
              onChange={(e) => setField('address', e.target.value)}
              placeholder="Address"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={form.jobTitle}
              onChange={(e) => setField('jobTitle', e.target.value)}
              placeholder="Job title"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={form.company}
              onChange={(e) => setField('company', e.target.value)}
              placeholder="Company"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
          </div>
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Social & Web</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <input
              value={form.linkedin}
              onChange={(e) => setField('linkedin', e.target.value)}
              placeholder="LinkedIn URL"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={form.github}
              onChange={(e) => setField('github', e.target.value)}
              placeholder="GitHub URL"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={form.portfolio}
              onChange={(e) => setField('portfolio', e.target.value)}
              placeholder="Portfolio URL"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
          </div>
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Bio</h2>
          <textarea
            value={form.summary}
            onChange={(e) => setField('summary', e.target.value)}
            placeholder="Short summary"
            rows={5}
            className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
          />
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Education</h2>
          <div className="relative flex flex-col sm:flex-row gap-3">
            <input
              value={universityQuery}
              onChange={(e) => {
                setUniversityQuery(e.target.value);
                setSelectedUniversityId(null);
              }}
              placeholder="Type university"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <button
              type="button"
              onClick={addEducation}
              className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
            >
              Add
            </button>
            {(isUniversitiesLoading || universitySuggestions.length > 0) && (
              <div className="absolute top-11 left-0 right-0 sm:right-24 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
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
                      }}
                      className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                    >
                      {uni.name}{uni.acronym ? ` (${uni.acronym})` : ''}
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          <div className="flex flex-wrap gap-2">
            {education.map((item) => (
              <span key={item.id} className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]">
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
              <div key={`row-${item.id}`} className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3">
                {editingEducationId === item.id ? (
                  <input
                    value={editingEducationValue}
                    onChange={(e) => setEditingEducationValue(e.target.value)}
                    className="flex-1 rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
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
                              : entry
                          )
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
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Skills</h2>
          <div className="relative flex flex-col sm:flex-row gap-3">
            <input
              value={skillQuery}
              onChange={(e) => {
                setSkillQuery(e.target.value);
                setSelectedSkillId(null);
              }}
              placeholder="Type a skill"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <button
              type="button"
              onClick={addSkill}
              className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
            >
              Add
            </button>
            {(isSkillsLoading || skillSuggestions.length > 0) && (
              <div className="absolute top-11 left-0 right-0 sm:right-24 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
                {isSkillsLoading ? (
                  <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
                ) : (
                  skillSuggestions.map((skill) => (
                    <button
                      key={skill.id}
                      type="button"
                      onClick={() => {
                        setSkillQuery(skill.name);
                        setSelectedSkillId(skill.id);
                      }}
                      className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                    >
                      {skill.name}
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          <div className="flex flex-wrap gap-2">
            {skills.map((item) => (
              <span key={item.id} className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]">
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
              <div key={`skill-row-${item.id}`} className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3">
                {editingSkillId === item.id ? (
                  <input
                    value={editingSkillValue}
                    onChange={(e) => setEditingSkillValue(e.target.value)}
                    className="flex-1 rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
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
                                : entry
                            )
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
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Languages</h2>
          <div className="relative flex flex-col sm:flex-row gap-3">
            <input
              value={languageQuery}
              onChange={(e) => {
                setLanguageQuery(e.target.value);
                setSelectedLanguageId(null);
              }}
              placeholder="Type a language"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <button
              type="button"
              onClick={addLanguage}
              className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
            >
              Add
            </button>
            {(isLanguagesLoading || languageSuggestions.length > 0) && (
              <div className="absolute top-11 left-0 right-0 sm:right-24 rounded-md border border-[var(--color-border)] bg-[var(--color-surface)] shadow-lg z-20 max-h-56 overflow-auto">
                {isLanguagesLoading ? (
                  <p className="px-3 py-2 text-sm text-[var(--color-textSecondary)]">Loading suggestions...</p>
                ) : (
                  languageSuggestions.map((language) => (
                    <button
                      key={language.id}
                      type="button"
                      onClick={() => {
                        setLanguageQuery(language.name);
                        setSelectedLanguageId(language.id);
                      }}
                      className="w-full text-left px-3 py-2 text-sm text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
                    >
                      {language.name}
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          <div className="flex flex-wrap gap-2">
            {languages.map((item) => (
              <span key={item.id} className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]">
                {item.name}
                <button
                  type="button"
                  onClick={() => setLanguages((prev) => prev.filter((entry) => entry.id !== item.id))}
                  className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
                >
                  ×
                </button>
              </span>
            ))}
          </div>

          <div className="space-y-2">
            {languages.map((item, index) => (
              <div key={`language-row-${item.id}`} className="flex flex-col sm:flex-row sm:items-center gap-2 rounded-md border border-[var(--color-border)] p-3">
                {editingLanguageId === item.id ? (
                  <input
                    value={editingLanguageValue}
                    onChange={(e) => setEditingLanguageValue(e.target.value)}
                    className="flex-1 rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                  />
                ) : (
                  <span className="flex-1 text-[var(--color-textPrimary)]">{item.name}</span>
                )}

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => index > 0 && setLanguages((prev) => moveItem(prev, index, index - 1))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    ↑
                  </button>
                  <button
                    type="button"
                    onClick={() => index < languages.length - 1 && setLanguages((prev) => moveItem(prev, index, index + 1))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    ↓
                  </button>
                  {editingLanguageId === item.id ? (
                    <button
                      type="button"
                      onClick={() => {
                        const nextValue = editingLanguageValue.trim();
                        if (nextValue) {
                          setLanguages((prev) =>
                            prev.map((entry) =>
                              entry.id === item.id
                                ? {
                                    ...entry,
                                    name: nextValue,
                                    languageId: normalizeOptionId('language', nextValue),
                                  }
                                : entry
                            )
                          );
                        }
                        setEditingLanguageId(null);
                        setEditingLanguageValue('');
                      }}
                      className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                    >
                      Save
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => {
                        setEditingLanguageId(item.id);
                        setEditingLanguageValue(item.name);
                      }}
                      className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                    >
                      Edit
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={() => setLanguages((prev) => prev.filter((entry) => entry.id !== item.id))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Experience</h2>
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
              <span key={item.id} className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]">
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
              <div key={`exp-row-${item.id}`} className="flex flex-col gap-2 rounded-md border border-[var(--color-border)] p-3">
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
                                : entry
                            )
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
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Projects</h2>
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
                              : entry
                          )
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
        </section>

        <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">Certificates</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <input
              value={certificateName}
              onChange={(e) => setCertificateName(e.target.value)}
              placeholder="Certificate name"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={certificateIssuer}
              onChange={(e) => setCertificateIssuer(e.target.value)}
              placeholder="Issuer"
              className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <input
              value={certificateCredentialUrl}
              onChange={(e) => setCertificateCredentialUrl(e.target.value)}
              placeholder="Credential URL"
              className="sm:col-span-1 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
            />
            <button
              type="button"
              onClick={addCertificate}
              className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
            >
              Add Certificate
            </button>
          </div>

          <div className="space-y-2">
            {certificates.map((item, index) => (
              <div key={item.id} className="rounded-md border border-[var(--color-border)] p-3 space-y-2">
                {editingCertificateId === item.id ? (
                  <>
                    <input
                      value={editingCertificateName}
                      onChange={(e) => setEditingCertificateName(e.target.value)}
                      className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                    />
                    <input
                      value={editingCertificateIssuer}
                      onChange={(e) => setEditingCertificateIssuer(e.target.value)}
                      className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                    />
                  </>
                ) : (
                  <p className="text-[var(--color-textPrimary)]">
                    {item.name}{item.issuer ? ` · ${item.issuer}` : ''}
                  </p>
                )}
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => index > 0 && setCertificates((prev) => moveItem(prev, index, index - 1))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    ↑
                  </button>
                  <button
                    type="button"
                    onClick={() => index < certificates.length - 1 && setCertificates((prev) => moveItem(prev, index, index + 1))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    ↓
                  </button>
                  {editingCertificateId === item.id ? (
                    <button
                      type="button"
                      onClick={() => {
                        const nextName = editingCertificateName.trim();
                        if (!nextName) {
                          return;
                        }
                        setCertificates((prev) =>
                          prev.map((entry) =>
                            entry.id === item.id
                              ? {
                                  ...entry,
                                  name: nextName,
                                  issuer: editingCertificateIssuer.trim(),
                                }
                              : entry
                          )
                        );
                        setEditingCertificateId(null);
                        setEditingCertificateName('');
                        setEditingCertificateIssuer('');
                      }}
                      className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                    >
                      Save
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => {
                        setEditingCertificateId(item.id);
                        setEditingCertificateName(item.name);
                        setEditingCertificateIssuer(item.issuer ?? '');
                      }}
                      className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                    >
                      Edit
                    </button>
                  )}
                  <button
                    type="button"
                    onClick={() => setCertificates((prev) => prev.filter((entry) => entry.id !== item.id))}
                    className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        </section>

        <div className="flex flex-col-reverse sm:flex-row gap-3 sm:justify-end pb-8">
          {fromOnboarding ? (
            <button
              type="button"
              onClick={() => confirmNavigationIfDirty(ROUTES.CHAT)}
              className="rounded-md border border-[var(--color-border)] px-4 py-2 text-[var(--color-textPrimary)]"
            >
              Skip for now
            </button>
          ) : (
            <button
              type="button"
              onClick={() => confirmNavigationIfDirty(ROUTES.SETTINGS)}
              className="rounded-md border border-[var(--color-border)] px-4 py-2 text-[var(--color-textPrimary)]"
            >
              Back to Settings
            </button>
          )}
          <button
            type="button"
            onClick={saveChanges}
            disabled={!isDirty || isSaving}
            className="rounded-md bg-[var(--color-primary)] px-5 py-2 font-semibold text-[var(--color-background)] disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSaving ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </div>
    </main>
  );
};

export default PersonalInfoPage;
