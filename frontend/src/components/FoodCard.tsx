import { type FoodItemResponse } from "../types/types";
import { formatName } from "../utility/utils";
type FoodCardProps = {
  item: FoodItemResponse;
  onDelete: (item: FoodItemResponse) => void;
  onEdit: (item: FoodItemResponse) => void;
};

export default function FoodCard({ item, onDelete, onEdit }: FoodCardProps) {
  return (
    <div className="foodcard" onClick={() => onEdit(item)}>
      <button
        className="deleteitem"
        onClick={(e) => {
          e.stopPropagation();
          onDelete(item);
        }}
      >
        ✕
      </button>
      <div className="badges">
        <p>{formatName(item.location)}</p>
        <p>{formatName(item.foodType)}</p>
        <h1 className="expdate">
          {new Date(item.expirationDate).toLocaleDateString().slice(0, -5)}
        </h1>
      </div>
      <div className="iteminfo">
        <h1 className="itemName">{formatName(item.name)}</h1>
        <h2 className="quantity">
          <span className="number">{item.quantity + " "}</span>
          {item.unit + " "}
        </h2>
      </div>
    </div>
  );
}
