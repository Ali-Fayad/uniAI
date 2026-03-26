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
  className: string;
  children: React.ReactNode;
}

const PersonalInfoSectionCard: React.FC<PersonalInfoSectionCardProps> = ({
  title,
  className,
  children,
}) => {
  return (
    <section className={className}>
      <h2 className="text-xl font-semibold text-[var(--color-textPrimary)]">{title}</h2>
      {children}
    </section>
  );
};

export default PersonalInfoSectionCard;
