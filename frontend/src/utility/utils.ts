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

export type ExpirationStatus = "none" | "expired" | "urgent" | "soon" | "good";

export function getExpirationStatus(expirationDate?: string): {
  status: ExpirationStatus;
  days: number | null;
  label: string;
} {
  if (!expirationDate) return { status: "none", days: null, label: "No expiry" };

  const exp = new Date(expirationDate);
  if (Number.isNaN(exp.getTime()))
    return { status: "none", days: null, label: "No expiry" };

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  exp.setHours(0, 0, 0, 0);

  const days = Math.round(
    (exp.getTime() - today.getTime()) / (1000 * 60 * 60 * 24),
  );

  if (days < 0)
    return {
      status: "expired",
      days,
      label: days === -1 ? "Expired yesterday" : `Expired ${-days}d ago`,
    };
  if (days === 0) return { status: "urgent", days, label: "Expires today" };
  if (days === 1) return { status: "urgent", days, label: "Tomorrow" };
  if (days <= 2) return { status: "urgent", days, label: `${days}d left` };
  if (days <= 7) return { status: "soon", days, label: `${days}d left` };
  return { status: "good", days, label: exp.toLocaleDateString(undefined, { month: "short", day: "numeric" }) };
}

export function formDataToFoodItemRequest(data: FormData) {
  const expirationDate = data.get("expirationDate") as string;
  const requestItem: FoodItemRequest = {
    name: data.get("name") as string,
    foodType: data.get("foodType") as string,
    quantity: Number(data.get("quantity") as string),
    unit: data.get("unit") as Unit,
    location: data.get("location") as string,
    purchaseDate: data.get("purchaseDate") as string,
    ...(expirationDate ? { expirationDate } : {}),
  };
  return requestItem;
}
