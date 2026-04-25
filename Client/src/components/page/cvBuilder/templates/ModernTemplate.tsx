import { ResumeHeader, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#ffffff',
  ink: '#1f2933',
  muted: '#667085',
  accent: '#2563eb',
  accentSoft: '#dbeafe',
  rule: '#d9e2ec',
};

const ModernTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Inter, Arial, sans-serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);
  const skillSection = sections.find((section) => section.key === 'skills');
  const mainSections = sections.filter((section) => section.key !== 'skills');

  return (
    <ResumePage theme={theme} className="p-[18mm]">
      <ResumeHeader
        name={getDisplayName()}
        title={personalInfo?.jobTitle}
        company={personalInfo?.company}
        summary={personalInfo?.summary}
        contactItems={contactItems}
        theme={theme}
      />

      <div className="mt-7 space-y-5">
        {skillSection && (
          <ResumeSection section={skillSection} theme={theme}>
            <SkillList items={skillSection.items} theme={theme} />
          </ResumeSection>
        )}
        {mainSections.map((section) => (
          <ResumeSection key={section.key} section={section} theme={theme} />
        ))}
      </div>
    </ResumePage>
  );
};

export default ModernTemplate;
