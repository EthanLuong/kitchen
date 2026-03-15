export const UNIT = ["OZ", "LBS", "ML", "L", "G", "KG", "COUNT"] as const;

export const TYPE = [
  "DAIRY",
  "MEAT",
  "PRODUCE",
  "GRAIN",
  "BEVERAGE",
  "FROZEN",
  "CONDIMENT",
  "SNACK",
  "OTHER",
] as const;
export const LOCATIONS = [
  "FRIDGE",
  "FREEZER",
  "PANTRY",
  "CABINET",
  "COUNTER",
  "OTHER",
] as const;

export type Token = string;
export type Unit = (typeof UNIT)[number];
export type FoodType = (typeof TYPE)[number];
export type FoodLocation = (typeof LOCATIONS)[number];

export type FoodItemRequest = {
  name: string;
  foodType: FoodType;
  quantity: number;
  unit: Unit;
  location: FoodLocation;
  expirationDate: string;
  purchaseDate: string;
  openedAt: string;
  notes: string;
};
export type FoodItemResponse = {
  id: number;
  name: string;
  foodType: FoodType;
  quantity: number;
  unit: Unit;
  location: FoodLocation;
  expirationDate: string;
  purchaseDate?: string;
  openedAt?: string;
  notes?: string;
  consumed?: boolean;
  createdAt?: string;
  updatedAt?: string;
};
