import { useState } from "react";
import {
  UNIT,
  type FoodItemRequest,
  type ItemDefaultsResponse,
  type Unit,
} from "../types/types";
import { todayISO } from "../utility/utils";
type ItemModalProps = {
  initialValue: FoodItemRequest | null | "add";
  userLocations: string[];
  userTypes: string[];
  itemDefaults: ItemDefaultsResponse[];
  onSubmit: (data: FormData) => Promise<void>;
  setIsOpen: (state: null) => void;
};

export default function ItemModal({
  initialValue,
  userLocations,
  userTypes,
  itemDefaults,
  onSubmit,
  setIsOpen,
}: ItemModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [itemError, setError] = useState<null | string>(null);
  const [suggestions, setSuggestions] = useState<ItemDefaultsResponse[]>([]);

  const defaultValues: FoodItemRequest =
    initialValue == "add" || initialValue == null
      ? {
          name: "",
          foodType: "OTHER",
          quantity: 0,
          unit: "COUNT",
          location: "FRIDGE",
          expirationDate: "",
          purchaseDate: todayISO(),
        }
      : initialValue;
  const [itemName, setItemName] = useState(defaultValues.name);
  const [itemType, setItemType] = useState(defaultValues.foodType);
  const [itemLocation, setItemLocation] = useState(defaultValues.location);
  const [itemUnit, setItemUnit] = useState(defaultValues.unit);
  const [itemExpDate, setItemExpDate] = useState(defaultValues.expirationDate);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
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

  function handleNameChange(name: string) {
    setItemName(name);
    if (name.trim() === "") {
      setSuggestions([]);
      return;
    }

    const suggestions = itemDefaults.filter((defaultValues) =>
      defaultValues.name.toUpperCase().includes(name.trim().toUpperCase()),
    );
    setSuggestions(suggestions);
  }

  function handleSuggestionSelect(defaults: ItemDefaultsResponse) {
    setItemName(defaults.name);
    setItemUnit(defaults.unit as Unit);
    setItemLocation(defaults.location);
    setItemType(defaults.foodType);
    if (defaults.expirationDays) {
      const date = new Date();
      date.setDate(date.getDate() + defaults.expirationDays);
      setItemExpDate(date.toISOString().split("T")[0]);
    }

    setSuggestions([]);
  }

  const isEdit = initialValue != null && initialValue != "add";

  if (!initialValue) return null;
  return (
    <div className="overlay">
      <div className="modalcard">
        <button
          className="modal-close"
          onClick={() => {
            setIsOpen(null);
            setError(null);
          }}
        >
          ✕
        </button>
        <h2 className="modal-title">{isEdit ? "Edit item" : "Add item"}</h2>
        <form className="modalform" onSubmit={handleSubmit}>
          <div className="formitem autocomplete-wrapper">
            <label>Name</label>
            <input
              type="text"
              name="name"
              value={itemName}
              onChange={(e) => handleNameChange(e.target.value)}
              autoComplete="off"
              placeholder="e.g. Milk"
            />
            {suggestions.length > 0 && (
              <ul className="autocomplete-dropdown">
                {suggestions.map((s) => (
                  <li key={s.name} onClick={() => handleSuggestionSelect(s)}>
                    {s.name}
                  </li>
                ))}
              </ul>
            )}
          </div>
          <div className="formrow">
            <div className="formitem">
              <label>Quantity</label>
              <input
                type="number"
                defaultValue={defaultValues.quantity}
                name="quantity"
                min="0"
                step="1"
              />
            </div>
            <div className="formitem">
              <label>Unit</label>
              <select
                name="unit"
                value={itemUnit}
                onChange={(e) => setItemUnit(e.target.value as Unit)}
              >
                {UNIT.map((unit) => (
                  <option key={unit} value={unit}>
                    {unit}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="formrow">
            <div className="formitem">
              <label>Type</label>
              <select
                name="foodType"
                value={itemType}
                onChange={(e) => setItemType(e.target.value)}
              >
                {userTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </div>
            <div className="formitem">
              <label>Location</label>
              <select
                name="location"
                value={itemLocation}
                onChange={(e) => setItemLocation(e.target.value)}
              >
                {userLocations.map((location) => (
                  <option key={location} value={location}>
                    {location}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <hr className="form-divider" />
          <div className="formrow">
            <div className="formitem">
              <label>Purchased</label>
              <input
                type="date"
                name="purchaseDate"
                defaultValue={defaultValues.purchaseDate}
              />
            </div>
            <div className="formitem">
              <label>Expires</label>
              <input
                type="date"
                name="expirationDate"
                value={itemExpDate}
                onChange={(e) => setItemExpDate(e.target.value)}
              />
            </div>
          </div>

          <button className="modal-submit" type="submit" disabled={isLoading}>
            {isLoading ? "Saving..." : isEdit ? "Save changes" : "Add item"}
          </button>
        </form>
        {itemError && <p className="form-error">{itemError}</p>}
      </div>
    </div>
  );
}
