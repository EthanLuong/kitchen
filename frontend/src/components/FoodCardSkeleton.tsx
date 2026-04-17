type SkeletonGridProps = {
  count?: number;
};

export default function SkeletonGrid({ count = 6 }: SkeletonGridProps) {
  return (
    <div className="food-section" aria-busy="true" aria-live="polite">
      <div className="foodlist foodlist--loading">
        {Array.from({ length: count }).map((_, i) => (
          <div key={i} className="skeleton-card" aria-hidden="true">
            <div className="skeleton-line skeleton-badges">
              <span className="skeleton-pill" />
              <span className="skeleton-pill" />
            </div>
            <div className="skeleton-line skeleton-title" />
            <div className="skeleton-line skeleton-meta" />
            <div className="skeleton-line skeleton-meta skeleton-meta--short" />
          </div>
        ))}
      </div>
    </div>
  );
}
