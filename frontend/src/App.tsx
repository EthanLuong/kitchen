import { useEffect, useMemo, useState } from "react";
import {
  type FoodItemResponse,
  type FoodItemRequest,
  type UserLocationResponse,
  type UserTypeResponse,
  type ItemDefaultsResponse,
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
  getItemDefaults,
} from "./api/fetchFood";
import ItemModal from "./components/ItemModal";
import QuickAddBar from "./components/QuickAddBar";
import AuthenticationModal from "./components/AuthCard";
import FilterBar from "./components/FilterBar";
import NavBar from "./components/NavBar";
import {
  responseToFoodItemRequest,
  formDataToFoodItemRequest,
  toggleInSet,
} from "./utility/utils";

import "./App.css";
import SettingsModal from "./components/SettingsModal";
import FoodSection from "./components/FoodSection";

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
  const [isLogin, setIsLogin] = useState(true);
  const [userTypes, setUserTypes] = useState<UserTypeResponse[]>([]);
  const [userLocations, setUserLocations] = useState<UserLocationResponse[]>(
    [],
  );
  const [settingModal, setSettingModal] = useState(false);
  const [itemDefaults, setItemDefaults] = useState<ItemDefaultsResponse[]>([]);
  const [groupBy, setGroupBy] = useState<"none" | "location" | "type">("none");

  useEffect(() => {
    if (!authToken) return;
    async function fetchItems() {
      try {
        setFoodLoading(true);
        const [items, types, locations, defaults] = await Promise.all([
          getAllFoodItems(authToken as string, setToken),
          getUserTypes(authToken as string, setToken),
          getUserLocations(authToken as string, setToken),
          getItemDefaults(authToken as string, setToken),
        ]);
        setFoodList(items.content);
        setUserTypes(types);
        setUserLocations(locations);
        setItemDefaults(defaults);
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
    const expirationDays = Math.max(
      0,
      Math.round(
        (new Date(request.expirationDate).getTime() -
          new Date(request.purchaseDate).getTime()) /
          (1000 * 60 * 60 * 24),
      ),
    );
    const defaults: ItemDefaultsResponse = {
      name: request.name.toUpperCase(),
      foodType: request.foodType.toUpperCase(),
      unit: request.unit.toUpperCase(),
      location: request.location.toUpperCase(),
      expirationDays,
    };
    setItemDefaults((prev) => [...prev, defaults]);
    setFoodList((prev) => [...prev, response]);
    setModalState(null);
  }

  async function handleLogout() {
    if (!authToken) return;
    try {
      await logoutUser(authToken, setToken);
    } catch (err) {
      console.log(err);
    } finally {
      setToken(null);
      localStorage.clear();
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
    setTypeFilter((prev) => toggleInSet(prev, type));
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
        `Food items with type ${name} exist. Delete items before deleting type.`,
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
    setLocationFilter((prev) => toggleInSet(prev, location));
  }

  function closeModals() {
    setSettingModal(false);
  }

  function groupItems(
    items: FoodItemResponse[],
  ): Map<string, FoodItemResponse[]> {
    const map = new Map<string, FoodItemResponse[]>([]);
    switch (groupBy) {
      case "none":
        return new Map([["all", items]]);
      case "location":
        userLocations.forEach((location) => map.set(location.name, []));
        items.forEach((item) => map.get(item.location)?.push(item));
        break;
      case "type":
        userTypes.forEach((type) => map.set(type.name, []));
        items.forEach((item) => map.get(item.foodType)?.push(item));
        break;
    }
    return map;
  }

  const modalSubmitHandler =
    modalState === "add" || modalState === null
      ? handleAddedItem
      : (data: FormData) => handleEditItem(data, modalState.id);

  const locationNames = useMemo(
    () => userLocations.map((l) => l.name),
    [userLocations],
  );
  const typeNames = useMemo(() => userTypes.map((t) => t.name), [userTypes]);

  const visibleItems = foodList
    .filter(
      (item) => locationFilter.size === 0 || locationFilter.has(item.location),
    )
    .filter((item) => typeFilter.size === 0 || typeFilter.has(item.foodType))
    .sort((a, b) => a.name.localeCompare(b.name));

  const grouped = groupItems(visibleItems);

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
      {modalState && (
        <ItemModal
          userLocations={locationNames}
          userTypes={typeNames}
          initialValue={initialForm}
          itemDefaults={itemDefaults}
          setIsOpen={setModalState}
          onSubmit={modalSubmitHandler}
        ></ItemModal>
      )}
      {authToken === null && (
        <AuthenticationModal
          mode={isLogin}
          setMode={setIsLogin}
          setToken={setToken}
        ></AuthenticationModal>
      )}
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
        userLocations={locationNames}
        userTypes={typeNames}
        locationFilter={locationFilter}
        typeFilter={typeFilter}
        setTypeFilter={typeFilterHandler}
        setLocationFilter={locationFilterHandler}
        setGroupBy={setGroupBy}
      ></FilterBar>
      <QuickAddBar
        itemDefaults={itemDefaults}
        onAddNew={() => setModalState("add")}
        onQuickAdd={async () => {}}
      />
      {foodLoading ? (
        <div>Loading...</div>
      ) : (
        [...grouped.entries()].map(([category, foodList]) =>
          foodList.length > 0 ? (
            <FoodSection
              title={category}
              showTitle={groupBy !== "none"}
              foodList={foodList}
              onDelete={handleDeleteFoodItem}
              onEdit={setModalState}
            ></FoodSection>
          ) : null,
        )
      )}
    </>
  );
}

export default App;
