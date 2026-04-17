import { formatName } from "../utility/utils";

type GroupByOptions = "none" | "location" | "type";
type FilterBarProps = {
  userLocations: string[];
  userTypes: string[];
  locationFilter: Set<string>;
  typeFilter: Set<string>;
  setLocationFilter: (location: string | null) => void;
  setTypeFilter: (type: string | null) => void;
  setGroupBy: (grouping: GroupByOptions) => void;
};

export default function FilterBar({
  userLocations,
  userTypes,
  locationFilter,
  typeFilter,
  setLocationFilter,
  setTypeFilter,
  setGroupBy,
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
            {formatName(location)}
          </button>
        ))}
        {locationFilter.size > 0 && (
          <button className="filter-clear" onClick={() => setLocationFilter(null)}>Clear</button>
        )}
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
            {formatName(type)}
          </button>
        ))}

        {typeFilter.size > 0 && (
          <button className="filter-clear" onClick={() => setTypeFilter(null)}>Clear</button>
        )}
      </div>

      <div className="filterrow">
        <span>Group</span>
        <select
          name="groupby"
          onChange={(e) => setGroupBy(e.target.value as GroupByOptions)}
        >
          <option value="none">None</option>
          <option value="location">Location</option>
          <option value="type">Type</option>
        </select>
      </div>
    </div>
  );
}
