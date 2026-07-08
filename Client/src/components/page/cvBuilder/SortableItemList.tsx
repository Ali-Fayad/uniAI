import React from 'react';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import type { DragEndEvent } from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
  useSortable
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { MdDragIndicator } from 'react-icons/md';

interface SortableItemProps {
  id: string;
  item: any;
  label: string;
  isSelected: boolean;
  onToggle: (id: string, checked: boolean) => void;
}

const SortableItem: React.FC<SortableItemProps> = ({ id, label, isSelected, onToggle }) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 1 : 0,
    opacity: isDragging ? 0.8 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`flex items-center gap-2 p-2 border border-[var(--color-border)] rounded-md mb-2 bg-[var(--color-surface)] ${
        isDragging ? 'shadow-md border-[var(--color-primary)]' : ''
      }`}
    >
      <div
        {...attributes}
        {...listeners}
        className="cursor-move text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
      >
        <MdDragIndicator className="h-5 w-5" />
      </div>
      <input
        type="checkbox"
        id={`chk-${id}`}
        checked={isSelected}
        onChange={(e) => onToggle(id, e.target.checked)}
        className="w-4 h-4 rounded border-gray-300 text-[var(--color-primary)] focus:ring-[var(--color-primary)]"
      />
      <label htmlFor={`chk-${id}`} className="text-sm flex-1 truncate text-[var(--color-textPrimary)] cursor-pointer select-none">
        {label}
      </label>
    </div>
  );
};

interface SortableItemListProps {
  items: any[];
  itemsOrder: string[];
  selectedItemIds: string[];
  onToggle: (id: string, checked: boolean) => void;
  onReorder: (newOrder: string[]) => void;
  getLabel: (item: any) => string;
}

const SortableItemList: React.FC<SortableItemListProps> = ({
  items,
  itemsOrder,
  selectedItemIds,
  onToggle,
  onReorder,
  getLabel,
}) => {
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Compute sorted items
  // 1. Items in itemsOrder
  // 2. Remaining items not in itemsOrder
  const remainingItems = items.filter((item) => !itemsOrder.includes(item.id));
  const sortedItemsId = [...itemsOrder, ...remainingItems.map(i => i.id)];

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = sortedItemsId.indexOf(String(active.id));
      const newIndex = sortedItemsId.indexOf(String(over.id));

      const newOrder = arrayMove(sortedItemsId, oldIndex, newIndex);
      onReorder(newOrder);
    }
  };

  return (
    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
      <SortableContext items={sortedItemsId} strategy={verticalListSortingStrategy}>
        <div className="flex flex-col">
          {sortedItemsId.map((id) => {
            const item = items.find((i) => i.id === id);
            if (!item) return null;
            return (
              <SortableItem
                key={item.id}
                id={item.id}
                item={item}
                label={getLabel(item)}
                isSelected={selectedItemIds.includes(item.id)}
                onToggle={onToggle}
              />
            );
          })}
        </div>
      </SortableContext>
    </DndContext>
  );
};

export default SortableItemList;
