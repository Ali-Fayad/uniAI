import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto, ItemsOrderDto } from '../../../../types/dto';

export interface CVTemplatePalette {
  paper: string;
  ink: string;
  muted: string;
  accent: string;
  accentSoft: string;
  rule: string;
  sidebar?: string;
  sidebarInk?: string;
}

export interface CVTemplateComponentProps {
  personalInfo: PersonalInfoResponseDto | null;
  sectionOrder: CVSectionKey[];
  selectedItems?: SelectedItemsDto;
  itemsOrder?: ItemsOrderDto;
  palette?: Partial<CVTemplatePalette>;
}

export const mergePalette = (
  defaultPalette: CVTemplatePalette,
  override?: Partial<CVTemplatePalette>,
): CVTemplatePalette => ({
  ...defaultPalette,
  ...override,
});
