import { ResumeHeader, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#fffdf8',
  ink: '#1f1a17',
  muted: '#6f6259',
  accent: '#7a3415',
  accentSoft: '#f6e7d8',
  rule: '#d9c6b8',
};

const ClassicTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Georgia, Times New Roman, serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);

  return (
    <ResumePage theme={theme} className="p-[19mm]">
      <ResumeHeader
        name={getDisplayName()}
        title={personalInfo?.jobTitle}
        company={personalInfo?.company}
        summary={personalInfo?.summary}
        contactItems={getContactItems(personalInfo)}
        theme={theme}
        align="center"
      />

      <div className="mt-6 space-y-5">
        {sections.map((section) => (
          <ResumeSection key={section.key} section={section} theme={theme} variant="ruled">
            {section.key === 'skills' || section.key === 'languages' ? (
              <SkillList items={section.items} theme={theme} variant="plain" />
            ) : undefined}
          </ResumeSection>
        ))}
      </div>
    </ResumePage>
  );
};

export default ClassicTemplate;
