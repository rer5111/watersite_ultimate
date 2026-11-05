import type { UserProfile } from '@/types';
import { PlayerCard } from '../components';

export interface ProfileHeroSectionProps {
  profile: UserProfile;
}

export function ProfileHeroSection({ profile }: ProfileHeroSectionProps) {
  return (
    <section aria-label="Профиль игрока" className="w-full">
      <PlayerCard profile={profile} />
    </section>
  );
}
