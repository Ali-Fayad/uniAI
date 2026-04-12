import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const CompactTemplate = ({ personalInfo, sectionOrder, selectedItems }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl bg-[var(--color-surface)] p-5 text-[var(--color-textPrimary)]">
      <header>
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h3 className="text-2xl font-bold">{getDisplayName()}</h3>
          {personalInfo?.jobTitle && <p className="text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        </div>
        {contactItems.length > 0 && <p className="mt-1 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
        {sections.map((section) => (
          <section key={section.key} className="rounded-md border border-[var(--color-border)] p-2">
            <h4 className="text-xs font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-1 space-y-1 text-xs">
              {section.items.map((item, index) => (
                <li key={`${section.key}-${index}`}>{item}</li>
              ))}
            </ul>
          </section>
        ))}
      </div>
    </article>
  );
};

export default CompactTemplate;
