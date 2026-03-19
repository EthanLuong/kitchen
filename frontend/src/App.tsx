import { useState, useEffect } from "react";
import {
  type FoodItemResponse,
  type FoodLocation,
  type FoodType,
  type FoodItemRequest,
  type SortOptions,
} from "./types/types";
import {
  getAllFoodItems,
  deleteFoodItem,
  createNewFoodItem,
  updateFoodItem,
} from "./api/fetchFood";
import ItemModal from "./components/ItemModal";
import AuthenticationModal from "./components/AuthCard";
import FilterBar from "./components/FilterBar";
import FoodList from "./components/FoodList";
import {
  responseToFoodItemRequest,
  formDataToFoodItemRequest,
} from "./utility/utils";

import "./App.css";

function App() {
  const [foodList, setFoodList] = useState<FoodItemResponse[]>([]);
  const [authToken, setToken] = useState<string | null>(() =>
    localStorage.getItem("token"),
  );
  const [locationFilter, setLocationFilter] = useState<Set<FoodLocation>>(
    new Set<FoodLocation>(),
  );
  const [typeFilter, setTypeFilter] = useState<Set<FoodType>>(
    new Set<FoodType>(),
  );
  const [foodLoading, setFoodLoading] = useState(false);
  const [foodError, setFoodError] = useState(false);
  const [modalState, setModalState] = useState<null | FoodItemResponse | "add">(
    null,
  );
  const [sortBy, setSortBy] = useState<SortOptions>("name");

  useEffect(() => {
    if (!authToken) return;
    async function fetchItems() {
      try {
        setFoodLoading(true);
        const response = await getAllFoodItems(authToken as string);
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

  const authIsOpen = authToken === null;

  async function handleDeleteFoodItem(item: FoodItemResponse) {
    try {
      await deleteFoodItem(item.id, authToken ? authToken : "");
      setFoodList((foodList) => foodList.filter((x) => x.id !== item.id));
    } catch (err) {
      console.log(err);
    }
  }

  async function handleAddedItem(data: FormData) {
    if (!authToken) {
      return;
    }
    const request: FoodItemRequest = formDataToFoodItemRequest(data);
    try {
      const response: FoodItemResponse = await createNewFoodItem(
        request,
        authToken,
      );
      setFoodList((prev) => [...prev, response]);
      setModalState(null);
    } catch (err) {
      console.log(err);
    }
  }

  async function handleEditItem(data: FormData, id: number) {
    const request: FoodItemRequest = formDataToFoodItemRequest(data);
    if (!authToken) return;

    try {
      const response: FoodItemResponse = await updateFoodItem(
        id,
        request,
        authToken,
      );
      setFoodList((prev) =>
        prev.map((item) => (item.id !== response.id ? item : response)),
      );
      setModalState(null);
    } catch (err) {
      console.log(err);
    }
  }

  function typeFilterHandler(type: FoodType | null) {
    if (type == null) {
      setTypeFilter(new Set());
      return;
    }

    const filterSet = typeFilter.has(type)
      ? new Set([...typeFilter].filter((filter) => filter != type))
      : new Set([...typeFilter, type]);
    setTypeFilter(filterSet);
  }

  function locationFilterHandler(location: FoodLocation | null) {
    if (location == null) {
      setLocationFilter(new Set());
      return;
    }

    const filterSet = locationFilter.has(location)
      ? new Set([...locationFilter].filter((filter) => filter != location))
      : new Set([...locationFilter, location]);
    setLocationFilter(filterSet);
  }

  function sortCards(a: FoodItemResponse, b: FoodItemResponse): number {
    switch (sortBy) {
      case "location":
        return a.location.localeCompare(b.location);
      case "name":
        return a.name.localeCompare(b.name);
      case "type":
        return a.foodType.localeCompare(b.foodType);
    }
  }

  const modalSubmitHandler =
    modalState === "add" || modalState === null
      ? handleAddedItem
      : (data: FormData) => handleEditItem(data, modalState.id);

  const visibleItems = foodList
    .filter(
      (item) => locationFilter.size === 0 || locationFilter.has(item.location),
    )
    .filter((item) => typeFilter.size === 0 || typeFilter.has(item.foodType))
    .sort(sortCards);

  const initialForm =
    modalState !== "add" && modalState != null
      ? responseToFoodItemRequest(modalState)
      : modalState;

  return (
    <>
      <ItemModal
        initialValue={initialForm}
        setIsOpen={setModalState}
        onSubmit={modalSubmitHandler}
      ></ItemModal>
      <AuthenticationModal
        isOpen={authIsOpen}
        setToken={setToken}
      ></AuthenticationModal>
      <FilterBar
        locationFilter={locationFilter}
        typeFilter={typeFilter}
        setTypeFilter={typeFilterHandler}
        setLocationFilter={locationFilterHandler}
        setSortType={setSortBy}
      ></FilterBar>
      <FoodList
        foodList={visibleItems}
        onDelete={handleDeleteFoodItem}
        onEdit={setModalState}
      />{" "}
    </>
  );
}

export default App;
