import { Suspense, createElement } from 'react';
import { getTemplateComponent } from './templates/templateRegistry';
import type { CVSectionKey, ItemsOrderDto, PersonalInfoResponseDto, SelectedItemsDto } from '../../../types/dto';

interface CVExportSurfaceProps {
  templateComponentName: string;
  personalInfo: PersonalInfoResponseDto | null;
  sectionOrder: CVSectionKey[];
  selectedItems: SelectedItemsDto;
  itemsOrder: ItemsOrderDto;
}

/**
 * Hidden export-only surface used as the html2pdf.js source node.
 * It renders only the template content so builder chrome never enters the PDF.
 */
const CVExportSurface = ({
  templateComponentName,
  personalInfo,
  sectionOrder,
  selectedItems,
  itemsOrder,
}: CVExportSurfaceProps) => {
  const TemplateComponent = getTemplateComponent(templateComponentName);

  return (
    <div
      id="cv-export-surface"
      aria-hidden="true"
      className="pointer-events-none fixed -left-[10000px] top-0 z-[-1] overflow-hidden bg-white"
      style={{
        width: '210mm',
        minHeight: '297mm',
      }}
    >
      <Suspense fallback={null}>
        {createElement(TemplateComponent, {
          personalInfo,
          sectionOrder,
          selectedItems,
          itemsOrder,
        })}
      </Suspense>
    </div>
  );
};

export default CVExportSurface;
