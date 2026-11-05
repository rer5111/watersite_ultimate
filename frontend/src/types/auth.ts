export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface MicrosoftAuthorizeUrlResponse {
  url: string;
}

export interface MicrosoftLoginRequest {
  code: string;
  redirectUri?: string;
}

export interface DeviceCodeResponse {
  deviceCode: string;
  userCode: string;
  verificationUri: string;
  expiresIn: number;
  interval: number;
}

export interface DeviceLoginResponse {
  status: 'PENDING' | 'SUCCESS';
  tokens?: TokenResponse;
}

export interface AuthUser {
  id: number;
  username: string;
  uuid: string | null;
  roles: string[];
}

export interface SendCodeResponse {
  email: string;
  expiresInSeconds: number;
  cooldownSeconds: number;
  message: string;
}
