import type { CVTemplateComponentProps } from './templateTypes';
import { getContactItems, getDisplayName, getSectionData } from './templateHelpers';

const TechnicalTemplate = ({ personalInfo, sectionOrder, selectedItems , itemsOrder }: CVTemplateComponentProps) => {
  const sections = getSectionData(personalInfo, sectionOrder, selectedItems, itemsOrder);
  const contactItems = getContactItems(personalInfo);
  const skills = sections.find((section) => section.key === 'skills');
  const otherSections = sections.filter((section) => section.key !== 'skills');

  return (
    <article className="mx-auto max-w-3xl bg-[var(--color-surface)] p-7 text-[var(--color-textPrimary)]">
      <header className="border-b border-[var(--color-border)] pb-4">
        <h3 className="text-3xl font-bold">{getDisplayName()}</h3>
        {personalInfo?.jobTitle && <p className="mt-1 text-sm text-[var(--color-textSecondary)]">{personalInfo.jobTitle}</p>}
        {contactItems.length > 0 && <p className="mt-2 text-xs text-[var(--color-textSecondary)]">{contactItems.join(' • ')}</p>}
      </header>

      {skills && (
        <section className="mt-4">
          <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">Core Skills</h4>
          <div className="mt-2 grid grid-cols-2 gap-2 text-sm sm:grid-cols-3">
            {skills.items.map((item, index) => (
              <span key={`skill-${index}`} className="rounded-md bg-[var(--color-elevatedSurface)] px-2 py-1 text-center">
                {item}
              </span>
            ))}
          </div>
        </section>
      )}

      <div className="mt-5 space-y-4">
        {otherSections.map((section) => (
          <section key={section.key}>
            <h4 className="text-sm font-semibold uppercase tracking-wide text-[var(--color-primary)]">{section.title}</h4>
            <ul className="mt-2 space-y-1 text-sm">
              {section.items.map((item, index) => (
                <li key={`${section.key}-${index}`}>
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

export default TechnicalTemplate;
