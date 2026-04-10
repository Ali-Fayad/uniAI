// ============================================================
// Authentication DTOs
// ============================================================

export interface SignUpDto {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  password: string;
}

export interface SignInDto {
  email: string;
  password: string;
}

export interface VerifyDto {
  email: string;
  verificationCode: string; // server expects verificationCode
}

export interface RequestPasswordDto {
  email: string;
  verificationCode: string;
  newPassword: string;
}

export interface EmailRequestDto {
  email: string;
}

export interface GoogleAuthUrlRequestDto {
  redirectUri?: string;
  state?: string;
}

// ============================================================
// Response DTOs
// ============================================================

export interface TokenResponse {
  token: string; // server returns only { token }
}

export interface AuthenticationResponseDto {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  isVerified: boolean;
  isTwoFacAuth: boolean;
}

export interface MessageResponse {
  message: string;
}

export interface UrlResponse {
  url: string;
}

export interface CheckEmailResponse {
  available: boolean;
  message: string;
}

export interface CheckUsernameResponse {
  available: boolean;
  message: string;
}

// ============================================================
// User DTOs
// ============================================================

export interface UserData {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  isVerified?: boolean;
  role?: string;
  twoFactorEnabled?: boolean;
  provider?: string;
}

export interface UpdateUserDto {
  username?: string;
  firstName?: string;
  lastName?: string;
  enableTwoFactor?: boolean;
}

export interface FeedbackRequest {
  rating?: number;
  content: string;
}

export interface SkillCatalogDto {
  id: number;
  name: string;
  category?: string;
}

export interface LanguageCatalogDto {
  id: number;
  name: string;
  code: string;
  nativeName?: string;
}

export interface PositionCatalogDto {
  id: number;
  name: string;
}

export interface UniversityCatalogDto {
  id: number;
  name: string;
  acronym?: string;
  nameAr?: string;
}

export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

export interface DeleteAccountDto {
  password: string;
}

// ============================================================
// Chat DTOs
// ============================================================

export interface ChatCreationResponseDto {
  chatId: number;
}

export interface SendMessageDto {
  chatId: number;
  content: string;
}

export interface MessageResponseDto {
  messageId: number;
  chatId: number;
  senderId: number; // 0 = AI, user ID = user
  content: string;
  timestamp: string;
}

export interface Chat {
  id: number;
  user: UserData;
  title: string | null;
  createdAt: string;
  updatedAt: string;
}

// ============================================================
// CV / Personal Info DTOs
// ============================================================

export interface UniversityDto {
  id: number;
  name: string;
  nameAr?: string;
  acronym?: string;
  latitude?: number;
  longitude?: number;
  campusName?: string;
  campusType?: string;
}

export interface PersonalInfoEducationEntryDto {
  id: string;
  universityId?: number | null;
  universityName: string;
}

export interface PersonalInfoSkillEntryDto {
  id: string;
  skillId: string;
  name: string;
}

export interface PersonalInfoLanguageEntryDto {
  id: string;
  languageId: string;
  name: string;
}

export interface PersonalInfoExperienceEntryDto {
  id: string;
  positionId: string;
  position: string;
  company: string;
}

export interface PersonalInfoProjectEntryDto {
  id: string;
  name: string;
  description?: string;
  repositoryUrl?: string;
  liveUrl?: string;
}

export interface PersonalInfoCertificateEntryDto {
  id: string;
  name: string;
  issuer?: string;
  credentialUrl?: string;
}

export interface PersonalInfoResponseDto {
  userId: number;
  hasPersonalInfo?: boolean;
  phone?: string | null;
  address?: string | null;
  linkedin?: string | null;
  github?: string | null;
  portfolio?: string | null;
  summary?: string | null;
  jobTitle?: string | null;
  company?: string | null;
  education?: PersonalInfoEducationEntryDto[];
  skills?: PersonalInfoSkillEntryDto[];
  languages?: PersonalInfoLanguageEntryDto[];
  experience?: PersonalInfoExperienceEntryDto[];
  projects?: PersonalInfoProjectEntryDto[];
  certificates?: PersonalInfoCertificateEntryDto[];
}

export interface UpdatePersonalInfoDto {
  phone?: string;
  address?: string;
  linkedin?: string;
  github?: string;
  portfolio?: string;
  summary?: string;
  jobTitle?: string;
  company?: string;
  education?: PersonalInfoEducationEntryDto[];
  skills?: PersonalInfoSkillEntryDto[];
  languages?: PersonalInfoLanguageEntryDto[];
  experience?: PersonalInfoExperienceEntryDto[];
  projects?: PersonalInfoProjectEntryDto[];
  certificates?: PersonalInfoCertificateEntryDto[];
}

export type CVSectionKey =
  | 'education'
  | 'experience'
  | 'skills'
  | 'languages'
  | 'projects'
  | 'certificates';

export interface CVTemplateDto {
  id: number;
  name: string;
  description?: string | null;
  thumbnailUrl?: string | null;
  componentName: string;
  isActive: boolean;
}

export interface CVDto {
  id: number;
  userId: number;
  cvName: string;
  templateId?: number | null;
  templateName?: string | null;
  templateComponentName?: string | null;
  template?: string | null;
  sectionsOrder?: CVSectionKey[];
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
  personalInfo?: PersonalInfoResponseDto | null;
}

export interface CreateCVDto {
  cvName: string;
  templateId: number;
  sectionsOrder: CVSectionKey[];
  isDefault?: boolean;
}

export interface UpdateCVDto {
  cvName?: string;
  templateId?: number;
  sectionsOrder?: CVSectionKey[];
  isDefault?: boolean;
}

