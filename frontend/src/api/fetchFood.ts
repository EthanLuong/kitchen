import {
  type FoodItemRequest,
  type FoodItemResponse,
  type Token,
  type Page,
} from "../types/types";

const BASE_URL = import.meta.env.VITE_API_URL;

export async function apiFetch(
  url: string,
  options: RequestInit,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<Response> {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.status !== 401) return response;

  const refreshResponse = await fetch(`${BASE_URL}/auth/refresh`, {
    method: "POST",
    credentials: "include",
  });

  if (!refreshResponse.ok) {
    setToken(null);
    localStorage.removeItem("token");
    throw new Error("Session expired. Log in again");
  }

  const { accessToken } = await refreshResponse.json();
  setToken(accessToken);
  localStorage.setItem("token", accessToken);

  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export async function getAllFoodItems(
  token: Token,
  setToken: (token: Token | null) => void,
  page: string = "",
): Promise<Page<FoodItemResponse>> {
  const response = await apiFetch(
    `${BASE_URL}/items${page}`,
    {
      method: "GET",
    },
    token,
    setToken,
  );

  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
  }

  const data = await response.json();
  return data;
}

export async function createNewFoodItem(
  item: FoodItemRequest,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<FoodItemResponse> {
  const response = await apiFetch(
    `${BASE_URL}/items`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    },
    token,
    setToken,
  );

  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
  }
  const data = await response.json();
  return data;
}
export async function updateFoodItem(
  id: number,
  item: FoodItemRequest,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<FoodItemResponse> {
  const response = await apiFetch(
    `${BASE_URL}/items/${id}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    },
    token,
    setToken,
  );

  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
  }

  const data = await response.json();
  return data;
}
export async function deleteFoodItem(
  id: number,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<void> {
  const response = await apiFetch(
    `${BASE_URL}/items/${id}`,
    {
      method: "DELETE",
    },
    token,
    setToken,
  );
  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
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
    credentials: "include",
  });

  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
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
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
  }
}

export async function logoutUser(
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<void> {
  const response = await apiFetch(
    `${BASE_URL}/auth/logout`,
    {
      method: "POST",
    },
    token,
    setToken,
  );

  if (!response.ok) {
    const errorBody = await response.json();
    throw new Error(errorBody.detail ?? "Something went wrong");
  }
}
