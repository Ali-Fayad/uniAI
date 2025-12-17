import React from "react";
import SettingsSection from "../settings/SettingsSection";
import FormInput from "../settings/FormInput";
import FormButton from "../settings/FormButton";

const SettingsPage: React.FC = () => {
  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // placeholder behavior to keep UI consistent
    console.log("Save Changes (placeholder)");
  };

  const handleFeedbackSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Feedback submitted (placeholder)");
  };

  return (
    <main className="flex-grow py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto space-y-8">
        <div className="md:flex md:items-center md:justify-between">
          <div className="min-w-0 flex-1">
            <h2 className="text-3xl font-bold leading-7 text-[#151514] sm:truncate sm:text-4xl sm:tracking-tight">
              Settings
            </h2>
          </div>
        </div>

        <SettingsSection title="Update Profile" icon="person">
          <form onSubmit={handleProfileSubmit}>
            <div className="grid grid-cols-1 gap-x-6 gap-y-6 sm:grid-cols-6">
              <div className="sm:col-span-3">
                <FormInput id="first-name" label="First name" value="Alex" />
              </div>
              <div className="sm:col-span-3">
                <FormInput id="last-name" label="Last name" value="Morgan" />
              </div>
              <div className="sm:col-span-4">
                <FormInput
                  id="username"
                  label="Username"
                  placeholder="janesmith"
                  value="alexmorgan"
                  prefix="uniai.com/"
                />
              </div>
              <div className="sm:col-span-4">
                <FormInput
                  id="email"
                  label="Email address"
                  type="email"
                  value="alex@example.com"
                />
              </div>

              <div className="sm:col-span-6 border-t border-gray-200 pt-6 mt-2">
                <div className="flex items-center justify-between">
                  <div className="flex flex-col">
                    <span className="text-sm font-medium leading-6 text-[#151514]">
                      Two-Factor Authentication (2FA)
                    </span>
                    <span className="text-sm text-gray-500">
                      Add an extra layer of security to your account.
                    </span>
                  </div>
                  <label className="relative inline-flex items-center cursor-pointer">
                    <input className="sr-only peer" type="checkbox" value="" />
                    <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-custom-primary/30 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-custom-primary"></div>
                  </label>
                </div>
              </div>
            </div>

            <div className="mt-8 flex justify-end gap-x-4">
              <FormButton variant="secondary" type="button">
                Cancel
              </FormButton>
              <FormButton variant="primary" type="submit">
                Save Changes
              </FormButton>
            </div>
          </form>
        </SettingsSection>

        <SettingsSection title="Theme Preference" icon="palette">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label className="relative flex cursor-pointer rounded-lg border bg-white p-4 shadow-sm focus:outline-none ring-2 ring-custom-primary">
              <input
                checked
                className="sr-only"
                name="theme-option"
                type="radio"
                value="light"
              />
              <span className="flex flex-1">
                <span className="flex flex-col">
                  <span className="block text-sm font-medium text-gray-900 flex items-center gap-2">
                    <span className="material-symbols-outlined text-amber-500">
                      light_mode
                    </span>
                    Light Mode
                  </span>
                  <span className="mt-1 flex items-center text-sm text-gray-500">
                    Default light appearance
                  </span>
                </span>
              </span>
              <span
                className="material-symbols-outlined text-custom-primary"
                style={{ fontVariationSettings: "'FILL' 1" }}
              >
                check_circle
              </span>
            </label>

            <label className="relative flex cursor-pointer rounded-lg border bg-gray-900 p-4 shadow-sm focus:outline-none hover:border-gray-700">
              <input
                className="sr-only"
                name="theme-option"
                type="radio"
                value="dark"
              />
              <span className="flex flex-1">
                <span className="flex flex-col">
                  <span className="block text-sm font-medium text-white flex items-center gap-2">
                    <span className="material-symbols-outlined text-white">
                      dark_mode
                    </span>
                    Dark Mode
                  </span>
                  <span className="mt-1 flex items-center text-gray-400">
                    Easy on the eyes
                  </span>
                </span>
              </span>
            </label>
          </div>
        </SettingsSection>

        <SettingsSection title="Feedback" icon="reviews">
          <p className="text-sm text-gray-600 mb-6">
            How was your experience using uniAI? We value your input.
          </p>

          <form onSubmit={handleFeedbackSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium leading-6 text-[#151514]">
                Rate your experience
              </label>
              <div className="mt-2 flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((i) => (
                  <button
                    key={i}
                    type="button"
                    className="text-yellow-400 hover:text-yellow-500 focus:outline-none transform hover:scale-110 transition-transform"
                  >
                    <span
                      className="material-symbols-outlined text-3xl"
                      style={{ fontVariationSettings: "'FILL' 1" }}
                    >
                      star
                    </span>
                  </button>
                ))}
                <span className="ml-2 text-sm text-gray-500">(4/5)</span>
              </div>
            </div>

            <div>
              <label
                className="block text-sm font-medium leading-6 text-[#151514]"
                htmlFor="feedback-text"
              >
                Your Feedback
              </label>
              <div className="mt-2">
                <textarea
                  id="feedback-text"
                  name="feedback"
                  placeholder="What features should we build next?"
                  rows={4}
                  className="block w-full rounded-md border-0 py-2.5 px-3.5 text-[#151514] bg-white/80 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-custom-primary sm:text-sm sm:leading-6"
                />
              </div>
            </div>

            <div className="flex justify-end">
              <FormButton
                type="submit"
                variant="secondary"
                className="bg-custom-secondary text-[#151514] hover:bg-[#b0b3a6]"
              >
                Submit Feedback
              </FormButton>
            </div>
          </form>
        </SettingsSection>

        <SettingsSection title="Danger Zone" icon="warning">
          <p className="text-sm text-red-600/80 mb-6">
            Irreversible and sensitive actions.
          </p>
          <div className="flex flex-col sm:flex-row gap-4">
            <FormButton variant="danger">Change Password</FormButton>
            <FormButton
              variant="ghost"
              className="border-2 border-red-600 text-red-700"
            >
              Delete Account
            </FormButton>
          </div>
        </SettingsSection>
      </div>
    </main>
  );
};

export default SettingsPage;
