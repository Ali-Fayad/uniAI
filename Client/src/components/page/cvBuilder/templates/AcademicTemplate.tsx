import { ResumeHeader, ResumeItem, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#ffffff',
  ink: '#202124',
  muted: '#5f6368',
  accent: '#174ea6',
  accentSoft: '#e8f0fe',
  rule: '#cfd8dc',
};

const AcademicTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const theme = { palette: mergePalette(defaultPalette, palette), fontFamily: 'Merriweather, Georgia, Times New Roman, serif' };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);

  return (
    <ResumePage theme={theme} className="p-[18mm]">
      <div className="border-b pb-4" style={{ borderColor: theme.palette.rule }}>
        <ResumeHeader
          name={getDisplayName()}
          title={personalInfo?.jobTitle}
          company={personalInfo?.company}
          summary={personalInfo?.summary}
          contactItems={getContactItems(personalInfo)}
          theme={theme}
          compact
        />
      </div>

      <div className="mt-5 grid grid-cols-[40mm_1fr] gap-8">
        <aside className="space-y-5">
          {sections
            .filter((section) => section.key === 'skills' || section.key === 'languages' || section.key === 'certificates')
            .map((section) => (
              <ResumeSection key={section.key} section={section} theme={theme} variant="compact">
                <SkillList items={section.items} theme={theme} variant="plain" />
              </ResumeSection>
            ))}
        </aside>

        <div className="space-y-5">
          {sections
            .filter((section) => section.key !== 'skills' && section.key !== 'languages' && section.key !== 'certificates')
            .map((section) => (
              <ResumeSection key={section.key} section={section} theme={theme} variant="ruled">
                <div className="space-y-3">
                  {section.items.map((item) => (
                    <div key={item.id} className="border-l-2 pl-3" style={{ borderColor: theme.palette.accentSoft }}>
                      <ResumeItem item={item} theme={theme} />
                    </div>
                  ))}
                </div>
              </ResumeSection>
            ))}
        </div>
      </div>
    </ResumePage>
  );
};

export default AcademicTemplate;
