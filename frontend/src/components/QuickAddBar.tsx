import { type ItemDefaultsResponse } from "../types/types";

export type QuickAddOverrides = {
  quantity: number;
  expirationDate: string;
};

type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (defaults: ItemDefaultsResponse, overrides: QuickAddOverrides) => Promise<void>;
  onAddNew: () => void;
};

export default function QuickAddBar({
  itemDefaults,
  onAddNew,
}: QuickAddBarProps) {
  const sorted = [...itemDefaults].sort((a, b) => a.name.localeCompare(b.name));

  return (
    <div className="quickaddbar" role="toolbar" aria-label="Quick add">
      {sorted.map((d) => (
        <button
          key={d.name}
          type="button"
          className="quickadd-chip"
        >
          {d.name}
        </button>
      ))}
      <button
        type="button"
        className="quickadd-chip quickadd-add-new"
        onClick={onAddNew}
      >
        + Add new
      </button>
    </div>
  );
}
