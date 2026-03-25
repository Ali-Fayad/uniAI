import type {
  LanguageCatalogDto,
  PersonalInfoResponseDto,
  SkillCatalogDto,
  UpdatePersonalInfoDto,
  UniversityDto,
} from '../types/dto';

/**
 * ICVService
 *
 * Abstraction for personal info and CV-support lookup endpoints.
 */
export interface ICVService {
  getPersonalInfo(): Promise<PersonalInfoResponseDto>;
  updatePersonalInfo(data: UpdatePersonalInfoDto): Promise<PersonalInfoResponseDto>;
  getUniversities(): Promise<UniversityDto[]>;
  getSkills(search?: string): Promise<SkillCatalogDto[]>;
  getLanguages(search?: string): Promise<LanguageCatalogDto[]>;
  getPositions(): Promise<string[]>;
}
