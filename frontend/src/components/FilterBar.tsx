import {
  type FoodLocation,
  type FoodType,
  type SortOptions,
  LOCATIONS,
  TYPE,
  SORT_TYPE,
} from "../types/types";
type FilterBarProps = {
  locationFilter: Set<FoodLocation>;
  typeFilter: Set<FoodType>;
  setLocationFilter: (location: FoodLocation | null) => void;
  setTypeFilter: (type: FoodType | null) => void;
  setSortType: (type: SortOptions) => void;
};

export default function FilterBar({
  locationFilter,
  typeFilter,
  setLocationFilter,
  setTypeFilter,
  setSortType,
}: FilterBarProps) {
  return (
    <div className="filterbar">
      <div className="filterrow">
        <span>Location</span>

        {LOCATIONS.map((location) => (
          <button
            onClick={() => setLocationFilter(location)}
            key={location}
            className={
              locationFilter.size === 0
                ? undefined
                : locationFilter.has(location)
                  ? "selected"
                  : undefined
            }
          >
            {location}
          </button>
        ))}
        <button onClick={() => setLocationFilter(null)}>Clear</button>
      </div>
      <div className="filterrow">
        <span>Type</span>
        {TYPE.map((type) => (
          <button
            onClick={() => setTypeFilter(type)}
            key={type}
            className={
              typeFilter.size === 0
                ? undefined
                : typeFilter.has(type)
                  ? "selected"
                  : undefined
            }
          >
            {type}
          </button>
        ))}

        <button onClick={() => setTypeFilter(null)}>Clear</button>
      </div>
      <div className="filterrow">
        <select
          name="sortby"
          onChange={(e) => setSortType(e.target.value as SortOptions)}
        >
          {SORT_TYPE.map((sort) => (
            <option key={sort} value={sort}>
              {sort}
            </option>
          ))}
        </select>
      </div>
    </div>
  );
}
