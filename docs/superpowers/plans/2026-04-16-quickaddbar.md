# QuickAddBar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent "quick add" bar above the food grid so recurring items (one chip per stored `ItemDefault`) can be added in one tap with qty + expiry override, while first-time items keep the existing ItemModal path.

**Architecture:** New `QuickAddBar` component rendered between `FilterBar` and the food grid in `App.tsx`. Each stored `ItemDefault` becomes a chip; tapping a chip expands it inline to reveal a qty stepper + expiry override form. Confirm POSTs via existing `createNewFoodItem`. A trailing `+ Add new` chip opens the existing `ItemModal`. Shelf-life (`ItemDefault.expirationDays`) is used to pre-fill the expiry picker — requires a bug fix in `App.tsx:handleAddedItem` which currently hardcodes `expirationDays: 0`.

**Tech Stack:** React 19, TypeScript, Vite. Existing `fetchFood.ts` API. Existing CSS variables (`--tc`, `--sage`, `--cream`, `--ink`, `--sand`). No new dependencies. No test framework in repo — verification is type-check + Vite build + manual browser check.

**Spec:** `docs/superpowers/specs/2026-04-16-quickaddbar-design.md`

---

## File Structure

| Path | Role | Action |
| --- | --- | --- |
| `frontend/src/components/QuickAddBar.tsx` | New component — chips, expansion state, inline form | Create |
| `frontend/src/App.tsx` | Host; owns `itemDefaults`/`foodList`; adds `handleQuickAdd`; fixes `handleAddedItem` bug | Modify |
| `frontend/src/App.css` | Styles for `.quickaddbar`, `.quickadd-chip`, expanded state | Modify |

No backend changes. No new utility modules (component-local state is sufficient).

**Interface contract between App and QuickAddBar** (defined in `QuickAddBar.tsx`, consumed by `App.tsx`):

```ts
export type QuickAddOverrides = {
  quantity: number;
  expirationDate: string; // ISO yyyy-mm-dd
};

type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (defaults: ItemDefaultsResponse, overrides: QuickAddOverrides) => Promise<void>;
  onAddNew: () => void;
};
```

---

## Task 1: Fix `expirationDays` hardcode in `handleAddedItem`

**Why first:** The entire feature depends on stored shelf-life being accurate. Currently every new `ItemDefault` gets `expirationDays: 0`, which would make every chip pre-fill the expiry picker to today. Fixing this standalone means Tasks 2–4 can be tested with real data without interleaving concerns.

**Files:**
- Modify: `frontend/src/App.tsx:95-114` (`handleAddedItem`)

- [ ] **Step 1: Update `handleAddedItem` to compute `expirationDays` from the request's dates**

Replace the current function body:

```ts
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
```

- [ ] **Step 2: Verify type-check**

Run from `F:\Claude\projects\kitchen\frontend`:
```
npx tsc --noEmit
```
Expected: exit 0, no output.

- [ ] **Step 3: Verify Vite build**

Run from the same directory:
```
npm run build
```
Expected: exit 0, "✓ built in …" at the end.

- [ ] **Step 4: Manual smoke test**

Boot the app. Log in. Open the full ItemModal via the NavBar add button. Create a new item named `TEST-SHELFLIFE` with purchase date today and expiration date 7 days from today. Submit. Verify in React DevTools (or by logging) that the newly-appended `itemDefaults` entry has `expirationDays: 7`, not 0.

- [ ] **Step 5: Commit**

```
git add frontend/src/App.tsx
git commit -m "fix(app): compute expirationDays from request dates"
```

---

## Task 2: Scaffold `QuickAddBar` component and wire `onAddNew`

**Why second:** Gets the component into the render tree with the `+ Add new` path working before any expansion state is involved. This is a testable increment: chips render, alphabetical order works, empty state is OK, `+ Add new` opens ItemModal.

**Files:**
- Create: `frontend/src/components/QuickAddBar.tsx`
- Modify: `frontend/src/App.tsx` (import + render)
- Modify: `frontend/src/App.css` (base chip styles)

