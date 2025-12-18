import React, { useEffect, useState } from "react";
import SettingsSection from "../settings/SettingsSection";
import FormInput from "../settings/FormInput";
import FormButton from "../settings/FormButton";
import { Storage } from "../../utils/Storage";
import { userService } from "../../services/user";
import type { UpdateUserDto } from "../../types/dto";
import { applyThemeByName, getSavedTheme } from "../../styles/themes";
import type { ThemeName } from "../../styles/themes";

// Reusable hook for scroll animations (copies behavior from MainPage)
const useScrollAnimation = (delay = 0) => {
  const ref = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;

    // Initial state
    el.style.opacity = "0";
    el.style.transform = "translateY(20px)";
    // faster transition
    el.style.transition = "opacity 300ms ease, transform 300ms ease";

    const obs = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            // Animate in
            setTimeout(() => {
              el.style.opacity = "1";
              el.style.transform = "translateY(0)";
            }, delay);
          } else {
            // Reverse animation (hide) when scrolling up/away
            el.style.opacity = "0";
            el.style.transform = "translateY(20px)";
          }
        });
      },
      // lower threshold so element triggers sooner
      { threshold: 0.05 }
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [delay]);

  return ref;
};

const AnimatedField: React.FC<{ children: React.ReactNode; delay?: number }> = ({ children, delay = 0 }) => {
  const ref = useScrollAnimation(delay);
  return <div ref={ref}>{children}</div>;
};

