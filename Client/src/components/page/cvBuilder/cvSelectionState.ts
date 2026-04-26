import type { CVSectionKey, ItemsOrderDto, PersonalInfoResponseDto, SelectedItemsDto } from '../../../types/dto';

const SECTION_ID_KEYS = {
  education: 'educationIds',
  experience: 'experienceIds',
  skills: 'skillIds',
  languages: 'languageIds',
  projects: 'projectIds',
  certificates: 'certificateIds',
} as const satisfies Record<CVSectionKey, keyof SelectedItemsDto & keyof ItemsOrderDto>;

export const getInitialSelectedItems = (): SelectedItemsDto => ({
  skillIds: [],
  languageIds: [],
  educationIds: [],
  experienceIds: [],
  projectIds: [],
  certificateIds: [],
});

export const getInitialItemsOrder = (): ItemsOrderDto => ({
  skillIds: [],
  languageIds: [],
  educationIds: [],
  experienceIds: [],
  projectIds: [],
  certificateIds: [],
});

export const buildSelectedItemsFromPersonalInfo = (info: PersonalInfoResponseDto): SelectedItemsDto => ({
  skillIds: info.skills?.map((item) => item.id) || [],
  languageIds: info.languages?.map((item) => item.id) || [],
  educationIds: info.education?.map((item) => item.id) || [],
  experienceIds: info.experience?.map((item) => item.id) || [],
  projectIds: info.projects?.map((item) => item.id) || [],
  certificateIds: info.certificates?.map((item) => item.id) || [],
});

export const toggleSelectedItem = (
  selectedItems: SelectedItemsDto,
  sectionKey: CVSectionKey,
  itemId: string,
): SelectedItemsDto => {
  const key = SECTION_ID_KEYS[sectionKey];
  const ids = selectedItems[key] || [];
  const nextIds = ids.includes(itemId) ? ids.filter((id) => id !== itemId) : [...ids, itemId];
  return { ...selectedItems, [key]: nextIds };
};

export const updateSectionItemsOrder = (
  itemsOrder: ItemsOrderDto,
  sectionKey: CVSectionKey,
  newOrder: string[],
): ItemsOrderDto => ({
  ...itemsOrder,
  [SECTION_ID_KEYS[sectionKey]]: newOrder,
});

export const appendNewPersonalInfoItems = <T extends SelectedItemsDto | ItemsOrderDto>(
  current: T,
  info: PersonalInfoResponseDto,
): T => {
  const appendNew = (existingIds: string[] = [], items: Array<{ id: string }> = []) => {
    const itemIds = items.map((item) => item.id);
    const newIds = itemIds.filter((id) => !existingIds.includes(id));
    return [...existingIds, ...newIds];
  };

  return {
    ...current,
    educationIds: appendNew(current.educationIds, info.education),
    experienceIds: appendNew(current.experienceIds, info.experience),
    skillIds: appendNew(current.skillIds, info.skills),
    languageIds: appendNew(current.languageIds, info.languages),
    projectIds: appendNew(current.projectIds, info.projects),
    certificateIds: appendNew(current.certificateIds, info.certificates),
  };
};
