import { useState } from "react";
import { UNIT, TYPE, LOCATIONS, type FoodItemRequest } from "../types/types";
type ItemModalProps = {
  initialValue: FoodItemRequest | null | "add";
  onSubmit: (data: FormData) => Promise<void>;
  setIsOpen: (state: null) => void;
};

export default function ItemModal({
  initialValue,
  onSubmit,
  setIsOpen,
}: ItemModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [itemError, setError] = useState<null | string>(null);

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

  async function handleSubmit(event: React.SubmitEvent) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget as HTMLFormElement);
    try {
      setIsLoading(true);
      setError(null);
      await onSubmit(formData);
      setIsOpen(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Submit request failed");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="overlay">
      <div className="modalcard">
        <form className="modalform" onSubmit={handleSubmit}>
          <div className="formitem">
            <label>Name</label>
            <input type="text" defaultValue={defaultValues.name} name="name" />
          </div>
          <div className="formitem">
            <label>Quantity</label>
            <input
              type="number"
              defaultValue={defaultValues.quantity}
              name="quantity"
              min="0"
              step="1"
            ></input>
          </div>
          <div className="formitem">
            <label>Unit</label>
            <select name="unit" defaultValue={defaultValues.unit}>
              {UNIT.map((unit) => (
                <option key={unit} value={unit}>
                  {unit}
                </option>
              ))}
            </select>
          </div>
          <div className="formitem">
            <label>Type</label>
            <select name="foodType" defaultValue={defaultValues.foodType}>
              {TYPE.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>
          <div className="formitem">
            <label>Location</label>
            <select name="location" defaultValue={defaultValues.location}>
              {LOCATIONS.map((location) => (
                <option key={location} value={location}>
                  {location}
                </option>
              ))}
            </select>
          </div>
          <div className="formitem">
            <label>Expiration</label>
            <input
              type="date"
              name="expirationDate"
              defaultValue={defaultValues.expirationDate}
            ></input>
          </div>

          <button className="modal-submit" type="submit" disabled={isLoading}>
            {isLoading
              ? "Saving..."
              : initialValue != null && initialValue != "add"
                ? "Edit Item"
                : "Add Item"}
          </button>
        </form>
        {itemError && <p className="form-error">{itemError}</p>}

        <button
          className="modal-close"
          onClick={() => {
            setIsOpen(null);
            setError(null);
          }}
        >
          Exit
        </button>
      </div>
    </div>
  );
}
