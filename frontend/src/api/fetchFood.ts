import {
  type FoodItemRequest,
  type FoodItemResponse,
  type Token,
  type Page,
  type UserLocationResponse,
  type UserTypeResponse,
} from "../types/types";

const API_URI = import.meta.env.VITE_API_URL;

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

  const refreshResponse = await fetch(`${API_URI}/auth/refresh`, {
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
    `${API_URI}/items${page}`,
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
    `${API_URI}/items`,
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
    `${API_URI}/items/${id}`,
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
    `${API_URI}/items/${id}`,
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
  const response = await fetch(`${API_URI}/auth/login`, {
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
  const response = await fetch(`${API_URI}/auth/signup`, {
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
    `${API_URI}/auth/logout`,
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

export async function getUserLocations(
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<UserLocationResponse[]> {
  const response = await apiFetch(
    `${API_URI}/user/locations`,
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

export async function getUserTypes(
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<UserTypeResponse[]> {
  const response = await apiFetch(
    `${API_URI}/user/types`,
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

export async function addUserLocations(
  name: string,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<UserLocationResponse> {
  const response = await apiFetch(
    `${API_URI}/user/locations`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ name }),
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

export async function addUserTypes(
  name: string,
  token: Token,
  setToken: (token: Token | null) => void,
): Promise<UserTypeResponse> {
  const response = await apiFetch(
    `${API_URI}/user/types`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ name }),
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

export async function deleteUserLocations(
  id: number,
  token: Token,
  setToken: (token: Token | null) => void,
) {
  const response = await apiFetch(
    `${API_URI}/user/locations/${id}`,
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

export async function deleteUserTypes(
  id: number,
  token: Token,
  setToken: (token: Token | null) => void,
) {
  const response = await apiFetch(
    `${API_URI}/user/types/${id}`,
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
