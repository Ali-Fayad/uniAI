import { ResumeHeader, ResumeItem, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#fdfefe',
  ink: '#101828',
  muted: '#667085',
  accent: '#047857',
  accentSoft: '#d1fae5',
  rule: '#d0d5dd',
};

const TechnicalTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Inter, Arial, sans-serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const skills = sections.find((section) => section.key === 'skills');
  const projects = sections.find((section) => section.key === 'projects');
  const otherSections = sections.filter((section) => section.key !== 'skills' && section.key !== 'projects');

  return (
    <ResumePage theme={theme} className="p-[17mm]">
      <ResumeHeader
        name={getDisplayName()}
        title={personalInfo?.jobTitle}
        company={personalInfo?.company}
        summary={personalInfo?.summary}
        contactItems={getContactItems(personalInfo)}
        theme={theme}
      />

      <div className="mt-6 space-y-5">
        {skills && (
          <ResumeSection section={skills} theme={theme}>
            <SkillList items={skills.items} theme={theme} variant="pills" />
          </ResumeSection>
        )}

        {projects && (
          <ResumeSection section={projects} theme={theme}>
            <div className="grid grid-cols-2 gap-4">
              {projects.items.map((item) => (
                <div key={item.id} className="break-inside-avoid border p-3" style={{ borderColor: theme.palette.rule }}>
                  <ResumeItem item={item} theme={theme} compact />
                </div>
              ))}
            </div>
          </ResumeSection>
        )}

        {otherSections.map((section) => (
          <ResumeSection key={section.key} section={section} theme={theme} />
        ))}
      </div>
    </ResumePage>
  );
};

export default TechnicalTemplate;
