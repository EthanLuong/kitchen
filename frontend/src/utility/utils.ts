import type {
  FoodItemRequest,
  FoodItemResponse,
  FoodLocation,
  FoodType,
  Unit,
} from "../types/types";

export function responseToFoodItemRequest(item: FoodItemResponse) {
  const requestItem: FoodItemRequest = {
    name: item.name,
    foodType: item.foodType,
    quantity: item.quantity,
    unit: item.unit,
    location: item.location,
    expirationDate: item.expirationDate,
    purchaseDate: item.purchaseDate ?? new Date().toISOString().split("T")[0],
    openedAt: item.openedAt ?? new Date().toISOString().split("T")[0],
    notes: item.notes ?? "",
  };

  return requestItem;
}

export function formDataToFoodItemRequest(data: FormData) {
  const requestItem: FoodItemRequest = {
    name: data.get("name") as string,
    foodType: data.get("foodType") as FoodType,
    quantity: Number(data.get("quantity") as string),
    unit: data.get("unit") as Unit,
    location: data.get("location") as FoodLocation,
    expirationDate: data.get("expirationDate") as string,
    purchaseDate: data.get("purchaseDate") as string,
    openedAt: data.get("openedAt") as string,
    notes: data.get("notes") as string,
  };
  return requestItem;
}
