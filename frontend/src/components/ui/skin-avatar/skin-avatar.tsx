import { useState } from 'react';
import { cn } from '@/lib/utils';

export type SkinAvatarProps = {
  uuid?: string | null;
  fallbackImage?: string;
  name: string;
  className?: string;
  mode?: 'full' | 'bust' | 'face';
};

export function SkinAvatar({ uuid, fallbackImage, name, className, mode = 'full' }: SkinAvatarProps) {
  const [imageFailed, setImageFailed] = useState(false);
  const apiUrl = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '');
  const skinEndpoint = uuid
    ? `${apiUrl}/api/skin-service/api/skin/${encodeURIComponent(uuid)}/${mode}`
    : undefined;

  const defaultGhostUrl = `${apiUrl}/api/skin-service/api/skin/subject/X-Ghost/${mode}`;
  const image = (skinEndpoint && !imageFailed) ? skinEndpoint : (fallbackImage ?? defaultGhostUrl);

  return (
    <img
      src={image}
      alt={`Скин ${name}`}
      decoding="async"
      width={120}
      height={240}
      className={cn('text-transparent text-[0px] shrink-0', className)}
      onError={() => {
        if (!imageFailed && skinEndpoint) {
          setImageFailed(true);
        }
      }}
    />
  );
}
