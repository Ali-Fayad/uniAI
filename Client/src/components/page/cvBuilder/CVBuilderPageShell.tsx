import { Suspense, useState } from 'react';
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core';
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical } from 'lucide-react';
import LoadingSpinner from '../../common/LoadingSpinner';
import { SectionItemSelector } from './SectionItemSelector';
import { AddItemModal } from './AddItemModal';
import { getTemplateComponent } from './templates/templateRegistry';
import { CV_SECTION_OPTIONS } from './cvBuilderSections';
import type { UseCVBuilderControllerReturn } from './useCVBuilderController';
import type { CVSectionKey } from '../../../types/dto';

interface CVBuilderPageShellProps {
  controller: UseCVBuilderControllerReturn;
}

interface SortableSectionItemProps {
  section: { key: CVSectionKey; label: string };
  enabled: boolean;
  onToggle: (section: CVSectionKey) => void;
}

const SortableSectionItem = ({ section, enabled, onToggle }: SortableSectionItemProps) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: section.key });

  return (
    <li
      ref={setNodeRef}
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
      }}
      className="flex items-center gap-3 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2"
    >
      <button
        type="button"
        className="touch-none rounded p-1 text-[var(--color-textSecondary)] hover:text-[var(--color-primary)]"
        aria-label={`Reorder ${section.label}`}
        {...attributes}
        {...listeners}
      >
        <GripVertical size={16} />
      </button>

      <label className="flex items-center gap-2 text-sm text-[var(--color-textPrimary)] w-full cursor-pointer">
        <input
          type="checkbox"
          checked={enabled}
          onChange={() => onToggle(section.key)}
          className="h-4 w-4 rounded border-[var(--color-border)] accent-[var(--color-primary)]"
        />
        {section.label}
      </label>
    </li>
  );
};

const CVBuilderPageShell = ({ controller }: CVBuilderPageShellProps) => {
  const [addItemModalSection, setAddItemModalSection] = useState<CVSectionKey | null>(null);
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(TouchSensor, { activationConstraint: { delay: 120, tolerance: 6 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const onDragEnd = (event: DragEndEvent) => {
    if (!event.over) {
      return;
    }
    controller.reorderSections(String(event.active.id), String(event.over.id));
  };

  const TemplatePreview = getTemplateComponent(controller.selectedTemplateComponentName);

  if (controller.isLoading) {
    return (
      <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading CV builder..." />
      </div>
    );
  }

  return (
    <main className="min-h-[calc(100vh-64px)] bg-[var(--color-background)] px-3 py-4 sm:px-6 sm:py-6">
      <div className="mx-auto max-w-[1600px] space-y-4">
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl font-bold text-[var(--color-textPrimary)]">CV Builder</h1>
          <p className="text-sm text-[var(--color-textSecondary)]">
            Select a template, choose your sections, and reorder them with drag and drop.
          </p>
        </div>

        {controller.error && (
          <div className="rounded-md border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-3 text-sm text-[var(--color-textPrimary)]">
            {controller.error}
          </div>
        )}

        <div className="grid grid-cols-1 gap-4 xl:grid-cols-[280px_1fr_320px]">
          <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
            <h2 className="text-base font-semibold text-[var(--color-textPrimary)]">Sections</h2>
            <p className="mt-1 text-xs text-[var(--color-textSecondary)]">Toggle and reorder sections for this CV.</p>

            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={onDragEnd}>
              <SortableContext items={controller.sectionOrder} strategy={verticalListSortingStrategy}>
                <ul className="mt-4 space-y-2">
                  {controller.sectionOrder.map((sectionKey) => {
                    const section = CV_SECTION_OPTIONS.find((item) => item.key === sectionKey);
                    if (!section) {
                      return null;
                    }

                    return (
                      <div key={section.key} className="flex flex-col gap-2">
                        <SortableSectionItem
                          section={section}
                          enabled={controller.sectionEnabled[section.key]}
                          onToggle={controller.toggleSection}
                        />
                        {controller.sectionEnabled[section.key] && (
                          <div className="pl-10 pb-2">
                            <SectionItemSelector
                              sectionKey={section.key}
                              personalInfo={controller.personalInfo}
                              selectedItems={controller.selectedItems}
                              itemsOrder={controller.itemsOrder}
                              onToggleItem={controller.toggleItem}
                              onReorderItems={controller.updateItemsOrder}
                              onAddNew={setAddItemModalSection}
                            />
                          </div>
                        )}
                      </div>
                    );
                  })}
                </ul>
              </SortableContext>
            </DndContext>
          </section>

          <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
            <h2 className="text-base font-semibold text-[var(--color-textPrimary)]">Live Preview</h2>
            <p className="mt-1 text-xs text-[var(--color-textSecondary)]">Preview updates in real-time.</p>

            <div className="mt-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-background)] p-3 sm:p-6">
              <Suspense fallback={<div className="text-sm text-[var(--color-textSecondary)]">Loading template preview...</div>}>
                <TemplatePreview personalInfo={controller.personalInfo} sectionOrder={controller.selectedSectionsOrder} selectedItems={controller.selectedItems} itemsOrder={controller.itemsOrder} />
              </Suspense>
            </div>
          </section>

          <section className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
            <h2 className="text-base font-semibold text-[var(--color-textPrimary)]">Templates</h2>
            <p className="mt-1 text-xs text-[var(--color-textSecondary)]">Choose the style for this CV.</p>

            <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-1">
              {controller.templates.map((template) => {
                const selected = template.id === controller.selectedTemplateId;

                return (
                  <button
                    key={template.id}
                    type="button"
                    onClick={() => controller.selectTemplate(template.id)}
                    className={`rounded-lg border p-3 text-left transition-colors ${
                      selected
                        ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/10'
                        : 'border-[var(--color-border)] bg-[var(--color-background)] hover:border-[var(--color-primary)]/60'
                    }`}
                  >
                    <div className="h-16 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-elevatedSurface)]" />
                    <p className="mt-2 text-sm font-semibold text-[var(--color-textPrimary)]">{template.name}</p>
                    <p className="mt-1 text-xs text-[var(--color-textSecondary)] line-clamp-2">
                      {template.description || 'No description provided.'}
                    </p>
                  </button>
                );
              })}
            </div>
          </section>
        </div>

        <div className="sticky bottom-0 z-20 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-3 sm:p-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <input
              type="text"
              value={controller.cvName}
              onChange={(event) => controller.setCvName(event.target.value)}
              placeholder="CV name"
              className="w-full sm:max-w-sm rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-sm text-[var(--color-textPrimary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-focusRing)]"
            />

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => void controller.save()}
                disabled={controller.isSaving}
                className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-semibold text-[var(--color-background)] disabled:opacity-70"
              >
                {controller.isSaving ? 'Saving...' : controller.isEditing ? 'Save CV' : 'Create CV'}
              </button>
              <button
                type="button"
                onClick={controller.downloadPdf}
                className="rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-2 text-sm font-semibold text-[var(--color-textPrimary)]"
              >
                Download PDF
              </button>
            </div>
          </div>
        </div>
      </div>
      {addItemModalSection && (
        <AddItemModal
          sectionKey={addItemModalSection}
          onClose={() => setAddItemModalSection(null)}
          onAdded={async () => {
            await controller.refreshPersonalInfo();
          }}
        />
      )}
      {addItemModalSection && (
        <AddItemModal
          sectionKey={addItemModalSection}
          onClose={() => setAddItemModalSection(null)}
          onAdded={async () => {
            await controller.refreshPersonalInfo();
          }}
        />
      )}
    </main>
  );
};

export default CVBuilderPageShell;
