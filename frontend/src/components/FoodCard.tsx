import { type FoodItemResponse } from "../types/types";
type FoodCardProps = {
  item: FoodItemResponse;
  onDelete: (item: FoodItemResponse) => void;
  onEdit: (item: FoodItemResponse) => void;
};

export default function FoodCard({ item, onDelete, onEdit }: FoodCardProps) {
  return (
    <div className="foodcard">
      <div className="badges">
        <p>{item.location}</p>
        <p>{item.foodType}</p>
      </div>
      <div className="iteminfo">
        <h1 className="itemName">{item.name}</h1>
        <h2 className="quantity">
          <span className="number">{item.quantity + " "}</span>
          {item.unit + " "}
        </h2>
      </div>

      <div className="cardfooter">
        <h1 className="expdate">
          {new Date(item.expirationDate).toLocaleDateString().slice(0, -5)}
        </h1>
        <button className="deleteitem" onClick={() => onDelete(item)}>
          Delete
        </button>
        <button onClick={() => onEdit(item)}>Edit</button>
      </div>
    </div>
  );
}
