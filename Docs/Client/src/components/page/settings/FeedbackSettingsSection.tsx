/**
 * FeedbackSettingsSection
 *
 * Responsibility:
 * - Render feedback form UI (rating + comment + submit).
 *
 * Does NOT:
 * - Own feedback state (provided via props)
 * - Handle navigation side-effects (delegates submit handler)
 */

import React from 'react';
import SettingsSection from '../../settings/SettingsSection';
import FormButton from '../../settings/FormButton';
import { TEXT } from '../../../constants/static';
import type { FeedbackState } from '../../../hooks/useSettingsFeedback';

export interface FeedbackSettingsSectionProps {
  feedback: FeedbackState;
  setFeedback: React.Dispatch<React.SetStateAction<FeedbackState>>;
  onFeedbackSubmit: (e: React.FormEvent) => Promise<void>;
}

const FeedbackSettingsSection: React.FC<FeedbackSettingsSectionProps> = ({
  feedback,
  setFeedback,
  onFeedbackSubmit,
}) => {
  return (
    <SettingsSection title={TEXT.settings.feedback.title} icon="reviews">
      <p className="text-sm text-[var(--color-textSecondary)] mb-6">
        {TEXT.settings.feedback.description}
      </p>

      <form onSubmit={onFeedbackSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]">
            {TEXT.settings.feedback.ratingLabel}
          </label>
          <div className="mt-2 flex items-center gap-1">
            {[1, 2, 3, 4, 5].map((i) => (
              <button
                key={i}
                type="button"
                onClick={() => setFeedback((prev) => ({ ...prev, rating: i }))}
                className={`${
                  feedback.rating >= i ? 'text-yellow-400' : 'text-[var(--color-textMuted)]'
                } hover:text-yellow-500 focus:outline-none transform hover:scale-110 transition-transform`}
              >
                <span
                  className="material-symbols-outlined text-3xl"
                  style={{ fontVariationSettings: "'FILL' 1" }}
                >
                  star
                </span>
              </button>
            ))}
            <span className="ml-2 text-sm text-[var(--color-textSecondary)]">({feedback.rating}/5)</span>
          </div>
        </div>

        <div>
          <label
            className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]"
            htmlFor="feedback-text"
          >
            {TEXT.settings.feedback.feedbackLabel}
          </label>
          <div className="mt-2">
            <textarea
              id="feedback-text"
              name="comment"
              value={feedback.comment}
              onChange={(e) =>
                setFeedback((prev) => ({
                  ...prev,
                  comment: e.target.value,
                }))
              }
              placeholder={TEXT.settings.feedback.feedbackPlaceholder}
              rows={4}
              className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[var(--color-textPrimary)] bg-[var(--color-surface)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textMuted)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-focusRing)] sm:text-sm sm:leading-6"
            />
          </div>
        </div>

        <div className="flex justify-end">
          <FormButton
            type="submit"
            variant="secondary"
            className="bg-[var(--color-surface)] text-[var(--color-textPrimary)] hover:bg-[var(--color-elevatedSurface)]"
          >
            {TEXT.settings.feedback.submitButton}
          </FormButton>
        </div>
      </form>
    </SettingsSection>
  );
};

export default FeedbackSettingsSection;
