import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto } from '../../../../types/dto';

export interface CVTemplateComponentProps {
  personalInfo: PersonalInfoResponseDto | null;
  sectionOrder: CVSectionKey[];
  selectedItems?: SelectedItemsDto;
}
