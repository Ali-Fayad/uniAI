import type {
  CVSectionKey,
  ItemsOrderDto,
  PersonalInfoCertificateEntryDto,
  PersonalInfoEducationEntryDto,
  PersonalInfoExperienceEntryDto,
  PersonalInfoLanguageEntryDto,
  PersonalInfoProjectEntryDto,
  PersonalInfoResponseDto,
  PersonalInfoSkillEntryDto,
  SelectedItemsDto,
} from '../../../../types/dto';

export interface ResumeEntry {
  id: string;
  title: string;
  subtitle?: string;
  meta?: string;
  description?: string;
  tags?: string[];
}

export interface TemplateSectionData {
  key: CVSectionKey;
  title: string;
  items: ResumeEntry[];
}

const SECTION_LABELS: Record<CVSectionKey, string> = {
  education: 'Education',
  experience: 'Experience',
  skills: 'Skills',
  languages: 'Languages',
  projects: 'Projects',
  certificates: 'Certifications',
};

const formatDate = (value?: string | null) => {
  if (!value) return '';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    year: 'numeric',
  }).format(date);
};

const formatDateRange = (start?: string | null, end?: string | null, current?: boolean) => {
  const startLabel = formatDate(start);
  const endLabel = current ? 'Present' : formatDate(end);

  if (startLabel && endLabel) return `${startLabel} - ${endLabel}`;
  return startLabel || endLabel || undefined;
};

const getOrderedItems = <T extends { id: string }>(
  items: T[] | undefined,
  orderIds: string[] | undefined,
  isSelected: (id: string) => boolean,
) => {
  if (!items) return [];

  const filtered = items.filter((entry) => isSelected(entry.id));
  if (!orderIds || orderIds.length === 0) return filtered;

  return [...filtered].sort((a, b) => {
    const idxA = orderIds.indexOf(a.id);
    const idxB = orderIds.indexOf(b.id);
    if (idxA === -1 && idxB === -1) return 0;
    if (idxA === -1) return 1;
    if (idxB === -1) return -1;
    return idxA - idxB;
  });
};

export const getDisplayName = () => 'Your Name';

export const getContactItems = (personalInfo: PersonalInfoResponseDto | null): string[] => {
  if (!personalInfo) {
    return [];
  }

  return [
    personalInfo.phone,
    personalInfo.address,
    personalInfo.linkedin,
    personalInfo.github,
    personalInfo.portfolio,
  ].filter(Boolean) as string[];
};

const mapEducation = (entry: PersonalInfoEducationEntryDto): ResumeEntry => ({
  id: entry.id,
  title: [entry.degree, entry.fieldOfStudy].filter(Boolean).join(', ') || 'Degree',
  subtitle: entry.universityName || 'University',
  meta: [formatDateRange(entry.startDate, entry.endDate), entry.grade].filter(Boolean).join(' | '),
  description: entry.description,
});

const mapExperience = (entry: PersonalInfoExperienceEntryDto): ResumeEntry => ({
  id: entry.id,
  title: entry.position || 'Role',
  subtitle: [entry.company, entry.location].filter(Boolean).join(' | '),
  meta: formatDateRange(entry.startDate, entry.endDate, entry.currentlyWorking),
  description: entry.description,
});

const mapSkill = (entry: PersonalInfoSkillEntryDto): ResumeEntry => ({
  id: entry.id,
  title: entry.name,
  meta: entry.level,
});

const mapLanguage = (entry: PersonalInfoLanguageEntryDto): ResumeEntry => ({
  id: entry.id,
  title: entry.name,
  meta: entry.proficiency,
});

const mapProject = (entry: PersonalInfoProjectEntryDto): ResumeEntry => ({
  id: entry.id,
  title: entry.name || 'Project',
  subtitle: [entry.repositoryUrl, entry.liveUrl].filter(Boolean).join(' | '),
  meta: formatDateRange(entry.startDate, entry.endDate),
  description: entry.description,
  tags: entry.technologies,
});

const mapCertificate = (entry: PersonalInfoCertificateEntryDto): ResumeEntry => ({
  id: entry.id,
  title: entry.name || 'Certificate',
  subtitle: entry.issuer,
  meta: formatDate(entry.date),
  description: entry.credentialUrl,
});

export const getSectionData = (
  personalInfo: PersonalInfoResponseDto | null,
  order: CVSectionKey[],
  selectedItems?: SelectedItemsDto,
  itemsOrder?: ItemsOrderDto,
): TemplateSectionData[] => {
  const map: Record<CVSectionKey, ResumeEntry[]> = {
    education: getOrderedItems(
      personalInfo?.education,
      itemsOrder?.educationIds,
      (id) => !selectedItems?.educationIds || selectedItems.educationIds.includes(id),
    ).map(mapEducation),
    experience: getOrderedItems(
      personalInfo?.experience,
      itemsOrder?.experienceIds,
      (id) => !selectedItems?.experienceIds || selectedItems.experienceIds.includes(id),
    ).map(mapExperience),
    skills: getOrderedItems(
      personalInfo?.skills,
      itemsOrder?.skillIds,
      (id) => !selectedItems?.skillIds || selectedItems.skillIds.includes(id),
    ).map(mapSkill),
    languages: getOrderedItems(
      personalInfo?.languages,
      itemsOrder?.languageIds,
      (id) => !selectedItems?.languageIds || selectedItems.languageIds.includes(id),
    ).map(mapLanguage),
    projects: getOrderedItems(
      personalInfo?.projects,
      itemsOrder?.projectIds,
      (id) => !selectedItems?.projectIds || selectedItems.projectIds.includes(id),
    ).map(mapProject),
    certificates: getOrderedItems(
      personalInfo?.certificates,
      itemsOrder?.certificateIds,
      (id) => !selectedItems?.certificateIds || selectedItems.certificateIds.includes(id),
    ).map(mapCertificate),
  };

  return order
    .map((key) => ({
      key,
      title: SECTION_LABELS[key],
      items: map[key],
    }))
    .filter((section) => section.items.length > 0);
};
