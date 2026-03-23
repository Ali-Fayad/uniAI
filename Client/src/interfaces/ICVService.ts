import type {
  PersonalInfoResponseDto,
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
  getSkills(): Promise<string[]>;
  getPositions(): Promise<string[]>;
}
