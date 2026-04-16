import type { FoodItemRequest, FoodItemResponse, Unit } from "../types/types";

export function todayISO() {
  return new Date().toISOString().split("T")[0];
}

export function toggleInSet<T>(set: Set<T>, value: T): Set<T> {
  const next = new Set(set);
  if (next.has(value)) next.delete(value);
  else next.add(value);
  return next;
}

export function formatName(name: string) {
  return name
    .toLowerCase()
    .split(" ")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ")
    .trim();
}

export function responseToFoodItemRequest(item: FoodItemResponse) {
  const requestItem: FoodItemRequest = {
    name: item.name,
    foodType: item.foodType,
    quantity: item.quantity,
    unit: item.unit,
    location: item.location,
    expirationDate: item.expirationDate,
    purchaseDate: item.purchaseDate ?? todayISO(),
  };

  return requestItem;
}

export function formDataToFoodItemRequest(data: FormData) {
  const requestItem: FoodItemRequest = {
    name: data.get("name") as string,
    foodType: data.get("foodType") as string,
    quantity: Number(data.get("quantity") as string),
    unit: data.get("unit") as Unit,
    location: data.get("location") as string,
    expirationDate: data.get("expirationDate") as string,
    purchaseDate: data.get("purchaseDate") as string,
  };
  return requestItem;
}
