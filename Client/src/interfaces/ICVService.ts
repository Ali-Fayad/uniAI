import type {
  CreateCVDto,
  CVDto,
  CVTemplateDto,
  LanguageCatalogDto,
  PositionCatalogDto,
  PersonalInfoResponseDto,
  PersonalInfoStatusDto,
  SkillCatalogDto,
  UpdateCVDto,
  UpdatePersonalInfoDto,
  UniversityCatalogDto,
} from '../types/dto';
import type { AxiosRequestConfig } from 'axios';

type ProfileIncompleteHandling = 'local' | 'navigate';

export type ProfileAwareRequestConfig = AxiosRequestConfig & {
  profileIncompleteHandling?: ProfileIncompleteHandling;
};

/**
 * ICVService
 *
 * Abstraction for personal info and CV-support lookup endpoints.
 */
export interface ICVService {
  getPersonalInfoStatus(): Promise<PersonalInfoStatusDto>;
  getPersonalInfo(requestConfig?: ProfileAwareRequestConfig): Promise<PersonalInfoResponseDto>;
  updatePersonalInfo(data: UpdatePersonalInfoDto): Promise<PersonalInfoResponseDto>;
  getUniversities(search?: string): Promise<UniversityCatalogDto[]>;
  getSkills(search?: string): Promise<SkillCatalogDto[]>;
  getLanguages(search?: string): Promise<LanguageCatalogDto[]>;
  getPositions(search?: string): Promise<PositionCatalogDto[]>;
  getTemplates(): Promise<CVTemplateDto[]>;
  getTemplate(templateId: number): Promise<CVTemplateDto>;
  getCVs(): Promise<CVDto[]>;
  getCV(cvId: number, requestConfig?: ProfileAwareRequestConfig): Promise<CVDto>;
  createCV(data: CreateCVDto, requestConfig?: ProfileAwareRequestConfig): Promise<CVDto>;
  updateCV(cvId: number, data: UpdateCVDto, requestConfig?: ProfileAwareRequestConfig): Promise<CVDto>;
  deleteCV(cvId: number): Promise<void>;
}
