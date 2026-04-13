import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const ModernTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl bg-[var(--color-surface)] p-8 text-[var(--color-textPrimary)]">
      <header className="border-b border-[var(--color-border)] pb-5">
        <h3 className="text-3xl font-bold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-2 text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      {personalInfo?.summary && <p className="mt-5 text-sm leading-6 text-[var(--color-textSecondary)]">{personalInfo.summary}</p>}

      <div className="mt-6 space-y-5">
        {sections.map((section) => (
          <section key={section.key}>
            <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-[var(--color-textPrimary)]">
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

export default ModernTemplate;
