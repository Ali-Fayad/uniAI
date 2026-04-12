import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import type { CVSectionKey } from '../../../types/dto';
import { usePersonalInfoController } from '../personalInfo/usePersonalInfoController';

// We could reuse the internal form components from personalInfo, but for speed 
// and encapsulation, we'll just refer user to the Personal Info page for now or open it. 
// A real implementation would pull from `components/page/personalInfo/sections/...`
// We'll leave it as a simple placeholder for this demonstration that closes the loop.

interface AddItemModalProps {
  sectionKey: CVSectionKey;
  onClose: () => void;
  onAdded: () => Promise<void>;
}

export const AddItemModal = ({ sectionKey, onClose, onAdded }: AddItemModalProps) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="relative w-full max-w-lg rounded-xl bg-[var(--color-surface)] p-6 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-[var(--color-textPrimary)] capitalize">
            Add New {sectionKey}
          </h2>
          <button
            onClick={onClose}
            className="rounded p-1 text-[var(--color-textSecondary)] hover:bg-[var(--color-surfaceHover)]"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <p className="text-sm text-[var(--color-textSecondary)] mb-6">
          Form for {sectionKey} goes here. (Assuming integration with PersonalInfo internal forms).
        </p>
        <div className="flex justify-end gap-3 mt-4">
          <button
            type="button"
            onClick={onClose}
            className="rounded-md border border-[var(--color-border)] px-4 py-2 text-sm font-medium text-[var(--color-textPrimary)]"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={async () => {
              // Simulate saving
              await onAdded();
              onClose();
            }}
            className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-[var(--color-background)]"
          >
            Save Item
          </button>
        </div>
      </div>
    </div>
  );
};
