import {
  type FoodLocation,
  type FoodType,
  LOCATIONS,
  TYPE,
} from "../types/types";

type FilterBarProps = {
  setLocationFilter: (location: FoodLocation | null) => void;
  setTypeFilter: (type: FoodType | null) => void;
};

export default function FilterBar({
  setLocationFilter,
  setTypeFilter,
}: FilterBarProps) {
  return (
    <div className="filterbar">
      <div className="filterrow">
        <span>Location</span>

        {LOCATIONS.map((location) => (
          <button onClick={() => setLocationFilter(location)} key={location}>
            {location}
          </button>
        ))}
        <button onClick={() => setLocationFilter(null)}>Clear</button>
      </div>
      <div className="filterrow">
        <span>Type</span>
        {TYPE.map((type) => (
          <button onClick={() => setTypeFilter(type)} key={type}>
            {type}
          </button>
        ))}

        <button onClick={() => setTypeFilter(null)}>Clear</button>
      </div>
    </div>
  );
}
