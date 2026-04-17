type EmptyStateProps =
  | {
      variant: "no-items";
      onAddItem: () => void;
    }
  | {
      variant: "no-matches";
      onClearFilters: () => void;
    };

export default function EmptyState(props: EmptyStateProps) {
  if (props.variant === "no-items") {
    return (
      <div className="empty-state">
        <span className="empty-state-mark" aria-hidden="true">
          <svg
            width="42"
            height="42"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.4"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M4 13h16a6 6 0 0 1-6 6h-4a6 6 0 0 1-6-6z" />
            <path d="M7 13V9a5 5 0 0 1 10 0v4" />
            <path d="M12 4v2" />
          </svg>
        </span>
        <h2 className="empty-state-title">Your kitchen is empty</h2>
        <p className="empty-state-body">
          Track what's in your fridge, pantry, and freezer so nothing gets lost
          behind the leftovers.
        </p>
        <button
          type="button"
          className="empty-state-cta"
          onClick={props.onAddItem}
        >
          Add your first item
        </button>
      </div>
    );
  }

  return (
    <div className="empty-state">
      <span className="empty-state-mark empty-state-mark--muted" aria-hidden="true">
        <svg
          width="38"
          height="38"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" />
        </svg>
      </span>
      <h2 className="empty-state-title">No items match your filters</h2>
      <p className="empty-state-body">
        Try loosening the location or type filters to see more of your kitchen.
      </p>
      <button
        type="button"
        className="empty-state-cta empty-state-cta--ghost"
        onClick={props.onClearFilters}
      >
        Clear filters
      </button>
    </div>
  );
}
