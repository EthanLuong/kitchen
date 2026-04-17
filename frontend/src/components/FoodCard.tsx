import { type FoodItemResponse } from "../types/types";
import { formatName, getExpirationStatus } from "../utility/utils";
type FoodCardProps = {
  item: FoodItemResponse;
  onDelete: (item: FoodItemResponse) => void;
  onEdit: (item: FoodItemResponse) => void;
};

export default function FoodCard({ item, onDelete, onEdit }: FoodCardProps) {
  const expiration = getExpirationStatus(item.expirationDate);
  const isConsumed = item.consumed === true;

  return (
    <article
      className={`foodcard foodcard--${expiration.status}${
        isConsumed ? " foodcard--consumed" : ""
      }`}
      onClick={() => onEdit(item)}
    >
      <span className="foodcard-accent" aria-hidden="true" />
      <button
        className="deleteitem"
        aria-label={`Delete ${item.name}`}
        onClick={(e) => {
          e.stopPropagation();
          onDelete(item);
        }}
      >
        <svg
          width="14"
          height="14"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M18 6 6 18" />
          <path d="m6 6 12 12" />
        </svg>
      </button>
      <div className="badges">
        <p>{formatName(item.location)}</p>
        <p>{formatName(item.foodType)}</p>
      </div>
      <div className="iteminfo">
        <h3 className="itemName">{formatName(item.name)}</h3>
        <p className="quantity">
          <span className="number">{item.quantity}</span>
          <span className="unit">{" " + item.unit.toLowerCase()}</span>
        </p>
      </div>
      <footer className="foodcard-meta">
        <span
          className={`expiry-tag expiry-tag--${expiration.status}`}
          aria-label={`Expiration: ${expiration.label}`}
        >
          <svg
            className="expiry-icon"
            width="12"
            height="12"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            {expiration.status === "none" ? (
              <>
                <circle cx="12" cy="12" r="9" />
                <path d="M8 12h8" />
              </>
            ) : expiration.status === "expired" ? (
              <>
                <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <path d="M12 9v4" />
                <path d="M12 17h.01" />
              </>
            ) : (
              <>
                <circle cx="12" cy="12" r="9" />
                <path d="M12 7v5l3 2" />
              </>
            )}
          </svg>
          {expiration.label}
        </span>
        {isConsumed && <span className="consumed-tag">Consumed</span>}
      </footer>
    </article>
  );
}
