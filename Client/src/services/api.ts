import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { API_URL } from '../constants';
import { Storage } from '../utils/Storage';
import { handleHttpError } from '../utils/httpErrorHandler';

/**
 * SRP: this file is responsible only for creating and configuring the shared
 * Axios instance.  Error-handling policy lives in httpErrorHandler.ts (OCP).
 */
const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor – attach JWT bearer token when present.
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = Storage.getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error),
);

/**
 * Response interceptor – delegate HTTP error handling to the dedicated module
 * so this file stays closed for modification (OCP).
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    handleHttpError(error);
    return Promise.reject(error);
  },
);

export default apiClient;
