import React from "react";
import SettingsSection from "../settings/SettingsSection";
import FormInput from "../settings/FormInput";
import FormButton from "../settings/FormButton";
import { useSettings } from "../../hooks/useSettings";
import { useScrollAnimation } from "../../hooks/useScrollAnimation";
import { TEXT } from "../../constants/static";

/**
 * AnimatedField Component
 * 
 * Wrapper component for scroll animations
 */
const AnimatedField: React.FC<{ children: React.ReactNode; delay?: number }> = ({ 
  children, 
  delay = 0 
}) => {
  const ref = useScrollAnimation({
    delay,
    threshold: 0.05,
    transition: "opacity 300ms ease, transform 300ms ease",
  });
  return <div ref={ref}>{children}</div>;
};

/**
 * SettingsPage Component
 * 
 * Responsibilities:
 * - Render settings interface layout
 * - Compose settings sections
 * 
 * All business logic is encapsulated in useSettings hook.
 */
const SettingsPage: React.FC = () => {
  const {
    profile,
    feedback,
    isLoading,
    selectedTheme,
    setSelectedTheme,
    handleProfileChange,
    handleProfileSubmit,
    handleFeedbackSubmit,
    setFeedback,
  } = useSettings();

  return (
    <main className="flex-grow py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="md:flex md:items-center md:justify-between">
          <div className="min-w-0 flex-1">
            <h2 className="text-3xl font-bold leading-7 text-[var(--color-textPrimary)] sm:truncate sm:text-4xl sm:tracking-tight">
              {TEXT.settings.title}
            </h2>
          </div>
        </div>

        <AnimatedField delay={0}>
          <SettingsSection title={TEXT.settings.profile.title} icon="person">
            <form onSubmit={handleProfileSubmit}>
              <div className="grid grid-cols-1 gap-x-6 gap-y-6 sm:grid-cols-6">
                <div className="sm:col-span-3">
                  <FormInput
                    id="first-name"
                    name="firstName"
                    label={TEXT.settings.profile.firstName}
                    value={profile.firstName}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-3">
                  <FormInput
                    id="last-name"
                    name="lastName"
                    label={TEXT.settings.profile.lastName}
                    value={profile.lastName}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-4">
                  <FormInput
                    id="username"
                    name="username"
                    label={TEXT.settings.profile.username}
                    placeholder={TEXT.settings.profile.usernamePlaceholder}
                    value={profile.username}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-4">
                  <FormInput
                    id="email"
                    name="email"
                    label={TEXT.settings.profile.email}
                    type="email"
                    value={profile.email}
                    disabled={true}
                    onChange={handleProfileChange}
                  />
                </div>

                <div className="sm:col-span-6 border-t border-[var(--color-border)] pt-6 mt-2">
                  <div className="flex items-center justify-between">
                    <div className="flex flex-col">
                      <span className="text-sm font-medium leading-6 text-[var(--color-textPrimary)]">
                        {TEXT.settings.profile.twoFactor.title}
                      </span>
                      <span className="text-sm text-[var(--color-textSecondary)]">
                        {TEXT.settings.profile.twoFactor.description}
                      </span>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        className="sr-only peer"
                        type="checkbox"
                        name="twoFactorEnabled"
                        checked={profile.twoFactorEnabled}
                        onChange={handleProfileChange}
                      />
                      <div className="w-11 h-6 bg-[var(--color-elevatedSurface)] peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-[var(--color-focusRing)]/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-[var(--color-surface)] after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[var(--color-surface)] after:border-[var(--color-border)] after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[var(--color-primary)]"></div>
                    </label>
                  </div>
                </div>
              </div>

              <div className="mt-8 flex justify-end gap-x-4">
                <FormButton variant="secondary" type="button">
                  {TEXT.common.cancel}
                </FormButton>
                <button
                  type="submit"
                  className="inline-flex items-center justify-center rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-[var(--color-background)] hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={isLoading}
                >
                  {isLoading ? TEXT.settings.profile.saveButtonLoading : TEXT.settings.profile.saveButton}
                </button>
              </div>
            </form>
          </SettingsSection>
        </AnimatedField>

        <AnimatedField delay={50}>
          <SettingsSection title={TEXT.settings.theme.title} icon="palette">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <label
                className={`relative flex cursor-pointer rounded-lg border p-4 shadow-sm focus:outline-none ring-2 ${
                  selectedTheme === "light"
                    ? "ring-[var(--color-focusRing)] bg-[var(--color-surface)]"
                    : "bg-[var(--color-surface)] border-[var(--color-border)]"
                }`}
                onClick={() => setSelectedTheme("light")}
              >
                <input
                  checked={selectedTheme === "light"}
                  onChange={() => setSelectedTheme("light")}
                  className="sr-only"
                  name="theme-option"
                  type="radio"
                  value="light"
                />
                <span className="flex flex-1">
                  <span className="flex flex-col">
                    <span className="block text-sm font-medium text-[var(--color-textPrimary)] flex items-center gap-2">
                      <span className="material-symbols-outlined text-yellow-500">
                        light_mode
                      </span>
                      {TEXT.settings.theme.light.title}
                    </span>
                    <span className="mt-1 flex items-center text-sm text-[var(--color-textSecondary)]">
                      {TEXT.settings.theme.light.description}
                    </span>
                  </span>
                </span>
                {selectedTheme === "light" && (
                  <span
                    className="material-symbols-outlined text-[var(--color-primary)]"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    check_circle
                  </span>
                )}
              </label>

              <label
                className={`relative flex cursor-pointer rounded-lg border p-4 shadow-sm focus:outline-none ${
                  selectedTheme === "dark"
                    ? "ring-[var(--color-focusRing)] bg-[var(--color-background)] border-[var(--color-border)]"
                    : "bg-[var(--color-surface)] border-[var(--color-border)]"
                }`}
                onClick={() => setSelectedTheme("dark")}
              >
                <input
                  className="sr-only"
                  name="theme-option"
                  type="radio"
                  value="dark"
                  checked={selectedTheme === "dark"}
                  onChange={() => setSelectedTheme("dark")}
                />
                <span className="flex flex-1">
                  <span className="flex flex-col">
                    <span className={`block text-sm font-medium text-[var(--color-textPrimary)] flex items-center gap-2`}>
                      <span className="material-symbols-outlined text-[var(--color-textPrimary)]">
                        dark_mode
                      </span>
                      {TEXT.settings.theme.dark.title}
                    </span>
                    <span className="mt-1 flex items-center text-[var(--color-textSecondary)]">
                      {TEXT.settings.theme.dark.description}
                    </span>
                  </span>
                </span>
                {selectedTheme === "dark" && (
                  <span
                    className="material-symbols-outlined text-[var(--color-primary)]"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    check_circle
                  </span>
                )}
              </label>
            </div>
          </SettingsSection>
        </AnimatedField>

        <AnimatedField delay={100}>
          <SettingsSection title={TEXT.settings.feedback.title} icon="reviews">
            <p className="text-sm text-[var(--color-textSecondary)] mb-6">
              {TEXT.settings.feedback.description}
            </p>

            <form onSubmit={handleFeedbackSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]">
                  {TEXT.settings.feedback.ratingLabel}
                </label>
                <div className="mt-2 flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <button
                      key={i}
                      type="button"
                      onClick={() =>
                        setFeedback((prev) => ({ ...prev, rating: i }))
                      }
                      className={`${
                        feedback.rating >= i
                          ? "text-yellow-400"
                          : "text-[var(--color-textMuted)]"
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
                  <span className="ml-2 text-sm text-[var(--color-textSecondary)]">
                    ({feedback.rating}/5)
                  </span>
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
        </AnimatedField>

        <AnimatedField delay={150}>
          <SettingsSection title={TEXT.settings.dangerZone.title} icon="warning">
            <p className="text-sm text-[var(--color-error)]/80 mb-6">
              {TEXT.settings.dangerZone.description}
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <FormButton variant="danger">{TEXT.settings.dangerZone.changePassword}</FormButton>
              <FormButton
                variant="ghost"
                className="border-2 border-[var(--color-error)] text-[var(--color-error)]"
              >
                {TEXT.settings.dangerZone.deleteAccount}
              </FormButton>
            </div>
          </SettingsSection>
        </AnimatedField>
      </div>
    </main>
  );
};

export default SettingsPage;
