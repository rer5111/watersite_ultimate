import { useState } from 'react';
import { cn } from '@/lib';

export type SkinAvatarProps = {
  uuid?: string | null;
  fallbackImage?: string;
  name: string;
  className?: string;
  mode?: 'full' | 'bust' | 'face';
};

export function SkinAvatar({
  uuid,
  fallbackImage,
  name,
  className,
  mode = 'full',
}: SkinAvatarProps) {
  const [failedEndpoint, setFailedEndpoint] = useState<string | null>(null);
  const apiUrl = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');

  const defaultSteveUrl = `${apiUrl}/api/skin-service/api/skin/subject/X-Steve/${mode}`;
  const skinEndpoint = uuid
    ? `${apiUrl}/api/skin-service/api/skin/${encodeURIComponent(uuid)}/${mode}`
    : name
      ? `${apiUrl}/api/skin-service/api/skin/subject/${encodeURIComponent(name)}/${mode}`
      : undefined;

  const imageFailed = skinEndpoint !== undefined && failedEndpoint === skinEndpoint;
  const image = skinEndpoint && !imageFailed ? skinEndpoint : (fallbackImage ?? defaultSteveUrl);

  return (
    <img
      src={image}
      alt={`Скин ${name}`}
      decoding="async"
      width={120}
      height={240}
      className={cn('shrink-0 text-[0px] text-transparent', className)}
      onError={() => {
        if (skinEndpoint) {
          setFailedEndpoint(skinEndpoint);
        }
      }}
    />
  );
}
