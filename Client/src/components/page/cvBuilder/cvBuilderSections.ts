import type { CVSectionKey } from '../../../types/dto';

export const CV_SECTION_OPTIONS: Array<{ key: CVSectionKey; label: string }> = [
  { key: 'education', label: 'Education' },
  { key: 'experience', label: 'Experience' },
  { key: 'skills', label: 'Skills' },
  { key: 'languages', label: 'Languages' },
  { key: 'projects', label: 'Projects' },
  { key: 'certificates', label: 'Certificates' },
];

export const DEFAULT_CV_SECTIONS_ORDER: CVSectionKey[] = CV_SECTION_OPTIONS.map((section) => section.key);
