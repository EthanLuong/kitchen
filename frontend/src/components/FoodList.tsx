import { type FoodItemResponse } from "../types/types";
import FoodCard from "./FoodCard";
type FoodListProps = {
  foodList: FoodItemResponse[];
  onDelete: (item: FoodItemResponse) => void;
  onEdit: (item: FoodItemResponse | "add") => void;
};

export default function FoodList({
  foodList,
  onDelete,
  onEdit,
}: FoodListProps) {
  return (
    <div className="foodlist">
      {foodList.map((item) => (
        <FoodCard
          item={item}
          key={item.id}
          onDelete={onDelete}
          onEdit={onEdit}
        />
      ))}
      <button className="buttoncard" onClick={() => onEdit("add")}>
        Add Item
      </button>
    </div>
  );
}
