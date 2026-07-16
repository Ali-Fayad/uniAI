import { useEffect, useMemo, useState } from 'react';
import { isAxiosError } from 'axios';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { cvService } from '../../../services/cv';
import { ROUTES } from '../../../router';
import type { CVSectionKey, CVTemplateDto, PersonalInfoResponseDto, SelectedItemsDto, ItemsOrderDto } from '../../../types/dto';
import { DEFAULT_CV_SECTIONS_ORDER } from './cvBuilderSections';
import {
  appendNewPersonalInfoItems,
  buildSelectedItemsFromPersonalInfo,
  getInitialItemsOrder,
  getInitialSelectedItems,
  toggleSelectedItem,
  updateSectionItemsOrder,
} from './cvSelectionState';

export interface UseCVBuilderControllerReturn {
  isLoading: boolean;
  isProfileComplete: boolean;
  isSaving: boolean;
  isExporting: boolean;
  error: string | null;
  exportError: string | null;
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
  itemsOrder: ItemsOrderDto;
  updateItemsOrder: (sectionKey: CVSectionKey, newOrder: string[]) => void;
  refreshPersonalInfo: () => Promise<void>;
  personalInfo: PersonalInfoResponseDto | null;
  displayName: string;
  selectedTemplateComponentName: string;
  isEditing: boolean;
  save: () => Promise<void>;
  downloadPdf: () => Promise<void>;
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

const sanitizePdfFileName = (value: string) =>
  `${value}`
    .replace(/[\\/:*?"<>|]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\.+$/, '') || 'CV';

const waitForNextFrame = () => new Promise<void>((resolve) => {
  window.requestAnimationFrame(() => resolve());
});

const waitForExportContent = async (selector: string, timeoutMs = 2000) => {
  const startedAt = window.performance.now();

  while (window.performance.now() - startedAt < timeoutMs) {
    const element = document.querySelector(selector);
    if (element instanceof HTMLElement) {
      return element;
    }

    await waitForNextFrame();
  }

  throw new Error('Export surface is not ready.');
};

type Html2PdfOptions = {
  filename?: string;
  margin?: number | [number, number] | [number, number, number, number];
  image?: {
    type?: 'jpeg' | 'png' | 'webp';
    quality?: number;
  };
  html2canvas?: object;
  jsPDF?: {
    unit?: string;
    format?: string | [number, number];
    orientation?: 'portrait' | 'landscape';
  };
  pagebreak: {
    mode: Array<'avoid-all' | 'css' | 'legacy'>;
  };
};

export const useCVBuilderController = (
  cvId: number | null,
): UseCVBuilderControllerReturn => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [isLoading, setIsLoading] = useState(true);
  const [isProfileComplete, setIsProfileComplete] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);

  const [cvName, setCvName] = useState('My CV');
  const [templates, setTemplates] = useState<CVTemplateDto[]>([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
  const [sectionOrder, setSectionOrder] = useState<CVSectionKey[]>(DEFAULT_CV_SECTIONS_ORDER);
  const [sectionEnabled, setSectionEnabled] = useState<Record<CVSectionKey, boolean>>(getInitialEnabledSections);
  const [personalInfo, setPersonalInfo] = useState<PersonalInfoResponseDto | null>(null);
  const [selectedItems, setSelectedItems] = useState<SelectedItemsDto>(getInitialSelectedItems());
  const [itemsOrder, setItemsOrder] = useState<ItemsOrderDto>(getInitialItemsOrder());

  useEffect(() => {
    let isActive = true;

    const loadData = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const [profileStatus, templateList] = await Promise.all([
          cvService.getPersonalInfoStatus(),
          cvService.getTemplates(),
        ]);

        if (!isActive) {
          return;
        }

        setTemplates(templateList);
        setIsProfileComplete(profileStatus.isFilled);

        const modernTemplate = templateList.find((template) => template.componentName === 'ModernTemplate');
        setSelectedTemplateId(modernTemplate?.id ?? templateList[0]?.id ?? null);

        if (profileStatus.isFilled) {
          try {
            const [info, cv] = await Promise.all([
              cvService.getPersonalInfo({ profileIncompleteHandling: 'local' }),
              cvId ? cvService.getCV(cvId, { profileIncompleteHandling: 'local' }) : Promise.resolve(null),
            ]);

            if (!isActive) {
              return;
            }

            setPersonalInfo(info);

            if (!cvId) {
              setSelectedItems(buildSelectedItemsFromPersonalInfo(info));
            } else if (cv) {
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
              }

              if (cv.itemsOrder) {
                setItemsOrder(cv.itemsOrder);
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
          } catch (protectedLoadError) {
            if (isAxiosError(protectedLoadError) && protectedLoadError.response?.status === 410) {
              if (!isActive) {
                return;
              }

              setIsProfileComplete(false);
              setPersonalInfo(null);
              setSelectedItems(getInitialSelectedItems());
              setItemsOrder(getInitialItemsOrder());
            } else {
              throw protectedLoadError;
            }
          }
        }
      } catch {
        if (!isActive) {
          return;
        }

        setError('Failed to load CV builder data. Please try again.');
      } finally {
        // no-op: loading state is finalized below so eslint does not flag
        // early returns inside the finally block.
      }

      if (isActive) {
        setIsLoading(false);
      }
    };

    void loadData();

    return () => {
      isActive = false;
    };
  }, [cvId]);

  const selectedSectionsOrder = useMemo(
    () => sectionOrder.filter((section) => sectionEnabled[section]),
    [sectionEnabled, sectionOrder],
  );

  const selectedTemplateComponentName = useMemo(() => {
    const template = templates.find((item) => item.id === selectedTemplateId);
    return template?.componentName ?? 'ModernTemplate';
  }, [selectedTemplateId, templates]);

  const displayName = useMemo(() => {
    const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ').trim();
    return fullName || user?.username || user?.email?.split('@')[0] || 'Your Name';
  }, [user?.email, user?.firstName, user?.lastName, user?.username]);

  const toggleSection = (section: CVSectionKey) => {
    setSectionEnabled((previous) => ({
      ...previous,
      [section]: !previous[section],
    }));
  };

  const toggleItem = (sectionKey: CVSectionKey, itemId: string) => {
    setSelectedItems((prev) => toggleSelectedItem(prev, sectionKey, itemId));
  };

  const updateItemsOrder = (sectionKey: CVSectionKey, newOrder: string[]) => {
    setItemsOrder((prev) => updateSectionItemsOrder(prev, sectionKey, newOrder));
  };

  const refreshPersonalInfo = async () => {
    if (!isProfileComplete) {
      return;
    }

    try {
      const info = await cvService.getPersonalInfo({ profileIncompleteHandling: 'local' });
      setPersonalInfo(info);

      setSelectedItems((prev) => appendNewPersonalInfoItems(prev, info));
      setItemsOrder((prevOrder) => appendNewPersonalInfoItems(prevOrder, info));
    } catch (refreshError) {
      if (isAxiosError(refreshError) && refreshError.response?.status === 410) {
        setIsProfileComplete(false);
        setError('Complete your profile before continuing with CV Builder.');
      } else {
        setError('Failed to refresh profile information. Please try again.');
      }
    }
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

    if (!isProfileComplete) {
      setError('Complete your profile before saving this CV.');
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
        await cvService.updateCV(
          cvId,
          {
            cvName,
            templateId: selectedTemplateId,
            sectionsOrder: selectedSectionsOrder,
            selectedItems,
            itemsOrder,
          },
          { profileIncompleteHandling: 'local' },
        );
      } else {
        const created = await cvService.createCV(
          {
            cvName,
            templateId: selectedTemplateId,
            sectionsOrder: selectedSectionsOrder,
            selectedItems,
            itemsOrder,
          },
          { profileIncompleteHandling: 'local' },
        );
        navigate(`${ROUTES.CV_BUILDER}/${created.id}`, { replace: true });
      }
    } catch (saveError) {
      if (isAxiosError(saveError) && saveError.response?.status === 410) {
        setIsProfileComplete(false);
        setError('Complete your profile before saving this CV.');
      } else {
        setError('Failed to save CV configuration. Please try again.');
      }
    } finally {
      setIsSaving(false);
    }
  };

  const downloadPdf = async () => {
    if (isExporting) {
      return;
    }

    setIsExporting(true);
    setExportError(null);

    try {
      await waitForNextFrame();
      await waitForNextFrame();

      const exportRoot = await waitForExportContent('#cv-export-surface article');

      const html2pdfModule = await import('html2pdf.js');
      const html2pdf = html2pdfModule.default;
      const fileName = `${sanitizePdfFileName(cvName)}.pdf`;
      const exportOptions: Html2PdfOptions = {
        filename: fileName,
        margin: [8, 8, 10, 8],
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: {
          scale: 2,
          useCORS: true,
          backgroundColor: '#ffffff',
          scrollX: 0,
          scrollY: 0,
        },
        jsPDF: {
          unit: 'mm',
          format: 'a4',
          orientation: 'portrait',
        },
        pagebreak: {
          mode: ['css', 'legacy', 'avoid-all'],
        },
      };

      await html2pdf()
        .set(exportOptions)
        .from(exportRoot)
        .save();
    } catch {
      setExportError('Failed to generate PDF. Please try again.');
    } finally {
      setIsExporting(false);
    }
  };

  return {
    isLoading,
    isProfileComplete,
    isSaving,
    isExporting,
    error,
    exportError,
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
    itemsOrder,
    updateItemsOrder,
    refreshPersonalInfo,
    selectedSectionsOrder,
    personalInfo,
    displayName,
    selectedTemplateComponentName,
    isEditing: Boolean(cvId),
    save,
    downloadPdf,
  };
};
