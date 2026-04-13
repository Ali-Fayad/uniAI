import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const ClassicTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);

  return (
    <article className="mx-auto max-w-3xl bg-[var(--color-surface)] p-8 text-[var(--color-textPrimary)]" style={{ fontFamily: 'Georgia, Times New Roman, serif' }}>
      <header className="text-center">
        <h3 className="text-3xl font-semibold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-1 text-sm">{personalInfo.jobTitle}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' | ')}</p>}
      </header>

      <div className="mt-6 space-y-5">
        {sections.map((section) => (
          <section key={section.key}>
            <h4 className="border-b border-[var(--color-border)] pb-1 text-sm font-semibold uppercase tracking-wide">{section.title}</h4>
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

export default ClassicTemplate;
