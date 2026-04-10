import type { CVSectionKey, PersonalInfoResponseDto } from '../../../../types/dto';

export interface CVTemplateComponentProps {
  personalInfo: PersonalInfoResponseDto | null;
  sectionOrder: CVSectionKey[];
}