- [ ] **Step 1: Create the component file**

Write this full file at `frontend/src/components/QuickAddBar.tsx`:

```tsx
import { type ItemDefaultsResponse } from "../types/types";

export type QuickAddOverrides = {
  quantity: number;
  expirationDate: string;
};

type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (defaults: ItemDefaultsResponse, overrides: QuickAddOverrides) => Promise<void>;
  onAddNew: () => void;
};

export default function QuickAddBar({
  itemDefaults,
  onAddNew,
}: QuickAddBarProps) {
  const sorted = [...itemDefaults].sort((a, b) => a.name.localeCompare(b.name));

  return (
    <div className="quickaddbar" role="toolbar" aria-label="Quick add">
      {sorted.map((d) => (
        <button
          key={d.name}
          type="button"
          className="quickadd-chip"
        >
          {d.name}
        </button>
      ))}
      <button
        type="button"
        className="quickadd-chip quickadd-add-new"
        onClick={onAddNew}
      >
        + Add new
      </button>
    </div>
  );
}
```

`onQuickAdd` is in the props type now (keeps the public contract stable for Task 3) but intentionally unused here — Task 3 wires it up. This avoids a future Props churn.

- [ ] **Step 2: Import and render in `App.tsx`**

Add this import near the existing component imports (roughly `frontend/src/App.tsx:23-35`, wherever you add it preserves alphabetical-ish ordering — no strict rule exists in the file):

```ts
import QuickAddBar from "./components/QuickAddBar";
```

Insert the component into the JSX *after* `<FilterBar … />` and *before* the `{foodLoading ? … : …}` block. Current structure is at `frontend/src/App.tsx:289-297` for FilterBar and `:298-312` for the loading/grid block. The insertion point is between line 297 (`</FilterBar>`) and line 298 (`{foodLoading …}`).

Insert:

```tsx
      <QuickAddBar
        itemDefaults={itemDefaults}
        onAddNew={() => setModalState("add")}
        onQuickAdd={async () => {}}
      />
```

`onQuickAdd` is a no-op placeholder for this task. Task 3 replaces it with `handleQuickAdd`.

- [ ] **Step 3: Add base CSS**

Append to `frontend/src/App.css`:

```css
.quickaddbar {
  display: flex;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  overflow-x: auto;
  background: var(--cream);
  border-bottom: 1px solid var(--sand-lt);
}

.quickadd-chip {
  flex-shrink: 0;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  border: 1px solid var(--sand);
  background: var(--card);
  color: var(--ink);
  font-family: inherit;
  font-size: 0.95rem;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.quickadd-chip:hover {
  background: var(--tc-pale);
  border-color: var(--tc-light);
}

.quickadd-add-new {
  border-style: dashed;
  color: var(--mid);
}

.quickadd-add-new:hover {
  color: var(--tc);
}
```

- [ ] **Step 4: Verify type-check**

Run from `frontend/`:
```
npx tsc --noEmit
```
Expected: exit 0.

- [ ] **Step 5: Verify Vite build**

```
npm run build
```
Expected: exit 0.

- [ ] **Step 6: Manual smoke test**

Boot the app, log in. Confirm:
- A bar appears above the food grid with a chip for each of the user's `itemDefaults` (alphabetical) and a trailing `+ Add new` chip.
- Fresh account / no defaults: the bar shows only `+ Add new`.
- Clicking a regular chip does nothing (Task 3).
- Clicking `+ Add new` opens `ItemModal` in empty / add mode.
- Submitting a new item through the modal closes it and a chip for that name appears in the bar (with correct casing — uppercased by `handleAddedItem`).

- [ ] **Step 7: Commit**

```
git add frontend/src/components/QuickAddBar.tsx frontend/src/App.tsx frontend/src/App.css
git commit -m "feat(quickadd): scaffold QuickAddBar with + Add new entry"
```

---

## Task 3: Add chip expansion, inline form, and `onQuickAdd` submit path

