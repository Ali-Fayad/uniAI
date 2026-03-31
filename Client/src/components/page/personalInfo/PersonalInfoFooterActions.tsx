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
import FormButton from '../../settings/FormButton';

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
        <FormButton
          variant="secondary"
          type="button"
          onClick={onSkip}
        >
          Skip for now
        </FormButton>
      ) : (
        <FormButton
          variant="secondary"
          type="button"
          onClick={onBack}
        >
          Back to Settings
        </FormButton>
      )}
      <FormButton
        variant="primary"
        type="button"
        onClick={onSave}
        disabled={!isDirty || isSaving}
      >
        {isSaving ? 'Saving...' : 'Save Changes'}
      </FormButton>
    </div>
  );
};

export default PersonalInfoFooterActions;
