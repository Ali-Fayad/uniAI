import { ResumeHeader, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#ffffff',
  ink: '#111827',
  muted: '#6b7280',
  accent: '#4f46e5',
  accentSoft: '#e0e7ff',
  rule: '#e5e7eb',
};

const CompactTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Arial, Helvetica, sans-serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);

  return (
    <ResumePage theme={theme} className="p-[13mm] text-[12px]">
      <ResumeHeader
        name={getDisplayName()}
        title={personalInfo?.jobTitle}
        company={personalInfo?.company}
        summary={personalInfo?.summary}
        contactItems={getContactItems(personalInfo)}
        theme={theme}
        compact
      />

      <div className="mt-4 grid grid-cols-2 gap-x-6 gap-y-4">
        {sections.map((section) => (
          <ResumeSection key={section.key} section={section} theme={theme} variant="compact">
            {section.key === 'skills' || section.key === 'languages' ? (
              <SkillList items={section.items} theme={theme} variant="plain" />
            ) : undefined}
          </ResumeSection>
        ))}
      </div>
    </ResumePage>
  );
};

export default CompactTemplate;
