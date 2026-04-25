import { ResumeItem, ResumePage, ResumeSection, SkillList } from './ResumePrimitives';
import type { CVTemplateComponentProps, CVTemplatePalette } from './templateTypes';
import { mergePalette } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const defaultPalette: CVTemplatePalette = {
  paper: '#ffffff',
  ink: '#253238',
  muted: '#65737e',
  accent: '#0f766e',
  accentSoft: '#ccfbf1',
  rule: '#d7e4e2',
  sidebar: '#12343b',
  sidebarInk: '#f8fafc',
};

const CreativeTemplate = ({ personalInfo, sectionOrder, selectedItems, itemsOrder, palette }: CVTemplateComponentProps) => {
  const mergedPalette = mergePalette(defaultPalette, palette);
  const theme = { palette: mergedPalette, fontFamily: 'Inter, Arial, sans-serif' };
  const sidebarTheme = {
    ...theme,
    palette: {
      ...mergedPalette,
      paper: mergedPalette.sidebar ?? mergedPalette.accent,
      ink: mergedPalette.sidebarInk ?? '#ffffff',
      muted: '#cbd5e1',
      accent: '#5eead4',
      accentSoft: 'rgba(255,255,255,0.12)',
      rule: 'rgba(255,255,255,0.22)',
    },
  };
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const sidebarSections = sections.filter((section) => section.key === 'skills' || section.key === 'languages');
  const mainSections = sections.filter((section) => section.key !== 'skills' && section.key !== 'languages');

  return (
    <ResumePage theme={theme} className="grid grid-cols-[58mm_1fr] overflow-hidden">
      <aside className="p-[12mm] text-[12px]" style={{ backgroundColor: sidebarTheme.palette.paper, color: sidebarTheme.palette.ink }}>
        <h3 className="text-[28px] font-semibold leading-tight" style={{ color: sidebarTheme.palette.ink }}>
          {getDisplayName()}
        </h3>
        {personalInfo?.jobTitle && (
          <p className="mt-2 text-[11px] font-semibold uppercase" style={{ color: sidebarTheme.palette.accent, letterSpacing: '0.12em' }}>
            {personalInfo.jobTitle}
          </p>
        )}

        {getContactItems(personalInfo).length > 0 && (
          <div className="mt-7 space-y-1.5 text-[11px]" style={{ color: sidebarTheme.palette.muted }}>
            {getContactItems(personalInfo).map((item) => (
              <p key={item} className="break-words">
                {item}
              </p>
            ))}
          </div>
        )}

        <div className="mt-8 space-y-6">
          {sidebarSections.map((section) => (
            <ResumeSection key={section.key} section={section} theme={sidebarTheme} variant="compact">
              <SkillList items={section.items} theme={sidebarTheme} variant="bars" />
            </ResumeSection>
          ))}
        </div>
      </aside>

      <div className="p-[15mm]">
        {personalInfo?.summary && (
          <section className="mb-6 break-inside-avoid">
            <h4 className="mb-2 text-[11px] font-bold uppercase" style={{ color: theme.palette.accent, letterSpacing: '0.12em' }}>
              Profile
            </h4>
            <p className="text-[13px] leading-relaxed" style={{ color: theme.palette.ink }}>
              {personalInfo.summary}
            </p>
          </section>
        )}

        <div className="space-y-5">
          {mainSections.map((section) => (
            <ResumeSection key={section.key} section={section} theme={theme}>
              <div className="space-y-3">
                {section.items.map((item) => (
                  <ResumeItem key={item.id} item={item} theme={theme} />
                ))}
              </div>
            </ResumeSection>
          ))}
        </div>
      </div>
    </ResumePage>
  );
};

export default CreativeTemplate;
