import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import { API_URL } from '../constants';
import { Storage } from '../utils/Storage';
import { handleResponseError } from '../http/errorHandlers';
/**
 * Create axios instance with base configuration.
 *
 * api.ts is responsible only for creating and configuring the HTTP
 * transport layer (base URL, timeout, headers, interceptors).
 * Error-response policy lives in http/errorHandlers.ts (SRP).
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
 * Response interceptor – delegates error policy to handleResponseError (SRP).
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => handleResponseError(error)
);

export default apiClient;
