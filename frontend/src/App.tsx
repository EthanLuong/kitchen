import { useState } from "react";
import {
  sampleFoodItems,
  type FoodItem,
  type FoodLocation,
  type FoodType,
} from "./data/sampleFoodList";
import "./App.css";

type FoodListProps = {
  foodList: FoodItem[];
  onDelete: (item: FoodItem) => void;
};

type FoodCardProps = {
  item: FoodItem;
  onDelete: (item: FoodItem) => void;
};

type AuthenticationModalProps = {
  isOpen: boolean;
  setToken: (token: string) => void;
};

type FIlterBarProps = {
  setLocationFilter: (location: FoodLocation | null) => void;
  setTypeFilter: (type: FoodType | null) => void;
};

function AuthenticationModal({ isOpen, setToken }: AuthenticationModalProps) {
  async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
    e.preventDefault();
    console.log(e);
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
    }
    const result = await response.json();
    setToken(result.token);
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
          Password: <input type="text" name="password"></input>
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
  );
}

function FilterBar({ setLocationFilter, setTypeFilter }: FIlterBarProps) {
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

function FoodCard({ item, onDelete }: FoodCardProps) {
  return (
    <div className="foodcard">
      <div className="foodinfo">
        <h1>{item.name}</h1>
        <h2>
          {item.quantity + " "}
          {item.unit + " "}
          {item.type + " "}
          {item.location}
        </h2>
      </div>

      <h1 className="expdate">{item.expirationDate.toLocaleDateString()}</h1>
      <button className="deleteitem" onClick={() => onDelete(item)}>
        Delete
      </button>
    </div>
  );
}

function FoodList({ foodList, onDelete }: FoodListProps) {
  return foodList.map((item) => (
    <FoodCard item={item} key={item.id} onDelete={onDelete} />
  ));
}

function App() {
  const [foodList, setFoodList] = useState<FoodItem[]>(sampleFoodItems);
  const [authToken, setToken] = useState<string>("");
  const [locationFilter, setLocationFilter] = useState<FoodLocation | null>(
    null,
  );
  const [typeFilter, setTypeFilter] = useState<FoodType | null>(null);
  const isOpen = authToken == "" ? true : false;

  function handleDeleteFoodItem(item: FoodItem) {
    setFoodList((foodList) => foodList.filter((x) => x.id != item.id));
  }

  console.log(locationFilter, typeFilter);

  const visibleItems = foodList
    .filter((item) => (locationFilter ? item.location == locationFilter : true))
    .filter((item) => (typeFilter ? item.type == typeFilter : true));

  console.log(visibleItems);

  return (
    <>
      <AuthenticationModal
        isOpen={isOpen}
        setToken={setToken}
      ></AuthenticationModal>
      <FilterBar
        setTypeFilter={setTypeFilter}
        setLocationFilter={setLocationFilter}
      ></FilterBar>
      <FoodList foodList={visibleItems} onDelete={handleDeleteFoodItem} />{" "}
    </>
  );
}

export default App;
