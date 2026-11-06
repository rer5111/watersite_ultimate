import { useState } from 'react';
import { SkinAvatar } from '@/components/ui';
import { Button } from '@/components/ui';
import type { UserProfile } from '@/types';

type SkinMode = 'face' | 'bust' | 'full';

const modes = [
  { value: 'face', label: 'Портрет', description: 'Аватар игрока', imageClass: 'size-36' },
  { value: 'bust', label: 'По пояс', description: 'Вид по пояс', imageClass: 'h-60 w-48' },
  { value: 'full', label: 'Полный рост', description: 'Персонаж целиком', imageClass: 'h-80 w-60' },
] as const;

export function SkinShowcaseSection({ profile }: { profile: UserProfile }) {
  const [mode, setMode] = useState<SkinMode>('full');
  const selectedMode = mode === 'face' ? modes[0] : mode === 'bust' ? modes[1] : modes[2];
  const minecraftName = profile.minecraftNickname || profile.username;

  return (
    <section aria-labelledby="skin-showcase-title" className="bg-card grid gap-8 p-6 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] md:p-8">
      <div className="flex min-h-80 items-center justify-center overflow-hidden bg-secondary/40 p-6">
        <SkinAvatar
          uuid={profile.minecraftUuid}
          name={minecraftName}
          mode={mode}
          className={`${selectedMode.imageClass} object-contain`}
        />
      </div>

      <div className="flex flex-col justify-center gap-5">
        <div className="flex flex-col gap-2">
          <h2 id="skin-showcase-title" className="text-2xl font-bold">Игровой облик</h2>
          <p className="text-foreground-muted text-sm">
            Просмотрите скин Minecraft в трёх ракурсах. Используется ник {minecraftName}.
          </p>
        </div>

        <div className="flex flex-wrap gap-2" role="group" aria-label="Ракурс скина">
          {modes.map((item) => (
            <Button
              key={item.value}
              variant={mode === item.value ? 'secondary' : 'tertiary'}
              aria-pressed={mode === item.value}
              onClick={() => setMode(item.value)}
            >
              {item.label}
            </Button>
          ))}
        </div>

        <p className="text-foreground-muted text-sm">{selectedMode.description}</p>
      </div>
    </section>
  );
}
