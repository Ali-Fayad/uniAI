/**
 * usePersonalInfoFormState
 *
 * Responsibility:
 * - Own basic Personal Info form state and field setter helper.
 *
 * Does NOT:
 * - Perform API calls
 * - Validate or persist data
 */

import { useCallback, useState } from 'react';
import type { BasicFormState } from './personalInfoTypes';
import { createEmptyPersonalInfoFormState } from './personalInfoStateHelpers';

export interface UsePersonalInfoFormStateReturn {
  form: BasicFormState;
  setForm: React.Dispatch<React.SetStateAction<BasicFormState>>;
  setField: (field: keyof BasicFormState, value: string) => void;
}

export const usePersonalInfoFormState = (): UsePersonalInfoFormStateReturn => {
  const [form, setForm] = useState<BasicFormState>(createEmptyPersonalInfoFormState);

  const setField = useCallback((field: keyof BasicFormState, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  }, []);

  return { form, setForm, setField };
};
