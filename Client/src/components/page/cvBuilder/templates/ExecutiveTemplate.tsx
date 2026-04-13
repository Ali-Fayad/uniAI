import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const ExecutiveTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-textPrimary)]">
      <header className="bg-[var(--color-elevatedSurface)] px-8 py-6">
        <h3 className="text-3xl font-bold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-1 text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        {personalInfo?.company && <p className="text-sm text-[var(--color-textSecondary)]">{personalInfo.company}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      <div className="space-y-5 px-8 py-6">
        {sections.map((section) => (
          <section key={section.key}>
            <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-2 space-y-1 text-sm">
              {section.items.map((item, index) => (
                <li key={`${section.key}-${index}`} className="rounded-md bg-[var(--color-background)] px-3 py-2">
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

export default ExecutiveTemplate;
