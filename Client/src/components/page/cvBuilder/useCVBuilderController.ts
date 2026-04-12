import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { cvService } from '../../../services/cv';
import { ROUTES } from '../../../router';
import type { CVSectionKey, CVTemplateDto, PersonalInfoResponseDto, SelectedItemsDto } from '../../../types/dto';
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

  selectedItems: SelectedItemsDto;
  toggleItem: (sectionKey: CVSectionKey, itemId: string) => void;
  refreshPersonalInfo: () => Promise<void>;
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

const getInitialSelectedItems = (): SelectedItemsDto => ({
  skillIds: [],
  languageIds: [],
  educationIds: [],
  experienceIds: [],
  projectIds: [],
  certificateIds: [],
});

const getInitialSelectedItems = (): SelectedItemsDto => ({
  skillIds: [],
  languageIds: [],
  educationIds: [],
  experienceIds: [],
  projectIds: [],
  certificateIds: [],
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
  const [selectedItems, setSelectedItems] = useState<SelectedItemsDto>(getInitialSelectedItems());
  const [selectedItems, setSelectedItems] = useState<SelectedItemsDto>(getInitialSelectedItems());

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

        if (!cvId && info) {
          setSelectedItems({
            skillIds: info.skills?.map(s => s.id) || [],
            languageIds: info.languages?.map(l => l.id) || [],
            educationIds: info.education?.map(e => e.id) || [],
            experienceIds: info.experience?.map(e => e.id) || [],
            projectIds: info.projects?.map(p => p.id) || [],
            certificateIds: info.certificates?.map(c => c.id) || [],
          });
        }


        if (!cvId && info) {
          setSelectedItems({
            skillIds: info.skills?.map(s => s.id) || [],
            languageIds: info.languages?.map(l => l.id) || [],
            educationIds: info.education?.map(e => e.id) || [],
            experienceIds: info.experience?.map(e => e.id) || [],
            projectIds: info.projects?.map(p => p.id) || [],
            certificateIds: info.certificates?.map(c => c.id) || [],
          });
        }


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

          if (cv.selectedItems) {
            setSelectedItems(cv.selectedItems);
          } else {
            // Auto-select all by default if newly loading?
          }

          if (cv.selectedItems) {
            setSelectedItems(cv.selectedItems);
          } else {
            // Auto-select all by default if newly loading?
          }

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


  const toggleItem = (sectionKey: CVSectionKey, itemId: string) => {
    setSelectedItems((prev) => {
      const next = { ...prev };
      let ids: string[] = [];
      switch (sectionKey) {
        case 'education': ids = next.educationIds || []; break;
        case 'experience': ids = next.experienceIds || []; break;
        case 'skills': ids = next.skillIds || []; break;
        case 'languages': ids = next.languageIds || []; break;
        case 'projects': ids = next.projectIds || []; break;
        case 'certificates': ids = next.certificateIds || []; break;
      }
      
      const newIds = ids.includes(itemId) 
        ? ids.filter(id => id !== itemId)
        : [...ids, itemId];
        
      switch (sectionKey) {
        case 'education': next.educationIds = newIds; break;
        case 'experience': next.experienceIds = newIds; break;
        case 'skills': next.skillIds = newIds; break;
        case 'languages': next.languageIds = newIds; break;
        case 'projects': next.projectIds = newIds; break;
        case 'certificates': next.certificateIds = newIds; break;
      }
      return next;
    });
  };

  const refreshPersonalInfo = async () => {
    const info = await cvService.getPersonalInfo();
    setPersonalInfo(info);
    
    // Automatically select newly added items
    setSelectedItems((prev) => {
      const selected = { ...prev };
      const selectNew = (existingIds: string[] = [], items: any[] = []) => {
        const itemIds = items.map(item => item.id);
        const newIds = itemIds.filter(id => !existingIds.includes(id));
        return [...existingIds, ...newIds];
      };
      
      selected.educationIds = selectNew(selected.educationIds, info.education);
      selected.experienceIds = selectNew(selected.experienceIds, info.experience);
      selected.skillIds = selectNew(selected.skillIds, info.skills);
      selected.languageIds = selectNew(selected.languageIds, info.languages);
      selected.projectIds = selectNew(selected.projectIds, info.projects);
      selected.certificateIds = selectNew(selected.certificateIds, info.certificates);
      
      return selected;
    });
  };


  const toggleItem = (sectionKey: CVSectionKey, itemId: string) => {
    setSelectedItems((prev) => {
      const next = { ...prev };
      let ids: string[] = [];
      switch (sectionKey) {
        case 'education': ids = next.educationIds || []; break;
        case 'experience': ids = next.experienceIds || []; break;
        case 'skills': ids = next.skillIds || []; break;
        case 'languages': ids = next.languageIds || []; break;
        case 'projects': ids = next.projectIds || []; break;
        case 'certificates': ids = next.certificateIds || []; break;
      }
      
      const newIds = ids.includes(itemId) 
        ? ids.filter(id => id !== itemId)
        : [...ids, itemId];
        
      switch (sectionKey) {
        case 'education': next.educationIds = newIds; break;
        case 'experience': next.experienceIds = newIds; break;
        case 'skills': next.skillIds = newIds; break;
        case 'languages': next.languageIds = newIds; break;
        case 'projects': next.projectIds = newIds; break;
        case 'certificates': next.certificateIds = newIds; break;
      }
      return next;
    });
  };

  const refreshPersonalInfo = async () => {
    const info = await cvService.getPersonalInfo();
    setPersonalInfo(info);
    
    // Automatically select newly added items
    setSelectedItems((prev) => {
      const selected = { ...prev };
      const selectNew = (existingIds: string[] = [], items: any[] = []) => {
        const itemIds = items.map(item => item.id);
        const newIds = itemIds.filter(id => !existingIds.includes(id));
        return [...existingIds, ...newIds];
      };
      
      selected.educationIds = selectNew(selected.educationIds, info.education);
      selected.experienceIds = selectNew(selected.experienceIds, info.experience);
      selected.skillIds = selectNew(selected.skillIds, info.skills);
      selected.languageIds = selectNew(selected.languageIds, info.languages);
      selected.projectIds = selectNew(selected.projectIds, info.projects);
      selected.certificateIds = selectNew(selected.certificateIds, info.certificates);
      
      return selected;
    });
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
          selectedItems,
          selectedItems,
        });
      } else {
        const created = await cvService.createCV({
          cvName,
          templateId: selectedTemplateId,
          sectionsOrder: selectedSectionsOrder,
          selectedItems,
          selectedItems,
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
    selectedItems,
    toggleItem,
    refreshPersonalInfo,
    selectedItems,
    toggleItem,
    refreshPersonalInfo,
    selectedSectionsOrder,
    personalInfo,
    selectedTemplateComponentName,
    isEditing: Boolean(cvId),
    save,
    downloadPdf,
  };
};
