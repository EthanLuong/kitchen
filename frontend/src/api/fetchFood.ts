import {
  type FoodItemRequest,
  type FoodItemResponse,
  type Token,
} from "../types/types";

const BASE_URL = "http://localhost:8080";

export async function getAllFoodItems(
  token: Token,
): Promise<FoodItemResponse[]> {
  const response = await fetch(`${BASE_URL}/items`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Bad response");
  }
  const data = await response.json();

  return data;
}

export async function createNewFoodItem(
  item: FoodItemRequest,
  token: Token,
): Promise<FoodItemResponse> {
  const response = await fetch(`${BASE_URL}/items`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(item),
  });

  if (!response.ok) {
    throw new Error("Failed to create new item");
  }
  const data = await response.json();
  return data;
}
export async function updateFoodItem(
  id: number,
  item: FoodItemRequest,
  token: Token,
): Promise<FoodItemResponse> {
  const response = await fetch(`${BASE_URL}/items/${id}`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(item),
  });

  if (!response.ok) {
    throw new Error("Failed to update item");
  }

  const data = await response.json();
  return data;
}
export async function deleteFoodItem(id: number, token: Token): Promise<void> {
  const response = await fetch(`${BASE_URL}/items/${id}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) {
    throw new Error("Failed to delete item");
  }
}

export async function getTokenLogin(
  username: string,
  password: string,
): Promise<Token> {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Bad Login");
  }
  const result = await response.json();
  return result.accessToken as Token;
}
export async function createNewUser(
  username: string,
  password: string,
): Promise<void> {
  const response = await fetch(`${BASE_URL}/auth/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Failed to create user");
  }
}
