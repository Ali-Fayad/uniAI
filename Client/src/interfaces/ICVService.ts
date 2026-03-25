import type {
  LanguageCatalogDto,
  PositionCatalogDto,
  PersonalInfoResponseDto,
  SkillCatalogDto,
  UpdatePersonalInfoDto,
  UniversityCatalogDto,
} from '../types/dto';

/**
 * ICVService
 *
 * Abstraction for personal info and CV-support lookup endpoints.
 */
export interface ICVService {
  getPersonalInfo(): Promise<PersonalInfoResponseDto>;
  updatePersonalInfo(data: UpdatePersonalInfoDto): Promise<PersonalInfoResponseDto>;
  getUniversities(search?: string): Promise<UniversityCatalogDto[]>;
  getSkills(search?: string): Promise<SkillCatalogDto[]>;
  getLanguages(search?: string): Promise<LanguageCatalogDto[]>;
  getPositions(search?: string): Promise<PositionCatalogDto[]>;
}
