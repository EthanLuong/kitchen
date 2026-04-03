import { type SortOptions, SORT_TYPE } from "../types/types";

type FilterBarProps = {
  userLocations: string[];
  userTypes: string[];
  locationFilter: Set<string>;
  typeFilter: Set<string>;
  setLocationFilter: (location: string | null) => void;
  setTypeFilter: (type: string | null) => void;
  setSortType: (type: SortOptions) => void;
};

export default function FilterBar({
  userLocations,
  userTypes,
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

        {userLocations.map((location) => (
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
        {userTypes.map((type) => (
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
