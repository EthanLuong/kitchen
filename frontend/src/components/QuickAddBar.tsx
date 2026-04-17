import { useEffect, useRef, useState } from "react";
import { type ItemDefaultsResponse } from "../types/types";
import { todayISO } from "../utility/utils";

export type QuickAddOverrides = {
  quantity: number;
  expirationDate: string;
};

type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (
    defaults: ItemDefaultsResponse,
    overrides: QuickAddOverrides,
  ) => Promise<void>;
  onAddNew: () => void;
};

export default function QuickAddBar({
  itemDefaults,
  onQuickAdd,
  onAddNew,
}: QuickAddBarProps) {
  const [expandedName, setExpandedName] = useState<string | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [expirationDate, setExpirationDate] = useState<string>(todayISO());
  const [isSubmitting, setIsSubmitting] = useState(false);

  const barRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (expandedName === null) return;

    function handleKey(e: KeyboardEvent) {
      if (e.key === "Escape") setExpandedName(null);
    }
    function handleClick(e: MouseEvent) {
      if (barRef.current && !barRef.current.contains(e.target as Node)) {
        setExpandedName(null);
      }
    }

    window.addEventListener("keydown", handleKey);
    window.addEventListener("mousedown", handleClick);
    return () => {
      window.removeEventListener("keydown", handleKey);
      window.removeEventListener("mousedown", handleClick);
    };
  }, [expandedName]);

  const sorted = [...itemDefaults].sort((a, b) => a.name.localeCompare(b.name));

  function openChip(d: ItemDefaultsResponse) {
    if (expandedName === d.name) {
      setExpandedName(null);
      return;
    }
    const seedDate = new Date();
    seedDate.setDate(seedDate.getDate() + (d.expirationDays ?? 0));
    setExpandedName(d.name);
    setQuantity(1);
    setExpirationDate(seedDate.toISOString().split("T")[0]);
  }

  async function confirm(d: ItemDefaultsResponse) {
    if (isSubmitting || quantity < 1) return;
    setIsSubmitting(true);
    try {
      await onQuickAdd(d, { quantity, expirationDate });
      setExpandedName(null);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div ref={barRef} className="quickaddbar" role="toolbar" aria-label="Quick add">
      {sorted.map((d) => {
        const expanded = expandedName === d.name;
        return (
          <div
            key={d.name}
            className={
              expanded ? "quickadd-chip--expanded" : undefined
            }
          >
            {expanded ? (
              <form
                className="quickadd-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  confirm(d);
                }}
              >
                <span className="quickadd-form-name">{d.name}</span>
                <input
                  type="number"
                  min={1}
                  step={1}
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  aria-label="Quantity"
                />
                <input
                  type="date"
                  value={expirationDate}
                  onChange={(e) => setExpirationDate(e.target.value)}
                  aria-label="Expiration date"
                />
                <button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "…" : "Add"}
                </button>
                <button
                  type="button"
                  onClick={() => setExpandedName(null)}
                  disabled={isSubmitting}
                >
                  Cancel
                </button>
              </form>
            ) : (
              <button
                type="button"
                className="quickadd-chip"
                onClick={() => openChip(d)}
              >
                {d.name}
              </button>
            )}
          </div>
        );
      })}
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
