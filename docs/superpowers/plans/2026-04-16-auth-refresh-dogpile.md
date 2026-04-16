# Auth Refresh Dogpile Fix — Implementation Plan

**Goal:** Concurrent 401s must share a single `/auth/refresh` call instead of each firing their own.

**Architecture:** Module-level single-flight promise in `fetchFood.ts`. If a refresh is in flight, any caller that hits a 401 awaits that same promise. Resolves to the new access token. Cleared on settle so the next refresh starts fresh.

**Tech Stack:** TypeScript, no test framework in repo (verification is `tsc --noEmit` + `npm run build`).

---

## Context: The Bug

`frontend/src/App.tsx:63-68` — initial page load:

```ts
const [items, types, locations, defaults] = await Promise.all([
  getAllFoodItems(authToken, setToken),
  getUserTypes(authToken, setToken),
  getUserLocations(authToken, setToken),
  getItemDefaults(authToken, setToken),
]);
```

If the access token is expired, four requests 401 simultaneously. Each independently calls `apiFetch`'s refresh block (`fetchFood.ts:33-48`), which POSTs `/auth/refresh`. The Spring backend rotates the refresh cookie on use — so only the first POST succeeds. The other three get 401 from refresh, hit `setToken(null) + localStorage.removeItem("token")`, and throw `"Session expired"`. User is silently logged out right after page load even though their refresh token was valid.

## File Structure

Only one file changes: `frontend/src/api/fetchFood.ts`.

- Existing `apiFetch` function stays the public API — no caller changes.
- New private helper `refreshAccessToken` handles the single-flight logic.
- Module-level `let refreshInFlight: Promise<string> | null = null` tracks in-flight state.

---

### Task 1: Single-flight refresh in `fetchFood.ts`

**Files:**
- Modify: `frontend/src/api/fetchFood.ts:19-57` (the `apiFetch` function)

- [ ] **Step 1: Add module-level state and refresh helper above `apiFetch`**

Insert after the `throwIfNotOk` helper (around line 17), before `apiFetch`:

```ts
let refreshInFlight: Promise<string> | null = null;

async function refreshAccessToken(
  setToken: (token: Token | null) => void,
): Promise<string> {
  if (refreshInFlight) return refreshInFlight;

  refreshInFlight = (async () => {
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
    return accessToken as string;
  })().finally(() => {
    refreshInFlight = null;
  });

  return refreshInFlight;
}
```

**Why `.finally`:** reset the singleton on both success and failure so the next 401 that arrives *after* this round finishes can start a fresh refresh. Without finally, a failed refresh would leave a rejected promise cached forever and every subsequent request would re-throw the same stale error.

- [ ] **Step 2: Rewrite `apiFetch` to use the helper**

Replace the current body of `apiFetch` (from `if (response.status !== 401) return response;` through the end of the function) with:

```ts
  if (response.status !== 401) return response;

  const accessToken = await refreshAccessToken(setToken);

  return fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      Authorization: `Bearer ${accessToken}`,
    },
  });
```

The initial request-with-old-token logic at the top of `apiFetch` stays unchanged. Concurrent callers that share the refresh each still retry their own original request — the single-flight only dedupes the refresh call, not the retries.

- [ ] **Step 3: Verify build**

Run from `frontend/`:
```
npx tsc --noEmit
npm run build
```
Expected: both exit 0, no errors, no new warnings.

- [ ] **Step 4: Manual reasoning check**

Walk through these three scenarios in your head; confirm the code handles each:

1. **Single 401:** One caller, `refreshInFlight` starts null, caller assigns it, awaits, gets token, retries, `finally` clears state. Works.
2. **Four concurrent 401s (the bug):** First caller assigns `refreshInFlight` and kicks off the fetch. Callers 2-4 arrive while `refreshInFlight` is non-null and await the same promise. All four resolve together with the same new token, each retries its own original request. One `/auth/refresh` POST total.
3. **Refresh itself fails:** Promise rejects, `finally` clears state, all awaiting callers see the same error (`"Session expired"`), `setToken(null)` ran exactly once inside the promise.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api/fetchFood.ts
git commit -m "fix(auth): single-flight refresh to prevent dogpile logout"
```

---

## Out of Scope (do NOT do)

- Adding tests — there is no test framework in `frontend/package.json`. Do not set one up.
- Changing `apiFetch`'s signature or any caller in `fetchFood.ts` / `App.tsx`.
- Moving refresh into a React context / zustand store / custom hook.
- Fixing the `any`-typed `accessToken` destructure from the json response beyond the `as string` cast shown (the backend shape is stable; this isn't a type-safety pass).
- Anything in `App.tsx` — the four-way `Promise.all` stays exactly as is. This plan fixes the underlying API layer so parallel fetches are safe.

## Review Focus

For the spec reviewer: verify Step 1 and Step 2 code matches the plan verbatim, no extra abstractions, `apiFetch` public signature unchanged, only `fetchFood.ts` modified.

For the code-quality reviewer: check the `.finally` reset is on the promise (not inside the async IIFE body), check the stale-`token`-param edge case isn't introduced (callers retry with the *new* token from the helper return, not the old `token` param), check no new `any`s slipped in.
