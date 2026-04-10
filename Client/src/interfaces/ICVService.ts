import type {
  CreateCVDto,
  CVDto,
  CVTemplateDto,
  LanguageCatalogDto,
  PositionCatalogDto,
  PersonalInfoResponseDto,
  SkillCatalogDto,
  UpdateCVDto,
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
  getTemplates(): Promise<CVTemplateDto[]>;
  getTemplate(templateId: number): Promise<CVTemplateDto>;
  getCVs(): Promise<CVDto[]>;
  getCV(cvId: number): Promise<CVDto>;
  createCV(data: CreateCVDto): Promise<CVDto>;
  updateCV(cvId: number, data: UpdateCVDto): Promise<CVDto>;
  deleteCV(cvId: number): Promise<void>;
}
