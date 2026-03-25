import apiClient from './api';
import { ENDPOINTS } from '../constants';
import type {
  LanguageCatalogDto,
  PersonalInfoResponseDto,
  SkillCatalogDto,
  UpdatePersonalInfoDto,
  UniversityDto,
} from '../types/dto';
import type { ICVService } from '../interfaces';

/**
 * CV service for personal profile information and CV lookup endpoints.
 */
export const cvService: ICVService = {
  async getPersonalInfo(): Promise<PersonalInfoResponseDto> {
    const response = await apiClient.get<PersonalInfoResponseDto>(ENDPOINTS.CV.PERSONAL_INFO);
    return response.data;
  },

  async updatePersonalInfo(data: UpdatePersonalInfoDto): Promise<PersonalInfoResponseDto> {
    const response = await apiClient.put<PersonalInfoResponseDto>(ENDPOINTS.CV.PERSONAL_INFO, data);
    return response.data;
  },

  async getUniversities(): Promise<UniversityDto[]> {
    const response = await apiClient.get<UniversityDto[]>(ENDPOINTS.CV.UNIVERSITIES);
    return response.data;
  },

  async getSkills(search?: string): Promise<SkillCatalogDto[]> {
    const response = await apiClient.get<SkillCatalogDto[]>(ENDPOINTS.CATALOG.SKILLS, {
      params: search?.trim() ? { search: search.trim() } : undefined,
    });
    return response.data;
  },

  async getLanguages(search?: string): Promise<LanguageCatalogDto[]> {
    const response = await apiClient.get<LanguageCatalogDto[]>(ENDPOINTS.CATALOG.LANGUAGES, {
      params: search?.trim() ? { search: search.trim() } : undefined,
    });
    return response.data;
  },

  async getPositions(): Promise<string[]> {
    const response = await apiClient.get<string[]>(ENDPOINTS.CV.POSITIONS);
    return response.data;
  },
};