**Why third:** This is the main interaction. Separates cleanly from Task 2's scaffolding. Ends with a functioning quick-add that creates real items.

**Files:**
- Modify: `frontend/src/components/QuickAddBar.tsx` (expansion state + form + submit wiring)
- Modify: `frontend/src/App.tsx` (add `handleQuickAdd`, pass it as prop)
- Modify: `frontend/src/App.css` (expanded-state and form styles)

- [ ] **Step 1: Rewrite `QuickAddBar.tsx` to handle expansion + submit**

Replace the entire file content from Task 2 with:

```tsx
import { useState } from "react";
import { type ItemDefaultsResponse } from "../types/types";
import { todayISO } from "../utility/utils";

export type QuickAddOverrides = {
  quantity: number;
  expirationDate: string;
};

type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (
    defaults: ItemDefaultsResponse,
    overrides: QuickAddOverrides,
  ) => Promise<void>;
  onAddNew: () => void;
};

export default function QuickAddBar({
  itemDefaults,
  onQuickAdd,
  onAddNew,
}: QuickAddBarProps) {
  const [expandedName, setExpandedName] = useState<string | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [expirationDate, setExpirationDate] = useState<string>(todayISO());
  const [isSubmitting, setIsSubmitting] = useState(false);

  const sorted = [...itemDefaults].sort((a, b) => a.name.localeCompare(b.name));

  function openChip(d: ItemDefaultsResponse) {
    if (expandedName === d.name) {
      setExpandedName(null);
      return;
    }
    const seedDate = new Date();
    seedDate.setDate(seedDate.getDate() + (d.expirationDays ?? 0));
    setExpandedName(d.name);
    setQuantity(1);
    setExpirationDate(seedDate.toISOString().split("T")[0]);
  }

  async function confirm(d: ItemDefaultsResponse) {
    if (isSubmitting || quantity < 1) return;
    setIsSubmitting(true);
    try {
      await onQuickAdd(d, { quantity, expirationDate });
      setExpandedName(null);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="quickaddbar" role="toolbar" aria-label="Quick add">
      {sorted.map((d) => {
        const expanded = expandedName === d.name;
        return (
          <div
            key={d.name}
            className={
              expanded ? "quickadd-chip quickadd-chip--expanded" : undefined
            }
          >
            {expanded ? (
              <form
                className="quickadd-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  confirm(d);
                }}
              >
                <span className="quickadd-form-name">{d.name}</span>
                <input
                  type="number"
                  min={1}
                  step={1}
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  aria-label="Quantity"
                />
                <input
                  type="date"
                  value={expirationDate}
                  onChange={(e) => setExpirationDate(e.target.value)}
                  aria-label="Expiration date"
                />
                <button type="submit" disabled={isSubmitting}>
                  {isSubmitting ? "…" : "Add"}
                </button>
                <button
                  type="button"
                  onClick={() => setExpandedName(null)}
                  disabled={isSubmitting}
                >
                  Cancel
                </button>
              </form>
            ) : (
              <button
                type="button"
                className="quickadd-chip"
                onClick={() => openChip(d)}
              >
                {d.name}
              </button>
            )}
          </div>
        );
      })}
      <button
        type="button"
        className="quickadd-chip quickadd-add-new"
        onClick={onAddNew}
      >
        + Add new
      </button>
    </div>
  );
}
```

- [ ] **Step 2: Add `handleQuickAdd` in `App.tsx`**

Insert this new handler alongside `handleAddedItem` (near `frontend/src/App.tsx:95`, anywhere among the handlers is fine — keep it co-located with `handleAddedItem` for readability):

```ts
  async function handleQuickAdd(
    defaults: ItemDefaultsResponse,
    overrides: QuickAddOverrides,
  ) {
    if (!authToken) throw new Error("Not authenticated");
    const request: FoodItemRequest = {
      name: defaults.name,
      foodType: defaults.foodType,
      unit: defaults.unit,
      location: defaults.location,
      quantity: overrides.quantity,
      purchaseDate: todayISO(),
      expirationDate: overrides.expirationDate,
    };
    const response = await createNewFoodItem(request, authToken, setToken);
    setFoodList((prev) => [...prev, response]);
  }
```

