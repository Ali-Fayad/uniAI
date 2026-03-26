/**
 * PersonalInfoSectionCard
 *
 * Responsibility:
 * - Render a consistent section wrapper (title + content) for Personal Info.
 *
 * Does NOT:
 * - Own any Personal Info state
 * - Perform API calls
 */

import React from 'react';

export interface PersonalInfoSectionCardProps {
  title: string;
  icon?: React.ReactNode;
  className: string;
  children: React.ReactNode;
}

const PersonalInfoSectionCard: React.FC<PersonalInfoSectionCardProps> = ({
  title,
  icon,
  className,
  children,
}) => {
  return (
    <section className={className}>
      <h2 className="text-xl font-bold text-[var(--color-textPrimary)] flex items-center gap-2">
        {icon ? (
          <span className="inline-flex h-6 w-6 items-center justify-center text-[var(--color-textPrimary)]">
            {icon}
          </span>
        ) : null}
        {title}
      </h2>
      {children}
    </section>
  );
};

export default PersonalInfoSectionCard;
