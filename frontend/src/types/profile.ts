export interface UserProfile {
  id: number;
  username: string;
  email: string | null;
  minecraftNickname: string;
  minecraftUuid: string | null;
  joinDate: string | null;
  roles: string[];
}

export interface UpdateMinecraftNicknameRequest {
  minecraftNickname: string;
}

export interface SendEmailChangeCodeRequest {
  newEmail: string;
}

export interface ChangeEmailRequest {
  newEmail: string;
  code: string;
}

export interface ChangePasswordRequest {
  code: string;
  newPassword: string;
}
