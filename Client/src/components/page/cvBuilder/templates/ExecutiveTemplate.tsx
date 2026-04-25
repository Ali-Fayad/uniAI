import { ResumeHeader, ResumeItem, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#fbfbf8',
  ink: '#172033',
  muted: '#5b6472',
  accent: '#9a6a16',
  accentSoft: '#f3ead7',
  rule: '#d8d2c4',
};

const ExecutiveTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Inter, Arial, sans-serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const sidebarSections = sections.filter((section) => section.key === 'skills' || section.key === 'languages' || section.key === 'certificates');
  const mainSections = sections.filter((section) => !sidebarSections.includes(section));

  return (
    <ResumePage theme={theme} className="p-[18mm]">
      <div className="border-b-4 pb-5" style={{ borderColor: theme.palette.accent }}>
        <ResumeHeader
          name={getDisplayName()}
          title={personalInfo?.jobTitle}
          company={personalInfo?.company}
          summary={personalInfo?.summary}
          contactItems={getContactItems(personalInfo)}
          theme={theme}
        />
      </div>

      <div className="mt-7 grid grid-cols-[1fr_52mm] gap-9">
        <div className="space-y-5">
          {mainSections.map((section) => (
            <ResumeSection key={section.key} section={section} theme={theme} variant="ruled">
              <div className="space-y-4">
                {section.items.map((item) => (
                  <ResumeItem key={item.id} item={item} theme={theme} />
                ))}
              </div>
            </ResumeSection>
          ))}
        </div>

        <aside className="space-y-5 border-l pl-5" style={{ borderColor: theme.palette.rule }}>
          {sidebarSections.map((section) => (
            <ResumeSection key={section.key} section={section} theme={theme} variant="compact">
              <SkillList items={section.items} theme={theme} variant="plain" />
            </ResumeSection>
          ))}
        </aside>
      </div>
    </ResumePage>
  );
};

export default ExecutiveTemplate;
