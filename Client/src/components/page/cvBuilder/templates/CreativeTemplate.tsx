import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const CreativeTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-textPrimary)]">
      <header className="bg-[var(--color-primary)]/15 px-6 py-5">
        <h3 className="text-3xl font-bold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-1 text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      <div className="grid gap-4 p-6 sm:grid-cols-2">
        {sections.map((section) => (
          <section key={section.key} className="rounded-lg border border-[var(--color-border)] p-3">
            <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-2 space-y-1 text-sm">
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

export default CreativeTemplate;
