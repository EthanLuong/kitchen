import { useState, useEffect } from "react";
import {
  type FoodItemResponse,
  type FoodItemRequest,
  type SortOptions,
  type UserLocationResponse,
  type UserTypeResponse,
} from "./types/types";
import {
  getAllFoodItems,
  deleteFoodItem,
  createNewFoodItem,
  updateFoodItem,
  logoutUser,
  getUserTypes,
  getUserLocations,
  addUserTypes,
  addUserLocations,
  deleteUserLocations,
  deleteUserTypes,
} from "./api/fetchFood";
import ItemModal from "./components/ItemModal";
import AuthenticationModal from "./components/AuthCard";
import FilterBar from "./components/FilterBar";
import FoodList from "./components/FoodList";
import NavBar from "./components/NavBar";
import {
  responseToFoodItemRequest,
  formDataToFoodItemRequest,
} from "./utility/utils";

import "./App.css";
import SettingsModal from "./components/SettingsModal";

function App() {
  const [foodList, setFoodList] = useState<FoodItemResponse[]>([]);
  const [authToken, setToken] = useState<string | null>(() =>
    localStorage.getItem("token"),
  );
  const [locationFilter, setLocationFilter] = useState<Set<string>>(new Set());
  const [typeFilter, setTypeFilter] = useState<Set<string>>(new Set());
  const [foodLoading, setFoodLoading] = useState(false);
  const [foodError, setFoodError] = useState<string | null>(null);
  const [modalState, setModalState] = useState<null | FoodItemResponse | "add">(
    null,
  );
  const [sortBy, setSortBy] = useState<SortOptions>("name");
  const [isLogin, setIsLogin] = useState(true);
  const [userTypes, setUserTypes] = useState<UserTypeResponse[]>([]);
  const [userLocations, setUserLocations] = useState<UserLocationResponse[]>(
    [],
  );
  const [settingModal, setSettingModal] = useState(false);

  useEffect(() => {
    if (!authToken) return;
    async function fetchItems() {
      try {
        setFoodLoading(true);
        const [items, types, locations] = await Promise.all([
          getAllFoodItems(authToken as string, setToken),
          getUserTypes(authToken as string, setToken),
          getUserLocations(authToken as string, setToken),
        ]);
        setFoodList(items.content);
        setUserTypes(types);
        setUserLocations(locations);
      } catch (err) {
        setFoodError("Failed to load items");
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
      await deleteFoodItem(item.id, authToken ? authToken : "", setToken);
      setFoodList((foodList) => foodList.filter((x) => x.id !== item.id));
    } catch (err) {
      setFoodError(
        err instanceof Error ? err.message : "Failed to delete item",
      );
    }
  }

  async function handleAddedItem(data: FormData) {
    if (!authToken) throw new Error("Not authenticated");

    const request: FoodItemRequest = formDataToFoodItemRequest(data);
    const response: FoodItemResponse = await createNewFoodItem(
      request,
      authToken,
      setToken,
    );
    setFoodList((prev) => [...prev, response]);
    setModalState(null);
  }

  async function handleLogout() {
    if (!authToken) return;
    try {
      logoutUser(authToken, setToken);
      setToken(null);
      localStorage.clear();
    } catch (err) {
      console.log(err);
    }
  }

  async function handleEditItem(data: FormData, id: number) {
    const request: FoodItemRequest = formDataToFoodItemRequest(data);
    if (!authToken) throw new Error("Not authenticated");

    const response: FoodItemResponse = await updateFoodItem(
      id,
      request,
      authToken,
      setToken,
    );
    setFoodList((prev) =>
      prev.map((item) => (item.id !== response.id ? item : response)),
    );
    setModalState(null);
  }

  function typeFilterHandler(type: string | null) {
    if (type == null) {
      setTypeFilter(new Set());
      return;
    }

    const filterSet = typeFilter.has(type)
      ? new Set([...typeFilter].filter((filter) => filter != type))
      : new Set([...typeFilter, type]);
    setTypeFilter(filterSet);
  }
  async function handleAddType(name: string) {
    if (!authToken) return;
    const newType = await addUserTypes(name.toUpperCase(), authToken, setToken);
    setUserTypes((prev) => [...prev, newType]);
  }

  async function handleDeleteType(id: number, name: string) {
    if (!authToken) return;

    const usedTypes = foodList.map((item) => item.foodType);
    if (usedTypes.includes(name.toUpperCase())) {
      throw new Error(
        `Food items with location ${name} exist. Delete items before deleting location.`,
      );
    }
    await deleteUserTypes(id, authToken, setToken);
    setUserTypes((prev) => prev.filter((t) => t.id !== id));
  }

  async function handleAddLocation(name: string) {
    if (!authToken) return;
    const newLocation = await addUserLocations(
      name.toUpperCase(),
      authToken,
      setToken,
    );
    setUserLocations((prev) => [...prev, newLocation]);
  }

  async function handleDeleteLocation(id: number, name: string) {
    if (!authToken) return;
    const usedLocations = foodList.map((item) => item.location);
    if (usedLocations.includes(name.toUpperCase())) {
      throw new Error(
        `Food items with location ${name} exist. Delete items before deleting location.`,
      );
    }
    await deleteUserLocations(id, authToken, setToken);
    setUserLocations((prev) => prev.filter((l) => l.id !== id));
  }

  function locationFilterHandler(location: string | null) {
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

  function closeModals() {
    setSettingModal(false);
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
      {settingModal && (
        <SettingsModal
          types={userTypes}
          locations={userLocations}
          onAddLocation={handleAddLocation}
          onDeleteLocation={handleDeleteLocation}
          onAddType={handleAddType}
          onDeleteType={handleDeleteType}
          onClose={closeModals}
        ></SettingsModal>
      )}
      <ItemModal
        userLocations={userLocations.map((location) => location.name)}
        userTypes={userTypes.map((type) => type.name)}
        initialValue={initialForm}
        setIsOpen={setModalState}
        onSubmit={modalSubmitHandler}
      ></ItemModal>
      <AuthenticationModal
        isOpen={authIsOpen}
        mode={isLogin}
        setMode={setIsLogin}
        setToken={setToken}
      ></AuthenticationModal>
      <NavBar
        setModal={setModalState}
        handleLogout={handleLogout}
        setSettingModal={setSettingModal}
      ></NavBar>
      {foodError && (
        <div className="error-banner">
          {foodError}
          <button onClick={() => setFoodError(null)}>✕</button>
        </div>
      )}
      <FilterBar
        userLocations={userLocations.map((location) => location.name)}
        userTypes={userTypes.map((type) => type.name)}
        locationFilter={locationFilter}
        typeFilter={typeFilter}
        setTypeFilter={typeFilterHandler}
        setLocationFilter={locationFilterHandler}
        setSortType={setSortBy}
      ></FilterBar>
      {foodLoading ? (
        <div>Loading...</div>
      ) : (
        <FoodList
          foodList={visibleItems}
          onDelete={handleDeleteFoodItem}
          onEdit={setModalState}
        />
      )}
    </>
  );
}

export default App;
