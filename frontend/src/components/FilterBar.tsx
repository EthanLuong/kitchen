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
          name="groupby"
          onChange={(e) => setGroupBy(e.target.value as GroupByOptions)}
        >
          <option value="none">none</option>
          <option value="location">location</option>
          <option value="type">type</option>
        </select>
      </div>
    </div>
  );
}
