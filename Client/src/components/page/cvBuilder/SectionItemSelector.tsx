import { Check } from 'lucide-react';
import type { CVSectionKey, PersonalInfoResponseDto, SelectedItemsDto } from '../../../types/dto';

interface SectionItemSelectorProps {
  sectionKey: CVSectionKey;
  personalInfo: PersonalInfoResponseDto | null;
  selectedItems: SelectedItemsDto;
  onToggleItem: (sectionKey: CVSectionKey, itemId: string) => void;
  onAddNew: (sectionKey: CVSectionKey) => void;
}

export const SectionItemSelector = ({
  sectionKey,
  personalInfo,
  selectedItems,
  onToggleItem,
  onAddNew,
}: SectionItemSelectorProps) => {
  if (!personalInfo) return null;

  const getItemsForSection = () => {
    switch (sectionKey) {
      case 'education': return personalInfo.education || [];
      case 'experience': return personalInfo.experience || [];
      case 'skills': return personalInfo.skills || [];
      case 'languages': return personalInfo.languages || [];
      case 'projects': return personalInfo.projects || [];
      case 'certificates': return personalInfo.certificates || [];
      default: return [];
    }
  };

  const getDisplayLabel = (item: any) => {
    switch (sectionKey) {
      case 'education': return `${item.degree} at ${item.universityName}`;
      case 'experience': return `${item.position} at ${item.company}`;
      case 'skills': return item.name;
      case 'languages': return item.name;
      case 'projects': return item.name;
      case 'certificates': return item.name;
      default: return 'Unknown item';
    }
  };

  const getSelectedIdsForSection = (): string[] => {
    switch (sectionKey) {
      case 'education': return selectedItems.educationIds || [];
      case 'experience': return selectedItems.experienceIds || [];
      case 'skills': return selectedItems.skillIds || [];
      case 'languages': return selectedItems.languageIds || [];
      case 'projects': return selectedItems.projectIds || [];
      case 'certificates': return selectedItems.certificateIds || [];
      default: return [];
    }
  };

  const items = getItemsForSection();
  const selectedIds = getSelectedIdsForSection();

  if (items.length === 0) {
    return (
      <div className="mt-3 flex flex-col items-center justify-center p-4 border border-dashed border-[var(--color-border)] rounded-md">
        <p className="text-sm text-[var(--color-textSecondary)] mb-2">No items available in your profile.</p>
        <button
          type="button"
          onClick={() => onAddNew(sectionKey)}
          className="text-sm font-medium text-[var(--color-primary)] hover:underline"
        >
          + Add New {sectionKey}
        </button>
      </div>
    );
  }

  return (
    <div className="mt-3 flex flex-col gap-2">
      <div className="flex justify-between items-center mb-1">
        <span className="text-xs font-semibold text-[var(--color-textSecondary)] uppercase tracking-wider">Select items to include</span>
        <button
          type="button"
          onClick={() => onAddNew(sectionKey)}
          className="text-xs font-medium text-[var(--color-primary)] hover:underline"
        >
          + Add New
        </button>
      </div>
      <div className="flex flex-col gap-1 max-h-48 overflow-y-auto pr-1">
        {items.map((item: any) => {
          const isSelected = selectedIds.includes(item.id);
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => onToggleItem(sectionKey, item.id)}
              className={`flex items-center gap-3 p-2 rounded-md transition-colors text-left ${isSelected ? 'bg-[var(--color-primary)]/10 text-[var(--color-textPrimary)]' : 'hover:bg-[var(--color-surfaceHover)] text-[var(--color-textSecondary)]'}`}
            >
              <div className={`flex items-center justify-center w-4 h-4 rounded border ${isSelected ? 'border-[var(--color-primary)] bg-[var(--color-primary)] text-white' : 'border-[var(--color-border)]'}`}>
                {isSelected && <Check className="w-3 h-3" />}
              </div>
              <span className="text-sm truncate leading-tight">{getDisplayLabel(item)}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
};