Also add `QuickAddOverrides` to the import from `./components/QuickAddBar`. Change the current import line:

```ts
import QuickAddBar from "./components/QuickAddBar";
```

to:

```ts
import QuickAddBar, { type QuickAddOverrides } from "./components/QuickAddBar";
```

And `todayISO` should already be imported from `./utility/utils`; verify and add if missing. Check around `frontend/src/App.tsx:27-31`:

```ts
import {
  responseToFoodItemRequest,
  formDataToFoodItemRequest,
  toggleInSet,
} from "./utility/utils";
```

Add `todayISO`:

```ts
import {
  responseToFoodItemRequest,
  formDataToFoodItemRequest,
  toggleInSet,
  todayISO,
} from "./utility/utils";
```

- [ ] **Step 3: Replace the placeholder `onQuickAdd` prop with the real handler**

Find the JSX where `QuickAddBar` was rendered in Task 2:

```tsx
      <QuickAddBar
        itemDefaults={itemDefaults}
        onAddNew={() => setModalState("add")}
        onQuickAdd={async () => {}}
      />
```

Replace with:

```tsx
      <QuickAddBar
        itemDefaults={itemDefaults}
        onAddNew={() => setModalState("add")}
        onQuickAdd={handleQuickAdd}
      />
```

- [ ] **Step 4: Append expanded-state CSS**

Append to `frontend/src/App.css`:

```css
.quickadd-chip--expanded {
  flex-shrink: 0;
  padding: 0.5rem 0.75rem;
  border-radius: 14px;
  border: 1px solid var(--tc-light);
  background: var(--tc-pale);
}

.quickadd-form {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: inherit;
}

.quickadd-form-name {
  font-weight: 600;
  color: var(--ink);
}

.quickadd-form input[type="number"] {
  width: 4rem;
}

.quickadd-form input[type="date"] {
  width: 9rem;
}

.quickadd-form button {
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--sand);
  background: var(--card);
  color: var(--ink);
  cursor: pointer;
  font-family: inherit;
}

.quickadd-form button[type="submit"] {
  background: var(--tc);
  color: var(--cream);
  border-color: var(--tc);
}

.quickadd-form button[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}
```

- [ ] **Step 5: Verify type-check**

```
npx tsc --noEmit
```
Expected: exit 0.

- [ ] **Step 6: Verify Vite build**

```
npm run build
```
Expected: exit 0.

- [ ] **Step 7: Manual smoke test**

Boot app, log in. At minimum one `ItemDefault` must exist — add one via ItemModal with a non-zero shelf-life if needed (e.g., Milk, purchase today, expire 7 days out).

Confirm:
- Tap the `MILK` chip → chip expands inline with qty `1`, expiry pre-filled to 7 days from today.
- Tap `Add` → new food card appears in the grid; chip collapses.
- Tap `MILK` again, change expiry to a different date, tap `Add` → food card uses the overridden date. Tap `MILK` a third time → the expansion pre-fills to today+7 again (the stored default was not mutated — as per spec Open Question 1).
- Tap chip A, then tap chip B without confirming → A collapses and B expands.
- Tap `Cancel` in the form → collapse, no submit.
- Tap the same expanded chip again → collapse, no submit.
- Double-tap `Add` rapidly → only one POST fires (button disabled during `isSubmitting`).

- [ ] **Step 8: Commit**

```
git add frontend/src/components/QuickAddBar.tsx frontend/src/App.tsx frontend/src/App.css
git commit -m "feat(quickadd): chip expansion, qty+expiry form, submit path"
```

---

## Task 4: Collapse on Escape and outside-click

**Why last:** Nice-to-have polish that layers cleanly on top of the expansion state from Task 3. Isolating it keeps Task 3's diff readable.

**Files:**
- Modify: `frontend/src/components/QuickAddBar.tsx` (add useEffect + ref)

