export interface ProblemDetailResponse {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  code?: string;
  message?: string;
  errors?: Record<string, string> | Array<{ field?: string; defaultMessage?: string }>;
  details?: Record<string, string>;
}

export function getStatusFallbackMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Некорректный запрос или неверные данные';
    case 401:
      return 'Требуется авторизация';
    case 403:
      return 'Недостаточно прав для выполнения действия';
    case 404:
      return 'Запрошенные данные не найдены';
    case 409:
      return 'Конфликт данных или запись уже существует';
    case 422:
      return 'Введенные данные не прошли проверку';
    case 429:
      return 'Слишком много запросов. Пожалуйста, подождите немного';
    case 500:
      return 'Внутренняя ошибка сервера. Повторите попытку позже';
    case 502:
      return 'Шлюз не получил ответ от сервиса. Повторите попытку позже';
    case 503:
      return 'Сервис временно недоступен. Повторите попытку позже';
    case 504:
      return 'Время ожидания ответа сервиса истекло. Повторите попытку позже';
    default:
      return `Ошибка запроса (${status})`;
  }
}

export async function parseApiError(response: Response, defaultMessage?: string): Promise<string> {
  let message: string | undefined;

  try {
    const data = (await response.json()) as ProblemDetailResponse;

    if (data.message && data.message.trim()) {
      message = data.message.trim();
    } else if (data.detail && data.detail.trim()) {
      message = data.detail.trim();
    } else if (data.title && data.title.trim() && data.title !== 'Bad Request' && data.title !== 'Internal Server Error') {
      message = data.title.trim();
    } else if (data.errors) {
      if (Array.isArray(data.errors)) {
        const first = data.errors.find((err) => err?.defaultMessage);
        if (first?.defaultMessage) message = first.defaultMessage;
      } else if (typeof data.errors === 'object') {
        const first = Object.values(data.errors).find(Boolean);
        if (first) message = first;
      }
    } else if (data.details && typeof data.details === 'object') {
      const first = Object.values(data.details).find(Boolean);
      if (first && !first.endsWith('-service')) message = first;
    }
  } catch {
    // response is not JSON
  }

  if (message) {
    return message;
  }

  if (defaultMessage && !defaultMessage.includes('(')) {
    return defaultMessage;
  }

  return getStatusFallbackMessage(response.status);
}
