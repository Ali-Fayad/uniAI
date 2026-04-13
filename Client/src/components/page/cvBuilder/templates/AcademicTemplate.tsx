import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const AcademicTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl bg-[var(--color-surface)] p-8 text-[var(--color-textPrimary)]">
      <header>
        <h3 className="text-2xl font-bold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-1 text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      {personalInfo?.summary && (
        <section className="mt-5">
          <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">Research Summary</h4>
          <p className="mt-2 text-sm text-[var(--color-textSecondary)]">{personalInfo.summary}</p>
        </section>
      )}

      <div className="mt-5 space-y-4">
        {sections.map((section) => (
          <section key={section.key}>
            <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-2 space-y-1 text-sm">
              {section.items.map((item, index) => (
                <li key={`${section.key}-${index}`} className="border-l-2 border-[var(--color-border)] pl-3">
                  {item}
                </li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </article>
  );
};

export default AcademicTemplate;