- [ ] **Step 1: Add Escape-key and outside-click handlers**

Change the React import at the top of `QuickAddBar.tsx` from:

```ts
import { useState } from "react";
```

to:

```ts
import { useEffect, useRef, useState } from "react";
```

Add a ref and effect inside the component, immediately after the four `useState` calls:

```ts
  const barRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (expandedName === null) return;

    function handleKey(e: KeyboardEvent) {
      if (e.key === "Escape") setExpandedName(null);
    }
    function handleClick(e: MouseEvent) {
      if (barRef.current && !barRef.current.contains(e.target as Node)) {
        setExpandedName(null);
      }
    }

    window.addEventListener("keydown", handleKey);
    window.addEventListener("mousedown", handleClick);
    return () => {
      window.removeEventListener("keydown", handleKey);
      window.removeEventListener("mousedown", handleClick);
    };
  }, [expandedName]);
```

Attach the ref to the outer `<div className="quickaddbar" …>`:

```tsx
    <div ref={barRef} className="quickaddbar" role="toolbar" aria-label="Quick add">
```

- [ ] **Step 2: Verify type-check**

```
npx tsc --noEmit
```
Expected: exit 0.

- [ ] **Step 3: Verify Vite build**

```
npm run build
```
Expected: exit 0.

- [ ] **Step 4: Manual smoke test**

Boot app, log in. Expand any chip, then verify each collapse trigger:
- Press `Escape` → collapses.
- Click anywhere outside the bar (food grid, header, background) → collapses.
- Click inside the bar (on another chip or the form) → does not trigger outside-click collapse. Re-tap of the expanded chip still collapses (Task 3 behavior). Clicking a different chip opens that one (Task 3 behavior).
- Clicking inside the form inputs does NOT collapse (the `<form>` is inside `barRef.current`).
- After collapse via Escape, `expandedName === null` so the effect's cleanup runs — no stray listeners. Re-expand and verify collapse still works (i.e., effect re-runs on `expandedName` change).

- [ ] **Step 5: Commit**

```
git add frontend/src/components/QuickAddBar.tsx
git commit -m "feat(quickadd): collapse on Escape and outside-click"
```

---

## Verification After All Tasks

Run from `frontend/`:

```
npx tsc --noEmit
npm run build
```

Manual end-to-end check (don't skip — no automated tests cover this):

- [ ] Fresh account: bar shows only `+ Add new`. `+ Add new` opens ItemModal.
- [ ] Submit new item via ItemModal with non-zero shelf-life → chip appears in bar, `expirationDays` correctly computed.
- [ ] Quick-add flow end-to-end on a known chip with and without expiry override.
- [ ] Overriding expiry during quick-add does NOT mutate the stored default.
- [ ] Collapse interactions: Cancel, Escape, outside-click, re-tap, tap-another-chip.
- [ ] Mobile-width (≤480px) the bar scrolls horizontally; expanded chip stays legible.
- [ ] Logout + log back in → bar repopulates from `itemDefaults`.
- [ ] No regressions: FilterBar, grouping, edit via card, delete via card, SettingsModal all still work.

---

## Notes for the Implementer

- **No test framework.** Do not set one up; the spec explicitly excludes it.
- **CSS variables.** The palette tokens (`--tc`, `--sage`, `--cream`, `--ink`, `--sand`, `--tc-pale`, `--tc-light`, `--sand-lt`, `--card`, `--mid`) are defined at the top of `frontend/src/App.css`. Use them rather than raw hex colors.
- **Existing utilities.** `todayISO()` lives in `frontend/src/utility/utils.ts`. Reuse it rather than writing new date helpers.
- **Don't mutate stored defaults on override.** The spec resolves this explicitly: quick-add is consumption-side only. `handleQuickAdd` must NOT call `setItemDefaults` or post anything to `/user/item-defaults`.
- **Branch.** Work on `feature/quickaddbar`. Do not merge to master as part of implementation — that's a user action after review.
