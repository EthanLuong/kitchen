const now = new Date();
const daysFromNow = (d: number) => new Date(now.getTime() + d * 86400000);

export const sampleFoodItems: FoodItem[] = [
  {
    id: 1,
    name: "Whole Milk",
    type: "DAIRY",
    quantity: 1,
    unit: "LITER",
    location: "FRIDGE",
    expirationDate: daysFromNow(5),
    purchaseDate: daysFromNow(-2),
    openedAt: daysFromNow(-1),
    notes: "Organic, 2%",
    consumed: false,
    createdAt: daysFromNow(-2),
    updatedAt: daysFromNow(-1),
  },
  {
    id: 2,
    name: "Chicken Breast",
    type: "MEAT",
    quantity: 500,
    unit: "GRAM",
    location: "FREEZER",
    expirationDate: daysFromNow(60),
    purchaseDate: daysFromNow(-3),
    openedAt: daysFromNow(-3),
    notes: "Frozen raw, vacuum sealed",
    consumed: false,
    createdAt: daysFromNow(-3),
    updatedAt: daysFromNow(-3),
  },
  {
    id: 3,
    name: "All-Purpose Flour",
    type: "DRY_GOODS",
    quantity: 2,
    unit: "KILOGRAM",
    location: "PANTRY",
    expirationDate: daysFromNow(180),
    purchaseDate: daysFromNow(-10),
    openedAt: daysFromNow(-10),
    notes: "",
    consumed: false,
    createdAt: daysFromNow(-10),
    updatedAt: daysFromNow(-10),
  },
  {
    id: 4,
    name: "Orange Juice",
    type: "BEVERAGE",
    quantity: 500,
    unit: "MILLILITER",
    location: "FRIDGE",
    expirationDate: daysFromNow(3),
    purchaseDate: daysFromNow(-4),
    openedAt: daysFromNow(-1),
    notes: "Freshly squeezed",
    consumed: false,
    createdAt: daysFromNow(-4),
    updatedAt: daysFromNow(-1),
  },
  {
    id: 5,
    name: "Spinach",
    type: "PRODUCE",
    quantity: 200,
    unit: "GRAM",
    location: "FRIDGE",
    expirationDate: daysFromNow(-1), // already expired
    purchaseDate: daysFromNow(-6),
    openedAt: daysFromNow(-6),
    notes: "Baby spinach",
    consumed: false,
    createdAt: daysFromNow(-6),
    updatedAt: daysFromNow(-6),
  },
  {
    id: 6,
    name: "Olive Oil",
    type: "DRY_GOODS",
    quantity: 750,
    unit: "MILLILITER",
    location: "PANTRY",
    expirationDate: daysFromNow(365),
    purchaseDate: daysFromNow(-20),
    openedAt: daysFromNow(-20),
    notes: "Extra virgin",
    consumed: false,
    createdAt: daysFromNow(-20),
    updatedAt: daysFromNow(-20),
  },
  {
    id: 7,
    name: "Greek Yogurt",
    type: "DAIRY",
    quantity: 500,
    unit: "GRAM",
    location: "FRIDGE",
    expirationDate: daysFromNow(7),
    purchaseDate: daysFromNow(-1),
    openedAt: daysFromNow(0),
    notes: "Plain, full fat",
    consumed: false,
    createdAt: daysFromNow(-1),
    updatedAt: daysFromNow(0),
  },
  {
    id: 8,
    name: "Frozen Peas",
    type: "FROZEN",
    quantity: 1,
    unit: "KILOGRAM",
    location: "FREEZER",
    expirationDate: daysFromNow(90),
    purchaseDate: daysFromNow(-15),
    openedAt: daysFromNow(-15),
    notes: "",
    consumed: false,
    createdAt: daysFromNow(-15),
    updatedAt: daysFromNow(-15),
  },
  {
    id: 9,
    name: "Eggs",
    type: "DAIRY",
    quantity: 6,
    unit: "PIECE",
    location: "FRIDGE",
    expirationDate: daysFromNow(14),
    purchaseDate: daysFromNow(-7),
    openedAt: daysFromNow(-7),
    notes: "Free range",
    consumed: false,
    createdAt: daysFromNow(-7),
    updatedAt: daysFromNow(-7),
  },
  {
    id: 10,
    name: "Cheddar Cheese",
    type: "DAIRY",
    quantity: 250,
    unit: "GRAM",
    location: "FRIDGE",
    expirationDate: daysFromNow(21),
    purchaseDate: daysFromNow(-5),
    openedAt: daysFromNow(-2),
    notes: "Sharp cheddar, sliced",
    consumed: true,
    createdAt: daysFromNow(-5),
    updatedAt: daysFromNow(-2),
  },
];

export type Unit =
  | "GRAM"
  | "KILOGRAM"
  | "MILLILITER"
  | "LITER"
  | "PIECE"
  | "TABLESPOON"
  | "TEASPOON";
export type FoodType =
  | "PRODUCE"
  | "DAIRY"
  | "MEAT"
  | "DRY_GOODS"
  | "FROZEN"
  | "BEVERAGE";
export type FoodLocation = "FRIDGE" | "FREEZER" | "PANTRY";

export type FoodItem = {
  id: number;
  name: string;
  type: FoodType;
  quantity: number;
  unit: Unit;
  location: FoodLocation;
  expirationDate: Date;
  purchaseDate?: Date;
  openedAt?: Date;
  notes?: string;
  consumed?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
};
