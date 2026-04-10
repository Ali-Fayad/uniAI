import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cvService } from '../../../services/cv';
import { ROUTES } from '../../../router';
import type { CVSectionKey, CVTemplateDto, PersonalInfoResponseDto } from '../../../types/dto';
import { DEFAULT_CV_SECTIONS_ORDER } from './cvBuilderSections';

export interface UseCVBuilderControllerReturn {
  isLoading: boolean;
  isSaving: boolean;
  error: string | null;
  cvName: string;
  setCvName: (name: string) => void;
  templates: CVTemplateDto[];
  selectedTemplateId: number | null;
  selectTemplate: (templateId: number) => void;
  sectionOrder: CVSectionKey[];
  sectionEnabled: Record<CVSectionKey, boolean>;
  toggleSection: (section: CVSectionKey) => void;
  reorderSections: (activeId: string, overId: string) => void;
  selectedSectionsOrder: CVSectionKey[];
  personalInfo: PersonalInfoResponseDto | null;
  selectedTemplateComponentName: string;
  isEditing: boolean;
  save: () => Promise<void>;
  downloadPdf: () => void;
}

const getInitialEnabledSections = (): Record<CVSectionKey, boolean> => ({
  education: true,
  experience: true,
  skills: true,
  languages: true,
  projects: true,
  certificates: true,
});

const moveItem = <T,>(array: T[], fromIndex: number, toIndex: number): T[] => {
  const updated = [...array];
  const [moved] = updated.splice(fromIndex, 1);
  updated.splice(toIndex, 0, moved);
  return updated;
};

export const useCVBuilderController = (cvId: number | null): UseCVBuilderControllerReturn => {
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [cvName, setCvName] = useState('My CV');
  const [templates, setTemplates] = useState<CVTemplateDto[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
  const [sectionOrder, setSectionOrder] = useState<CVSectionKey[]>(DEFAULT_CV_SECTIONS_ORDER);
  const [sectionEnabled, setSectionEnabled] = useState<Record<CVSectionKey, boolean>>(getInitialEnabledSections);
  const [personalInfo, setPersonalInfo] = useState<PersonalInfoResponseDto | null>(null);

  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const [info, templateList] = await Promise.all([cvService.getPersonalInfo(), cvService.getTemplates()]);
        setPersonalInfo(info);
        setTemplates(templateList);

        const modernTemplate = templateList.find((template) => template.componentName === 'ModernTemplate');
        setSelectedTemplateId(modernTemplate?.id ?? templateList[0]?.id ?? null);

        if (cvId) {
          const cv = await cvService.getCV(cvId);
          setCvName(cv.cvName || 'My CV');

          const savedOrder = (cv.sectionsOrder ?? []).filter(Boolean) as CVSectionKey[];
          const mergedOrder = [
            ...savedOrder,
            ...DEFAULT_CV_SECTIONS_ORDER.filter((section) => !savedOrder.includes(section)),
          ];

          const nextEnabled = getInitialEnabledSections();
          (Object.keys(nextEnabled) as CVSectionKey[]).forEach((section) => {
            nextEnabled[section] = savedOrder.length === 0 ? true : savedOrder.includes(section);
          });

          setSectionOrder(mergedOrder);
          setSectionEnabled(nextEnabled);

          if (cv.templateId) {
            setSelectedTemplateId(cv.templateId);
          } else if (cv.templateComponentName) {
            const selected = templateList.find((template) => template.componentName === cv.templateComponentName);
            if (selected) {
              setSelectedTemplateId(selected.id);
            }
          }
        }
      } catch {
        setError('Failed to load CV builder data. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    void loadData();
  }, [cvId]);

  const selectedSectionsOrder = useMemo(
    () => sectionOrder.filter((section) => sectionEnabled[section]),
    [sectionEnabled, sectionOrder],
  );

  const selectedTemplateComponentName = useMemo(() => {
    const template = templates.find((item) => item.id === selectedTemplateId);
    return template?.componentName ?? 'ModernTemplate';
  }, [selectedTemplateId, templates]);

  const toggleSection = (section: CVSectionKey) => {
    setSectionEnabled((previous) => ({
      ...previous,
      [section]: !previous[section],
    }));
  };

  const reorderSections = (activeId: string, overId: string) => {
    if (activeId === overId) {
      return;
    }

    setSectionOrder((previous) => {
      const oldIndex = previous.indexOf(activeId as CVSectionKey);
      const newIndex = previous.indexOf(overId as CVSectionKey);

      if (oldIndex < 0 || newIndex < 0) {
        return previous;
      }

      return moveItem(previous, oldIndex, newIndex);
    });
  };

  const save = async () => {
    if (!selectedTemplateId) {
      setError('Please select a template before saving.');
      return;
    }

    if (selectedSectionsOrder.length === 0) {
      setError('Please keep at least one section enabled.');
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      if (cvId) {
        await cvService.updateCV(cvId, {
          cvName,
          templateId: selectedTemplateId,
          sectionsOrder: selectedSectionsOrder,
        });
      } else {
        const created = await cvService.createCV({
          cvName,
          templateId: selectedTemplateId,
          sectionsOrder: selectedSectionsOrder,
        });
        navigate(`${ROUTES.CV_BUILDER}/${created.id}`, { replace: true });
      }
    } catch {
      setError('Failed to save CV configuration. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const downloadPdf = () => {
    window.print();
  };

  return {
    isLoading,
    isSaving,
    error,
    cvName,
    setCvName,
    templates,
    selectedTemplateId,
    selectTemplate: setSelectedTemplateId,
    sectionOrder,
    sectionEnabled,
    toggleSection,
    reorderSections,
    selectedSectionsOrder,
    personalInfo,
    selectedTemplateComponentName,
    isEditing: Boolean(cvId),
    save,
    downloadPdf,
  };
};
