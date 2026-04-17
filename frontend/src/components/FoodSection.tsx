import type { FoodItemResponse } from "../types/types";
import { formatName } from "../utility/utils";
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
    <section className="food-section">
      {showTitle && (
        <header className="food-section-header">
          <h2 className="group-header">{formatName(title)}</h2>
          <span className="group-count" aria-label={`${foodList.length} items`}>
            {foodList.length}
          </span>
        </header>
      )}
      <FoodList foodList={foodList} onDelete={onDelete} onEdit={onEdit} />
    </section>
  );
}
