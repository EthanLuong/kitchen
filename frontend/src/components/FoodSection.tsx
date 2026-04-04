import type { FoodItemResponse } from "../types/types";
import FoodList from "./FoodList";

type FoodSectionProps = {
  title: string;
  showTitle: boolean;
  foodList: FoodItemResponse[];
  onDelete: (item: FoodItemResponse) => void;
  onEdit: (item: FoodItemResponse) => void;
};
export default function FoodSection({
  title,
  showTitle,
  foodList,
  onDelete,
  onEdit,
}: FoodSectionProps) {
  return (
    <div className="food-section">
      {showTitle && <h2 className="group-header">{title}</h2>}
      <FoodList foodList={foodList} onDelete={onDelete} onEdit={onEdit} />
    </div>
  );
}
