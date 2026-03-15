import { useState, useEffect } from "react";
import { sampleFoodItems } from "./data/sampleFoodList";
import {
  UNIT,
  TYPE,
  LOCATIONS,
  type FoodItemRequest,
  type FoodItemResponse,
  type FoodLocation,
  type FoodType,
  type Token,
  type Unit,
} from "./types/types";
import {
  getAllFoodItems,
  createNewFoodItem,
  updateFoodItem,
  deleteFoodItem,
} from "./api/fetchFood";
import "./App.css";

type FoodListProps = {
  foodList: FoodItemResponse[];
  onDelete: (item: FoodItemResponse) => void;
  setEdit: (item: FoodItemResponse) => void;
};

type FoodCardProps = {
  item: FoodItemResponse;
  onDelete: (item: FoodItemResponse) => void;
  setEdit: (item: FoodItemResponse) => void;
};

type AuthenticationModalProps = {
  isOpen: boolean;
  setToken: (token: string) => void;
};

type FilterBarProps = {
  setLocationFilter: (location: FoodLocation | null) => void;
  setTypeFilter: (type: FoodType | null) => void;
};

type AddFoodModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onItemAdded: (item: FoodItemResponse) => void;
  token: Token;
};

type EditModalProps = {
  item: FoodItemResponse;
  token: Token;
  onEdit: (item: FoodItemResponse) => void;
  setEdit: (item: FoodItemResponse | null) => void;
};

// function Modal() {} TODO: create common modal function?

function AuthenticationModal({ isOpen, setToken }: AuthenticationModalProps) {
  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = new FormData(e.currentTarget);
    const username = data.get("username") as string;
    const password = data.get("password") as string;

    const response = await fetch("http://localhost:8080/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      console.error("Login Fail");
    } else {
      const result = await response.json();
      localStorage.setItem("token", result.accessToken);
      setToken(result.accessToken);
    }
  }

  if (!isOpen) {
    return null;
  }

  return (
    <div className="overlay">
      <form onSubmit={handleSubmit} className="login">
        <div>
          Username: <input type="text" name="username"></input>
        </div>
        <div>
          Password: <input type="password" name="password"></input>
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
  );
}

function FilterBar({ setLocationFilter, setTypeFilter }: FilterBarProps) {
  return (
    <div className="filterbar">
      <div>
        <button onClick={() => setLocationFilter("FRIDGE")}>Fridge</button>
        <button onClick={() => setLocationFilter("FREEZER")}>Freezer</button>
        <button onClick={() => setLocationFilter("PANTRY")}>Pantry</button>
        <button onClick={() => setLocationFilter(null)}>Clear</button>
      </div>
      <div>
        <button onClick={() => setTypeFilter("PRODUCE")}>Produce</button>
        <button onClick={() => setTypeFilter("DAIRY")}>Dairy</button>
        <button onClick={() => setTypeFilter("MEAT")}>Meat</button>
        <button onClick={() => setTypeFilter("DRY_GOODS")}>Dry Goods</button>
        <button onClick={() => setTypeFilter("FROZEN")}>Frozen</button>
        <button onClick={() => setTypeFilter("BEVERAGE")}>Beverage</button>
        <button onClick={() => setTypeFilter(null)}>Clear</button>
      </div>
    </div>
  );
}

function FoodCard({ item, onDelete, setEdit }: FoodCardProps) {
  return (
    <div className="foodcard">
      <div className="foodinfo">
        <h1>{item.name}</h1>
        <h2>
          {item.quantity + " "}
          {item.unit + " "}
          {item.foodType + " "}
          {item.location}
        </h2>
      </div>

      <h1 className="expdate">
        {new Date(item.expirationDate).toLocaleDateString()}
      </h1>
      <button className="deleteitem" onClick={() => onDelete(item)}>
        Delete
      </button>
      <button onClick={() => setEdit(item)}>Edit</button>
    </div>
  );
}

function FoodList({ foodList, onDelete, setEdit }: FoodListProps) {
  return foodList.map((item) => (
    <FoodCard item={item} key={item.id} onDelete={onDelete} setEdit={setEdit} />
  ));
}

