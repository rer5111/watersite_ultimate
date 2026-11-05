import type {
  DeviceCodeResponse,
  DeviceLoginResponse,
  MicrosoftAuthorizeUrlResponse,
  SendCodeResponse,
  TokenResponse,
} from '@/types';

export class AuthApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly details?: Record<string, string>;

  constructor(message: string, status: number, code?: string, details?: Record<string, string>) {
    super(message);
    this.name = 'AuthApiError';
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

function getApiBaseUrl(): string {
  return (import.meta.env.VITE_API_URL ?? '').replace(/\/+$/, '');
}

export async function getMicrosoftAuthorizeUrl(redirectUri?: string): Promise<string> {
  const base = getApiBaseUrl();
  const target = new URL(`${base}/api/auth-service/auth/microsoft/authorize-url`, window.location.origin);
  if (redirectUri) {
    target.searchParams.set('redirect_uri', redirectUri);
  }

  const response = await fetch(target.toString());
  if (!response.ok) {
    throw new AuthApiError(
      `Не удалось получить ссылку для входа через Microsoft (${response.status})`,
      response.status
    );
  }

  const data = (await response.json()) as MicrosoftAuthorizeUrlResponse;
  return data.url;
}

export async function loginWithMicrosoft(
  code: string,
  redirectUri?: string
): Promise<TokenResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/microsoft/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ code, redirectUri }),
  });

  if (!response.ok) {
    let errorMessage = `Ошибка авторизации Microsoft (${response.status})`;
    let errorCode: string | undefined;
    let errorDetails: Record<string, string> | undefined;
    try {
      const err = (await response.json()) as {
        message?: string;
        detail?: string;
        code?: string;
        details?: Record<string, string>;
      };
      if (err.message) errorMessage = err.message;
      else if (err.detail) errorMessage = err.detail;
      errorCode = err.code;
      errorDetails = err.details;
    } catch {
      // fallback to status error
    }
    throw new AuthApiError(errorMessage, response.status, errorCode, errorDetails);
  }

  const tokens = (await response.json()) as TokenResponse;
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
  return tokens;
}

export async function getMicrosoftDeviceCode(): Promise<DeviceCodeResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/microsoft/device-code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    let errorMessage = `Не удалось получить код устройства (${response.status})`;
    try {
      const err = (await response.json()) as { message?: string };
      if (err.message) {
        errorMessage = err.message;
      }
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status);
  }

  return (await response.json()) as DeviceCodeResponse;
}

export async function pollMicrosoftDeviceLogin(
  deviceCode: string
): Promise<DeviceLoginResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/microsoft/device-login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ deviceCode }),
  });

  if (!response.ok) {
    let errorMessage = `Ошибка проверки авторизации (${response.status})`;
    try {
      const err = (await response.json()) as { message?: string };
      if (err.message) {
        errorMessage = err.message;
      }
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status);
  }

  const result = (await response.json()) as DeviceLoginResponse;
  if (result.status === 'SUCCESS' && result.tokens) {
    localStorage.setItem('accessToken', result.tokens.accessToken);
    localStorage.setItem('refreshToken', result.tokens.refreshToken);
  }
  return result;
}

export async function refreshAccessToken(refreshToken: string): Promise<TokenResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    throw new AuthApiError(`Не удалось обновить токен (${response.status})`, response.status);
  }

  const tokens = (await response.json()) as TokenResponse;
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
  return tokens;
}

export async function loginWithCredentials(
  identifier: string,
  password: string
): Promise<TokenResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username: identifier, password }),
  });

  if (!response.ok) {
    let errorMessage = 'Неверный логин или пароль';
    let errorCode: string | undefined;
    let errorDetails: Record<string, string> | undefined;
    try {
      const err = (await response.json()) as {
        message?: string;
        detail?: string;
        code?: string;
        details?: Record<string, string>;
      };
      if (err.message) errorMessage = err.message;
      else if (err.detail) errorMessage = err.detail;
      errorCode = err.code;
      errorDetails = err.details;
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status, errorCode, errorDetails);
  }

  const tokens = (await response.json()) as TokenResponse;
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
  return tokens;
}

export async function sendRegistrationCode(
  email: string,
  username: string,
  password: string
): Promise<SendCodeResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/register/send-code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, username, password }),
  });

  if (!response.ok) {
    let errorMessage = 'Не удалось отправить код подтверждения';
    let errorCode: string | undefined;
    let errorDetails: Record<string, string> | undefined;
    try {
      const err = (await response.json()) as {
        message?: string;
        detail?: string;
        code?: string;
        details?: Record<string, string>;
      };
      if (err.message) errorMessage = err.message;
      else if (err.detail) errorMessage = err.detail;
      errorCode = err.code;
      errorDetails = err.details;
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status, errorCode, errorDetails);
  }

  return (await response.json()) as SendCodeResponse;
}

export async function verifyRegistrationCode(
  email: string,
  username: string,
  password: string,
  code: string
): Promise<TokenResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/register/verify-code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, username, password, code }),
  });

  if (!response.ok) {
    let errorMessage = 'Неверный код подтверждения';
    let errorCode: string | undefined;
    let errorDetails: Record<string, string> | undefined;
    try {
      const err = (await response.json()) as {
        message?: string;
        detail?: string;
        code?: string;
        details?: Record<string, string>;
      };
      if (err.message) errorMessage = err.message;
      else if (err.detail) errorMessage = err.detail;
      errorCode = err.code;
      errorDetails = err.details;
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status, errorCode, errorDetails);
  }

  const tokens = (await response.json()) as TokenResponse;
  localStorage.setItem('accessToken', tokens.accessToken);
  localStorage.setItem('refreshToken', tokens.refreshToken);
  return tokens;
}

export async function resendVerificationCode(
  email: string,
  purpose: string = 'REGISTRATION'
): Promise<SendCodeResponse> {
  const base = getApiBaseUrl();
  const response = await fetch(`${base}/api/auth-service/auth/resend-code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, purpose }),
  });

  if (!response.ok) {
    let errorMessage = 'Не удалось повторно отправить код';
    let errorCode: string | undefined;
    let errorDetails: Record<string, string> | undefined;
    try {
      const err = (await response.json()) as {
        message?: string;
        detail?: string;
        code?: string;
        details?: Record<string, string>;
      };
      if (err.message) errorMessage = err.message;
      else if (err.detail) errorMessage = err.detail;
      errorCode = err.code;
      errorDetails = err.details;
    } catch {
      // fallback
    }
    throw new AuthApiError(errorMessage, response.status, errorCode, errorDetails);
  }

  return (await response.json()) as SendCodeResponse;
}
