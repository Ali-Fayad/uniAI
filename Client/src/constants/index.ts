// API Base URL - empty string = same-origin (works through nginx in Docker).
// Override with VITE_API_URL for local dev without Docker: http://localhost:9090
export const API_URL: string = import.meta.env.VITE_API_URL ?? '';

// API Endpoints
export const ENDPOINTS = {
  // Authentication endpoints
  AUTH: {
    SIGNUP: '/api/auth/signup',
    SIGNIN: '/api/auth/signin',
    VERIFY: '/api/auth/verify',
    VERIFY_2FA: '/api/auth/2fa/verify',
    FORGOT_PASSWORD: '/api/auth/forget-password',
    FORGOT_PASSWORD_CONFIRM: '/api/auth/forget-password/confirm',
    GOOGLE_URL: '/api/auth/google/url',
  },

  // User endpoints
  USER: {
    ME: '/api/users/me',
    UPDATE_ME: '/api/users/me',
    DELETE_ME: '/api/users/me',
    CHANGE_PASSWORD: '/api/users/change-password',
    FEEDBACK: '/api/users/feedback',
  },

  // Chat endpoints
  CHAT: {
    CREATE: '/api/chats',
    SEND_MESSAGE: '/api/chats/messages',
    GET_ALL: '/api/chats',
    GET_MESSAGES: (chatId: number) => `/api/chats/${chatId}/messages`,
    DELETE: (chatId: number) => `/api/chats/${chatId}`,
    DELETE_ALL: '/api/chats',
  },

  // CV / Personal info endpoints
  CV: {
    PERSONAL_INFO: '/api/cv/personal-info',
    UNIVERSITIES: '/api/cv/universities',
    SKILLS: '/api/cv/skills',
    POSITIONS: '/api/cv/positions',
  },
} as const;

// Storage keys
export const STORAGE_KEYS = {
  TOKEN: 'auth_token',
  USER: 'user_data',
} as const;
