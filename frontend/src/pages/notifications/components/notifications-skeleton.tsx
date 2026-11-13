export type NotificationsSkeletonProps = {
  count?: number;
};

export function NotificationsSkeleton({ count = 3 }: NotificationsSkeletonProps) {
  return (
    <div className="flex flex-col gap-3" aria-busy="true">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="border-border bg-card h-32 animate-pulse border" />
      ))}
    </div>
  );
}
