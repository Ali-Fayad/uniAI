import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { API_URL } from '../constants';
import { Storage } from '../utils/Storage';

/**
 * Create axios instance with base configuration
 */
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor to add JWT token to headers
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = Storage.getToken();
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor for error handling
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError) => {
    // Handle 401 Unauthorized - clear storage and redirect to auth
    if (error.response && error.response.status === 401) {
      Storage.clearAll();
      
      // Only redirect if we're not already on the auth pages
      const currentPath = window.location.pathname;
      if (!currentPath.startsWith('/auth') && !currentPath.startsWith('/signin') && !currentPath.startsWith('/signup')) {
        window.location.href = '/auth';
      }
    }
    
    // Normalize backend messages and attach status so frontend can use them
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data as any;

      // Best-effort extraction of a user-facing message from backend payload
      let backendMessage: string | undefined;
      if (typeof data === 'string') {
        backendMessage = data;
      } else if (data) {
        backendMessage = data.message || data.error || (Array.isArray(data.messages) ? data.messages[0] : undefined);
      }

      if (backendMessage) {
        // Prefer backend message as the error's message so existing catch blocks can display it
        (error as any).message = backendMessage;
      }

      // Attach status and raw payload for callers that need them
      (error as any).status = status;
      (error as any).payload = data;
    }

    return Promise.reject(error);
  }
);

export default apiClient;
