/**
 * usePersonalInfoDirtyTracking
 *
 * Responsibility:
 * - Track whether Personal Info state is dirty (differs from last clean snapshot).
 * - Register a beforeunload guard when the state is dirty.
 *
 * Does NOT:
 * - Perform API calls
 * - Validate data
 */

import { useEffect, useMemo, useState } from 'react';
import type { PersonalInfoState } from './personalInfoStateHelpers';
import { personalInfoStateToSnapshot } from './personalInfoStateHelpers';

export interface UsePersonalInfoDirtyTrackingReturn {
  isDirty: boolean;
  markClean: (state: PersonalInfoState) => void;
}

export const usePersonalInfoDirtyTracking = (state: PersonalInfoState): UsePersonalInfoDirtyTrackingReturn => {
  const [initialSnapshot, setInitialSnapshot] = useState('');

  const snapshot = useMemo(() => personalInfoStateToSnapshot(state), [
    state.form,
    state.education,
    state.skills,
    state.languages,
    state.experience,
    state.projects,
    state.certificates,
  ]);

  const isDirty = initialSnapshot.length > 0 && snapshot !== initialSnapshot;

  const markClean = (nextState: PersonalInfoState) => {
    setInitialSnapshot(personalInfoStateToSnapshot(nextState));
  };

  useEffect(() => {
    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!isDirty) {
        return;
      }
      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [isDirty]);

  return { isDirty, markClean };
};
