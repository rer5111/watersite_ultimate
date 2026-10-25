export type AdminCardProps = {
  image: string;
  title: string;
  description: string;
};

export function AdminCard({ image, title, description }: AdminCardProps) {
  return (
    <div className="bg-card flex flex-col gap-4 p-8">
      <img
        src={image}
        alt={`Скин ${title}`}
        loading="lazy"
        className="aspect-1/2 h-60 w-auto object-scale-down"
      />
      <div className="flex flex-col gap-2">
        <h4 className="text-center text-2xl font-bold">{title}</h4>
        <p className="text-foreground-muted text-center text-base text-pretty">{description}</p>
      </div>
    </div>
  );
}
