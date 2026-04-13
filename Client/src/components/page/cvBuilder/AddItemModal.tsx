/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import { X, Loader2 } from "lucide-react";
import type { CVSectionKey } from "../../../types/dto";
import { cvService } from "../../../services/cv";
import { useNotification } from "../../../hooks/useNotification";
import {
  SkillForm,
  LanguageForm,
  EducationForm,
  ExperienceForm,
  ProjectForm,
  CertificateForm,
} from "./forms/CVBuilderForms";

interface AddItemModalProps {
  sectionKey: CVSectionKey;
  onClose: () => void;
  onAdded: () => Promise<void>;
}

export const AddItemModal = ({
  sectionKey,
  onClose,
  onAdded,
}: AddItemModalProps) => {
  const [formData, setFormData] = useState<any>({});
  const [isSaving, setIsSaving] = useState(false);
  const { showNotification } = useNotification();

  const handleSave = async () => {
    setIsSaving(true);
    try {
      // 1. Fetch current info
      const currentInfo = await cvService.getPersonalInfo();

      // 2. Create the new item DTO with a safe local ID (timestamp based)
      const newItem = {
        ...formData,
        id: `new-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
      };

      // 3. Append to the correct array and build the UpdatePersonalInfoDto
      const updateData: any = { ...currentInfo };

      switch (sectionKey) {
        case "skills":
          updateData.skills = [...(currentInfo.skills || []), newItem];
          break;
        case "languages":
          updateData.languages = [...(currentInfo.languages || []), newItem];
          break;
        case "education":
          updateData.education = [...(currentInfo.education || []), newItem];
          break;
        case "experience":
          updateData.experience = [...(currentInfo.experience || []), newItem];
          break;
        case "projects":
          updateData.projects = [...(currentInfo.projects || []), newItem];
          break;
        case "certificates":
          updateData.certificates = [
            ...(currentInfo.certificates || []),
            newItem,
          ];
          break;
      }

      // 4. PUT back to backend
      await cvService.updatePersonalInfo(updateData);

      showNotification({
        type: "success",
        message: `Added new ${sectionKey} item successfully.`,
      });
      await onAdded();
      onClose();
    } catch (error) {
      console.error(error);
      showNotification({
        type: "error",
        message: `Failed to save ${sectionKey}. Please try again.`,
      });
    } finally {
      setIsSaving(false);
    }
  };

  const renderForm = () => {
    switch (sectionKey) {
      case "skills":
        return <SkillForm formData={formData} setFormData={setFormData} />;
      case "languages":
        return <LanguageForm formData={formData} setFormData={setFormData} />;
      case "education":
        return <EducationForm formData={formData} setFormData={setFormData} />;
      case "experience":
        return <ExperienceForm formData={formData} setFormData={setFormData} />;
      case "projects":
        return <ProjectForm formData={formData} setFormData={setFormData} />;
      case "certificates":
        return (
          <CertificateForm formData={formData} setFormData={setFormData} />
        );
      default:
        return <p>Unknown Section: {sectionKey}</p>;
    }
  };

  // Helper validation to prevent saving completely empty necessary fields before backend fails
  const isValid = () => {
    switch (sectionKey) {
      case "skills":
        return !!formData.name;
      case "languages":
        return !!formData.name;
      case "education":
        return !!formData.universityName;
      case "experience":
        return !!formData.position;
      case "projects":
        return !!formData.name;
      case "certificates":
        return !!formData.name;
      default:
        return true;
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <div className="relative w-full max-w-lg max-h-[90vh] overflow-y-auto rounded-xl bg-[var(--color-surface)] p-6 shadow-xl">
        <div className="flex items-center justify-between mb-4 sticky top-0 bg-[var(--color-surface)] z-20 pb-2 border-b border-transparent">
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

        <div className="py-2">{renderForm()}</div>

        <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-[var(--color-border)] sticky bottom-0 bg-[var(--color-surface)] z-20">
          <button
            type="button"
            onClick={onClose}
            disabled={isSaving}
            className="rounded-md border border-[var(--color-border)] px-4 py-2 text-sm font-medium text-[var(--color-textPrimary)] disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            disabled={!isValid() || isSaving}
            onClick={handleSave}
            className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-[var(--color-background)] disabled:opacity-50 flex items-center gap-2"
          >
            {isSaving && <Loader2 className="h-4 w-4 animate-spin" />}
            {isSaving ? "Saving..." : "Save Item"}
          </button>
        </div>
      </div>
    </div>
  );
};
