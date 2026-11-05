import type { UserProfile } from '@/types';
import {
  MinecraftNicknameForm,
  ChangeEmailForm,
  ChangePasswordForm,
} from '../components';

export interface AccountManagementSectionProps {
  profile: UserProfile;
}

export function AccountManagementSection({ profile }: AccountManagementSectionProps) {
  return (
    <section aria-label="Управление аккаунтом" className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-2xl font-bold tracking-tight text-foreground md:text-3xl">
          Управление аккаунтом
        </h2>
        <p className="text-sm text-foreground-muted">
          Настройте игровой никнейм Minecraft, контактные данные и параметры входа в систему.
        </p>
      </div>

      <div className="flex flex-col gap-6">
        <MinecraftNicknameForm currentNickname={profile.minecraftNickname} />
        <ChangeEmailForm currentEmail={profile.email} />
        <ChangePasswordForm hasEmail={Boolean(profile.email)} emailHint={profile.email} />
      </div>
    </section>
  );
}
