import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto, ItemsOrderDto } from '../../../../types/dto';

export interface CVTemplateComponentProps {
  personalInfo: PersonalInfoResponseDto | null;
  sectionOrder: CVSectionKey[];
  selectedItems?: SelectedItemsDto;
  itemsOrder?: ItemsOrderDto;
}
