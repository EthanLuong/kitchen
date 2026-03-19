import { UNIT, TYPE, LOCATIONS, type FoodItemRequest } from "../types/types";
type ItemModalProps = {
  initialValue: FoodItemRequest | null | "add";
  onSubmit: (data: FormData) => void;
  setIsOpen: (state: null) => void;
};

export default function ItemModal({
  initialValue,
  onSubmit,
  setIsOpen,
}: ItemModalProps) {
  if (!initialValue) return null;

  const defaultValues: FoodItemRequest =
    initialValue == "add"
      ? {
          name: "",
          foodType: "OTHER",
          quantity: 0,
          unit: "COUNT",
          location: "FRIDGE",
          expirationDate: new Date().toISOString().split("T")[0],
          purchaseDate: new Date().toISOString().split("T")[0],
          openedAt: new Date().toISOString().split("T")[0],
          notes: "",
        }
      : initialValue;

  function handleSubmit(event: React.SubmitEvent) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget as HTMLFormElement);
    onSubmit(formData);
  }

  return (
    <div className="overlay">
      <form onSubmit={handleSubmit}>
        <input type="text" defaultValue={defaultValues.name} name="name" />
        <input
          type="number"
          defaultValue={defaultValues.quantity}
          name="quantity"
          min="0"
          step="1"
        ></input>

        <select name="unit" defaultValue={defaultValues.unit}>
          {UNIT.map((unit) => (
            <option key={unit} value={unit}>
              {unit}
            </option>
          ))}
        </select>

        <select name="foodType" defaultValue={defaultValues.foodType}>
          {TYPE.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
        <select name="location" defaultValue={defaultValues.location}>
          {LOCATIONS.map((location) => (
            <option key={location} value={location}>
              {location}
            </option>
          ))}
        </select>
        <input
          type="date"
          name="expirationDate"
          defaultValue={defaultValues.expirationDate}
        ></input>
        <button type="submit">
          {initialValue != null && initialValue != "add"
            ? "Edit Item"
            : "Add Item"}
        </button>
      </form>
      <button onClick={() => setIsOpen(null)}>Exit</button>
    </div>
  );
}