const SettingsPage: React.FC = () => {
  // Initialize state from storage to avoid "uncontrolled" error and unnecessary API calls
  const [profile, setProfile] = useState(() => {
    const stored = Storage.getUser();
    return {
      firstName: stored?.firstName || "",
      lastName: stored?.lastName || "",
      username: stored?.username || "",
      email: stored?.email || "",
      twoFactorEnabled: stored?.twoFactorEnabled || false,
    };
  });

  const [feedback, setFeedback] = useState({
    rating: 0,
    comment: "",
  });

  const [isLoading, setIsLoading] = useState(false);

  // Theme selection state
  const [selectedTheme, setSelectedTheme] = useState<ThemeName>(() => getSavedTheme());

  useEffect(() => {
    // Ensure the currently saved theme is applied when this page mounts
    applyThemeByName(selectedTheme);
  }, [selectedTheme]);

  // We rely on Storage data as primary source.
  // Only fetch if storage is empty (rare if protected route)
  useEffect(() => {
    if (!Storage.getUser()) {
      const loadUserData = async () => {
        try {
          const userData = await userService.getMe();
          setProfile({
            firstName: userData.firstName || "",
            lastName: userData.lastName || "",
            username: userData.username || "",
            email: userData.email || "",
            twoFactorEnabled: userData.isTwoFacAuth,
          });

          // Save to storage mapped correctly
          Storage.setUser({
            id: 0, // ID might be missing if not in token/response, but UserData requires it? Check DTO.
            ...userData,
            twoFactorEnabled: userData.isTwoFacAuth,
          } as any); // Type assertion might be needed if DTOs don't perfectly align
        } catch (error) {
          console.error("Failed to fetch user data", error);
        }
      };
      loadUserData();
    }
  }, []);

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setProfile((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const stored = Storage.getUser();
      const updateDto: UpdateUserDto = {};
      let hasChanges = false;

      // Only include fields that have changed
      if (profile.firstName !== (stored?.firstName || "")) {
        updateDto.firstName = profile.firstName;
        hasChanges = true;
      }
      if (profile.lastName !== (stored?.lastName || "")) {
        updateDto.lastName = profile.lastName;
        hasChanges = true;
      }
      if (profile.username !== (stored?.username || "")) {
        updateDto.username = profile.username;
        hasChanges = true;
      }
      if (profile.twoFactorEnabled !== (stored?.twoFactorEnabled || false)) {
        updateDto.enableTwoFactor = profile.twoFactorEnabled;
        hasChanges = true;
      }

      if (!hasChanges) {
        console.log("No changes to save");
        setIsLoading(false);
        return;
      }

      const updatedUser = await userService.updateMe(updateDto);

      // Update local state and storage
      setProfile((prev) => ({
        ...prev,
        firstName: updatedUser.firstName || "",
        lastName: updatedUser.lastName || "",
        username: updatedUser.username || "",
      }));

      const currentStorage = Storage.getUser();
      if (currentStorage) {
        Storage.setUser({
          ...currentStorage,
          firstName: updatedUser.firstName,
          lastName: updatedUser.lastName,
          username: updatedUser.username,
          twoFactorEnabled: updatedUser.isTwoFacAuth,
        });
      }

      console.log("Profile updated successfully");
    } catch (error) {
      console.error("Failed to update profile", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFeedbackSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const commentWithRating = `Rating: ${feedback.rating}/5. ${feedback.comment}`;
      await userService.sendFeedback({
        email: profile.email,
        comment: commentWithRating,
      });
      setFeedback({ rating: 0, comment: "" });
      console.log("Feedback submitted successfully");
    } catch (error) {
      console.error("Failed to submit feedback", error);
    }
  };

  return (
    <main className="flex-grow py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="md:flex md:items-center md:justify-between">
          <div className="min-w-0 flex-1">
            <h2 className="text-3xl font-bold leading-7 text-[var(--color-textPrimary)] sm:truncate sm:text-4xl sm:tracking-tight">
              Settings
            </h2>
          </div>
        </div>

        <AnimatedField delay={0}>
          <SettingsSection title="Update Profile" icon="person">
            <form onSubmit={handleProfileSubmit}>
              <div className="grid grid-cols-1 gap-x-6 gap-y-6 sm:grid-cols-6">
                <div className="sm:col-span-3">
                  <FormInput
                    id="first-name"
                    name="firstName"
                    label="First name"
                    value={profile.firstName}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-3">
                  <FormInput
                    id="last-name"
                    name="lastName"
                    label="Last name"
                    value={profile.lastName}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-4">
                  <FormInput
                    id="username"
                    name="username"
                    label="Username"
                    placeholder="janesmith"
                    value={profile.username}
                    onChange={handleProfileChange}
                  />
                </div>
                <div className="sm:col-span-4">
                  <FormInput
                    id="email"
                    name="email"
                    label="Email address"
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
                        Two-Factor Authentication (2FA)
                      </span>
                      <span className="text-sm text-[var(--color-textSecondary)]">
                        Add an extra layer of security to your account.
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
                  Cancel
                </FormButton>
                <button
                  type="submit"
                  className="inline-flex items-center justify-center rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-[var(--color-background)] hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={isLoading}
                >
                  {isLoading ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </SettingsSection>
        </AnimatedField>

        <AnimatedField delay={50}>
          <SettingsSection title="Theme Preference" icon="palette">
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
                      Light Mode
                    </span>
                    <span className="mt-1 flex items-center text-sm text-[var(--color-textSecondary)]">
                      Default light appearance
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
                      Dark Mode
                    </span>
                    <span className="mt-1 flex items-center text-[var(--color-textSecondary)]">
                      Easy on the eyes
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
          <SettingsSection title="Feedback" icon="reviews">
            <p className="text-sm text-[var(--color-textSecondary)] mb-6">
              How was your experience using uniAI? We value your input.
            </p>

            <form onSubmit={handleFeedbackSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]">
                  Rate your experience
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
                  Your Feedback
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
                    placeholder="What features should we build next?"
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
                  Submit Feedback
                </FormButton>
              </div>
            </form>
          </SettingsSection>
        </AnimatedField>

        <AnimatedField delay={150}>
          <SettingsSection title="Danger Zone" icon="warning">
            <p className="text-sm text-[var(--color-error)]/80 mb-6">
              Irreversible and sensitive actions.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <FormButton variant="danger">Change Password</FormButton>
              <FormButton
                variant="ghost"
                className="border-2 border-[var(--color-error)] text-[var(--color-error)]"
              >
                Delete Account
              </FormButton>
            </div>
          </SettingsSection>
        </AnimatedField>
      </div>
    </main>
  );
};

export default SettingsPage;
