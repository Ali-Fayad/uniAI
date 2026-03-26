/**
 * PersonalInfoFooterActions
 *
 * Responsibility:
 * - Render the Personal Info footer actions (back/skip + save).
 *
 * Does NOT:
 * - Perform navigation confirmation
 * - Persist data
 */

import React from 'react';

export interface PersonalInfoFooterActionsProps {
  fromOnboarding: boolean;
  isDirty: boolean;
  isSaving: boolean;
  onSkip: () => void;
  onBack: () => void;
  onSave: () => void;
}

const PersonalInfoFooterActions: React.FC<PersonalInfoFooterActionsProps> = ({
  fromOnboarding,
  isDirty,
  isSaving,
  onSkip,
  onBack,
  onSave,
}) => {
  return (
    <div className="flex flex-col-reverse sm:flex-row gap-3 sm:justify-end pb-8">
      {fromOnboarding ? (
        <button
          type="button"
          onClick={onSkip}
          className="rounded-md border border-[var(--color-border)] px-4 py-2 text-[var(--color-textPrimary)]"
        >
          Skip for now
        </button>
      ) : (
        <button
          type="button"
          onClick={onBack}
          className="rounded-md border border-[var(--color-border)] px-4 py-2 text-[var(--color-textPrimary)]"
        >
          Back to Settings
        </button>
      )}
      <button
        type="button"
        onClick={onSave}
        disabled={!isDirty || isSaving}
        className="rounded-md bg-[var(--color-primary)] px-5 py-2 font-semibold text-[var(--color-background)] disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isSaving ? 'Saving...' : 'Save Changes'}
      </button>
    </div>
  );
};

export default PersonalInfoFooterActions;