function AddFoodModal({
  isOpen,
  onClose,
  onItemAdded,
  token,
}: AddFoodModalProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  async function handleSubmit(data: FormData) {
    const newItem: FoodItemRequest = {
      name: data.get("name") as string,
      foodType: data.get("type") as FoodType,
      quantity: Number(data.get("quantity")),
      unit: data.get("unit") as Unit,
      location: data.get("location") as FoodLocation,
      expirationDate: data.get("expirationDate") as string,
      purchaseDate: new Date().toISOString().split("T")[0],
      openedAt: new Date().toISOString().split("T")[0],
      notes: "",
    };

    try {
      setLoading(true);
      setError(null);
      const responseItem: FoodItemResponse = await createNewFoodItem(
        newItem,
        token,
      );
      onItemAdded(responseItem);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add item");
    } finally {
      setLoading(false);
    }
  }

  if (!isOpen) return null;

  return (
    <div className="overlay">
      <form action={handleSubmit}>
        <input type="text" name="name" />
        <input type="number" name="quantity" min="0" step="1"></input>

        <select name="unit">
          {UNIT.map((unit) => (
            <option key={unit} value={unit}>
              {unit}
            </option>
          ))}
        </select>

        <select name="type">
          {TYPE.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
        <select name="location">
          {LOCATIONS.map((location) => (
            <option key={location} value={location}>
              {location}
            </option>
          ))}
        </select>
        <input type="date" name="expirationDate"></input>
        <button type="submit" disabled={loading}>
          Add Item
        </button>
      </form>
      <button onClick={onClose}>Exit</button>
    </div>
  );
}

function EditFoodModal({ item, token, onEdit, setEdit }: EditModalProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  async function handleFormSubmission(data: FormData) {
    const newItem: FoodItemRequest = {
      name: data.get("name") as string,
      foodType: data.get("type") as FoodType,
      quantity: Number(data.get("quantity")),
      unit: data.get("unit") as Unit,
      location: data.get("location") as FoodLocation,
      expirationDate: data.get("expirationDate") as string,
      purchaseDate: new Date().toISOString().split("T")[0],
      openedAt: new Date().toISOString().split("T")[0],
      notes: "",
    };

    try {
      setLoading(true);
      setError(null);
      const responseItem: FoodItemResponse = await updateFoodItem(
        item.id,
        newItem,
        token,
      );
      onEdit(responseItem);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add item");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="overlay">
      <form action={handleFormSubmission}>
        <input type="text" defaultValue={item.name} name="name" />
        <input
          type="number"
          defaultValue={item.quantity}
          name="quantity"
          min="0"
          step="1"
        ></input>

        <select name="unit" defaultValue={item.unit}>
          {UNIT.map((unit) => (
            <option key={unit} value={unit}>
              {unit}
            </option>
          ))}
        </select>

        <select name="type" defaultValue={item.foodType}>
          {TYPE.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
        <select name="location" defaultValue={item.location}>
          {LOCATIONS.map((location) => (
            <option key={location} value={location}>
              {location}
            </option>
          ))}
        </select>
        <input
          type="date"
          name="expirationDate"
          defaultValue={item.expirationDate}
        ></input>
        <button type="submit">Edit Item</button>
      </form>
      <button onClick={() => setEdit(null)}>Exit</button>
    </div>
  );
}

function App() {
  const [foodList, setFoodList] = useState<FoodItemResponse[]>([]);
  const [authToken, setToken] = useState<string | null>(() =>
    localStorage.getItem("token"),
  );
  const [locationFilter, setLocationFilter] = useState<FoodLocation | null>(
    null,
  );
  const [typeFilter, setTypeFilter] = useState<FoodType | null>(null);
  const [addIsOpen, setAddIsOpen] = useState(false);
  const [foodLoading, setFoodLoading] = useState(false);
  const [foodError, setFoodError] = useState(false);
  const [editItem, setEditItem] = useState<FoodItemResponse | null>(null);

  useEffect(() => {
    if (!authToken) return;
    async function fetchItems() {
      try {
        setFoodLoading(true);
        const response = await getAllFoodItems(authToken ? authToken : "");
        setFoodList(response);
      } catch (err) {
        setFoodError(true);
        setToken(null);
        console.log(err);
      } finally {
        setFoodLoading(false);
      }
    }
    fetchItems();
  }, [authToken]);

  const authIsOpen = authToken == null ? true : false;

  function handleDeleteFoodItem(item: FoodItemResponse) {
    try {
      deleteFoodItem(item.id, authToken ? authToken : "");
      setFoodList((foodList) => foodList.filter((x) => x.id != item.id));
    } catch (err) {
      console.log(err);
    }
  }

  function handleAddedItem(item: FoodItemResponse) {
    setFoodList((prev) => [...prev, item]);
  }
  function handleEditItem(item: FoodItemResponse) {
    setFoodList((foodList) =>
      foodList.map((original) => (original.id != item.id ? original : item)),
    );
    setEditItem(null);
  }

  function closeAddModal() {
    setAddIsOpen(false);
  }

  const visibleItems = foodList
    .filter((item) => (locationFilter ? item.location == locationFilter : true))
    .filter((item) => (typeFilter ? item.foodType == typeFilter : true));

  return (
    <>
      {editItem && (
        <EditFoodModal
          item={editItem}
          token={authToken ? authToken : ""}
          onEdit={handleEditItem}
          setEdit={setEditItem}
        ></EditFoodModal>
      )}
      <AddFoodModal
        isOpen={addIsOpen}
        onItemAdded={handleAddedItem}
        onClose={closeAddModal}
        token={authToken ? authToken : ""}
      ></AddFoodModal>
      <AuthenticationModal
        isOpen={authIsOpen}
        setToken={setToken}
      ></AuthenticationModal>
      <button onClick={() => setAddIsOpen(true)}>Add Item</button>
      <FilterBar
        setTypeFilter={setTypeFilter}
        setLocationFilter={setLocationFilter}
      ></FilterBar>
      <FoodList
        foodList={visibleItems}
        onDelete={handleDeleteFoodItem}
        setEdit={setEditItem}
      />{" "}
    </>
  );
}

export default App;
