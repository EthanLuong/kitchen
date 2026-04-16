# QuickAddBar Design Spec

## Goal

Cut friction from two recurring flows that currently require opening the full `ItemModal`:
- **Batch-add after a grocery trip** (many items, desktop)
- **Add-one-off when running low** (single item, mobile/tablet)

Known recurring items become one-tap adds with stored-per-name defaults. First-time items keep the existing modal path.

## Approach

A persistent `QuickAddBar` component mounts above the food grid (sibling to `FilterBar`). The bar shows a horizontal strip of **chips**, one per `ItemDefault` row from `/user/item-defaults`, followed by a trailing `+ Add new` chip.

Tapping a chip expands it **inline** — no modal, no popover. The expanded chip reveals a quantity stepper and an expiry date field (pre-filled from the default's `expirationDays`). Confirm creates a `FoodItem`; cancel or Escape collapses back. The `+ Add new` chip opens the existing `ItemModal` for first-time items.

Split-path design: known items take the fast path; new items take the ItemModal path. No hybrid.

## Tech Stack

React 19, TypeScript, existing `fetchFood.ts` API. No new dependencies. Styling via existing CSS variables (terracotta / sage / cream palette, Lora + Source Sans 3).

## Data Model

No backend schema change. `/user/item-defaults` already stores per-name `{ name, foodType, unit, location, expirationDays }`.

One bug fix required as part of this work: `App.tsx:handleAddedItem` currently hardcodes `expirationDays: 0` when appending a new default (line 109). Replace with a computed value: days between `request.purchaseDate` and `request.expirationDate`. Without this fix, every chip expands with today's date as the default expiry and the "shelf-life" feature is inert.

## Components

### New: `QuickAddBar.tsx`

**Location:** `frontend/src/components/QuickAddBar.tsx`

**Props:**
```ts
type QuickAddBarProps = {
  itemDefaults: ItemDefaultsResponse[];
  onQuickAdd: (defaults: ItemDefaultsResponse, overrides: QuickAddOverrides) => Promise<void>;
  onAddNew: () => void;
};

type QuickAddOverrides = {
  quantity: number;
  expirationDate: string; // ISO yyyy-mm-dd
};
```

**Internal state:** `expandedName: string | null` — at most one chip expanded at a time.

**Rendering:** horizontally-scrollable flex row (`overflow-x: auto`) with chips + trailing `+ Add new`. The expanded chip swaps its label for an inline form (qty stepper, expiry input, confirm/cancel). Other chips remain visible; user can scroll past them.

**Sort:** alphabetical by `name`. Future iterations may add usage-based ordering.

### Modified: `App.tsx`

- Render `<QuickAddBar>` above the grouped `FoodSection`s, below `FilterBar`.
- New handler `handleQuickAdd(defaults, overrides)` composes a `FoodItemRequest` from defaults + overrides + `purchaseDate = todayISO()`, POSTs via `createNewFoodItem`, and updates `foodList` state.
- `handleAddedItem` bug fix: compute `expirationDays` from the request's date fields rather than hardcoding 0.

### Modified: `ItemModal.tsx`

No behavioral change. The `+ Add new` button reuses the existing "add" mode entry point (`setModalState("add")`).

### New styles

New CSS classes in `App.css`:
- `.quickaddbar` — flex container, horizontal scroll
- `.quickadd-chip` — base chip pill
- `.quickadd-chip--expanded` — expanded-state styling
- `.quickadd-form` — inline form inside expanded chip
- `.quickadd-add-new` — trailing "+ Add new" chip

## User Flows

### A. Quick-add a known item
1. User sees chip for `MILK`.
2. Taps chip → chip expands in place. Siblings stay visible.
3. Qty stepper shows `1`. Expiry shows `today + default.expirationDays`.
4. User taps Confirm.
5. `createNewFoodItem` POSTs. Food grid updates with the new card. Chip collapses.

### B. Quick-add with expiry override
1. Same as A, but user changes the expiry date before confirming.
2. Food item is created with the overridden date.
3. **The stored `ItemDefault.expirationDays` is not updated.** Quick-add never mutates stored defaults (see Open Question resolution).

### C. First-time item (no default exists)
1. User taps `+ Add new` at the end of the bar.
2. `ItemModal` opens via existing add-mode path.
3. User fills the full form, submits.
4. `handleAddedItem` creates the food item AND appends an `ItemDefault` with correctly-computed `expirationDays`.
5. Next render: a chip for this name appears in the bar.

### D. Cancel expansion
1. User taps chip, expands.
2. One of: taps Cancel button inside the form / presses Escape / taps the chip again / taps outside the bar.
3. Chip collapses. No submit, no state change.

## Edge Cases

- **Empty `itemDefaults` (fresh account):** bar shows only `+ Add new`. No empty-state copy needed — the button is the affordance.
- **`expirationDays === 0` for a default:** expiry picker pre-fills to today. User may explicitly pick a date. No hiding — the field is always present for consistency.
- **Quantity constraint:** min 1. Stepper prevents zero/negative. Matches existing `ItemModal` quantity input (`min="0", step="1"` — QuickAddBar uses `min="1"` to avoid accidental zero submits on the fast path).
- **Name collision with existing filters:** QuickAddBar's source of truth is `itemDefaults`, not `foodList`. Chips appear for names the user has ever added, even if no item currently exists. That's the intended behavior for re-adding a depleted item.
- **Two chips expanded simultaneously:** prevented by single `expandedName` state; tapping a second chip collapses the first.
- **Concurrent quick-adds while one is pending:** no optimistic UI. Second tap is ignored while the first is mid-flight (disable confirm button during `isSubmitting`).

## Open Questions (Resolved)

1. **Does expiry-override update stored `expirationDays`?**
   - **Resolution: No.** Quick-add is consumption-side only. Training the default is explicit — user edits via `ItemModal`. Predictable, avoids silent rewrites from one-off weird expiries.

2. **Sort order of chips.**
   - **Resolution: Alphabetical by name for MVP.** Frequency/recency ordering deferred. Consistent with how filters in `FilterBar` are displayed.

## Out of Scope (this iteration)

- Bulk-add (multi-select chips, one submit)
- Autocomplete / typeahead in the bar
- Chip reordering, favoriting, or removal
- Editing an `ItemDefault` (name, type, location, etc.) — stays in ItemModal / Settings
- Mobile-specific gestures (long-press, swipe)
- Optimistic UI for the food grid on quick-add
- Analytics / usage tracking for chip taps

## Testing Plan (manual — no frontend test framework)

Run through before merging:

- [ ] Fresh account: bar shows only `+ Add new`. Click it → ItemModal opens.
- [ ] After first ItemModal submit of "MILK" with expiry 7 days out → chip for `MILK` appears, its expansion pre-fills expiry to today + 7.
- [ ] Tap `MILK` chip → expand → Confirm → new food card appears, chip collapses.
- [ ] Tap `MILK` chip → expand → change expiry → Confirm → card uses overridden date; tap chip again → expansion still pre-fills with stored default (override did not persist).
- [ ] Tap chip → expand → press Escape → collapse, no submit.
- [ ] Tap chip A → expand → tap chip B → A collapses, B expands.
- [ ] Many defaults (≥15) → bar scrolls horizontally on mobile width. Expanded chip stays pinned at tap location.
- [ ] Type-check + Vite build pass.
- [ ] Visual pass on mobile (375px) and desktop (≥1024px) widths.

## Architecture Summary

```
App
├── FilterBar (existing)
├── QuickAddBar (NEW)
│   ├── .quickadd-chip × N (one per ItemDefault)
│   └── .quickadd-add-new (opens ItemModal)
├── FoodSection × N (existing — one per group)
└── ItemModal (existing — opens from QuickAddBar's "+ Add new" and from FoodCard click)
```

No state lifts up from existing components. `QuickAddBar` consumes props; `App` already owns `itemDefaults` and `foodList`.
