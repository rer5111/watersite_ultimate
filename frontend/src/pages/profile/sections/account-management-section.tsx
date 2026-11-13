import type { UserProfile } from '@/types';
import { MinecraftNicknameForm, ChangeEmailForm, ChangePasswordForm } from '../components';

export interface AccountManagementSectionProps {
  profile: UserProfile;
}

export function AccountManagementSection({ profile }: AccountManagementSectionProps) {
  return (
    <section aria-label="Управление аккаунтом" className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-foreground text-2xl font-bold tracking-tight md:text-3xl">
          Управление аккаунтом
        </h2>
        <p className="text-foreground-muted text-sm">
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
